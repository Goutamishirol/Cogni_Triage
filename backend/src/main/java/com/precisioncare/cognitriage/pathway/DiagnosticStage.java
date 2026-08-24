package com.precisioncare.cognitriage.pathway;

/**
 * The four-stage escalation framework from the challenge brief.
 * Each stage adds a data modality and re-scores the patient.
 */
public enum DiagnosticStage {

    COGNITIVE_SCREENING(1, "Cognitive Screening",
            "MoCA/MMSE plus baseline clinical data. Produces initial risk prioritization."),

    BLOOD_BIOMARKER(2, "Blood-Based Biomarkers",
            "Plasma p-tau217 and Abeta42/40 for higher-risk patients. Refines prioritization."),

    MRI_EVALUATION(3, "MRI Evaluation",
            "Structural imaging to assess medial temporal atrophy and narrow high-risk candidates."),

    PET_PRIORITIZATION(4, "PET Scan Prioritization",
            "Amyloid PET or CSF for advanced confirmation and treatment-eligibility review.");

    private final int order;
    private final String displayName;
    private final String description;

    DiagnosticStage(int order, String displayName, String description) {
        this.order = order;
        this.displayName = displayName;
        this.description = description;
    }

    public int getOrder() { return order; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public boolean isTerminal() { return this == PET_PRIORITIZATION; }

    public DiagnosticStage next() {
        return isTerminal() ? this : values()[ordinal() + 1];
    }
}