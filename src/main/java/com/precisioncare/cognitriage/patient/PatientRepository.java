package com.precisioncare.cognitriage.patient;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface PatientRepository  extends JpaRepository<Patient,Long>{
    List<Patient> findByCohortIdContainingIgnoreCase(String fragment);

    Optional<Patient> findByCohortId(String cohortId);
    boolean existsByCohortId(String cohortId);
}