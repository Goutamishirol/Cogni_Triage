package com.precisioncare.cognitriage.risk;

import java.util.List;

/**
 * Output of the scoring engine. Computed fresh on every request, never stored.
 */
public record RiskAssessment(
        double score,
        RiskTier tier,
        List<RiskFactor> factors,
        List<String> dataGaps
) {
    /** One-line summary for the worklist row. */
    public String headline() {
        return factors.stream()
                .filter(f -> !f.isProtective())
                .findFirst()
                .map(f -> f.name() + " " + f.observedValue() + " is the leading driver")
                .orElse("No elevated risk factors identified");
    }
}