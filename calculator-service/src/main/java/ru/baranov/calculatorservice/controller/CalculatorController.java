package ru.baranov.calculatorservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
@Tag(name = "Calculator Controller", description = "API для расчета кредитных предложений и скоринга")
public class CalculatorController {
    @Autowired
    CalculatorService calculatorService;

    @Operation(
        summary = "Получить кредитные предложения",
        description = "Возвращает список из четырех кредитных предложений с разными комбинациями страховки и зарплатного клиента"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный расчет предложений",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = LoanOfferDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/offers")
    public ResponseEntity<List<LoanOfferDto>> offers(
            @Parameter(description = "Данные для расчета кредитных предложений", required = true)
            @RequestBody @Valid LoanStatementRequestDto loanStatementRequestDto) {
        log.info("Получен запрос на расчет кредитных предложений: {}", loanStatementRequestDto);
        List<LoanOfferDto> offers = calculatorService.offers(loanStatementRequestDto);
        log.info("Сформированы кредитные предложения: {}", offers);
        return ResponseEntity.ok(offers);
    }

    @Operation(
        summary = "Рассчитать кредит",
        description = "Выполняет полный расчет кредита на основе скоринговых данных, включая график платежей"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный расчет кредита",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = CreditDto.class))),
        @ApiResponse(responseCode = "400", description = "Неверные входные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/calc")
    public ResponseEntity<CreditDto> calc(
            @Parameter(description = "Скоринговые данные для расчета кредита", required = true)
            @RequestBody @Valid ScoringDataDto scoringDataDto) {
        log.info("Получен запрос на расчет кредита: {}", scoringDataDto);
        CreditDto calc = calculatorService.calc(scoringDataDto);
        log.info("Рассчитан кредит: {}", calc);
        return ResponseEntity.ok(calc);
    }
}
