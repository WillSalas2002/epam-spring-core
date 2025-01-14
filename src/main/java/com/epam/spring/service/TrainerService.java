package com.epam.spring.service;

import com.epam.spring.model.Trainer;
import com.epam.spring.repository.TrainerRepository;
import com.epam.spring.util.PasswordGenerator;
import com.epam.spring.util.UsernameGenerator;
import com.epam.spring.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerService implements ExtendedOperationsService<Trainer>, TrainerSpecificOperationsService {

    private final UsernameGenerator usernameGenerator;
    private final TrainerRepository trainerRepository;
    private final PasswordGenerator passwordGenerator;
    private final Validator validator;

    @Override
    public Trainer create(Trainer trainer) {
        validator.validateUser(trainer.getUser());
        String uniqueUsername = usernameGenerator.generateUniqueUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String password = passwordGenerator.generatePassword();
        trainer.getUser().setUsername(uniqueUsername);
        trainer.getUser().setPassword(password);
        return trainerRepository.create(trainer);
    }

    @Override
    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    @Override
    public Trainer findById(Long id) {
        Optional<Trainer> trainerOptional = trainerRepository.findById(id);
        if (trainerOptional.isEmpty()) {
            throw new RuntimeException("Trainer with id " + id + " not found");
        }
        return trainerOptional.get();
    }

    @Override
    public Trainer findByUsername(String username) {
        Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);
        if (trainerOptional.isEmpty()) {
            throw new RuntimeException("Trainer with username " + username + " not found");
        }
        return trainerOptional.get();
    }

    @Override
    public boolean authenticate(String username, String password) {
        Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);

        return trainerOptional.filter(trainer -> Objects.equals(trainer.getUser().getPassword(), password)).isPresent();
    }

    @Override
    public Trainer update(Trainer updatedTrainer) {
        validator.validateUser(updatedTrainer.getUser());
        Long id = updatedTrainer.getId();
        Optional<Trainer> trainerOptional = trainerRepository.findById(id);
        if (trainerOptional.isEmpty()) {
            throw new NoSuchElementException("Trainee with id " + id + " not found");
        }
        if (isNameChanged(trainerOptional.get(), updatedTrainer)) {
            String uniqueUsername = usernameGenerator.generateUniqueUsername(updatedTrainer.getUser().getFirstName(), updatedTrainer.getUser().getLastName());
            updatedTrainer.getUser().setUsername(uniqueUsername);
        }
        return trainerRepository.update(updatedTrainer);
    }

    @Override
    public void activate(Trainer trainer) {
        trainer.getUser().setActive(!trainer.getUser().isActive());
        trainerRepository.update(trainer);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Optional<Trainer> trainerOptional = trainerRepository.findByUsername(username);
        if (trainerOptional.isEmpty()) {
            throw new RuntimeException("Incorrect username");
        }
        Trainer trainer = trainerOptional.get();
        if (!Objects.equals(trainer.getUser().getPassword(), oldPassword)) {
            throw new RuntimeException("Incorrect password");
        }
        trainer.getUser().setPassword(newPassword);

        trainerRepository.update(trainer);
    }

    @Override
    public void delete(Trainer trainer) {
        trainerRepository.delete(trainer);
    }

    @Override
    public List<Trainer> findTrainersByTraineeUsername(String username){
        return trainerRepository.findUnassignedTrainersByTraineeUsername(username);
    }

    private boolean isNameChanged(Trainer trainer, Trainer updatedTrainer) {
        return !Objects.equals(trainer.getUser().getFirstName(), updatedTrainer.getUser().getFirstName()) ||
                !Objects.equals(trainer.getUser().getLastName(), updatedTrainer.getUser().getLastName());
    }
}
