package com.precisioncare.cognitriage.risk;
import com.precisioncare.cognitriage.patient.Patient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class RiskScoringService{
    private static final double W_CDR=30.0;
    private static final double W_MMSE = 25.0;

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
