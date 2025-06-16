package ru.baranov.calculatorservice.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import ru.baranov.calculatorservice.annotations.Age;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanStatementRequestDto {
    @NotNull
    @DecimalMin(value = "20000", message = "amount must be >= 20000")
    BigDecimal amount;
    @NotNull
    @Min(value = 6, message = "term must be >= 6")
    Integer term;
    @NotBlank
    @Size(min = 2, max = 30, message = "firstname length must be >= 2 and <= 30")
    String firstName;
    @NotNull
    @Size(min = 2, max = 30, message = "lastname length must be >= 2 and <= 30")
    String lastName;
    @Size(min = 2, max = 30, message = "middlename length must be >= 2 and <= 30")
    String middleName;
    @Email
    @NotNull
    String email;
    @Age(value = 18)
    LocalDate birthdate;
    @NotNull
    @Size(min = 4, max = 4, message = "passportSeries length must be 4")
    @Pattern(regexp = "\\d{4}", message = "passportSeries must contain only digits")
    String passportSeries;
    @NotNull
    @Size(min = 6, max = 6, message = "passportSeries length must be 4")
    @Pattern(regexp = "\\d{6}", message = "passportNumber must contain only digits")
    String passportNumber;
}