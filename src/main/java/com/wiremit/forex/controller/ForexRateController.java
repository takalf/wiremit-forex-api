package com.wiremit.forex.controller;

import com.wiremit.forex.dto.ForexRateDTO;
import com.wiremit.forex.service.ForexRateService;
import com.wiremit.forex.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/v1/forex-rates")
@RequiredArgsConstructor
@Tag(name = "Forex Rates", description = "Foreign exchange rates management and retrieval endpoints")
public class ForexRateController {

    private final ForexRateService forexRateService;

    @GetMapping("/latest")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all latest forex rates", description = "Retrieves the most recent exchange rates for all currency pairs")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest rates retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getAllLatestRates(HttpServletRequest request) {
        return forexRateService.getAllLatestRates(request);
    }

    @GetMapping("/latest/{pairCode}")
    @Operation(summary = "Get latest rate by pair code", description = "Retrieves the most recent exchange rate for a specific currency pair using pair code (e.g., USDEUR)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest rate retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pair code format")
    })
    public ResponseEntity<ApiResponse<ForexRateDTO>> getLatestRateByPairCode(
            @Parameter(description = "Currency pair code (e.g., USDEUR, GBPJPY)", example = "USDEUR")
            @PathVariable String pairCode,
            HttpServletRequest request) {
        return forexRateService.getLatestRateByPairCode(pairCode, request);
    }

    @GetMapping("/latest/{baseCurrency}/{targetCurrency}")
    @Operation(summary = "Get latest rate by currencies", description = "Retrieves the most recent exchange rate between two specific currencies")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Latest rate retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid currency code format")
    })
    public ResponseEntity<ApiResponse<ForexRateDTO>> getLatestRate(
            @Parameter(description = "Base currency code", example = "USD")
            @PathVariable String baseCurrency,
            @Parameter(description = "Target currency code", example = "EUR")
            @PathVariable String targetCurrency,
            HttpServletRequest request) {
        return forexRateService.getLatestRate(baseCurrency, targetCurrency, request);
    }

    @PostMapping("/latest/batch")
    @Operation(summary = "Get latest rates for multiple pairs", description = "Retrieves the most recent exchange rates for multiple currency pairs in a single request")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Batch rates retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data or too many pair codes"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "One or more currency pairs not found")
    })
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getLatestRatesForPairs(
            @Parameter(description = "Batch request containing list of currency pair codes")
            @Valid @RequestBody BatchRateRequest request,
            HttpServletRequest httpRequest) {
        return forexRateService.getLatestRatesForPairs(request.getPairCodes(), httpRequest);
    }

    @GetMapping("/history/{pairCode}")
    @Operation(summary = "Get rate history", description = "Retrieves historical exchange rates for a specific currency pair")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Rate history retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pair code or limit parameter")
    })
    public ResponseEntity<ApiResponse<List<ForexRateDTO>>> getRateHistory(
            @Parameter(description = "Currency pair code", example = "USDEUR")
            @PathVariable String pairCode,
            @Parameter(description = "Maximum number of historical rates to return", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        return forexRateService.getRateHistory(pairCode, limit, request);
    }

    /**
     * Request DTO for batch rate requests
     */
    @Setter
    @Getter
    @Schema(description = "Request payload for batch forex rate retrieval")
    public static class BatchRateRequest {
        @NotEmpty(message = "Currency pair codes list cannot be empty")
        @Size(max = 50, message = "Cannot request more than 50 currency pairs at once")
        @Schema(description = "List of currency pair codes to retrieve rates for",
                example = "[\"USDEUR\", \"GBPJPY\", \"AUDCAD\"]")
        private List<String> pairCodes;
    }
}