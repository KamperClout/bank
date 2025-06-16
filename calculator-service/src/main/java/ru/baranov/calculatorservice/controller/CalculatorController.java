package ru.baranov.calculatorservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.baranov.calculatorservice.dto.CreditDto;
import ru.baranov.calculatorservice.dto.LoanOfferDto;
import ru.baranov.calculatorservice.dto.LoanStatementRequestDto;
import ru.baranov.calculatorservice.dto.ScoringDataDto;
import ru.baranov.calculatorservice.service.CalculatorService;

import java.util.List;

@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorController {
    @Autowired
    CalculatorService calculatorService;

    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDto>> offers(@RequestBody @Valid LoanStatementRequestDto loanStatementRequestDto) {
        List<LoanOfferDto> offers = calculatorService.offers(loanStatementRequestDto);
        return ResponseEntity.ok(offers);
    }

    @PostMapping("/calc")
    public ResponseEntity<CreditDto> calc(@RequestBody ScoringDataDto scoringDataDto) {
        CreditDto calc = calculatorService.calc(scoringDataDto);
        return ResponseEntity.ok(calc);
    }

}
