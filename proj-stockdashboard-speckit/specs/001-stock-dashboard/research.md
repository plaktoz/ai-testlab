# Phase 0 Research: Stock Analysis Dashboard

**Branch**: `001-stock-dashboard` | **Date**: 2026-07-12

All decisions below are derived from the user-provided implementation guidance, lab instructions,
and FMP/TradingView official documentation patterns. No unknowns remain.

---

## FMP REST API v3

### Decision: Base URL and authentication

- **Decision**: `https://financialmodelingprep.com/api/v3` — append `?apikey=<value>` (first
  param) or `&apikey=<value>` (subsequent param) to every request URL.
- **Rationale**: FMP v3 is the stable free-tier endpoint. Key is passed as a query param, never in
  headers, matching the FMP documentation pattern and the constitution requirement to store it
  client-side in localStorage.
- **Alternatives considered**: FMP v4 (some newer endpoints) — rejected; v3 covers all required
  endpoints and is free-tier compatible.

### Decision: Search endpoint

- **Decision**: `GET /search?query={q}&limit=10&apikey={key}`
- **Response shape** (array):
  ```json
  [
    { "symbol": "AAPL", "name": "Apple Inc.", "stockExchange": "NASDAQ", "exchangeShortName": "NASDAQ" }
  ]
  ```
- **Rationale**: The `/search` endpoint supports both ticker and company name lookup, satisfying
  FR-001. Limit of 10 keeps the results list short and avoids hitting rate limits on keystrokes.
- **Note**: Implement with a debounce (~300ms) to avoid firing a request on every keystroke.

### Decision: Quote endpoint

- **Decision**: `GET /quote/{symbol}?apikey={key}`
- **Response shape** (array with one element):
  ```json
  [
    {
      "symbol": "AAPL",
      "name": "Apple Inc.",
      "price": 189.84,
      "changesPercentage": -0.42,
      "change": -0.80,
      "marketCap": 2950000000000,
      "pe": 29.4,
      "open": 190.64,
      "previousClose": 190.64,
      "volume": 52000000,
      "avgVolume": 57000000,
      "timestamp": 1720800000
    }
  ]
  ```
- **Fields used**: `symbol`, `name`, `price`, `changesPercentage`, `marketCap`, `pe`.
- **Rationale**: Single endpoint returns all fields needed for watchlist rows (price, %change) and
  the Overview section (price, %change, marketCap, pe). Avoids a second round-trip.

### Decision: Profile endpoint

- **Decision**: `GET /profile/{symbol}?apikey={key}`
- **Response shape** (array with one element):
  ```json
  [
    {
      "symbol": "AAPL",
      "companyName": "Apple Inc.",
      "sector": "Technology",
      "industry": "Consumer Electronics",
      "description": "Apple Inc. designs, manufactures, and markets smartphones...",
      "price": 189.84,
      "mktCap": 2950000000000
    }
  ]
  ```
- **Fields used**: `companyName`, `sector`, `description`.
- **Rationale**: Profile provides the company name, sector, and description paragraph required by
  FR-008. Quote provides the numeric metrics; both are needed for a complete Overview panel.

### Decision: Ratios TTM endpoint

- **Decision**: `GET /ratios-ttm/{symbol}?apikey={key}`
- **Response shape** (array with one element):
  ```json
  [
    {
      "revenuePerShareTTM": 24.63,
      "netIncomePerShareTTM": 6.13,
      "grossProfitMarginTTM": 0.4531,
      "operatingProfitMarginTTM": 0.2984,
      "debtEquityRatioTTM": 1.52,
      "peRatioTTM": 29.4
    }
  ]
  ```
- **Fields used**: `revenuePerShareTTM` (proxy for Revenue TTM per share; display as-is with unit
  label), `netIncomePerShareTTM` (EPS TTM), `grossProfitMarginTTM` (× 100 for %), 
  `operatingProfitMarginTTM` (× 100 for %), `debtEquityRatioTTM`.
- **Note**: FMP `/ratios-ttm` returns per-share and ratio values, not total revenue. Display label
  will read "Revenue/Share (TTM)" to avoid misrepresenting it as total revenue TTM.
- **Rationale**: This is the only free-tier FMP endpoint returning TTM financial ratios in a single
  call. Satisfies FR-011.

### Decision: Historical price endpoint

- **Decision**: `GET /historical-price-full/{symbol}?timeseries=365&apikey={key}`
- **Response shape**:
  ```json
  {
    "symbol": "AAPL",
    "historical": [
      { "date": "2024-07-11", "open": 188.0, "high": 191.2, "low": 187.5, "close": 189.84, "volume": 52000000 }
    ]
  }
  ```
- **Ordering**: FMP returns data newest-first. TradingView Lightweight Charts requires
  oldest-first. **Must reverse the array** before passing to `series.setData()`.
- **`timeseries=365`** fetches 365 trading days (~1.4 calendar years), covering the 1Y range
  button and all shorter ranges from a single response. The 1W/1M/3M ranges are applied by
  calling `chart.timeScale().setVisibleRange()` client-side — no additional API call needed.
- **Rationale**: One fetch covers all four time-range buttons, avoiding 4× the API calls and
  reducing rate-limit risk on the free tier.

### Decision: Earnings endpoint

- **Decision**: `GET /historical/earning_calendar/{symbol}?apikey={key}`
- **Response shape** (array):
  ```json
  [
    {
      "symbol": "AAPL",
      "date": "2024-07-30",
      "eps": 1.40,
      "epsEstimated": 1.35,
      "fiscalDateEnding": "2024-06-30",
      "time": "amc"
    }
  ]
  ```
- **Fields used**: `date`, `eps` (actual — null for future dates), `epsEstimated`, computed
  `surprisePct = ((eps - epsEstimated) / Math.abs(epsEstimated)) * 100`.
- **Sorting strategy**: The endpoint returns a mix of past and future records. Sort by `date`
  descending, then take: last 4 with non-null `eps` (past), and up to 2 with null `eps` (upcoming).
- **Rationale**: The `/historical/earning_calendar/{symbol}` endpoint returns symbol-specific
  historical and upcoming earnings, satisfying FR-012. The generic `/earning_calendar` mentioned
  in the user's plan input is a date-range calendar endpoint that does not filter by symbol.
- **Note**: This endpoint name differs from the user's plan input (`/earning_calendar`) — the
  correct symbol-scoped path is `/historical/earning_calendar/{symbol}`.

### Decision: Error shape and rate-limit detection

- **HTTP 401 / 403**: FMP returns these for an invalid or missing API key. Trigger redirect to
  Settings panel with inline error message (FR-014, FR-015).
- **Rate-limit exceeded**: FMP returns HTTP 429 or a JSON body `{"Error Message": "Limit Reach."}`.
  Detect by status code or body content and display FR-017 rate-limit message.
- **Empty array response**: Some endpoints return `[]` for unknown symbols. Treat as "no data" —
  show `—` per-field rather than crashing (FR-017, spec Edge Cases).

---

## TradingView Lightweight Charts (CDN)

### Decision: CDN URL

- **Decision**:
  ```html
  <script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>
  ```
- **Rationale**: The standalone build exposes `LightweightCharts` as a browser global — no module
  bundling needed, matching Constitution III (CDN-first). `unpkg.com` serves the latest stable
  release; pinning to a specific version (e.g., `lightweight-charts@4.2.0`) is recommended for
  production stability.
- **Alternatives considered**: jsDelivr CDN — equivalent, slightly different URL. Either works.

### Decision: Chart type

- **Decision**: Line chart (`chart.addLineSeries()`) with area fill disabled.
- **Rationale**: The spec allows either candlestick or line (Assumptions). A line chart avoids the
  complexity of OHLC data rendering and is cleaner for the 1W/1M short-term ranges. The
  `/historical-price-full` response includes full OHLC, so candlestick can be substituted without
  data changes if preferred during implementation.
- **Alternatives considered**: Candlestick — more data-dense, but requires more screen width and
  more complex empty-state handling for thin candles.

### Decision: Interactive features (hover crosshair, scroll-zoom, drag-pan)

- **Decision**: All three interaction modes are enabled by default in Lightweight Charts v4.
  No explicit configuration needed for scroll-zoom and drag-pan. Crosshair requires:
  ```js
  chart.applyOptions({
    crosshair: { mode: LightweightCharts.CrosshairMode.Normal }
  })
  ```
- **Tooltip**: Subscribe to `chart.subscribeCrosshairMove(handler)` to render a custom HTML
  tooltip showing price and date on hover.
- **Rationale**: Satisfies FR-009 (crosshair + tooltip + scroll-zoom + drag-pan) without any
  additional library.

### Decision: Time-range control implementation

- **Decision**: On range button click, call `chart.timeScale().setVisibleRange({from, to})` where
  `from` is computed as `today minus N days` and `to` is today. The full 365-day dataset is
  already loaded; only the visible window changes.
- **Range offsets**: 1W = 7 days, 1M = 30 days, 3M = 90 days, 1Y = 365 days.
- **Rationale**: Avoids repeated API calls on range switch. Chart re-renders the visible slice
  instantly (well under the 2-second SC-005 target).

### Decision: Chart resize

- **Decision**: Call `chart.resize(container.clientWidth, container.clientHeight)` on
  `window.resize` events (debounced ~200ms) to keep the chart responsive.

---

## Vite Configuration

### Decision: Minimal vite.config.js

- **Decision**:
  ```js
  import { defineConfig } from 'vite'
  export default defineConfig({})
  ```
  No plugins required for vanilla JS + ES modules. Vite's defaults handle HTML entry point,
  CSS import, and JS module bundling.
- **Rationale**: Satisfies the "build tool: Vite" requirement with zero additional configuration.
  Adding plugins would only be needed for React/Vue (Constitution V violation).

---

## localStorage Schema

### Decision: Watchlist storage

- **Decision**: Key `"watchlist"`, value `JSON.stringify(string[])` — an ordered array of
  uppercase ticker symbols, e.g., `["AAPL", "MSFT", "TSLA"]`.
- **Rationale**: Symbols are the canonical identity for stocks in this app. Storing only symbols
  keeps the localStorage footprint small; live price data is always fetched fresh (FR-018).

### Decision: API key storage

- **Decision**: Key `"fmp_api_key"`, value raw string (the key itself).
- **Rationale**: FMP keys are non-sensitive enough for localStorage (no user auth, no PII). The
  constitution explicitly mandates localStorage for this value.

### Decision: Last-updated timestamp

- **Decision**: Key `"watchlist_updated_at"`, value ISO 8601 string from `new Date().toISOString()`.
  Written every time a watchlist price refresh completes. Displayed as human-readable string in
  the sidebar (e.g., "Last updated 2:34 PM").
- **Rationale**: Satisfies FR-018 ("last updated timestamp MUST be visible").

---

## Module Dependency Order

```text
index.html (entry)
  └── src/main.js (app bootstrap — not a module in spec, but needed as Vite entry point)
        ├── src/watchlist.js  (no imports from other src modules)
        ├── src/api.js        (no imports from other src modules)
        ├── src/chart.js      (imports nothing from src — uses LightweightCharts global)
        └── src/ui.js         (imports api.js, watchlist.js, chart.js)
```

**Note**: The constitution's four modules (`api.js`, `watchlist.js`, `chart.js`, `ui.js`) do not
include a bootstrap/entry file. A thin `src/main.js` (or inline `<script type="module">` in
`index.html`) is needed to wire event listeners and orchestrate the app. This is an implementation
detail not requiring a constitutional amendment.
