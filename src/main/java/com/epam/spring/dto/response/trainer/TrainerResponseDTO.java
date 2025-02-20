package com.epam.spring.dto.response.trainer;

import com.epam.spring.dto.response.TrainingTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TrainerResponseDTO {

    private String username;
    private String firstName;
    private String lastName;
    private TrainingTypeDTO specialization;
}
