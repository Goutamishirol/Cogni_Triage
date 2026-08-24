import { useDeferredValue, useEffect, useState } from "react";

// const API = "http://localhost:8080/api/v1";
// const API = "https://cogni-triage-backend.onrender.com";
const API = "https://cogni-triage-backend.onrender.com/api/v1";

export default function App() {
  const [all, setAll] = useState([]);
  const [summary, setSummary] = useState(null);
  const [selected, setSelected] = useState(null);
  const [query, setQuery] = useState("");
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [openingId, setOpeningId] = useState(null);
  const [retryToken, setRetryToken] = useState(0);
  const [view, setView] = useState("lookup");
  const [riskFilter, setRiskFilter] = useState("ALL");
  const [theme, setTheme] = useState(() => {
    const savedTheme = localStorage.getItem("cognitriage-theme");
    if (savedTheme === "dark" || savedTheme === "light") return savedTheme;
    return window.matchMedia?.("(prefers-color-scheme: dark)").matches
      ? "dark"
      : "light";
  });

  function request(url, options) {
    return fetch(url, options).then((response) => {
      if (!response.ok) {
        throw new Error(`Service returned ${response.status}`);
      }
      return response.json();
    });
  }

  useEffect(() => {
    Promise.all([
      request(`${API}/patients`),
      request(`${API}/cohort/summary`),
    ])
      .then(([list, cohort]) => {
        setAll(list);
        setSummary(cohort);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [retryToken]);

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem("cognitriage-theme", theme);
  }, [theme]);

  function openPatient(id) {
    setOpeningId(id);
    setError(null);
    request(`${API}/patients/${id}`)
      .then(setSelected)
      .catch((e) => setError(e.message))
      .finally(() => setOpeningId(null));
  }

  function advance(id) {
    request(`${API}/patients/${id}/advance`, { method: "POST" })
      .then((updated) => {
        setSelected(updated);
        return request(`${API}/patients`);
      })
      .then(setAll)
      .catch((e) => setError(e.message));
  }

  function reset() {
    setQuery("");
    setSelected(null);
  }

  function retry() {
    setError(null);
    setLoading(true);
    setRetryToken((token) => token + 1);
  }

  function submitLookup() {
    const match = all.find(
      (patient) => patient.cohortId.toLowerCase() === query.trim().toLowerCase()
    );
    if (match) openPatient(match.id);
  }

  function openFromDashboard(patient) {
    setView("lookup");
    setQuery(patient.cohortId);
    openPatient(patient.id);
  }

  const deferredQuery = useDeferredValue(query);
  const q = deferredQuery.trim().toLowerCase();
  const results = q
    ? all.filter((p) => p.cohortId.toLowerCase().includes(q))
    : [];
  const riskOrder = { HIGH: 0, MEDIUM: 1, LOW: 2 };
  const riskCounts = all.reduce(
    (counts, patient) => ({ ...counts, [patient.riskTier]: counts[patient.riskTier] + 1 }),
    { HIGH: 0, MEDIUM: 0, LOW: 0 }
  );
  const dashboardPatients = all
    .filter((patient) => riskFilter === "ALL" || patient.riskTier === riskFilter)
    .slice()
    .sort((a, b) =>
      riskOrder[a.riskTier] - riskOrder[b.riskTier] || b.riskScore - a.riskScore
    );
  const filteredDashboardPatients = dashboardPatients.filter((patient) =>
    patient.cohortId.toLowerCase().includes(deferredQuery.trim().toLowerCase())
  );

  if (error)
    return (
      <div className="fatal">
        <h2>Cannot reach the service</h2>
        <p className="mono">{error}</p>
        <p>Start the backend on port 8080 and reload.</p>
        <button className="retry" type="button" onClick={retry}>
          Try again
        </button>
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
          <button
            className="theme-toggle"
            type="button"
            onClick={() => setTheme((current) => current === "light" ? "dark" : "light")}
            aria-label={`Switch to ${theme === "light" ? "dark" : "light"} theme`}
            aria-pressed={theme === "dark"}
            title={`Switch to ${theme === "light" ? "dark" : "light"} theme`}
          >
            <span className="theme-icon" aria-hidden="true">{theme === "light" ? "☾" : "☀"}</span>
            <span>{theme === "light" ? "Dark" : "Light"}</span>
          </button>
          <span className="synthetic">Synthetic data · decision support only</span>
        </div>
      </header>

      <div className="app-layout">
        <aside className="sidebar" aria-label="Primary navigation">
          <p className="sidebar-label">Workspace</p>
          <button
            type="button"
            className={`nav-item ${view === "lookup" ? "active" : ""}`}
            onClick={() => setView("lookup")}
          >
            <span className="nav-icon" aria-hidden="true">⌕</span>
            Patient lookup
          </button>
          <button
            type="button"
            className={`nav-item ${view === "dashboard" ? "active" : ""}`}
            onClick={() => setView("dashboard")}
          >
            <span className="nav-icon" aria-hidden="true">✚</span>
            Risk Overview
          </button>
        </aside>

      <main className={`main ${view === "dashboard" ? "dashboard-main" : ""}`}>
        {view === "dashboard" ? (
          <section className="dashboard" aria-labelledby="dashboard-title">
            <div className="dashboard-heading">
              <div>
                <p className="section-kicker">Clinical worklist</p>
                <h2 id="dashboard-title">Risk Overview</h2>
                <p className="dashboard-subtitle">
                  Prioritize patients for diagnostic follow-up by risk level.
                </p>
              </div>
              <span className="dashboard-disclaimer">Decision support only</span>
            </div>

            <div className="dashboard-search">
              <label htmlFor="dashboard-search-input">Search patient ID</label>
              <input
                id="dashboard-search-input"
                type="search"
                placeholder="Search CT_0001"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>

            <div className="risk-filters" role="group" aria-label="Filter by risk level">
              {[["ALL", "All"], ["HIGH", "High"], ["MEDIUM", "Medium"], ["LOW", "Low"]].map(([value, label]) => (
                <button
                  type="button"
                  className={`risk-filter ${riskFilter === value ? "selected" : ""} ${value.toLowerCase()}`}
                  aria-pressed={riskFilter === value}
                  key={value}
                  onClick={() => setRiskFilter(value)}
                >
                  {value !== "ALL" && <span className="risk-dot" aria-hidden="true" />}
                  {label}
                </button>
              ))}
            </div>

            <div className="summary-cards" aria-label="Patient risk summary">
              <div className="summary-card total"><span>Total patients</span><strong>{all.length}</strong></div>
              <div className="summary-card high"><span>High risk</span><strong>{riskCounts.HIGH}</strong></div>
              <div className="summary-card medium"><span>Medium risk</span><strong>{riskCounts.MEDIUM}</strong></div>
              <div className="summary-card low"><span>Low risk</span><strong>{riskCounts.LOW}</strong></div>
            </div>

            <div className="dashboard-table-wrap">
              <div className="table-heading">
                <div>
                  <p className="section-kicker">Patient risk summary</p>
                  <p className="result-count">{filteredDashboardPatients.length} patient{filteredDashboardPatients.length === 1 ? "" : "s"}</p>
                </div>
                <span className="sort-note">Sorted high to low risk score</span>
              </div>
              {loading ? (
                <div className="dashboard-empty" role="status">
                  <p>Loading patient risk summary</p>
                  <span>Preparing the prioritized worklist.</span>
                </div>
              ) : filteredDashboardPatients.length === 0 ? (
                <div className="dashboard-empty" role="status">
                  <p>No patient found for this ID.</p>
                  <span>Try a different Patient ID or select All.</span>
                </div>
              ) : (
                <div className="table-scroll">
                  <table className="risk-table">
                    <caption className="sr-only">Patients ordered by risk level and score</caption>
                    <thead>
                      <tr><th>Patient ID</th><th>MMSE</th><th>CDR</th><th>Score</th><th>Risk level</th><th>Priority</th><th><span className="sr-only">Action</span></th></tr>
                    </thead>
                    <tbody>
                      {filteredDashboardPatients.map((patient) => (
                          <tr key={patient.id}>
                            <td><button type="button" className="patient-link mono" onClick={() => openFromDashboard(patient)}>{patient.cohortId}</button></td>
                            <td className="mono">{patient.mmse ?? "—"}</td>
                            <td className="mono">{patient.cdr ?? "—"}</td>
                            <td className="mono score">{patient.riskScore.toFixed(2)}</td>
                            <td><span className={`risk-badge ${patient.riskTier.toLowerCase()}`}><span className="risk-dot" aria-hidden="true" />{patient.riskTier}</span></td>
                            <td className="priority-text">{patient.riskTier === "HIGH" ? "Urgent review" : patient.riskTier === "MEDIUM" ? "Further review" : "Routine"}</td>
                            <td><button type="button" className="view-record" onClick={() => openFromDashboard(patient)}>View</button></td>
                          </tr>
                        ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </section>
        ) : (
        <>
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
              onKeyDown={(e) => {
                if (e.key === "Enter") submitLookup();
              }}
              aria-describedby="lookup-help"
              aria-keyshortcuts="Enter"
            />
            {query && (
              <button
                className="reset"
                type="button"
                onClick={reset}
                aria-label="Clear patient lookup"
                title="Clear patient lookup"
              >
                <span aria-hidden="true">×</span>
              </button>
            )}
          </div>
          <div className="lookup-meta" id="lookup-help">
            <span>Individual record lookup</span>
            <span className="mono">{summary ? `${summary.totalPatients} available` : ""}</span>
          </div>
        </div>

        {loading ? (
          <div className="idle loading-state" role="status">
            <span className="status-dot" aria-hidden="true" />
            <p className="idle-main">Loading patient directory</p>
            <p className="idle-sub">Preparing individual records for lookup.</p>
          </div>
        ) : !q ? (
          <div className="idle">
            <p className="idle-main">Enter a patient ID to begin.</p>
            <p className="idle-pattern mono">Patient_ID pattern: CT_0001</p>
            <p className="idle-sub">
              Records are retrieved individually. No cohort is displayed by default.
            </p>
            <button
              className="example-link"
              type="button"
              onClick={() => setQuery("CT_0001")}
            >
              Try an example ID <span aria-hidden="true">→</span>
            </button>
          </div>
        ) : results.length === 0 ? (
          <div className="idle">
            <p className="idle-main">
              No record found for <span className="mono">{query}</span>
            </p>
            <p className="idle-sub">Check the identifier and try again.</p>
          </div>
        ) : (
          <div className="results-wrap">
            <div className="result-summary" aria-live="polite">
              <div>
                <p className="section-kicker">Search results</p>
                <p className="result-count">{results.length} matching record{results.length === 1 ? "" : "s"}</p>
              </div>
              <span className="result-hint">Select a record to view its pathway</span>
            </div>
            <div className="results">
            {results.map((p) => (
              <div className="row" key={p.id}>
                <button
                  type="button"
                  className={`letterbox ${selected?.id === p.id ? "open" : ""}`}
                  aria-expanded={selected?.id === p.id}
                  aria-controls={`record-${p.id}`}
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
                    {openingId === p.id
                      ? "Loading"
                      : selected?.id === p.id
                      ? "Close"
                      : "Open record"}
                  </span>
                </button>

                {selected?.id === p.id && (
                  <article
                    className={`record u-${selected.riskTier.toLowerCase()}`}
                    id={`record-${p.id}`}
                  >
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
                    <div className="record-tools">
                      <span>Decision support summary</span>
                      <button className="print" type="button" onClick={() => window.print()}>
                        Print summary
                      </button>
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
                            type="button"
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
          </div>
        )}
        </>
        )}
      </main>
      </div>
    </div>
  );
}