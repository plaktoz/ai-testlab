# Quickstart Validation Guide: Stock Analysis Dashboard

**Branch**: `001-stock-dashboard` | **Date**: 2026-07-12

This guide describes how to set up and validate the app against the spec's acceptance scenarios.
It is a validation reference — implementation details (code bodies, full test suites) belong in
`tasks.md` and the implementation phase.

---

## Prerequisites

1. **Node.js 18+** — required by Vite.
2. **FMP API key** — free tier at [financialmodelingprep.com](https://financialmodelingprep.com/developer/docs).
   Free tier: 250 requests/day, no credit card required.
3. **Modern browser** — Chrome 90+, Firefox 88+, Safari 14+, or Edge 90+.

---

## Setup

```bash
# 1. Install dependencies (Vite only)
npm install

# 2. Start dev server
npm run dev

# 3. Open in browser
# → http://localhost:5173  (default Vite port)
```

On first load: the app should show the Settings panel because no API key is saved.

---

## Validation Scenarios

Scenarios map 1:1 to acceptance criteria in [spec.md](spec.md).

### V1 — First-run API Key Setup (US5)

**Preconditions**: Clear all browser storage (`DevTools → Application → Clear site data`).

| Step | Action | Expected |
|------|--------|----------|
| 1 | Open `http://localhost:5173` | Settings panel is the active view; explanatory message tells you to enter a key |
| 2 | Enter a valid FMP API key, click Save | Key saved; app transitions to main dashboard view |
| 3 | Close tab; reopen `http://localhost:5173` | Key is pre-filled as a masked (password-style) field; dashboard loads |

**Maps to**: US5 scenarios 1–3, FR-013, FR-014.

---

### V2 — Add Stock to Watchlist (US1)

**Preconditions**: API key is saved. Watchlist is empty.

| Step | Action | Expected |
|------|--------|----------|
| 1 | View the sidebar | Empty-state prompt: "Search for a ticker above to add it to your watchlist." |
| 2 | Type `AAPL` in the search box | Matching results appear within 2 seconds |
| 3 | Click "Add to Watchlist" for AAPL | AAPL row appears in sidebar with ticker, current price, and green/red % change |
| 4 | Click "Add to Watchlist" again for AAPL | Message appears: "AAPL is already in your watchlist"; no duplicate row |
| 5 | Close tab; reopen | AAPL row still present with fresh price data |

**Maps to**: US1 scenarios 1–3, 5–6, FR-002, FR-003, FR-005, FR-006.

---

### V3 — Remove Stock from Watchlist (US1, scenario 4)

**Preconditions**: At least one stock in watchlist.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Click the remove control on a watchlist row | Row disappears from sidebar immediately |

**Maps to**: US1 scenario 4, FR-004.

---

### V4 — Stock Overview (US2)

**Preconditions**: At least one stock in watchlist.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Click a ticker in the sidebar | Overview section loads within 3 seconds |
| 2 | Verify fields present | Company name, price, intraday % change, market cap, P/E ratio, sector, one-paragraph description |
| 3 | For any unavailable field | Field shows "—" — no blank or crash |

**Maps to**: US2 scenarios 1–2, FR-008.

---

### V5 — Price Chart & Time Ranges (US3)

**Preconditions**: Ticker is selected and Overview has loaded.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Scroll to Price Chart section | Chart renders with historical data |
| 2 | Hover over the chart | Crosshair appears; tooltip shows price and date |
| 3 | Scroll (mouse wheel) on chart | Chart zooms in/out |
| 4 | Click and drag chart | Chart pans left/right |
| 5 | Click "1W" button | Chart updates to show last 7 days within 2 seconds |
| 6 | Click "1M" button | Chart updates to show last 30 days within 2 seconds |
| 7 | Click "3M" button | Chart updates to show last 90 days within 2 seconds |
| 8 | Click "1Y" button | Chart updates to show last 365 days within 2 seconds |

**Maps to**: US3 scenarios 1–2, FR-009, FR-010, SC-005.

---

### V6 — Fundamentals (US4)

**Preconditions**: Ticker is selected.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Scroll to Fundamentals section | Revenue/Share (TTM), EPS (TTM), Gross Margin %, Operating Margin %, Debt/Equity each display with labels |
| 2 | For missing metrics (e.g., newer company) | Missing field shows "—" |

**Maps to**: US4 scenarios 1, 4, FR-011.

---

### V7 — Earnings Calendar (US4)

**Preconditions**: Ticker is selected (use a stock with public earnings history, e.g. AAPL).

| Step | Action | Expected |
|------|--------|----------|
| 1 | Scroll to Earnings Calendar section | Table with up to 4 past rows + up to 2 upcoming rows |
| 2 | Verify past row columns | Date, EPS Estimate, EPS Actual, Surprise % all populated |
| 3 | Verify upcoming row (if any) | EPS Actual and Surprise % show "—" |

**Maps to**: US4 scenarios 2–3, FR-012.

---

### V8 — Manual Refresh & Timestamp (FR-018)

**Preconditions**: Watchlist has at least one stock.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Note the "last updated" timestamp in the sidebar | Timestamp shows time of page load |
| 2 | Click the manual refresh control | Prices reload; timestamp updates |

**Maps to**: FR-018.

---

### V9 — Error States (US2 scenario 3, spec Edge Cases)

| Scenario | How to reproduce | Expected |
|----------|-----------------|----------|
| Missing API key | Clear `localStorage["fmp_api_key"]`, click a ticker | Redirect to Settings with explanatory error message |
| Invalid API key | Set key to `"invalid"`, click a ticker | Redirect to Settings with auth error message (FR-015) |
| Search no results | Search for `"ZZZZZ"` | Message: "No results found for 'ZZZZZ'." |
| Rate limit | Exhaust free-tier quota (250 req/day) | Rate-limit error displayed in affected section |

**Maps to**: FR-014, FR-015, FR-017, SC-004.

---

### V10 — New User Full Flow (SC-001)

**Preconditions**: All browser storage cleared.

| Step | Action | Expected |
|------|--------|----------|
| 1 | Open app | Settings panel appears |
| 2 | Enter key, save | Dashboard view |
| 3 | Search for ticker, add to watchlist | Ticker in sidebar |
| 4 | Click ticker | Overview → Price Chart → Fundamentals → Earnings all load |
| **Total time** | From first open to full detail view | Under 60 seconds |

**Maps to**: SC-001.

---

## References

- Data model and field mapping: [data-model.md](data-model.md)
- API function signatures: [contracts/api-module.md](contracts/api-module.md)
- Persistence API: [contracts/watchlist-module.md](contracts/watchlist-module.md)
- Full acceptance criteria: [spec.md](spec.md)
