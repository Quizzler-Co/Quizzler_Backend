package com.participation.service;

import com.participation.dto.*;

import java.util.List;

public interface ParticipationService {
    ParticipationResponseDTO getParticipationById(String id);
    ParticipationResponseDTO createParticipant(CreateParticipationRequestDTO createParticipationRequestDTO);
    List<ParticipationResponseDTO> getParticipationsByUserId(String userId);
    List<ParticipationResponseDTO> getParticipationsByQuizId(String quizId);

    List<ParticipationResponseDTO> getParticipationsByQuizIdAndUserId(String userId, String quizId);

    SubmissionResultDTO submitAnswers(String participantId, AnswerSubmissionDTO answers);

    List<AnswerDTO> getParticpantsAnswers(String participantId);


    void deleteParticipation(String participationId);

    List<ParticipationResponseDTO> getAllParticipations();
}
