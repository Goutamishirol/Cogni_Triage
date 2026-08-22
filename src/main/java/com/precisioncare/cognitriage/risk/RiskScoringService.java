package com.precisioncare.cognitriage.risk;
import com.precisioncare.cognitriage.patient.Patient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class RiskScoringService{
    private static final double W_CDR=30.0;
    private static final double W_MMSE = 25.0;
    private static final double W_APOE4       = 12.0;
    private static final double W_BRAIN_VOL   = 13.0;
    private static final double W_AGE         = 10.0;
    private static final double W_COMORBIDITY =  5.0;
    private static final double W_EDUCATION   = -5.0;   // protective

    public RiskAssessment score(Patient p) {
        List<RiskFactor> factors = new ArrayList<>();
        List<String> gaps = new ArrayList<>();
        double weighted = 0.0;
        double availableWeight = 0.0;


        if (p.getCdr() != null) {
            double sev = switch ((int) Math.round(p.getCdr() * 2)) {
                case 0 -> 0.0;
                case 1 -> 0.75;
                default -> 1.0;
            };

            weighted += sev * W_CDR;
            availableWeight += W_CDR;

            factors.add(new RiskFactor(
                    "CDR",
                    String.valueOf(p.getCdr()),
                    sev * W_CDR,
                    "CDR of " + p.getCdr() + " indicates measurable impairment."));
        } else {
            gaps.add("CDR not recorded");
        }


        if (p.getMmse() != null) {
            double sevMmse = clamp((30.0 - p.getMmse()) / 12.0);

            weighted += sevMmse * W_MMSE;
            availableWeight += W_MMSE;

            factors.add(new RiskFactor(
                    "MMSE",
                    String.valueOf(p.getMmse()),
                    sevMmse * W_MMSE,
                    "MMSE of " + p.getMmse() + " relative to the 24-point screening cut-off."));
        } else {
            gaps.add("MMSE not recorded");
        }
        // ---- APOE e4 allele count ----
        if (p.getApoe4AlleleCount() != null) {
            int alleles = p.getApoe4AlleleCount();
            double sevApoe = clamp(alleles / 2.0);
            weighted += sevApoe * W_APOE4;
            availableWeight += W_APOE4;
            factors.add(new RiskFactor("APOE e4", alleles + " allele(s)", sevApoe * W_APOE4,
                    alleles > 0
                            ? "Carrying " + alleles + " e4 allele(s) raises lifetime risk and lowers age at onset."
                            : "Non-carrier for APOE e4."));
        } else {
            gaps.add("APOE genotype unavailable");
        }

        // ---- Brain volume: INVERTED, lower is worse ----
        if (p.getNwbv() != null) {
            double sevVol = clamp((0.78 - p.getNwbv()) / 0.12);
            weighted += sevVol * W_BRAIN_VOL;
            availableWeight += W_BRAIN_VOL;
            factors.add(new RiskFactor("Brain volume", String.valueOf(p.getNwbv()), sevVol * W_BRAIN_VOL,
                    "Reduced normalized whole-brain volume is consistent with neurodegenerative atrophy."));
        } else {
            gaps.add("MRI volumetrics not yet acquired");
        }

        // ---- Age ----
        if (p.getAge() != null) {
            double sevAge = clamp((p.getAge() - 60.0) / 30.0);
            weighted += sevAge * W_AGE;
            availableWeight += W_AGE;
            factors.add(new RiskFactor("Age", p.getAge() + " yrs", sevAge * W_AGE,
                    "Age is the dominant non-modifiable risk factor; incidence roughly doubles every five years after 65."));
        } else {
            gaps.add("Age not recorded");
        }

        // ---- Comorbidity burden ----
        if (p.getComorbidities() != null && !p.getComorbidities().isEmpty()) {
            int count = p.getComorbidities().size();
            double sevCom = clamp(count / 3.0);
            weighted += sevCom * W_COMORBIDITY;
            availableWeight += W_COMORBIDITY;
            factors.add(new RiskFactor("Comorbidities", String.valueOf(count), sevCom * W_COMORBIDITY,
                    "Vascular and metabolic comorbidities are associated with accelerated cognitive decline."));
        }

        // ---- Education: PROTECTIVE, negative weight ----
        if (p.getEducationYears() != null) {
            double sevEdu = clamp((p.getEducationYears() - 8.0) / 10.0);
            weighted += sevEdu * W_EDUCATION;
            availableWeight += Math.abs(W_EDUCATION);
            factors.add(new RiskFactor("Education", p.getEducationYears() + " yrs", sevEdu * W_EDUCATION,
                    "Higher educational attainment is a proxy for cognitive reserve and is protective."));
        }





























        double score = availableWeight > 0
                ? clamp(weighted / availableWeight) * 100.0
                : 0.0;

        return new RiskAssessment(score, tierFor(score), factors, gaps);


    }

    private RiskTier tierFor(double score) {
        if (score >= 65) return RiskTier.HIGH;
        if (score >= 40) return RiskTier.MEDIUM;
        return RiskTier.LOW;
    }
    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));

    }



}
