package com.precisioncare.cognitriage.api.dto;

import java.util.List;

public record PatientDetailDto(
        Long id,
        String cohortId,
        Integer age,
        String sex,
        Integer educationYears,
        Integer mmse,
        Double cdr,
        Integer apoe4AlleleCount,
        List<String> comorbidities,
        Double nwbv,
        Double etiv,
        double riskScore,
        String riskTier,
        String recommendation,
        List<RiskFactorDto> factors,
        List<String> dataGaps
) {}