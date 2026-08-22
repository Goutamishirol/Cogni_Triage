package com.precisioncare.cognitriage.api;

import com.precisioncare.cognitriage.api.dto.PatientDetailDto;
import com.precisioncare.cognitriage.api.dto.PatientSummaryDto;
import com.precisioncare.cognitriage.api.dto.RiskFactorDto;
import com.precisioncare.cognitriage.pathway.DiagnosticStage;
import com.precisioncare.cognitriage.pathway.PathwayService;
import com.precisioncare.cognitriage.pathway.RecommendedAction;
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
    private final PathwayService pathway;

    public PatientController(PatientRepository patients,
                             RiskScoringService scoring,
                             PathwayService pathway) {
        this.patients = patients;
        this.scoring = scoring;
        this.pathway = pathway;
    }

    /** Prioritized worklist. Optional tier filter and cohort-ID search. */
    @GetMapping("/patients")
    public List<PatientSummaryDto> list(@RequestParam(required = false) String tier,
                                        @RequestParam(required = false) String search) {

        List<Patient> source = (search == null || search.isBlank())
                ? patients.findAll()
                : patients.findByCohortIdContainingIgnoreCase(search.trim());

        return source.stream()
                .map(p -> Map.entry(p, scoring.score(p)))
                .filter(e -> tier == null || e.getValue().tier().name().equalsIgnoreCase(tier))
                .sorted(Comparator.comparingDouble(
                        (Map.Entry<Patient, RiskAssessment> e) -> e.getValue().score()).reversed())
                .map(e -> toSummary(e.getKey(), e.getValue()))
                .toList();
    }

    /** Full record with the itemised risk breakdown and recommended tests. */
    @GetMapping("/patients/{id}")
    public ResponseEntity<PatientDetailDto> detail(@PathVariable Long id) {
        return patients.findById(id)
                .map(p -> ResponseEntity.ok(toDetail(p, scoring.score(p))))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Stable lookup by cohort ID - survives reseeding, unlike the numeric id. */
    @GetMapping("/patients/by-cohort/{cohortId}")
    public ResponseEntity<PatientDetailDto> byCohortId(@PathVariable String cohortId) {
        return patients.findByCohortId(cohortId)
                .map(p -> ResponseEntity.ok(toDetail(p, scoring.score(p))))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Clinician explicitly advances a patient to the next stage. */
    @PostMapping("/patients/{id}/advance")
    public ResponseEntity<PatientDetailDto> advance(@PathVariable Long id) {
        return patients.findById(id)
                .map(p -> {
                    if (!p.getCurrentStage().isTerminal()) {
                        p.setCurrentStage(p.getCurrentStage().next());
                        patients.save(p);
                    }
                    return ResponseEntity.ok(toDetail(p, scoring.score(p)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Dashboard header counts. */
    @GetMapping("/cohort/summary")
    public Map<String, Object> summary() {
        List<Patient> all = patients.findAll();
        List<RiskAssessment> assessments = all.stream().map(scoring::score).toList();

        Map<String, Long> byStage = new LinkedHashMap<>();
        for (DiagnosticStage s : DiagnosticStage.values()) {
            byStage.put(s.getDisplayName(),
                    all.stream().filter(p -> p.getCurrentStage() == s).count());
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("totalPatients", assessments.size());
        out.put("highRisk", assessments.stream().filter(a -> a.tier().name().equals("HIGH")).count());
        out.put("mediumRisk", assessments.stream().filter(a -> a.tier().name().equals("MEDIUM")).count());
        out.put("lowRisk", assessments.stream().filter(a -> a.tier().name().equals("LOW")).count());
        out.put("byStage", byStage);
        return out;
    }

    /** Stage metadata for the pathway visualisation. */
    @GetMapping("/stages")
    public List<Map<String, Object>> stages() {
        return java.util.Arrays.stream(DiagnosticStage.values())
                .map(s -> Map.<String, Object>of(
                        "key", s.name(),
                        "order", s.getOrder(),
                        "name", s.getDisplayName(),
                        "description", s.getDescription()))
                .toList();
    }

    private PatientSummaryDto toSummary(Patient p, RiskAssessment a) {
        RecommendedAction action = pathway.recommend(p, a);
        return new PatientSummaryDto(
                p.getId(), p.getCohortId(), p.getAge(), p.getSex().name(),
                p.getMmse(), p.getCdr(), a.score(), a.tier().name(),
                p.getCurrentStage().getDisplayName(),
                a.headline(), action.summary());
    }

    private PatientDetailDto toDetail(Patient p, RiskAssessment a) {
        RecommendedAction action = pathway.recommend(p, a);
        return new PatientDetailDto(
                p.getId(), p.getCohortId(), p.getAge(), p.getSex().name(),
                p.getEducationYears(), p.getMmse(), p.getCdr(), p.getApoe4AlleleCount(),
                p.getComorbidities().stream().map(Comorbidity::name).toList(),
                p.getNwbv(), p.getEtiv(),
                a.score(), a.tier().name(), a.tier().getRecommendation(),
                p.getCurrentStage().getDisplayName(),
                p.getCurrentStage().getDescription(),
                p.getCurrentStage().isTerminal() ? null : p.getCurrentStage().next().getDisplayName(),
                action.escalate(),
                action.summary(),
                action.tests(),
                action.rationale(),
                a.factors().stream()
                        .map(f -> new RiskFactorDto(f.name(), f.observedValue(),
                                Math.round(f.contribution() * 10.0) / 10.0,
                                f.rationale(), f.isProtective()))
                        .toList(),
                a.dataGaps());
    }
}