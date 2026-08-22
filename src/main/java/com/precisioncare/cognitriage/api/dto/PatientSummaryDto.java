package com.precisioncare.cognitriage.api.dto;

public record PatientSummaryDto(
        Long id,
        String cohortId,
        Integer age,
        String sex,
        Integer mmse,
        Double cdr,
        double riskScore,
        String riskTier,
        String leadingFactor,
        String recommendation
) {}