package com.precisioncare.cognitriage.api;

import com.precisioncare.cognitriage.api.dto.PatientDetailDto;
import com.precisioncare.cognitriage.api.dto.PatientSummaryDto;
import com.precisioncare.cognitriage.api.dto.RiskFactorDto;
import com.precisioncare.cognitriage.patient.Comorbidity;
import com.precisioncare.cognitriage.patient.Patient;
import com.precisioncare.cognitriage.patient.PatientRepository;
import com.precisioncare.cognitriage.risk.RiskAssessment;
import com.precisioncare.cognitriage.risk.RiskScoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PatientController {

    private final PatientRepository patients;
    private final RiskScoringService scoring;

    public PatientController(PatientRepository patients, RiskScoringService scoring) {
        this.patients = patients;
        this.scoring = scoring;
    }

    /** Prioritized worklist, highest risk first. */
    @GetMapping("/patients")
    public List<PatientSummaryDto> list(@RequestParam(required = false) String tier) {
        return patients.findAll().stream()
                .map(p -> Map.entry(p, scoring.score(p)))
                .filter(e -> tier == null || e.getValue().tier().name().equalsIgnoreCase(tier))
                .sorted(Comparator.comparingDouble(
                        (Map.Entry<Patient, RiskAssessment> e) -> e.getValue().score()).reversed())
                .map(e -> toSummary(e.getKey(), e.getValue()))
                .toList();
    }

    /** Full record with the itemised risk breakdown. */
    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDetailDto> detail(@PathVariable Long id) {
        return patients.findById(id)
                .map(p -> ResponseEntity.ok(toDetail(p, scoring.score(p))))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Dashboard header counts. */
    @GetMapping("/cohort/summary")
    public Map<String, Object> summary() {
        var assessments = patients.findAll().stream().map(scoring::score).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalPatients", assessments.size());
        out.put("highRisk", assessments.stream().filter(a -> a.tier().name().equals("HIGH")).count());
        out.put("mediumRisk", assessments.stream().filter(a -> a.tier().name().equals("MEDIUM")).count());
        out.put("lowRisk", assessments.stream().filter(a -> a.tier().name().equals("LOW")).count());
        return out;
    }

    private PatientSummaryDto toSummary(Patient p, RiskAssessment a) {
        return new PatientSummaryDto(
                p.getId(), p.getCohortId(), p.getAge(), p.getSex().name(),
                p.getMmse(), p.getCdr(), a.score(), a.tier().name(),
                a.headline(), a.tier().getRecommendation());
    }

    private PatientDetailDto toDetail(Patient p, RiskAssessment a) {
        return new PatientDetailDto(
                p.getId(), p.getCohortId(), p.getAge(), p.getSex().name(),
                p.getEducationYears(), p.getMmse(), p.getCdr(), p.getApoe4AlleleCount(),
                p.getComorbidities().stream().map(Comorbidity::name).toList(),
                p.getNwbv(), p.getEtiv(),
                a.score(), a.tier().name(), a.tier().getRecommendation(),
                a.factors().stream()
                        .map(f -> new RiskFactorDto(f.name(), f.observedValue(),
                                Math.round(f.contribution() * 10.0) / 10.0,
                                f.rationale(), f.isProtective()))
                        .toList(),
                a.dataGaps());
    }
}





