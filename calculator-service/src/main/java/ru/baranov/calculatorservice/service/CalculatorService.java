package ru.baranov.calculatorservice.service;

import ru.baranov.calculatorservice.dto.CreditDto;
import ru.baranov.calculatorservice.dto.LoanOfferDto;
import ru.baranov.calculatorservice.dto.LoanStatementRequestDto;
import ru.baranov.calculatorservice.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> offers(LoanStatementRequestDto loanStatementRequestDto);

    CreditDto calc(ScoringDataDto scoringDataDto);
}
