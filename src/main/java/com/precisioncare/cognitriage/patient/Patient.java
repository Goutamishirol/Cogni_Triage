package com.precisioncare.cognitriage.patient;
import com.precisioncare.cognitriage.pathway.DiagnosticStage;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.EnumSet;
import java.util.Set;


@Entity
@Table(name = "patients", indexes = {
        @Index(name = "idx_patient_cohort_id", columnList = "cohortId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String cohortId;

    @Enumerated(EnumType.STRING)
    private Sex sex = Sex.UNKNOWN;

    private Integer age;


    private Integer educationYears;

    /** mini mental state which is a required field*/
    private Integer mmse;


    private Double cdr;


    private Integer apoe4AlleleCount;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "patient_comorbidities", joinColumns = @JoinColumn(name = "patient_id"))
    @Column(name = "comorbidity")
    private Set<Comorbidity> comorbidities = EnumSet.noneOf(Comorbidity.class);


    private Double etiv;


    private Double nwbv;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiagnosticStage currentStage = DiagnosticStage.COGNITIVE_SCREENING;
}