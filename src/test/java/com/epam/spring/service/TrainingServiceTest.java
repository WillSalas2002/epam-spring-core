package com.epam.spring.service;

import com.epam.spring.config.AppConfig;
import com.epam.spring.model.Trainee;
import com.epam.spring.model.Trainer;
import com.epam.spring.model.Training;
import com.epam.spring.model.TrainingType;
import com.epam.spring.storage.InMemoryStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(AppConfig.class)
class TrainingServiceTest {

    @Autowired
    private InMemoryStorage inMemoryStorage;

    @Autowired
    private TrainingService trainingService;

    @BeforeEach
    void clearStorage() {
        inMemoryStorage.clearDB();
    }

    @Test
    public void testCreateTraining() {
        // Given
        Trainer trainer = buildTrainer();
        Trainee trainee = buildTrainee();
        Training training = new Training(trainee, trainer, "Strong man training", TrainingType.STRENGTH_TRAINING, LocalDateTime.now().plusHours(3), 120);

        // When
        Training createdTraining = trainingService.create(training);

        // Then
        assertNotNull(createdTraining);
        assertEquals(createdTraining, trainingService.findById(createdTraining.getUuid()));
        assertEquals(1, trainingService.findAll().size());
        assertTrue(trainingService.findAll().contains(createdTraining));

    }

    private static Trainer buildTrainer() {
        Trainer trainer = new Trainer();
        trainer.setFirstName("Jane");
        trainer.setLastName("Smith");
        trainer.setUsername("jane.smith");
        trainer.setPassword("password456");
        trainer.setSpecialization("Fitness");
        trainer.setActive(true);
        return trainer;
    }

    private static Trainee buildTrainee() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");
        trainee.setUsername("john.doe");
        trainee.setPassword("password123");
        trainee.setDataOfBirth(LocalDate.now());
        trainee.setAddress("123 Test St");
        trainee.setActive(true);
        return trainee;
    }
}