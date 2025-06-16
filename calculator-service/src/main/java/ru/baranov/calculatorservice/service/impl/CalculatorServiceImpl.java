package ru.baranov.calculatorservice.service.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import ru.baranov.calculatorservice.dto.*;
import ru.baranov.calculatorservice.exceptions.AgeException;
import ru.baranov.calculatorservice.exceptions.AmountException;
import ru.baranov.calculatorservice.exceptions.ScoringException;
import ru.baranov.calculatorservice.exceptions.WorkExperienceException;
import ru.baranov.calculatorservice.service.CalculatorService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@ConfigurationProperties(prefix = "credit.rate")
public class CalculatorServiceImpl implements CalculatorService {
    private Integer rate;

    @Override
    public List<LoanOfferDto> offers(LoanStatementRequestDto loanStatementRequestDto) {
        LoanOfferDto loanOfferDto1 = createLoanOffer(loanStatementRequestDto, true, true);
        LoanOfferDto loanOfferDto2 = createLoanOffer(loanStatementRequestDto, false, false);
        LoanOfferDto loanOfferDto3 = createLoanOffer(loanStatementRequestDto, true, false);
        LoanOfferDto loanOfferDto4 = createLoanOffer(loanStatementRequestDto, false, true);
        List<LoanOfferDto> offers = Stream.of(loanOfferDto1, loanOfferDto2, loanOfferDto3, loanOfferDto4).
                sorted(Comparator.comparing(LoanOfferDto::getTotalAmount).reversed()).toList();
        return offers;
    }

    private LoanOfferDto createLoanOffer(LoanStatementRequestDto loanStatementRequestDto, Boolean isInsuranceEnabled,
                                         Boolean isSalaryClient) {
        BigDecimal totalAmount;
        BigDecimal requestAmount = loanStatementRequestDto.getAmount();
        BigDecimal loanRate = BigDecimal.valueOf(rate);

        if (isInsuranceEnabled) {
            BigDecimal insuranceAmount = requestAmount.divide(BigDecimal.TEN);
            totalAmount = requestAmount.add(insuranceAmount);
            loanRate = loanRate.subtract(BigDecimal.valueOf(3));
        } else {
            totalAmount = requestAmount;
        }
        if (isSalaryClient) {
            loanRate = loanRate.subtract(new BigDecimal(1));
        }

        BigDecimal monthlyRate = loanRate.divide(new BigDecimal(100)).divide(new BigDecimal(12), MathContext.DECIMAL128);
        int term = loanStatementRequestDto.getTerm();

        BigDecimal numerator = monthlyRate.multiply(totalAmount)
                .multiply((monthlyRate.add(BigDecimal.ONE)).pow(term));
        BigDecimal denominator = (monthlyRate.add(BigDecimal.ONE))
                .pow(term).subtract(BigDecimal.ONE);
        BigDecimal monthlyPayment = numerator.divide(denominator, MathContext.DECIMAL128);
        BigDecimal totalPaymentAmount = monthlyPayment.multiply(new BigDecimal(term));

        return new LoanOfferDto(UUID.randomUUID(), requestAmount, totalPaymentAmount, term,
                monthlyPayment, loanRate, isInsuranceEnabled, isSalaryClient);
    }

    @Override
    public CreditDto calc(ScoringDataDto scoringDataDto) {
        BigDecimal loanRate = BigDecimal.valueOf(rate);
        int term = scoringDataDto.getTerm();
        BigDecimal amount = scoringDataDto.getAmount();
        BigDecimal monthlySalary = scoringDataDto.getEmployment().getSalary();
        int age = Period.between(scoringDataDto.getBirthdate(), LocalDate.now()).getYears();

        if (age < 20 || age > 65) {
            throw new AgeException("Возраст заемщика должен быть от 20 до 65 лет");
        }

        if (amount.compareTo(monthlySalary.multiply(new BigDecimal(25))) > 0) {
            throw new AmountException("Сумма кредита превышает допустимый лимит");
        }

        switch (scoringDataDto.getEmployment().getEmploymentStatus()) {
            case UNEMPLOYED:
                throw new ScoringException("Отказ в кредите: безработный");
            case SELF_EMPLOYED:
                loanRate = loanRate.add(new BigDecimal("1.0"));
                break;
            case BUSINESS_OWNER:
                loanRate = loanRate.add(new BigDecimal("2.0"));
                break;
            default:
                break;
        }

        switch (scoringDataDto.getEmployment().getPosition()) {
            case MIDDLE_MANAGER:
                loanRate = loanRate.subtract(new BigDecimal("2.0"));
                break;
            case TOP_MANAGER:
                loanRate = loanRate.subtract(new BigDecimal("3.0"));
                break;
            default:
                break;
        }

        switch (scoringDataDto.getMaritalStatus()) {
            case MARRIED:
                loanRate = loanRate.subtract(new BigDecimal("3.0"));
                break;
            case DIVORCED:
                loanRate = loanRate.add(new BigDecimal("1.0"));
                break;
            default:
                break;
        }

        switch (scoringDataDto.getGender()) {
            case FEMALE:
                if (age >= 32 && age <= 60) {
                    loanRate = loanRate.subtract(new BigDecimal("3.0"));
                }
                break;
            case MALE:
                if (age >= 30 && age <= 55) {
                    loanRate = loanRate.subtract(new BigDecimal("3.0"));
                }
                break;
            case NON_BINARY:
                loanRate = loanRate.add(new BigDecimal("7.0"));
                break;
            default:
                break;
        }
        if (scoringDataDto.getEmployment().getWorkExperienceTotal() < 18 ||
                scoringDataDto.getEmployment().getWorkExperienceCurrent() < 3) {
            throw new WorkExperienceException("Недостаточный опыт работы");
        }

        if (scoringDataDto.getIsSalaryClient()) {
            loanRate = loanRate.subtract(new BigDecimal("1.0"));
        }
        if (scoringDataDto.getIsInsuranceEnabled()) {
            loanRate = loanRate.subtract(new BigDecimal("0.5"));
        }

        BigDecimal monthlyRate = loanRate.divide(new BigDecimal("100"), MathContext.DECIMAL64)
                .divide(new BigDecimal("12"), MathContext.DECIMAL64);
        BigDecimal annuityCoefficient = monthlyRate.add(BigDecimal.ONE).pow(term).multiply(monthlyRate)
                .divide((monthlyRate.add(BigDecimal.ONE).pow(term)).subtract(BigDecimal.ONE), MathContext.DECIMAL64);
        BigDecimal monthlyPayment = amount.multiply(annuityCoefficient);

        BigDecimal psk = loanRate.multiply(new BigDecimal(term)).divide(new BigDecimal("12"), MathContext.DECIMAL64);

        List<PaymentScheduleElementDto> paymentSchedule = new ArrayList<>();
        BigDecimal remainingDebt = amount;
        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate);
            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment);
            remainingDebt = remainingDebt.subtract(debtPayment);

            PaymentScheduleElementDto paymentElement = new PaymentScheduleElementDto();
            paymentElement.setNumber(i);
            paymentElement.setDate(LocalDate.now().plusMonths(i));
            paymentElement.setTotalPayment(monthlyPayment);
            paymentElement.setInterestPayment(interestPayment);
            paymentElement.setDebtPayment(debtPayment);
            paymentElement.setRemainingDebt(remainingDebt);
            paymentSchedule.add(paymentElement);
        }

        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(amount);
        creditDto.setTerm(term);
        creditDto.setMonthlyPayment(monthlyPayment);
        creditDto.setRate(loanRate);
        creditDto.setPsk(psk);
        creditDto.setIsInsuranceEnabled(scoringDataDto.getIsInsuranceEnabled());
        creditDto.setIsSalaryClient(scoringDataDto.getIsSalaryClient());
        creditDto.setPaymentSchedule(paymentSchedule);
        return creditDto;
    }
}
