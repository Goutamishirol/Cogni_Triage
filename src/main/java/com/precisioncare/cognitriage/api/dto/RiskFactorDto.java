package com.precisioncare.cognitriage.api.dto;

public record RiskFactorDto(
        String name,
        String observedValue,
        double contribution,
        String rationale,
        boolean protective
) {}