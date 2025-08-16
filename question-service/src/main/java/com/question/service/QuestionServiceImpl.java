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
    public String createNewQuestion(QuestionReqDTO questionDTO) {
        String quizId=questionDTO.getQuizId();
        String text=questionDTO.getQuestionText();
        if(questionRepository.existsByQuestionTextAndQuizId(text,quizId)){
            throw new ResourceNotFoundException("Question text and quiz id not found");
        }
            Question question = modelMapper.map(questionDTO, Question.class);
            questionRepository.save(question);
            return "Successfully added question";
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
    public String updateQuestion(String id, QuestionReqDTO questionDTO) {
        Question q= questionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Question not found"));
        modelMapper.map(questionDTO,q);
        questionRepository.save(q);

        return "Question updated successfully";
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
