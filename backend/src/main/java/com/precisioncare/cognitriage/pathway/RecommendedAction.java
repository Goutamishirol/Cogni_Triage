package com.precisioncare.cognitriage.pathway;

import java.util.List;

/**
 * What the clinician should consider doing next for this patient.
 * Recommendation only - nothing here is an order or a diagnosis.
 */
public record RecommendedAction(
        boolean escalate,
        String nextStage,
        String summary,
        List<String> tests,
        String rationale
) {}
