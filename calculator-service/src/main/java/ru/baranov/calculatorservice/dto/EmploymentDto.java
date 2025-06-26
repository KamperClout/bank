package ru.baranov.calculatorservice.dto;

import lombok.Data;
import ru.baranov.calculatorservice.enums.EmploymentStatus;
import ru.baranov.calculatorservice.enums.Position;

import java.math.BigDecimal;

@Data
public class EmploymentDto {
    EmploymentStatus employmentStatus;
    String employerINN;
    BigDecimal salary;
    Position position;
    Integer workExperienceTotal;
    Integer workExperienceCurrent;
}
