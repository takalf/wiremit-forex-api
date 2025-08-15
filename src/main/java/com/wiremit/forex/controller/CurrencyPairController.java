package com.wiremit.forex.controller;

import com.wiremit.forex.model.CurrencyPair;
import com.wiremit.forex.service.CurrencyPairService;
import com.wiremit.forex.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/currency-pairs")
@RequiredArgsConstructor
@Tag(name = "Currency Pairs", description = "Currency pair management endpoints")
public class CurrencyPairController {

    private final CurrencyPairService currencyPairService;

    @GetMapping
    @Operation(summary = "Get all currency pairs", description = "Retrieves all currency pairs from the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pairs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<CurrencyPair>>> getAllCurrencyPairs(HttpServletRequest request) {
        return currencyPairService.getAllCurrencyPairs(request);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active currency pairs", description = "Retrieves only the currently active currency pairs")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Active currency pairs retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<CurrencyPair>>> getActiveCurrencyPairs(HttpServletRequest request) {
        return currencyPairService.getActiveCurrencyPairs(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get currency pair by ID", description = "Retrieves a specific currency pair by its unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pair found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<ApiResponse<CurrencyPair>> getCurrencyPairById(
            @Parameter(description = "Currency pair ID", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        return currencyPairService.getCurrencyPairById(id, request);
    }

    @PostMapping
    @Operation(summary = "Create new currency pair", description = "Creates a new currency pair in the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Currency pair created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid currency pair data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Currency pair already exists")
    })
    public ResponseEntity<ApiResponse<CurrencyPair>> createCurrencyPair(
            @Parameter(description = "Currency pair details")
            @Valid @RequestBody CurrencyPair currencyPair,
            HttpServletRequest request) {
        return currencyPairService.createCurrencyPair(currencyPair, request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update currency pair", description = "Updates an existing currency pair with new information")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pair updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid currency pair data")
    })
    public ResponseEntity<ApiResponse<CurrencyPair>> updateCurrencyPair(
            @Parameter(description = "Currency pair ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated currency pair details")
            @Valid @RequestBody CurrencyPair currencyPair,
            HttpServletRequest request) {
        return currencyPairService.updateCurrencyPair(id, currencyPair, request);
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate currency pair", description = "Sets the specified currency pair status to active")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pair activated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Currency pair already active")
    })
    public ResponseEntity<ApiResponse<CurrencyPair>> activateCurrencyPair(
            @Parameter(description = "Currency pair ID", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        return currencyPairService.activateCurrencyPair(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate currency pair", description = "Sets the specified currency pair status to inactive")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pair deactivated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Currency pair already inactive")
    })
    public ResponseEntity<ApiResponse<CurrencyPair>> deactivateCurrencyPair(
            @Parameter(description = "Currency pair ID", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        return currencyPairService.deactivateCurrencyPair(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete currency pair", description = "Permanently removes a currency pair from the system")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Currency pair deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Currency pair not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Cannot delete currency pair with active references")
    })
    public ResponseEntity<ApiResponse<String>> deleteCurrencyPair(
            @Parameter(description = "Currency pair ID", example = "1")
            @PathVariable Long id,
            HttpServletRequest request) {
        return currencyPairService.deleteCurrencyPair(id, request);
    }
}