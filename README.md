# CogniTriage 🧠

### AI-Driven Prioritization for Early Alzheimer's Diagnostic Pathways

**CogniTriage** is an AI-assisted clinical decision-support prototype designed to help prioritize patients within early Alzheimer's diagnostic pathways.

It transforms a screened patient population into an **explainable, clinician-ready worklist**, helping specialists identify which patients may require earlier review and what diagnostic stage should be considered next.

> ⚠️ **Important:** CogniTriage does not diagnose Alzheimer's disease, determine treatment eligibility, or replace clinical judgment. It provides prioritization recommendations intended for specialist review.

### 🚀 Live Demo

**[cogni-triage.vercel.app](https://cogni-triage.vercel.app)**

---

## 🎯 The Problem

Early cognitive-impairment assessment can involve:

* Multiple diagnostic stages
* Incomplete patient information
* Limited specialist capacity
* Long waiting times for advanced investigations
* Difficulty prioritizing large screened populations

A screening result alone does not always provide a clear way to organize patients for specialist review.

### The key question CogniTriage addresses:

> **Which patients should be prioritized for review, and what diagnostic step should be considered next?**

CogniTriage addresses this by combining **transparent scoring, patient prioritization, data-gap awareness, and pathway-based recommendations** into a single clinician-focused dashboard.

---

## 💡 Solution

CogniTriage provides a structured workflow that:

1. **Scores patients** using available clinical risk factors.
2. **Ranks patients** according to their current prioritization level.
3. **Explains each score** using factor-level contributions.
4. **Identifies missing information** without automatically penalizing the patient.
5. **Suggests the next diagnostic stage** within a structured pathway.
6. **Keeps pathway advancement under clinician control.**

The system is designed around **explainability and human oversight**, rather than fully automated clinical decision-making.

---

## ✨ Key Features

### 📊 Explainable Patient Prioritization

Patients receive a normalized **0–100 prioritization score** based on available clinical factors.

### 📋 Ranked Clinical Worklist

Patients are organized into a prioritized worklist so higher-priority cases can be reviewed first.

### 🔍 Factor-Level Explanation

The dashboard shows the factors contributing to a patient's score, making the recommendation easier to interpret.

### ⚠️ Missing-Data Awareness

Missing information is displayed separately instead of automatically lowering a patient's score.

### 🏥 Four-Stage Diagnostic Pathway

The platform represents a structured progression from cognitive screening to advanced pathology assessment.

### 👨‍⚕️ Clinician-Controlled Advancement

Patients do not automatically move through the diagnostic pathway. Advancement requires an explicit clinician action.

### 🧪 Synthetic Patient Cohort

The prototype uses automatically generated synthetic patients, allowing safe demonstration without exposing real patient information.

### 🤖 Optional ML Integration

The architecture supports an optional machine-learning service while maintaining a rule-based fallback if the model service is unavailable.

### 🛡️ Safety-First Design

The interface uses non-diagnostic terminology and clearly communicates the limitations of the prototype.

---

## 🧬 Diagnostic Pathway

```text
┌──────────────────────────┐
│   Cognitive Screening    │
│   MMSE / MoCA / Labs     │
└────────────┬─────────────┘
             ↓
┌──────────────────────────┐
│ Blood-Based Biomarkers   │
│ Plasma p-tau217 / Aβ42/40│
└────────────┬─────────────┘
             ↓
┌──────────────────────────┐
│      MRI Evaluation      │
│ Atrophy / Hippocampal    │
│       Assessment         │
└────────────┬─────────────┘
             ↓
┌──────────────────────────┐
│    PET Prioritization    │
│ Amyloid PET / CSF        │
│       Biomarkers         │
└──────────────────────────┘
```

The platform **does not automatically advance patients** between stages. Each progression remains under clinician control.

---

## 🧮 Explainable Scoring

Each patient receives a prioritization score between **0 and 100**.

The score is calculated using the available clinical factors:

```text
                    Σ (Severity × Weight)
Score = 100 × ─────────────────────────────
                    Σ |Weight| available
```

Only factors with available information contribute to the denominator.

This prevents a patient from being automatically assigned a lower score simply because certain information, such as MRI or biomarker data, is unavailable.

Instead, missing information is surfaced separately as **data gaps**, allowing clinicians to distinguish between:

* A genuinely lower-priority case
* A case where additional information is still required

### Current Scoring Factors

| Factor                               | Weight | Interpretation                            |
| ------------------------------------ | -----: | ----------------------------------------- |
| Clinical Dementia Rating (CDR)       |     30 | Higher CDR values increase prioritization |
| Mini-Mental State Examination (MMSE) |     25 | Lower MMSE values increase prioritization |

---

## 🚦 Risk Prioritization

|      Score | Label      | Interpretation                                          |
| ---------: | ---------- | ------------------------------------------------------- |
| **65–100** | 🔴 Urgent  | Prioritize clinician review and consider escalation     |
|  **40–64** | 🟠 Review  | Review alongside clinical context and data completeness |
|   **< 40** | 🟢 Routine | Not currently prioritized for escalation                |

> **Note:** A lower score does not indicate the absence of Alzheimer's disease or cognitive impairment. It only represents the patient's current prioritization relative to the demonstration cohort.

---

## 🤖 AI / ML Architecture

CogniTriage is designed to allow a statistical model to complement the transparent rule-based scoring engine.

```text
              Patient Data
                   │
                   ↓
        ┌─────────────────────┐
        │ Data Availability   │
        │ & Validation        │
        └──────────┬──────────┘
                   ↓
        ┌─────────────────────┐
        │ Explainable Rule-   │
        │ Based Scoring       │
        └──────────┬──────────┘
                   │
                   ├───────────────┐
                   ↓               ↓
        ┌─────────────────┐  ┌─────────────────┐
        │ Optional ML     │  │ Missing Data    │
        │ Probability     │  │ / Data Gaps     │
        └────────┬────────┘  └────────┬────────┘
                 │                    │
                 └──────────┬─────────┘
                            ↓
                 ┌─────────────────────┐
                 │ Explainable Patient │
                 │ Prioritization      │
                 └──────────┬──────────┘
                            ↓
                 ┌─────────────────────┐
                 │ Clinician Dashboard │
                 └─────────────────────┘
```

The ML component is **optional**. If unavailable, the core rule-based prioritization continues to operate.

This graceful-degradation approach ensures that a model-service failure does not prevent clinicians from accessing the worklist.

---

## 🛠️ Technology Stack

| Layer                    | Technology                      |
| ------------------------ | ------------------------------- |
| **Backend**              | Spring Boot 3.5.3               |
| **Programming Language** | Java 17                         |
| **Frontend**             | React + Vite                    |
| **Database**             | PostgreSQL 16                   |
| **ML Service**           | Python + scikit-learn + FastAPI |
| **Containerization**     | Docker Compose                  |
| **Deployment**           | Vercel                          |

---

## 📁 Project Structure

```text
AlzeihmerPCC/
│
├── backend/                  # Spring Boot backend
├── frontend/               # React + Vite clinician dashboard
├── model/                  # Optional ML model service
├── docker-compose.yml      # PostgreSQL container configuration
├── pom.xml                 # Maven configuration
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed:

* Java 17
* Node.js and npm
* Docker Desktop / Docker Engine
* Git

### 1. Clone the Repository

```bash
git clone https://github.com/shreyo2005/AlzeihmerPCC.git
cd AlzeihmerPCC
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

PostgreSQL runs locally on port `5434`.

### 3. Start the Backend

```bash
./mvnw spring-boot:run
```

The backend will be available at:

```text
http://localhost:8080
```

### 4. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at:

```text
http://localhost:5173
```

The application automatically seeds a synthetic cohort on startup, so no external dataset is required to explore the prototype.

---

## 🧪 Data & Safety

CogniTriage uses **synthetic patient data exclusively**.

No real patient records are included in the repository or deployed demonstration.

The synthetic cohort includes example attributes such as:

* Age
* Education
* MMSE
* CDR
* APOE ε4
* Neuroimaging-related measurements
* Comorbidities

This allows the system to demonstrate its prioritization workflow without exposing real patient information.

---

## ⚠️ Limitations

CogniTriage is a **technical and educational prototype**, not a clinically validated system.

### Current limitations include:

* The patient cohort is entirely synthetic.
* Scoring weights are reasoned defaults and are not clinically calibrated.
* The system has not undergone clinical validation.
* The current scoring approach is cross-sectional and does not model longitudinal cognitive decline.
* Missing information can affect the completeness of interpretation.
* The system does not provide a diagnosis.
* A low prioritization score does not rule out Alzheimer's disease or other cognitive disorders.
* Clinical interpretation and decision-making must remain with qualified healthcare professionals.

---

## 🔮 Future Scope

Potential future development includes:

* Integration with validated clinical datasets.
* Training and evaluation of interpretable ML models.
* Longitudinal patient-risk tracking.
* Improved missing-data handling.
* Model explainability using advanced interpretability techniques.
* Integration with healthcare information systems.
* Role-based access and secure authentication.
* Clinical validation with appropriate healthcare partners.
* Monitoring of model performance and potential bias.
* Expansion to broader cognitive and neurodegenerative conditions.

---

## 🌍 Real-World Impact

CogniTriage is designed around a practical healthcare challenge: **specialist capacity is limited, while the number of patients requiring assessment can be large.**

By converting patient information into an **ordered and explainable review worklist**, the platform could help clinicians organize their workload more effectively while preserving human oversight.

The key value is not replacing clinicians—it is helping them **focus attention where it may be most useful, understand why a patient was prioritized, and identify what information may still be missing.**

---

## 🏆 Built For

**Precision Care Challenge 2026**

CogniTriage was developed as a prototype demonstrating how AI-assisted prioritization, explainability, and clinician-controlled workflows can be combined in a healthcare-focused application.

---

## ⚕️ Disclaimer

> **CogniTriage is an educational and technical prototype developed for the Precision Care Challenge 2026. It is not certified medical software and has not been clinically validated for real-world patient care.**
>
> **It must not be used for diagnosis, treatment selection, emergency triage, or real clinical decision-making without appropriate validation, regulatory review, and qualified healthcare-professional oversight.**
