package com.question.service;

import com.question.custom_exception.ResourceNotFoundException;
import com.question.dto.QuestionCreateRequest;
import com.question.dto.QuestionDTO;
import com.question.dto.QuestionReqDTO;
import com.question.entity.Question;
import com.question.repository.QuestionRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.question.dto.QuestionOperationResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class QuestionServiceImpl implements QuestionService {
    @Override
    public List<String> createQuestions(List<QuestionCreateRequest> requests) {
        List<Question> questions = requests.stream()
                .map(request -> modelMapper.map(request, Question.class))
                .collect(Collectors.toList());
        questionRepository.saveAll(questions);
        return questions.stream().map(Question::getId).collect(Collectors.toList());
    }

    private QuestionRepository questionRepository;
    private ModelMapper modelMapper;

    @Override
    public QuestionOperationResponseDTO createNewQuestion(QuestionReqDTO questionDTO) {
        String quizId=questionDTO.getQuizId();
        String text=questionDTO.getQuestionText();
        if(questionRepository.existsByQuestionTextAndQuizId(text,quizId)){
            throw new ResourceNotFoundException("Question text and quiz id not found");
        }
            Question question = modelMapper.map(questionDTO, Question.class);
            Question savedQuestion = questionRepository.save(question);
            
            QuestionOperationResponseDTO response = new QuestionOperationResponseDTO();
            response.setSuccess(true);
            response.setMessage("Successfully added question");
            response.setQuestionId(savedQuestion.getId());
            response.setOperation("created");
            return response;
    }

    @Override
    public List<QuestionDTO> getAllQuestion() {
    List<Question> questions = questionRepository.findAll();
    if(questions.isEmpty()) {
        throw new ResourceNotFoundException("No questions found");
    }
    return questions.stream().map(question -> modelMapper.map(question, QuestionDTO.class)).collect(Collectors.toList());
    }

    @Override
    public QuestionOperationResponseDTO updateQuestion(String id, QuestionReqDTO questionDTO) {
        Question q= questionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Question not found"));
        modelMapper.map(questionDTO,q);
        Question updatedQuestion = questionRepository.save(q);

        QuestionOperationResponseDTO response = new QuestionOperationResponseDTO();
        response.setSuccess(true);
        response.setMessage("Question updated successfully");
        response.setQuestionId(updatedQuestion.getId());
        response.setOperation("updated");
        return response;
    }

    @Override
    public QuestionDTO getQuestionById(String id) {
        Question q= questionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Question not found"));
        return modelMapper.map(q, QuestionDTO.class);
    }

    @Override
    public List<QuestionDTO> getAllQuestionByQuizId(String quizId) {
        List<Question> questions=questionRepository.findAllByQuizId(quizId);
        if(questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions found");
        }
        return  questions.stream().map(question -> modelMapper.map(question, QuestionDTO.class)).collect(Collectors.toList());
    }


}
