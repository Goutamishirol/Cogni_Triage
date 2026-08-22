# CogniTriage

**AI-driven prioritization system for early Alzheimer's diagnostic pathways**
Precision Care Challenge 2026

CogniTriage ranks screened patients by how urgently they need the next diagnostic
test, and recommends which tests those should be. It converts a large screening
population into an ordered worklist a neurologist can act on.

> **This system does not diagnose.** It prioritizes patients for clinician review.
> Every output is a recommendation requiring specialist interpretation.
> All patient data in this prototype is **synthetic**.

---

## Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 3.5.3 · Java 17 |
| Database | PostgreSQL 16 (Docker, port **5434**) |
| Frontend | React + Vite (port 5173) |
| Model *(planned)* | Python · scikit-learn · FastAPI (port 8000) |

---

## Running it

```bash
git clone https://github.com/shreyo2005/AlzeihmerPCC.git
cd AlzeihmerPCC

# 1. Database
docker compose up -d

# 2. Backend  →  http://localhost:8080
./mvnw spring-boot:run

# 3. Frontend →  http://localhost:5173
cd frontend
npm install
npm run dev
```

Twenty synthetic patients seed automatically on first boot. No dataset needed.

**Windows note:** the Spring Boot plugin passes `-Duser.timezone=Asia/Kolkata`.
Without it, the Postgres JDBC driver sends the legacy zone name `Asia/Calcutta`,
which Postgres 16 rejects, and the app fails to connect.

---

## How the prioritization works

Each patient gets a score from 0–100 built from weighted, individually
explainable factors:

```
score = 100 × ( Σ severityᵢ × weightᵢ ) / ( Σ |weightᵢ| available )
```

The denominator counts **only the factors that patient actually has data for**.
A patient with no MRI is not penalised for missing it — the gap is reported
separately in `dataGaps`. Without this, everyone at Stage 1 would score near
zero and the ranking would be meaningless.

| Factor | Weight | Severity mapping |
|---|---|---|
| CDR | 30 | 0 → 0.0 · 0.5 → 0.75 · ≥1 → 1.0 |
| MMSE | 25 | linear: 30 → 0.0 · ≤18 → 1.0 |

Additional factors (APOE ε4, brain volume, age, comorbidities, education as a
protective term) are specified and pending clinical review before being enabled.

**Tiers:** ≥65 Urgent · 40–64 Review · <40 Routine

This is deliberately **not** a black-box model. Every point in a patient's score
traces to a named factor with a clinician-readable rationale.

---

## Four-stage diagnostic pathway

```
Cognitive Screening → Blood Biomarkers → MRI Evaluation → PET Prioritization
```

Recommended tests at each stage are grounded in published guidance:

- **Stage 1** follows the standard reversible-cause workup (TSH, B12, metabolic
  panel) before impairment is attributed to neurodegeneration.
- **Stage 2** uses plasma p-tau217 and Aβ42/40, the analytes covered by the
  Alzheimer's Association 2025 blood-biomarker clinical practice guideline.
  High-sensitivity assays serve as a triaging test — a negative result rules out
  AD pathology with high probability.
- **Stage 3** uses the Scheltens medial temporal atrophy visual rating scale,
  which predicts MCI-to-dementia conversion, plus hippocampal volumetry.
- **Stage 4** is confirmatory (amyloid PET or CSF) and gates anti-amyloid
  therapy eligibility.

**Nothing auto-advances.** Escalation is always an explicit clinician action.

---

## Limitations

| Field | Source | Status |
|---|---|---|
| MMSE, CDR, age, sex, education | `DemoCohortSeeder` | **Synthetic** |
| eTIV, nWBV | `DemoCohortSeeder` | **Synthetic** |
| APOE ε4 | `DemoCohortSeeder` | **Synthetic** |
| Comorbidities | `DemoCohortSeeder` | **Synthetic** |

- **The entire prototype cohort is fabricated.** No patient depicted is real.
  ADNI access takes weeks to approve; we built against synthetic data shaped to
  mirror a realistic clinical funnel so the workflow could be demonstrated now.
- **Not clinically validated.** Weights and thresholds are reasoned defaults, not
  fitted to outcome data.
- **Cross-sectional only.** The model sees a snapshot, not a trajectory. Rate of
  cognitive decline is a stronger predictor than any single score.
- **Low score is not reassurance.** It means not prioritized for escalation now.
- **Missing data lowers confidence, not score.** Read `dataGaps` before acting on
  a rank.

---

## Team

| Who | Owns |
|---|---|
| Shreyoshi | Backend — entities, scoring engine, pathway service, REST API |
| Suvidhya | ML model — training and serving |
| Gautami | Frontend — clinician dashboard |

---

## For Suvidhya — model integration

### What you're building

A small FastAPI service on **port 8000** that takes a patient's features and
returns a probability. The Java backend calls it as one factor among several.

**Your model does not replace the scoring engine.** It becomes one weighted
`RiskFactor` inside it. Two reasons: explainability is 20% of the grading
rubric and a bare probability provides none, and if your service is down the
dashboard still works.

### The contract — this is frozen

**Request** — what Java sends you:

```json
POST http://localhost:8000/predict
Content-Type: application/json

{
  "age": 74,
  "educationYears": 12,
  "mmse": 25,
  "cdr": 0.5,
  "apoe4AlleleCount": 1,
  "nwbv": 0.731,
  "comorbidityCount": 1
}
```

Any field may be `null` — real patients have gaps. Handle it.

**Response** — what you must return:

```json
{
  "probability": 0.73,
  "modelVersion": "logreg-oasis-v1"
}
```

`probability` is a float 0.0–1.0. Nothing else is read by the backend.
Field names are camelCase and must match exactly.

### Current state vs after your model

**Right now** (`alz.ml.enabled: false` in `application.yml`) — the factors array
in every patient response contains only rule-based entries:

```json
"factors": [
  {
    "name": "CDR",
    "observedValue": "0.5",
    "contribution": 22.5,
    "rationale": "CDR of 0.5 indicates measurable impairment.",
    "protective": false
  },
  {
    "name": "MMSE",
    "observedValue": "25",
    "contribution": 10.4,
    "rationale": "MMSE of 25 relative to the 24-point screening cut-off.",
    "protective": false
  }
],
"dataGaps": []
```

**After your service is live** (`alz.ml.enabled: true`) — one more entry appears,
weight 20, and every other score renormalises automatically:

```json
"factors": [
  {
    "name": "CDR",
    "observedValue": "0.5",
    "contribution": 22.5,
    "rationale": "CDR of 0.5 indicates measurable impairment.",
    "protective": false
  },
  {
    "name": "Model estimate",
    "observedValue": "73%",
    "contribution": 14.6,
    "rationale": "Logistic regression trained on the OASIS cohort. Statistical estimate, not a clinical judgment.",
    "protective": false
  },
  {
    "name": "MMSE",
    "observedValue": "25",
    "contribution": 10.4,
    "rationale": "MMSE of 25 relative to the 24-point screening cut-off.",
    "protective": false
  }
]
```

**If your service is unreachable**, nothing breaks. The backend catches the
failure, logs a warning, and the response reads:

```json
"dataGaps": ["Model estimate unavailable"]
```

The rule-based factors still produce a valid ranking. Please test this by
turning your service off before demo day.

### Three constraints

**1. Train on real OASIS, not our demo cohort.** The seeded patients are
fabricated. A model trained on them learns the rules we used to fabricate them
and reports meaningless accuracy.

**2. Watch for target leakage.** If you derive the label from CDR (`CDR > 0`),
then CDR cannot be an input feature — the model would just read the answer and
report ~99% accuracy. Java sends `cdr` in the request; ignore it in your
prediction.

**3. Logistic regression, not a neural net.** ~400 patients in OASIS-1.
Deep learning overfits immediately, and readable coefficients are worth more to
us than a fraction of a point of AUC. We need to be able to say *why*.

### Folder

Your work goes in `model/` at the repo root. Add `*.joblib` and `__pycache__/`
to `.gitignore` — trained model files shouldn't be committed.

---

## For Gautami — API reference

Backend runs on `http://localhost:8080`. Base path `/api/v1`.
CORS is already configured for ports 5173 and 3000.

### `GET /api/v1/patients`

Prioritized worklist, highest score first.

**Query params** (both optional): `tier` (`HIGH`/`MEDIUM`/`LOW`), `search`
(matches anywhere in the cohort ID).

```json
[
  {
    "id": 26,
    "cohortId": "CT_0006",
    "age": 85,
    "sex": "MALE",
    "mmse": 18,
    "cdr": 1.0,
    "riskScore": 100.0,
    "riskTier": "HIGH",
    "currentStage": "MRI Evaluation",
    "leadingFactor": "CDR 1.0 is the leading driver",
    "nextAction": "Prioritize for amyloid confirmation"
  }
]
```

### `GET /api/v1/patients/{id}`

Full record with the itemised risk breakdown. Returns **404** if the id doesn't
exist. Note `id` changes when the database is reseeded — see the cohort lookup
below for a stable alternative.

```json
{
  "id": 26,
  "cohortId": "CT_0006",
  "age": 85,
  "sex": "MALE",
  "educationYears": 8,
  "mmse": 18,
  "cdr": 1.0,
  "apoe4AlleleCount": 2,
  "comorbidities": ["STROKE_HISTORY", "HYPERTENSION"],
  "nwbv": 0.671,
  "etiv": 1520.0,
  "riskScore": 100.0,
  "riskTier": "HIGH",
  "recommendation": "Prioritize for escalation to the next diagnostic stage",
  "currentStage": "MRI Evaluation",
  "currentStageDescription": "Structural imaging to assess medial temporal atrophy and narrow high-risk candidates.",
  "nextStage": "PET Scan Prioritization",
  "escalationRecommended": true,
  "actionSummary": "Prioritize for amyloid confirmation",
  "recommendedTests": [
    "Amyloid PET, or CSF Abeta42/40 and p-tau181",
    "APOE genotyping (informs ARIA risk if therapy is considered)",
    "Baseline MRI for ARIA monitoring"
  ],
  "actionRationale": "Imaging supports a neurodegenerative pattern. Confirmatory testing is the gate for treatment eligibility.",
  "factors": [
    {
      "name": "CDR",
      "observedValue": "1.0",
      "contribution": 30.0,
      "rationale": "CDR of 1.0 indicates measurable impairment.",
      "protective": false
    }
  ],
  "dataGaps": []
}
```

### `GET /api/v1/patients/by-cohort/{cohortId}`

Same shape as above, looked up by cohort ID (e.g. `CT_0016`). **Prefer this for
anything hardcoded** — cohort IDs survive reseeding, numeric ids don't.

### `POST /api/v1/patients/{id}/advance`

Moves a patient one stage forward and returns the updated detail object.
No-op if already at PET. Nothing auto-advances — this is the clinician's action.

### `GET /api/v1/cohort/summary`

```json
{
  "totalPatients": 20,
  "highRisk": 8,
  "mediumRisk": 4,
  "lowRisk": 8,
  "byStage": {
    "Cognitive Screening": 13,
    "Blood-Based Biomarkers": 3,
    "MRI Evaluation": 2,
    "PET Scan Prioritization": 2
  }
}
```

### `GET /api/v1/stages`

Stage metadata for a pathway visualisation.

```json
[
  {
    "key": "COGNITIVE_SCREENING",
    "order": 1,
    "name": "Cognitive Screening",
    "description": "MoCA/MMSE plus baseline clinical data. Produces initial risk prioritization."
  }
]
```

### `GET /api/v1/ping`

Health check. Returns `pong`. Use it to confirm the backend is up before
debugging anything else.

### Notes for the UI

- **`riskTier` values are `HIGH` / `MEDIUM` / `LOW`.** The current dashboard
  displays these as **Urgent / Review / Routine** — action words rather than
  severity labels, which keeps the interface on the right side of the
  non-diagnostic requirement. Please keep that mapping.
- **`contribution` is signed.** Negative means protective (education is the only
  one currently). The `protective` boolean is provided so you don't have to
  check the sign yourself.
- **`factors` arrives pre-sorted** by absolute contribution, largest first.
- **`dataGaps` matters.** A score of 35 with two gaps means something different
  from a score of 35 with none. Please surface it.
- **Never display a synthetic value unlabelled.** Every number in this prototype
  is fabricated, and the disclaimer banner must stay visible.

---

## Repository layout

```
AlzeihmerPCC/
├── src/main/java/com/precisioncare/cognitriage/
│   ├── patient/     Patient entity, repository, enums
│   ├── risk/        Scoring engine, RiskFactor, RiskAssessment, RiskTier
│   ├── pathway/     DiagnosticStage, PathwayService, RecommendedAction
│   ├── api/         PatientController + DTOs
│   └── ingest/      DemoCohortSeeder
├── frontend/        React + Vite dashboard
├── model/           Python model service (Suvidhya)
├── docker-compose.yml
└── pom.xml
```

## Branching

Work on feature branches, PR into `main`. Rebase before opening a PR.
Announce before changing a DTO shape — it breaks work in progress.