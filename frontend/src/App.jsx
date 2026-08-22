import { useEffect, useState } from "react";

const API = "http://localhost:8080/api/v1";

export default function App() {
  const [patients, setPatients] = useState([]);
  const [summary, setSummary] = useState(null);
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetch(`${API}/patients`).then((r) => r.json()),
      fetch(`${API}/cohort/summary`).then((r) => r.json()),
    ])
      .then(([list, cohort]) => {
        setPatients(list);
        setSummary(cohort);
        setLoading(false);
      })
      .catch((e) => {
        setError(e.message);
        setLoading(false);
      });
  }, []);

  function openPatient(id) {
    fetch(`${API}/patients/${id}`)
      .then((r) => r.json())
      .then(setSelected)
      .catch((e) => setError(e.message));
  }

  function advance(id) {
    fetch(`${API}/patients/${id}/advance`, { method: "POST" })
      .then((r) => r.json())
      .then((updated) => {
        setSelected(updated);
        return fetch(`${API}/patients`).then((r) => r.json());
      })
      .then(setPatients)
      .catch((e) => setError(e.message));
  }

  // Filter happens here, in the browser. Instant, no round trip.
  const visible = patients.filter((p) =>
    p.cohortId.toLowerCase().includes(query.trim().toLowerCase())
  );

  if (error)
    return (
      <div className="state">
        <h2>Cannot reach the API</h2>
        <p className="mono">{error}</p>
        <p>Start the backend on port 8080, then reload.</p>
      </div>
    );

  return (
    <div className="app">
      <header className="masthead">
        <div>
          <h1>CogniTriage</h1>
          <p className="sub">
            Diagnostic pathway prioritization · Precision Care Challenge 2026
          </p>
        </div>
        {summary && (
          <div className="counts">
            <span>
              <b>{summary.totalPatients}</b> screened
            </span>
            <span className="c-high">
              <b>{summary.highRisk}</b> urgent
            </span>
            <span className="c-med">
              <b>{summary.mediumRisk}</b> review
            </span>
            <span className="c-low">
              <b>{summary.lowRisk}</b> routine
            </span>
          </div>
        )}
      </header>

      <p className="disclaimer">
        Synthetic cohort. Decision support only — this system does not diagnose.
      </p>

      <div className="layout">
        <div className="left">
          <div className="searchbar">
            <input
              type="search"
              placeholder="Search by patient ID…"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            {query && (
              <button className="clear" onClick={() => setQuery("")}>
                Clear
              </button>
            )}
          </div>

          {loading ? (
            <p className="note-empty">Loading cohort…</p>
          ) : visible.length === 0 ? (
            <p className="note-empty">
              No patient matches <span className="mono">{query}</span>
            </p>
          ) : (
            <div className="cards">
              {visible.map((p) => (
                <button
                  key={p.id}
                  className={`card ${selected?.id === p.id ? "on" : ""}`}
                  onClick={() => openPatient(p.id)}
                >
                  <span className="card-id mono">{p.cohortId}</span>
                  <span className="card-demo">
                    {p.age} · {p.sex === "FEMALE" ? "F" : "M"}
                  </span>
                  <span className="card-stage">{p.currentStage}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="right">
          {!selected ? (
            <div className="note note-blank">
              <p>Select a patient card to open their assessment.</p>
            </div>
          ) : (
            <article className={`note t-${selected.riskTier.toLowerCase()}`}>
              <div className="note-head">
                <div>
                  <span className="note-label">Patient</span>
                  <h2 className="mono">{selected.cohortId}</h2>
                </div>
                <div className="note-meta">
                  {selected.age} yrs ·{" "}
                  {selected.sex === "FEMALE" ? "Female" : "Male"} ·{" "}
                  {selected.educationYears} yrs education
                </div>
              </div>

              <div className="verdict">
                <div className="verdict-tier">
                  {selected.riskTier === "HIGH"
                    ? "Urgent"
                    : selected.riskTier === "MEDIUM"
                    ? "Review"
                    : "Routine"}
                </div>
                <div className="verdict-body">
                  <p className="verdict-action">{selected.actionSummary}</p>
                  <p className="verdict-score">
                    Priority score <b>{selected.riskScore}</b> · currently at{" "}
                    {selected.currentStage}
                  </p>
                </div>
              </div>

              <section className="block">
                <span className="note-label">Recommended next steps</span>
                <ul className="tests">
                  {selected.recommendedTests.map((t) => (
                    <li key={t}>{t}</li>
                  ))}
                </ul>
                <p className="reasoning">{selected.actionRationale}</p>
                {selected.nextStage && (
                  <button
                    className={`advance ${
                      selected.escalationRecommended ? "urge" : ""
                    }`}
                    onClick={() => advance(selected.id)}
                  >
                    Advance to {selected.nextStage}
                  </button>
                )}
              </section>

              <section className="block">
                <span className="note-label">Observations</span>
                <dl className="obs">
                  <div>
                    <dt>MMSE</dt>
                    <dd className="mono">{selected.mmse ?? "—"}</dd>
                  </div>
                  <div>
                    <dt>CDR</dt>
                    <dd className="mono">{selected.cdr ?? "—"}</dd>
                  </div>
                  <div>
                    <dt>APOE ε4</dt>
                    <dd className="mono">
                      {selected.apoe4AlleleCount ?? "—"}
                    </dd>
                  </div>
                  <div>
                    <dt>nWBV</dt>
                    <dd className="mono">{selected.nwbv ?? "—"}</dd>
                  </div>
                </dl>
                {selected.comorbidities.length > 0 && (
                  <p className="comorb">
                    <em>Co-morbid:</em>{" "}
                    {selected.comorbidities
                      .map((c) => c.replace(/_/g, " ").toLowerCase())
                      .join(", ")}
                  </p>
                )}
              </section>

              <section className="block">
                <span className="note-label">Basis for prioritization</span>
                {selected.factors.map((f) => (
                  <div className="factor" key={f.name}>
                    <div className="factor-row">
                      <span className="factor-name">
                        {f.name} <em className="mono">{f.observedValue}</em>
                      </span>
                      <span
                        className={`factor-num ${f.protective ? "pos" : "neg"}`}
                      >
                        {f.contribution > 0 ? "+" : ""}
                        {f.contribution}
                      </span>
                    </div>
                    <div className="track">
                      <div
                        className={`fill ${f.protective ? "pos" : "neg"}`}
                        style={{
                          width: `${Math.min(
                            Math.abs(f.contribution) * 3.2,
                            100
                          )}%`,
                        }}
                      />
                    </div>
                    <p className="reasoning">{f.rationale}</p>
                  </div>
                ))}
              </section>

              {selected.dataGaps.length > 0 && (
                <section className="block">
                  <span className="note-label">Not yet available</span>
                  <ul className="gaps">
                    {selected.dataGaps.map((g) => (
                      <li key={g}>{g}</li>
                    ))}
                  </ul>
                  <p className="reasoning">
                    Score computed on available data only. Absence of data is
                    not evidence of absence of disease.
                  </p>
                </section>
              )}
            </article>
          )}
        </div>
      </div>
    </div>
  );
}