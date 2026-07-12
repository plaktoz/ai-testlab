# Implementation Plan: Stock Analysis Dashboard

**Branch**: `001-stock-dashboard` | **Date**: 2026-07-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-stock-dashboard/spec.md`

## Summary

A pure-client-side personal stock analysis dashboard built with Vite + Vanilla JS. Users search
for stocks, maintain a persistent localStorage watchlist, and view per-ticker details across five
panels (Overview, Price Chart, Fundamentals, Earnings Calendar, Settings). All market data is
fetched at runtime from Financial Modeling Prep (FMP) REST API v3 using a user-supplied API key;
the interactive price chart is rendered by TradingView Lightweight Charts loaded from CDN.

## Technical Context

**Language/Version**: Vanilla JavaScript (ES modules, ES2020+) — no TypeScript, no transpilation
beyond Vite's default bundling.

**Primary Dependencies**:
- Vite 5.x — dev server + production bundler (devDependency; no SSR plugins)
- TradingView Lightweight Charts v4.x — charting library (CDN `<script>` tag, not npm)
- Financial Modeling Prep REST API v3 — external data source (no SDK, raw `fetch`)

**Storage**: `localStorage` exclusively — watchlist (JSON-serialised `string[]`), FMP API key
(plain string).

**Testing**: Manual browser testing against acceptance scenarios in `spec.md`. No automated test
framework (pure static site; no test runner specified in constitution).

**Target Platform**: Modern desktop browsers — Chrome 90+, Firefox 88+, Safari 14+, Edge 90+.
Functional (non-optimised) mobile layout acceptable per spec Assumptions.

**Project Type**: Static single-page web application. Single entry point `index.html`. Vite serves
and bundles; all logic executes client-side.

**Performance Goals**:
- Search results appear within 2 seconds (FR-002, SC-001)
- Detail view (Overview) loads within 3 seconds of ticker selection (SC-006)
- Chart time-range switch re-renders within 2 seconds (SC-005)
- Error states surface within 3 seconds (SC-004)

**Constraints**:
- No backend, no serverless, no Node.js API routes (Constitution I)
- No npm charting library — TradingView Charts via CDN only (Constitution III)
- No React / Vue / Angular / Svelte (Constitution V)
- FMP API key never committed to source — stored exclusively in localStorage (Constitution II)
- All FMP calls routed through `src/api.js` — no direct `fetch` in other modules (constitution
  Development Workflow rule)

**Scale/Scope**: Single-user personal tool. ~6 FMP endpoints, 4 source modules (~400–600 LOC
total), 1 HTML file, 1 CSS file. Watchlist bounded in practice by FMP free-tier rate limit
(250 req/day).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-checked post-design below.*

| # | Principle | Status | Notes |
|---|-----------|--------|-------|
| I | Pure Static Website | **PASS** | Vite builds/serves static HTML/CSS/JS only; no server routes |
| II | Runtime API Data via FMP | **PASS** | All 6 endpoints fetched at runtime; key in localStorage |
| III | CDN-First Charting | **PASS** | Lightweight Charts loaded via `<script>` CDN tag in `index.html` |
| IV | LocalStorage Persistence | **PASS** | Watchlist (`watchlist`) and API key (`fmp_api_key`) in localStorage |
| V | No UI Framework | **PASS** | Vanilla JS ES modules; DOM via native browser APIs in `ui.js` |

**Pre-design verdict**: All gates pass. No violations. No Complexity Tracking required.

## Project Structure

### Documentation (this feature)

```text
specs/001-stock-dashboard/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output — endpoint details, CDN integration, schema decisions
├── data-model.md        # Phase 1 output — entities, localStorage schema, state transitions
├── quickstart.md        # Phase 1 output — validation guide for acceptance scenarios
├── contracts/
│   ├── api-module.md        # api.js public interface (function signatures + endpoint mapping)
│   └── watchlist-module.md  # watchlist.js public interface (localStorage CRUD + API key)
└── tasks.md             # Phase 2 output (/speckit-tasks — NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
stock-dashboard/
├── index.html           # Single entry point — CDN <script> tags, app mount
├── vite.config.js       # Minimal Vite config (no plugins for vanilla JS)
├── style.css            # Global styles — layout, sidebar, detail panel, color-coding
├── src/
│   ├── api.js           # All FMP fetch wrappers (search + 5 data endpoints)
│   ├── watchlist.js     # localStorage CRUD — watchlist array + API key read/write
│   ├── chart.js         # TradingView Lightweight Charts init, data mapping, time-range control
│   └── ui.js            # DOM rendering helpers — watchlist rows, overview, fundamentals,
│                        #   earnings table, error/empty states
└── specs/
    └── 001-stock-dashboard/   # (documentation above)
```

**Structure Decision**: Option 1 (single project) with a web-app-specific flat layout. No
`backend/` or `frontend/` split — the entire project is frontend-only. Source modules mirror the
four domains mandated by the constitution: API, persistence, charting, UI rendering.

## Post-Design Constitution Check

Re-evaluated after Phase 1 design. All five gates continue to PASS. No new dependencies or
patterns introduced during design that violate any NON-NEGOTIABLE principle.
