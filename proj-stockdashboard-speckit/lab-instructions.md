# Project 5: Stock Analysis Dashboard with Spec Kit

Build a personal stock analysis dashboard using **Spec-Driven Development (SDD)** via [Spec Kit](https://github.com). This project shifts your workflow from "vibe coding" to structured specification — defining the *what* and *why* before the *how*.

**Stack:** Vite + Vanilla JS + Lightweight Charts (TradingView) + Financial Modeling Prep API

---

## Prerequisites

1. **Get a free FMP API key** at [financialmodelingprep.com](https://financialmodelingprep.com/developer/docs) — free tier gives 250 requests/day, no credit card required.
2. **Install Spec Kit** via `uv`:

```bash
uv tool install specify-cli --from git+https://github.com.git
specify init stock-dashboard
cd stock-dashboard
```

*During initialization, select your active AI coding agent (e.g., Claude Code) and terminal script environment.*

---

## Step 1: Set the Project Constitution

Run in your AI chat:
```text
/speckit.constitution
```

**Paste this prompt:**
> "The project must be a pure static website using vanilla HTML, CSS, and client-side JavaScript only. No backend servers, no Node.js API routes, no databases. All stock data must be fetched at runtime from the Financial Modeling Prep (FMP) REST API using a user-supplied API key stored in localStorage. The charting library must be TradingView Lightweight Charts loaded via CDN. The watchlist must be persisted in localStorage. Do not use React, Vue, or any other UI framework."

This creates `.specify/memory/constitution.md`.

---

## Step 2: Write the Specification

Run in your AI chat:
```text
/speckit.specify
```

**Paste this prompt:**
> "Build a personal stock analysis dashboard. The user can search for any stock ticker and add it to a persistent watchlist shown in a sidebar. Selecting a ticker from the watchlist loads a detail view with five sections:
>
> 1. **Overview** — company name, current price, intraday % change, market cap, P/E ratio, sector, and a one-paragraph company description.
> 2. **Price Chart** — interactive candlestick or line chart showing historical price data, with time range buttons for 1W, 1M, 3M, and 1Y.
> 3. **Fundamentals** — key financial metrics: revenue (TTM), EPS, gross margin %, operating margin %, and debt-to-equity ratio.
> 4. **Earnings Calendar** — a table of the last 4 and next 2 earnings dates, showing EPS estimate vs. EPS actual and the surprise %.
> 5. **Settings** — a field where the user can enter and save their FMP API key to localStorage.
>
> The app must work entirely client-side with no build-time data. Empty states must be handled gracefully (e.g., empty watchlist, API errors, rate limit exceeded)."

---

## Step 3: Run the Clarification Check

Run in your AI chat:
```text
/speckit.clarify
```

The AI will surface ambiguities in the spec. Expected questions and suggested answers:

| Question | Suggested Answer |
| --- | --- |
| How stale can price data be before a refresh is needed? | Show a "last updated" timestamp; add a manual refresh button. No auto-polling. |
| What happens when the FMP API key is missing or invalid? | Redirect to the Settings panel with an inline error message. |
| What is the empty state when the watchlist has no tickers? | Show a prompt: "Search for a ticker above to add it to your watchlist." |
| Should the watchlist show mini price data (e.g., current price + % change) per ticker? | Yes — each watchlist row shows ticker, current price, and intraday % change with green/red color coding. |

---

## Step 4: Generate the Technical Plan

Run in your AI chat:
```text
/speckit.plan
```

**Paste this prompt:**
> "Implement this using Vite as the build tool. Use TradingView Lightweight Charts via CDN for the price chart. Use the Financial Modeling Prep (FMP) API for all data — specifically these endpoints: `/quote/{symbol}` for price, `/profile/{symbol}` for company overview, `/ratios-ttm/{symbol}` for fundamentals, `/historical-price-full/{symbol}` for chart data, and `/earning_calendar` for earnings. Organize source code under `/src` with separate modules for: `api.js` (all FMP fetch calls), `watchlist.js` (localStorage read/write), `chart.js` (Lightweight Charts setup), and `ui.js` (DOM rendering). Use a single `index.html` entry point."

---

## Step 5: Break Down Into Tasks

Run in your AI chat:
```text
/speckit.tasks
```

This generates `tasks.md` covering the implementation lifecycle:

- Workspace setup (Vite config, CDN script tags)
- `api.js` — FMP fetch wrappers with error handling
- `watchlist.js` — localStorage CRUD + search
- `ui.js` — rendering overview, fundamentals, earnings table
- `chart.js` — Lightweight Charts initialization and data mapping
- `index.html` + `style.css` — layout, sidebar, detail panel
- Settings panel — API key save/load flow
- Empty state and error state rendering

---

## Step 6: Implement

Run in your AI chat:
```text
/speckit.implement
```

The agent steps through `tasks.md` sequentially, building the app segment by segment against the plan constraints. Once complete, run `npm run dev` and open the app in your browser.

---

## FMP API Reference

| Data | Endpoint |
| --- | --- |
| Live quote (price, % change, market cap) | `GET /quote/{symbol}` |
| Company profile (name, sector, description, P/E) | `GET /profile/{symbol}` |
| Financial ratios (margins, debt/equity) | `GET /ratios-ttm/{symbol}` |
| Historical prices (chart data) | `GET /historical-price-full/{symbol}?timeseries=365` |
| Earnings calendar | `GET /historical/earning_calendar/{symbol}` |
| Ticker search | `GET /search?query={q}&limit=10` |

Base URL: `https://financialmodelingprep.com/api/v3`
Auth: append `?apikey=YOUR_KEY` (or `&apikey=YOUR_KEY`) to every request.

---

## Expected File Structure (post-implement)

```text
stock-dashboard/
├── index.html
├── vite.config.js
├── src/
│   ├── api.js          # FMP fetch wrappers
│   ├── watchlist.js    # localStorage CRUD
│   ├── chart.js        # Lightweight Charts setup
│   └── ui.js           # DOM rendering
├── style.css
└── specs/
    ├── constitution.md
    ├── feature-stock-dashboard.md
    ├── plan.md
    └── tasks.md
```
