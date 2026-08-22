import { useEffect, useState } from "react";

const API = "http://localhost:8080/api/v1";

export default function App() {
  const [all, setAll] = useState([]);
  const [summary, setSummary] = useState(null);
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState("");
  const [error, setError] = useState(null);

  useEffect(() => {
    Promise.all([
      fetch(`${API}/patients`).then((r) => r.json()),
      fetch(`${API}/cohort/summary`).then((r) => r.json()),
    ])
      .then(([list, cohort]) => {
        setAll(list);
        setSummary(cohort);
      })
      .catch((e) => setError(e.message));
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
      .then(setAll)
      .catch((e) => setError(e.message));
  }

  function reset() {
    setQuery("");
    setSelected(null);
  }

  const q = query.trim().toLowerCase();
  const results = q
    ? all.filter((p) => p.cohortId.toLowerCase().includes(q))
    : [];

  if (error)
    return (
      <div className="fatal">
        <h2>Cannot reach the service</h2>
        <p className="mono">{error}</p>
        <p>Start the backend on port 8080 and reload.</p>
      </div>
    );

  return (
    <div className="shell">
      <header className="bar">
        <div className="brand">
          <span className="mark" aria-hidden="true" />
          <div>
            <h1>CogniTriage</h1>
            <p>Diagnostic pathway prioritization</p>
          </div>
        </div>
        <div className="bar-right">
          {summary && (
            <span className="cohort-size mono">
              {summary.totalPatients} in cohort
            </span>
          )}
          <span className="synthetic">Synthetic data · decision support only</span>
        </div>
      </header>

      <main className="main">
        <div className="lookup">
          <label htmlFor="q">Patient lookup</label>
          <div className="field">
            <input
              id="q"
              type="search"
              autoComplete="off"
              placeholder="Enter patient ID"
              value={query}
              onChange={(e) => {
                setQuery(e.target.value);
                setSelected(null);
              }}
            />
            {query && (
              <button className="reset" onClick={reset}>
                Reset
              </button>
            )}
          </div>
        </div>

        {!q ? (
          <div className="idle">
            <p className="idle-main">Enter a patient ID to begin.</p>
            <p className="idle-sub">
              Records are retrieved individually. No cohort is displayed by default.
            </p>
          </div>
        ) : results.length === 0 ? (
          <div className="idle">
            <p className="idle-main">
              No record found for <span className="mono">{query}</span>
            </p>
            <p className="idle-sub">Check the identifier and try again.</p>
          </div>
        ) : (
          <div className="results">
            {results.map((p) => (
              <div className="row" key={p.id}>
                <button
                  className={`letterbox ${selected?.id === p.id ? "open" : ""}`}
                  onClick={() =>
                    selected?.id === p.id ? setSelected(null) : openPatient(p.id)
                  }
                >
                  <div className="lb-left">
                    <span className="lb-id mono">{p.cohortId}</span>
                    <span className="lb-demo">
                      {p.age} years · {p.sex === "FEMALE" ? "Female" : "Male"}
                    </span>
                  </div>
                  <div className="lb-mid">
                    <span className="lb-stage-label">Current stage</span>
                    <span className="lb-stage">{p.currentStage}</span>
                  </div>
                  <span className="lb-open">
                    {selected?.id === p.id ? "Close" : "Open record"}
                  </span>
                </button>

                {selected?.id === p.id && (
                  <article className={`record u-${selected.riskTier.toLowerCase()}`}>
                    <div className="banner">
                      <span className="banner-flag">
                        {selected.riskTier === "HIGH"
                          ? "Urgent"
                          : selected.riskTier === "MEDIUM"
                          ? "Review"
                          : "Routine"}
                      </span>
                      <div className="banner-text">
                        <p className="banner-action">{selected.actionSummary}</p>
                        <p className="banner-meta">
                          Priority score <b className="mono">{selected.riskScore}</b>
                          {" · "}
                          {selected.currentStage}
                        </p>
                      </div>
                    </div>

                    <div className="cols">
                      <section>
                        <h3>Recommended next steps</h3>
                        <ul className="tests">
                          {selected.recommendedTests.map((t) => (
                            <li key={t}>{t}</li>
                          ))}
                        </ul>
                        <p className="prose">{selected.actionRationale}</p>
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

                      <section>
                        <h3>Assessment findings</h3>
                        <dl className="findings">
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
                          <div>
                            <dt>Education</dt>
                            <dd className="mono">
                              {selected.educationYears ?? "—"}
                            </dd>
                          </div>
                          <div>
                            <dt>eTIV</dt>
                            <dd className="mono">{selected.etiv ?? "—"}</dd>
                          </div>
                        </dl>
                        {selected.comorbidities.length > 0 && (
                          <p className="prose">
                            Co-morbid conditions:{" "}
                            {selected.comorbidities
                              .map((c) => c.replace(/_/g, " ").toLowerCase())
                              .join(", ")}
                            .
                          </p>
                        )}
                      </section>
                    </div>

                    <section className="full">
                      <h3>Basis for prioritization</h3>
                      {selected.factors.map((f) => (
                        <div className="factor" key={f.name}>
                          <div className="factor-row">
                            <span>
                              {f.name}{" "}
                              <em className="mono">{f.observedValue}</em>
                            </span>
                            <span
                              className={`factor-num ${
                                f.protective ? "pos" : "neg"
                              }`}
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
                          <p className="prose">{f.rationale}</p>
                        </div>
                      ))}
                    </section>

                    {selected.dataGaps.length > 0 && (
                      <section className="full gapbox">
                        <h3>Not yet available</h3>
                        <ul className="gaps">
                          {selected.dataGaps.map((g) => (
                            <li key={g}>{g}</li>
                          ))}
                        </ul>
                        <p className="prose">
                          Score computed on available data only. Absence of data
                          is not evidence of absence of disease.
                        </p>
                      </section>
                    )}
                  </article>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}