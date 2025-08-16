package com.participation.service;

import com.participation.custom_exception.ResourceAlreadyExistsException;
import com.participation.custom_exception.ResourceNotFoundException;
import com.participation.dto.*;
import com.participation.entity.Answer;
import com.participation.entity.Participation;
import com.participation.kafkaService.KafkaProducerService;
import com.participation.repository.ParticipationRepository;
import com.participation.sharedDTO.QuizDTO;
import com.participation.sharedDTO.QuizSubmitEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ParticipationServiceImpl implements ParticipationService{
    private final ParticipationRepository participationRepository;
    private final ModelMapper modelMapper;
    private QuizClient quizClient;
    private final KafkaProducerService kafkaProducerService;

    @Override
    public ParticipationResponseDTO getParticipationById(String id) {
        Participation participant=participationRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("particpant not found with id: "+id));
        return modelMapper.map(participant,ParticipationResponseDTO.class);
    }

    @Override
    public SubmissionResultDTO submitAnswers(String participantId, AnswerSubmissionDTO submission) {
        submission.getAnswers().forEach(ans ->
                log.info("Submitted: questionId={}, selectedOption={}", ans.getQuestionId(), ans.getSelectedOption()));

        Participation participation = participationRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + participantId));

        if (participation.isSubmitted()) {
            throw new ResourceAlreadyExistsException("Quiz already submitted for participation with id: " + participantId);
        }

        List<QuestionDTO> questions = quizClient.getQuestionsByQuizId(participation.getQuizId(),"true");
        Map<String, String> correctAnswerMap = questions.stream()
                .filter(q -> q.getId() != null && q.getCorrectAnswer() != null)
                .collect(Collectors.toMap(QuestionDTO::getId, QuestionDTO::getCorrectAnswer));

        log.info("Questions: {}", questions);
        log.info("Correct Answer Map: {}", correctAnswerMap);
        log.info("Submitted Answers: {}", submission.getAnswers());
        int score = 0;

        // NEW: Prepare list to hold answer details for response
        List<SubmissionResultDTO.AnswerDetail> answerDetails = new ArrayList<>();

        for (SingleAnswerDTO submittedAnswer : submission.getAnswers()) {
            String correctAnswer = correctAnswerMap.get(submittedAnswer.getQuestionId());
            boolean isCorrect = correctAnswer != null &&
                    correctAnswer.equalsIgnoreCase(submittedAnswer.getSelectedOption());

            Answer answer = Answer.builder()
                    .questionId(submittedAnswer.getQuestionId())
                    .selectedOption(submittedAnswer.getSelectedOption())
                    .isCorrect(isCorrect)
                    .build();

            participation.getAnswers().add(answer);
            if (isCorrect) score++;

            // NEW: Find explanation for the question (from questions list)
            String explanation = questions.stream()
                    .filter(q -> q.getId() != null && q.getId().equals(submittedAnswer.getQuestionId()))
                    .findFirst()
                    .map(QuestionDTO::getExplanation)
                    .orElse(null);

            // NEW: Build AnswerDetail and add to list
            SubmissionResultDTO.AnswerDetail detail = new SubmissionResultDTO.AnswerDetail();
            detail.setQuestionId(submittedAnswer.getQuestionId());
            detail.setSubmittedAnswer(submittedAnswer.getSelectedOption());
            detail.setCorrectAnswer(correctAnswer);
            detail.setExplanation(explanation);

            answerDetails.add(detail);
        }

        participation.setScore(score);
        participation.setSubmitted(true);
        participationRepository.save(participation);

        //Publish the score to Kafka
        QuizSubmitEvent quizSubmitEvent = new QuizSubmitEvent(participation.getUserId(), participation.getQuizId(), score);
        log.info("Publishing quiz submit event to kafka: {}", quizSubmitEvent);
        try {
            kafkaProducerService.sendQuizSubmit(quizSubmitEvent);
        } catch (Exception e) {
            log.error("Failed to send quiz event to Kafka", e);
        }

        // NEW: Build and return SubmissionResultDTO with answers and explanations
        SubmissionResultDTO result = new SubmissionResultDTO();
        result.setParticipantId(participantId);
        result.setAnswers(answerDetails);

        // Set score details
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        result.setPercentage(!questions.isEmpty() ? (double) score / questions.size() * 100 : 0.0);

        return result;
    }



    @Override
    public List<AnswerDTO> getParticpantsAnswers(String participantId) {
        Participation participation = participationRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + participantId));

        if (!participation.isSubmitted()) {
            throw new IllegalStateException("Answers not submitted for participation with id: " + participantId);
        }

        return participation.getAnswers().stream()
                .map(answer -> modelMapper.map(answer, AnswerDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationResponseDTO> getParticipationsByQuizIdAndUserId(String userId, String quizId) {
        List<Participation> participation = participationRepository.findAllByUserIdAndQuizId(userId, quizId);
        if (participation.isEmpty()) {
            throw new ResourceNotFoundException("No participations found for userId: " + userId + " and quizId: " + quizId);
        }
        return participation.stream()
                .map(part -> modelMapper.map(part, ParticipationResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<ParticipationResponseDTO> getParticipationsByUserId(String userId) {
        List<Participation> participationList = participationRepository.findAllByUserId(userId);

        if (participationList.isEmpty()) {
            throw new ResourceNotFoundException("No participations found for userId: " + userId);
        }

        return participationList.stream()
                .map(part -> {
                    // Step 1: Map basic fields using ModelMapper
                    ParticipationResponseDTO dto = modelMapper.map(part, ParticipationResponseDTO.class);

                    // Step 2: Fetch quiz title using Feign client
                    try {
                        QuizDTO quiz = quizClient.getQuizById(part.getQuizId(),"true");
                        dto.setQuizTitle(quiz.getTitle());
                    } catch (Exception e) {
                        dto.setQuizTitle("Unknown Title");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }




    @Override
    public List<ParticipationResponseDTO> getParticipationsByQuizId(String quizId) {
        List<Participation> participation= participationRepository.findAllByQuizId(quizId);
        if(participation.isEmpty()){
            throw new ResourceNotFoundException("No participations found for quizId: " + quizId);
        }
        return participation.stream()
                .map(part -> modelMapper.map(part, ParticipationResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationResponseDTO createParticipant(CreateParticipationRequestDTO dto) {

        if (!quizClient.doesQuizExist(dto.getQuizId(),"true")) {
            throw new ResourceNotFoundException("Invalid or non-existent quiz ID: " + dto.getQuizId());
        }

        Participation participation = new Participation();
        participation.setUserId(dto.getUserId());
        participation.setQuizId(dto.getQuizId());

        Participation saved = participationRepository.save(participation);
        ParticipationResponseDTO responseDTO = modelMapper.map(saved, ParticipationResponseDTO.class);
        return responseDTO;
    }


    @Override
    public void deleteParticipation(String participationId) {
        Participation participation = participationRepository.findById(participationId)
            .orElseThrow(() -> new ResourceNotFoundException("Participation not found with id: " + participationId));
        participationRepository.delete(participation);
    }

    @Override
    public List<ParticipationResponseDTO> getAllParticipations() {
        List<Participation> participations = participationRepository.findAll();
        if (participations.isEmpty()) {
            throw new ResourceNotFoundException("No participations found");
        }
        return participations.stream()
                .map(part -> modelMapper.map(part, ParticipationResponseDTO.class))
                .collect(Collectors.toList());
    }
}
