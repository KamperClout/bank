package ru.baranov.calculatorservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.baranov.calculatorservice.dto.*;
import ru.baranov.calculatorservice.enums.EmploymentStatus;
import ru.baranov.calculatorservice.enums.Gender;
import ru.baranov.calculatorservice.enums.MaritalStatus;
import ru.baranov.calculatorservice.enums.Position;
import ru.baranov.calculatorservice.exceptions.AgeException;
import ru.baranov.calculatorservice.exceptions.AmountException;
import ru.baranov.calculatorservice.exceptions.ScoringException;
import ru.baranov.calculatorservice.exceptions.WorkExperienceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceImplTest {

    @InjectMocks
    private CalculatorServiceImpl calculatorService;

    private LoanStatementRequestDto loanStatementRequestDto;
    private ScoringDataDto scoringDataDto;

    @BeforeEach
    void setUp() {
        calculatorService.setRate(15);

        loanStatementRequestDto = new LoanStatementRequestDto();
        loanStatementRequestDto.setAmount(new BigDecimal("100000"));
        loanStatementRequestDto.setTerm(12);

        scoringDataDto = new ScoringDataDto();
        scoringDataDto.setAmount(new BigDecimal("100000"));
        scoringDataDto.setTerm(12);
        scoringDataDto.setFirstName("John");
        scoringDataDto.setLastName("Doe");
        scoringDataDto.setGender(Gender.MALE);
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(30));
        scoringDataDto.setPassportSeries("1234");
        scoringDataDto.setPassportNumber("123456");
        scoringDataDto.setPassportIssueDate(LocalDate.now().minusYears(5));
        scoringDataDto.setPassportIssueBranch("Branch");
        scoringDataDto.setMaritalStatus(MaritalStatus.MARRIED);
        scoringDataDto.setDependentAmount(0);
        scoringDataDto.setIsInsuranceEnabled(true);
        scoringDataDto.setIsSalaryClient(true);

        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setPosition(Position.MIDDLE_MANAGER);
        employment.setSalary(new BigDecimal("100000"));
        employment.setWorkExperienceTotal(60);
        employment.setWorkExperienceCurrent(24);
        scoringDataDto.setEmployment(employment);
    }

    @Test
    void offers_ShouldReturnFourOffers() {
        List<LoanOfferDto> offers = calculatorService.offers(loanStatementRequestDto);

        assertNotNull(offers);
        assertEquals(4, offers.size());
        assertTrue(offers.stream().allMatch(offer -> offer.getTerm().equals(12)));
        assertTrue(offers.stream().allMatch(offer -> offer.getRequestedAmount().equals(new BigDecimal("100000"))));
    }

    @Test
    void calc_WithValidData_ShouldReturnCreditDto() {
        CreditDto creditDto = calculatorService.calc(scoringDataDto);

        assertNotNull(creditDto);
        assertEquals(new BigDecimal("100000"), creditDto.getAmount());
        assertEquals(12, creditDto.getTerm());
        assertTrue(creditDto.getIsInsuranceEnabled());
        assertTrue(creditDto.getIsSalaryClient());
        assertNotNull(creditDto.getPaymentSchedule());
        assertEquals(12, creditDto.getPaymentSchedule().size());
    }

    @Test
    void calc_WithInvalidAge_ShouldThrowAgeException() {
        scoringDataDto.setBirthdate(LocalDate.now().minusYears(15));

        assertThrows(AgeException.class, () -> calculatorService.calc(scoringDataDto));
    }

    @Test
    void calc_WithInvalidAmount_ShouldThrowAmountException() {
        scoringDataDto.setAmount(new BigDecimal("10000000"));

        assertThrows(AmountException.class, () -> calculatorService.calc(scoringDataDto));
    }

    @Test
    void calc_WithUnemployed_ShouldThrowScoringException() {
        scoringDataDto.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);

        assertThrows(ScoringException.class, () -> calculatorService.calc(scoringDataDto));
    }

    @Test
    void calc_WithInvalidWorkExperience_ShouldThrowWorkExperienceException() {
        scoringDataDto.getEmployment().setWorkExperienceTotal(12);
        scoringDataDto.getEmployment().setWorkExperienceCurrent(1);

        assertThrows(WorkExperienceException.class, () -> calculatorService.calc(scoringDataDto));
    }
} 