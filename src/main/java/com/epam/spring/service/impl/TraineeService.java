package com.epam.spring.service.impl;

import com.epam.spring.dto.request.trainee.CreateTraineeRequestDTO;
import com.epam.spring.dto.request.trainee.UpdateTraineeRequestDTO;
import com.epam.spring.dto.request.trainee.UpdateTraineeTrainerRequestDTO;
import com.epam.spring.dto.response.UserCredentialsResponseDTO;
import com.epam.spring.dto.response.trainee.FetchTraineeResponseDTO;
import com.epam.spring.dto.response.trainee.UpdateTraineeResponseDTO;
import com.epam.spring.dto.response.trainer.TrainerResponseDTO;
import com.epam.spring.error.exception.UserNotFoundException;
import com.epam.spring.mapper.TraineeMapper;
import com.epam.spring.model.Trainee;
import com.epam.spring.repository.impl.TraineeRepository;
import com.epam.spring.repository.impl.TrainerRepository;
import com.epam.spring.service.base.TraineeSpecificOperationsService;
import com.epam.spring.util.PasswordGenerator;
import com.epam.spring.util.UsernameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraineeService implements TraineeSpecificOperationsService {

    private final UsernameGenerator usernameGenerator;
    private final TraineeRepository traineeRepository;
    private final PasswordGenerator passwordGenerator;
    private final TrainerRepository trainerRepository;
    private final TraineeMapper traineeMapper;

    @Override
    public UserCredentialsResponseDTO create(CreateTraineeRequestDTO createRequest) {
        String uniqueUsername = usernameGenerator.generateUniqueUsername(createRequest.getFirstName(), createRequest.getLastName());
        String password = passwordGenerator.generatePassword();
        Trainee trainee = traineeMapper.fromCreateTraineeRequestToTrainee(createRequest, uniqueUsername, password);
        traineeRepository.create(trainee);
        return new UserCredentialsResponseDTO(uniqueUsername, password);
    }

    @Override
    public UpdateTraineeResponseDTO updateProfile(String username, UpdateTraineeRequestDTO updateRequest) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        traineeMapper.fromUpdateTraineeRequestToTrainee(trainee, updateRequest);
        Trainee updatedTrainee = traineeRepository.update(trainee);
        return traineeMapper.fromTraineeToUpdateTraineeResponse(updatedTrainee);
    }

    @Override
    public FetchTraineeResponseDTO getUserProfile(String username) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        return traineeMapper.fromTraineeToFetchTraineeResponse(trainee);
    }

    @Override
    public void deleteByUsername(String username) {
        traineeRepository.deleteByUsername(username);
    }

    @Override
    public TrainerResponseDTO updateTraineeTrainerList(String username, UpdateTraineeTrainerRequestDTO updateTraineeTrainerRequestDTO) {
        Trainee trainee = traineeRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee with username " + username + " not found"));
        List<String> trainerUsernames = updateTraineeTrainerRequestDTO.getTrainerUsernames();
        return null;
    }
}
