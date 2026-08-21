package com.precisioncare.cognitriage.ingest;

import com.precisioncare.cognitriage.patient.Comorbidity;
import com.precisioncare.cognitriage.patient.Patient;
import com.precisioncare.cognitriage.patient.PatientRepository;
import com.precisioncare.cognitriage.patient.Sex;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class DemoCohortSeeder implements ApplicationRunner {

    private final PatientRepository patients;

    public DemoCohortSeeder(PatientRepository patients) {
        this.patients = patients;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (patients.count() > 0) {
            return;
        }

        Patient p1 = new Patient();
        p1.setCohortId("CT_0001");
        p1.setSex(Sex.MALE);
        p1.setAge(63);
        p1.setEducationYears(16);
        p1.setMmse(30);
        p1.setCdr(0.0);
        p1.setApoe4AlleleCount(0);
        p1.setEtiv(1580.0);
        p1.setNwbv(0.782);

        Patient p2 = new Patient();
        p2.setCohortId("CT_0002");
        p2.setSex(Sex.FEMALE);
        p2.setAge(74);
        p2.setEducationYears(12);
        p2.setMmse(25);
        p2.setCdr(0.5);
        p2.setApoe4AlleleCount(1);
        p2.setEtiv(1420.0);
        p2.setNwbv(0.731);
        p2.setComorbidities(EnumSet.of(Comorbidity.HYPERTENSION));

        Patient p3 = new Patient();
        p3.setCohortId("CT_0003");
        p3.setSex(Sex.FEMALE);
        p3.setAge(81);
        p3.setEducationYears(9);
        p3.setMmse(20);
        p3.setCdr(1.0);
        p3.setApoe4AlleleCount(2);
        p3.setEtiv(1390.0);
        p3.setNwbv(0.688);
        p3.setComorbidities(EnumSet.of(Comorbidity.DIABETES, Comorbidity.HYPERTENSION));

        Patient p4 = new Patient();
        p4.setCohortId("CT_0004");
        p4.setSex(Sex.MALE);
        p4.setAge(68);
        p4.setEducationYears(18);
        p4.setMmse(29);
        p4.setCdr(0.0);
        p4.setApoe4AlleleCount(0);
        p4.setEtiv(1610.0);
        p4.setNwbv(0.775);

        Patient p5 = new Patient();
        p5.setCohortId("CT_0005");
        p5.setSex(Sex.FEMALE);
        p5.setAge(77);
        p5.setEducationYears(11);
        p5.setMmse(24);
        p5.setCdr(0.5);
        p5.setApoe4AlleleCount(1);
        p5.setEtiv(1440.0);
        p5.setNwbv(0.719);
        p5.setComorbidities(EnumSet.of(Comorbidity.DEPRESSION));

        Patient p6 = new Patient();
        p6.setCohortId("CT_0006");
        p6.setSex(Sex.MALE);
        p6.setAge(85);
        p6.setEducationYears(8);
        p6.setMmse(18);
        p6.setCdr(1.0);
        p6.setApoe4AlleleCount(2);
        p6.setEtiv(1520.0);
        p6.setNwbv(0.671);
        p6.setComorbidities(EnumSet.of(Comorbidity.STROKE_HISTORY, Comorbidity.HYPERTENSION));

        Patient p7 = new Patient();
        p7.setCohortId("CT_0007");
        p7.setSex(Sex.FEMALE);
        p7.setAge(61);
        p7.setEducationYears(20);
        p7.setMmse(30);
        p7.setCdr(0.0);
        p7.setApoe4AlleleCount(0);
        p7.setEtiv(1380.0);
        p7.setNwbv(0.798);

        Patient p8 = new Patient();
        p8.setCohortId("CT_0008");
        p8.setSex(Sex.MALE);
        p8.setAge(72);
        p8.setEducationYears(14);
        p8.setMmse(27);
        p8.setCdr(0.0);
        p8.setApoe4AlleleCount(1);
        p8.setEtiv(1590.0);
        p8.setNwbv(0.748);

        Patient p9 = new Patient();
        p9.setCohortId("CT_0009");
        p9.setSex(Sex.FEMALE);
        p9.setAge(79);
        p9.setEducationYears(10);
        p9.setMmse(22);
        p9.setCdr(0.5);
        p9.setApoe4AlleleCount(2);
        p9.setEtiv(1410.0);
        p9.setNwbv(0.702);
        p9.setComorbidities(EnumSet.of(Comorbidity.DIABETES));

        Patient p10 = new Patient();
        p10.setCohortId("CT_0010");
        p10.setSex(Sex.MALE);
        p10.setAge(88);
        p10.setEducationYears(9);
        p10.setMmse(17);
        p10.setCdr(2.0);
        p10.setApoe4AlleleCount(1);
        p10.setEtiv(1550.0);
        p10.setNwbv(0.658);
        p10.setComorbidities(EnumSet.of(Comorbidity.CARDIOVASCULAR_DISEASE));

        Patient p11 = new Patient();
        p11.setCohortId("CT_0011");
        p11.setSex(Sex.FEMALE);
        p11.setAge(66);
        p11.setEducationYears(16);
        p11.setMmse(29);
        p11.setCdr(0.0);
        p11.setApoe4AlleleCount(0);
        p11.setEtiv(1400.0);
        p11.setNwbv(0.781);

        Patient p12 = new Patient();
        p12.setCohortId("CT_0012");
        p12.setSex(Sex.FEMALE);
        p12.setAge(71);
        p12.setEducationYears(20);
        p12.setMmse(27);
        p12.setCdr(0.5);
        p12.setApoe4AlleleCount(2);
        p12.setEtiv(1430.0);
        p12.setNwbv(0.739);
        p12.setComorbidities(EnumSet.of(Comorbidity.HYPERTENSION));

        Patient p13 = new Patient();
        p13.setCohortId("CT_0013");
        p13.setSex(Sex.MALE);
        p13.setAge(83);
        p13.setEducationYears(12);
        p13.setMmse(21);
        p13.setCdr(1.0);
        p13.setApoe4AlleleCount(0);
        p13.setEtiv(1600.0);
        p13.setNwbv(0.685);
        p13.setComorbidities(EnumSet.of(Comorbidity.HYPERCHOLESTEROLEMIA));

        Patient p14 = new Patient();
        p14.setCohortId("CT_0014");
        p14.setSex(Sex.FEMALE);
        p14.setAge(69);
        p14.setEducationYears(13);
        p14.setMmse(28);
        p14.setCdr(0.0);
        p14.setApoe4AlleleCount(1);
        p14.setEtiv(1390.0);
        p14.setNwbv(0.762);

        Patient p15 = new Patient();
        p15.setCohortId("CT_0015");
        p15.setSex(Sex.MALE);
        p15.setAge(76);
        p15.setEducationYears(15);
        p15.setMmse(26);
        p15.setCdr(0.5);
        p15.setApoe4AlleleCount(0);
        p15.setEtiv(1570.0);
        p15.setNwbv(0.727);

        Patient p16 = new Patient();
        p16.setCohortId("CT_0016");
        p16.setSex(Sex.FEMALE);
        p16.setAge(90);
        p16.setEducationYears(7);
        p16.setMmse(15);
        p16.setCdr(2.0);
        p16.setApoe4AlleleCount(2);
        p16.setEtiv(1360.0);
        p16.setNwbv(0.641);
        p16.setComorbidities(EnumSet.of(Comorbidity.DIABETES, Comorbidity.STROKE_HISTORY));

        Patient p17 = new Patient();
        p17.setCohortId("CT_0017");
        p17.setSex(Sex.MALE);
        p17.setAge(64);
        p17.setEducationYears(17);
        p17.setMmse(30);
        p17.setCdr(0.0);
        p17.setApoe4AlleleCount(0);
        p17.setEtiv(1620.0);
        p17.setNwbv(0.789);

        Patient p18 = new Patient();
        p18.setCohortId("CT_0018");
        p18.setSex(Sex.FEMALE);
        p18.setAge(80);
        p18.setEducationYears(11);
        p18.setMmse(23);
        p18.setCdr(0.5);
        p18.setApoe4AlleleCount(1);
        p18.setEtiv(1420.0);
        p18.setNwbv(0.708);
        p18.setComorbidities(EnumSet.of(Comorbidity.DEPRESSION, Comorbidity.HYPERTENSION));

        Patient p19 = new Patient();
        p19.setCohortId("CT_0019");
        p19.setSex(Sex.MALE);
        p19.setAge(75);
        p19.setEducationYears(10);
        p19.setMmse(24);
        p19.setCdr(0.5);
        p19.setApoe4AlleleCount(2);
        p19.setEtiv(1580.0);
        p19.setNwbv(0.716);

        Patient p20 = new Patient();
        p20.setCohortId("CT_0020");
        p20.setSex(Sex.FEMALE);
        p20.setAge(59);
        p20.setEducationYears(14);
        p20.setMmse(22);
        p20.setCdr(0.5);
        p20.setApoe4AlleleCount(2);
        p20.setEtiv(1400.0);
        p20.setNwbv(0.744);
        p20.setComorbidities(EnumSet.of(Comorbidity.DEPRESSION));

        patients.saveAll(List.of(
                p1, p2, p3, p4, p5, p6, p7, p8, p9, p10,
                p11, p12, p13, p14, p15, p16, p17, p18, p19, p20
        ));
    }
}