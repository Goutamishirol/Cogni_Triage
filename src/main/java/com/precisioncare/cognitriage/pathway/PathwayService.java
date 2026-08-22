package com.precisioncare.cognitriage.pathway;

import com.precisioncare.cognitriage.patient.Patient;
import com.precisioncare.cognitriage.risk.RiskAssessment;
import com.precisioncare.cognitriage.risk.RiskTier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Maps (current stage + risk tier) onto a concrete set of suggested tests.
 *
 * Grounding:
 *  - Stage 1 labs follow the standard reversible-cause workup (thyroid, B12,
 *    metabolic panel) before attributing impairment to neurodegeneration.
 *  - Stage 2 uses plasma p-tau217 and Abeta42/40, the analytes covered by the
 *    Alzheimer's Association 2025 blood-biomarker clinical practice guideline.
 *    High-sensitivity assays are recommended as a TRIAGING test (a negative
 *    result rules out AD pathology with high probability).
 *  - Stage 3 uses the Scheltens medial temporal atrophy visual rating scale,
 *    which predicts MCI-to-dementia conversion, plus hippocampal volumetry.
 *  - Stage 4 is confirmatory (amyloid PET or CSF) and gates anti-amyloid
 *    therapy eligibility, which requires confirmed amyloid pathology.
 *
 * This service RECOMMENDS only. Advancing a patient is an explicit clinician
 * action; nothing auto-promotes.
 */
@Service
public class PathwayService {

    public boolean shouldEscalate(Patient p, RiskAssessment a) {
        if (p.getCurrentStage().isTerminal()) return false;
        if (a.tier() == RiskTier.HIGH) return true;
        // A blood draw is cheap enough to justify a wider net at screening.
        return a.tier() == RiskTier.MEDIUM
                && p.getCurrentStage() == DiagnosticStage.COGNITIVE_SCREENING;
    }

    public RecommendedAction recommend(Patient p, RiskAssessment a) {
        DiagnosticStage stage = p.getCurrentStage();
        RiskTier tier = a.tier();
        boolean escalate = shouldEscalate(p, a);

        return switch (stage) {
            case COGNITIVE_SCREENING -> screeningAction(tier, escalate);
            case BLOOD_BIOMARKER     -> biomarkerAction(tier, escalate);
            case MRI_EVALUATION      -> mriAction(tier, escalate);
            case PET_PRIORITIZATION  -> petAction(tier);
        };
    }

    private RecommendedAction screeningAction(RiskTier tier, boolean escalate) {
        if (tier == RiskTier.HIGH) {
            return new RecommendedAction(escalate, "Blood-Based Biomarkers",
                    "Prioritize for plasma biomarker testing",
                    List.of(
                            "Plasma p-tau217",
                            "Plasma Abeta42/40 ratio",
                            "TSH and free T4 (exclude thyroid cause)",
                            "Serum vitamin B12 and folate",
                            "Comprehensive metabolic panel"),
                    "Cognitive findings warrant biomarker assessment. Reversible causes are excluded in parallel, not after, to avoid delay.");
        }
        if (tier == RiskTier.MEDIUM) {
            return new RecommendedAction(escalate, "Blood-Based Biomarkers",
                    "Consider biomarker testing; exclude reversible causes first",
                    List.of(
                            "TSH and free T4",
                            "Serum vitamin B12 and folate",
                            "Comprehensive metabolic panel",
                            "MoCA (more sensitive than MMSE for mild impairment)",
                            "Depression screen (PHQ-9)"),
                    "Borderline findings. Reversible and mood-related causes account for a meaningful share of this group and are cheaper to exclude than to image.");
        }
        return new RecommendedAction(false, null,
                "Routine monitoring",
                List.of(
                        "Repeat cognitive screening in 12 months",
                        "Vascular risk factor review"),
                "No elevated indicators at screening. Re-screen at the scheduled interval.");
    }

    private RecommendedAction biomarkerAction(RiskTier tier, boolean escalate) {
        if (tier == RiskTier.HIGH) {
            return new RecommendedAction(escalate, "MRI Evaluation",
                    "Prioritize for structural MRI",
                    List.of(
                            "MRI brain, T1 volumetric sequence",
                            "Medial temporal atrophy visual rating (Scheltens 0-4)",
                            "Hippocampal volumetry, age and sex adjusted",
                            "FLAIR for white matter disease burden"),
                    "Biomarker and cognitive findings together justify imaging, both to assess atrophy and to exclude vascular or structural causes.");
        }
        if (tier == RiskTier.MEDIUM) {
            return new RecommendedAction(escalate, "MRI Evaluation",
                    "Repeat biomarkers; image if symptoms progress",
                    List.of(
                            "Repeat plasma p-tau217 in 12 months",
                            "Neuropsychological battery",
                            "MRI brain if functional decline is reported"),
                    "Intermediate findings. Rate of change over time discriminates better than any single measurement.");
        }
        return new RecommendedAction(false, null,
                "No further escalation indicated",
                List.of("Clinical review in 12 months"),
                "A negative high-sensitivity biomarker result rules out AD pathology with high probability, per the 2025 blood-biomarker guideline.");
    }

    private RecommendedAction mriAction(RiskTier tier, boolean escalate) {
        if (tier == RiskTier.HIGH) {
            return new RecommendedAction(escalate, "PET Scan Prioritization",
                    "Prioritize for amyloid confirmation",
                    List.of(
                            "Amyloid PET, or CSF Abeta42/40 and p-tau181",
                            "APOE genotyping (informs ARIA risk if therapy is considered)",
                            "Baseline MRI for ARIA monitoring"),
                    "Imaging supports a neurodegenerative pattern. Confirmatory testing is the gate for treatment eligibility.");
        }
        if (tier == RiskTier.MEDIUM) {
            return new RecommendedAction(escalate, "PET Scan Prioritization",
                    "Serial monitoring before advanced imaging",
                    List.of(
                            "Repeat MRI in 12 months to assess atrophy rate",
                            "Extended neuropsychological assessment"),
                    "Atrophy rate carries more information than a single cross-sectional scan.");
        }
        return new RecommendedAction(false, null,
                "Routine monitoring",
                List.of("Clinical review in 12 months"),
                "Imaging does not support prioritization for advanced diagnostics at this time.");
    }

    private RecommendedAction petAction(RiskTier tier) {
        if (tier == RiskTier.HIGH) {
            return new RecommendedAction(false, null,
                    "Flag for specialist treatment-eligibility review",
                    List.of(
                            "Anti-amyloid therapy eligibility assessment",
                            "APOE e4 genotyping if not already done",
                            "Baseline MRI and ARIA monitoring protocol",
                            "Anticoagulation and bleeding risk review"),
                    "Pathway complete. Eligibility is a specialist decision; this tool flags candidates only.");
        }
        return new RecommendedAction(false, null,
                "Pathway complete; specialist review",
                List.of("Multidisciplinary review of findings"),
                "Advanced diagnostics complete. Interpretation belongs with the treating specialist.");
    }
}