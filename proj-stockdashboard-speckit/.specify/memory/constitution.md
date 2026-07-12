<!--
SYNC IMPACT REPORT
==================
Version change: [template] → 1.0.0 (initial ratification)

Modified principles: N/A (first populated version)

Added sections:
  - Core Principles (I–V)
  - Technology Stack
  - Development Workflow
  - Governance

Removed sections: N/A

Templates checked:
  ✅ .specify/templates/plan-template.md — Constitution Check gate is generic; no hardcoded principle names
  ✅ .specify/templates/spec-template.md — no constitution-specific references
  ✅ .specify/templates/tasks-template.md — no constitution-specific references

Deferred TODOs: None
-->

# Stock Analysis Dashboard Constitution

## Core Principles

### I. Pure Static Website (NON-NEGOTIABLE)

The application MUST be delivered as a pure static website using only vanilla HTML, CSS,
and client-side JavaScript. No backend servers, no Node.js API routes, no serverless
functions, and no databases are permitted at any layer of the stack.

**Rationale**: Static delivery eliminates operational complexity, hosting costs, and
attack surface. Any deviation requires explicit constitutional amendment.

### II. Runtime API Data via FMP

All stock data MUST be fetched at runtime from the Financial Modeling Prep (FMP)
REST API. The user-supplied API key MUST be stored in `localStorage` and appended
to every outbound request. No build-time data fetching or data bundling is allowed.

**Rationale**: Keeps the project truly client-side and ensures data freshness. The key
lives in the user's browser, never in source code or environment variables.

### III. CDN-First Charting

The charting library MUST be TradingView Lightweight Charts, loaded via CDN script tag
in `index.html`. No locally bundled or npm-installed charting library may replace it.

**Rationale**: CDN loading enforces the static constraint and matches the official
Lightweight Charts integration pattern documented by TradingView.

### IV. LocalStorage Persistence

All client-side state requiring persistence (watchlist entries, FMP API key, user
preferences) MUST be stored exclusively in `localStorage`. No `sessionStorage`, cookies,
or IndexedDB are permitted unless a future amendment explicitly allows them.

**Rationale**: Keeps persistence simple, offline-capable, and zero-infrastructure.

### V. No UI Framework

The application MUST NOT use React, Vue, Angular, Svelte, or any other component-based
UI framework. DOM manipulation MUST be performed via native browser APIs or vanilla JS
helper utilities contained within the project's own source modules.

**Rationale**: The project is a learning exercise in Spec-Driven Development using a
minimal stack. Introducing a framework shifts complexity to tooling rather than to the
feature design.

## Technology Stack

- **Build tool**: Vite (development server + production bundler only; no SSR)
- **Language**: Vanilla JavaScript (ES modules, no TypeScript)
- **Charting**: TradingView Lightweight Charts via CDN
- **Data source**: Financial Modeling Prep (FMP) REST API v3
  - Base URL: `https://financialmodelingprep.com/api/v3`
  - Auth: `?apikey=<value>` query parameter
- **Persistence**: `localStorage` (watchlist, API key)
- **Source layout**:

  ```text
  src/
  ├── api.js        — FMP fetch wrappers with error handling
  ├── watchlist.js  — localStorage CRUD
  ├── chart.js      — Lightweight Charts setup and data mapping
  └── ui.js         — DOM rendering helpers
  index.html        — single entry point; CDN script tags here
  style.css         — global styles
  ```

## Development Workflow

- Features MUST be specified in `.specify/memory/` before implementation begins.
- All FMP endpoint calls MUST go through `src/api.js`; direct `fetch` calls in UI
  or chart modules are not permitted.
- Empty states (empty watchlist, missing API key, API errors, rate-limit exceeded)
  MUST be handled gracefully with user-visible feedback.
- The settings panel MUST be the fallback destination when the API key is absent
  or returns a 401/403 response.
- Code complexity MUST be justified against the constitution's static + no-framework
  constraints; if a pattern requires a framework analogue, simplify the design instead.

## Governance

This constitution supersedes all other coding practices and verbal agreements for this
project. Amendments MUST be made through the `/speckit-constitution` command, increment
the semantic version, and record the change in the Sync Impact Report above.

Compliance is verified at two gates:

1. **Before implementation planning** (`/speckit-plan` Constitution Check section)
2. **Before task generation** (`/speckit-tasks` prerequisite review)

Any implementation task that violates a NON-NEGOTIABLE principle MUST be rejected and
redesigned before work proceeds.

**Version**: 1.0.0 | **Ratified**: 2026-07-12 | **Last Amended**: 2026-07-12
