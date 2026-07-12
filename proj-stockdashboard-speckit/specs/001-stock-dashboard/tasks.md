# Tasks: Stock Analysis Dashboard

**Branch**: `001-stock-dashboard` | **Feature**: [spec.md](spec.md)

**Input**: Design documents from `specs/001-stock-dashboard/`

**Prerequisites met**: plan.md ✅ · spec.md ✅ · research.md ✅ · data-model.md ✅ · contracts/api-module.md ✅ · contracts/watchlist-module.md ✅

**Tests**: Not requested — manual browser validation per [quickstart.md](quickstart.md).

**Organization**: Tasks grouped by user story. Each phase is independently testable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: User story label ([US1]–[US5]) — required for all user-story-phase tasks

---

## Phase 1: Setup

**Purpose**: Initialize the Vite project and create all source stubs.

- [X] T001 Create `package.json` at project root with `"devDependencies": { "vite": "^5.0.0" }` and scripts `"dev": "vite"`, `"build": "vite build"`, `"preview": "vite preview"`; run `npm install` to verify Vite installs without errors
- [X] T002 [P] Create `vite.config.js` at project root: `import { defineConfig } from 'vite'; export default defineConfig({})` — no plugins needed for vanilla JS
- [X] T003 [P] Create `index.html` at project root: `<!DOCTYPE html>`, `<meta charset="UTF-8">`, `<meta name="viewport" content="width=device-width, initial-scale=1.0">`, `<link rel="stylesheet" href="/style.css">`, the TradingView CDN script tag `<script src="https://unpkg.com/lightweight-charts/dist/lightweight-charts.standalone.production.js"></script>`, and `<script type="module" src="/src/main.js"></script>`
- [X] T004 [P] Create `style.css` at project root with a CSS reset (`*, *::before, *::after { box-sizing: border-box; } body { margin: 0; font-family: system-ui, sans-serif; }`) and CSS custom properties: `--color-positive: #22c55e`, `--color-negative: #ef4444`, `--color-bg: #0f172a`, `--color-surface: #1e293b`, `--color-text: #f1f5f9`, `--color-muted: #94a3b8`
- [X] T005 Create empty stub files `src/api.js`, `src/watchlist.js`, `src/chart.js`, `src/ui.js`, `src/main.js` — each with a single `// stub` comment so Vite resolves the module graph without errors

**Checkpoint**: `npm run dev` starts at `http://localhost:5173` with a blank page and no console errors.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core modules that every user-story phase depends on. MUST complete before Phase 3+.

**⚠️ CRITICAL**: No user story implementation can begin until this phase is complete.

- [X] T006 Implement all 8 exported functions in `src/watchlist.js` per `specs/001-stock-dashboard/contracts/watchlist-module.md`: `getWatchlist()` (returns `[]` on missing/invalid JSON — never throws), `addToWatchlist(symbol)` (returns `{ success, message }`), `removeFromWatchlist(symbol)`, `isInWatchlist(symbol)`, `getApiKey()` (returns `null` if key missing or empty string), `setApiKey(key)`, `getLastUpdatedAt()`, `setLastUpdatedAt(isoString)`. Use `localStorage` keys `"watchlist"`, `"fmp_api_key"`, `"watchlist_updated_at"`.
- [X] T007 [P] Implement the `ApiError` class and `BASE_URL` constant in `src/api.js` per `specs/001-stock-dashboard/contracts/api-module.md`: `export class ApiError extends Error { constructor(message, status) { super(message); this.status = status; } }`, `const BASE_URL = 'https://financialmodelingprep.com/api/v3'`. Add a private `async function _apiFetch(url)` helper that: (a) wraps `fetch(url)` in try/catch — network failure → `throw new ApiError('Could not connect to FMP API.', 0)`, (b) parses JSON, (c) detects `{"Error Message": "Limit Reach."}` body → `throw new ApiError('Rate limit reached. Please wait before refreshing.', 429)`, (d) throws `new ApiError(body.message ?? 'API error', response.status)` for non-2xx.
- [X] T008 Implement `src/main.js` bootstrap: on `DOMContentLoaded`, import `getApiKey` from `./watchlist.js`, check if key is present — if null call `showView('settings')`, else call `showView('dashboard')`. Define `showView(name)` that shows/hides `#settings-panel` and `#dashboard` via `display` style or a CSS class. No data fetching yet — this is the routing skeleton only.
- [X] T009 [P] Update `index.html` with the full two-column app shell inside `<body>`: `<div id="settings-panel">` (initially hidden), `<div id="dashboard">` containing `<aside id="sidebar">` with `<input id="search-input" type="text" placeholder="Search ticker or company...">`, `<ul id="search-results"></ul>`, `<ul id="watchlist-list"></ul>`, `<div id="sidebar-footer">` holding `<span id="last-updated"></span>` and `<button id="refresh-btn">Refresh</button>`. Add `<main id="detail-panel">` with empty `<section id="overview-section">`, `<section id="chart-section">`, `<section id="fundamentals-section">`, `<section id="earnings-section">`. Update `style.css` with `#dashboard { display: flex; height: 100vh; }`, `#sidebar { width: 280px; flex-shrink: 0; overflow-y: auto; background: var(--color-surface); }`, `#detail-panel { flex: 1; overflow-y: auto; padding: 1.5rem; background: var(--color-bg); color: var(--color-text); }`, `#settings-panel { display: none; }`.

**Checkpoint**: HTML two-column shell renders, `npm run dev` shows the layout, and manually calling `getWatchlist()` in the browser console returns `[]` without throwing.

---

## Phase 3: User Story 1 — Watchlist Management (Priority: P1) 🎯 MVP

**Goal**: Search for a stock by ticker or name, add it to a persistent sidebar watchlist showing price and color-coded % change, remove entries, and manually refresh prices with a timestamp.

**Independent Test**: Open app, search "AAPL", add it, close tab, reopen — AAPL row shows price and % change (V2 in quickstart.md). V3 (remove) and V8 (refresh + timestamp) also pass.

- [X] T010 [US1] Implement `export async function searchStocks(query, apiKey)` in `src/api.js` per contract: `GET ${BASE_URL}/search?query=${encodeURIComponent(query)}&limit=10&apikey=${apiKey}` via `_apiFetch`. Map each result to `{ symbol, name, exchangeShortName }`. Return the array (or `[]` if the response is not an array). Throws `ApiError` on failure.
- [X] T011 [P] [US1] Implement `export async function fetchQuote(symbol, apiKey)` in `src/api.js` per contract: `GET ${BASE_URL}/quote/${encodeURIComponent(symbol)}?apikey=${apiKey}` via `_apiFetch`. FMP returns an array; extract `data[0]` and return `{ symbol, price: data[0].price ?? null, changesPercentage: data[0].changesPercentage ?? null, marketCap: data[0].marketCap ?? null, pe: data[0].pe ?? null }`. Throws `ApiError` on failure.
- [X] T012 [P] [US1] Implement `renderWatchlistRow(symbol, quote)`, `renderWatchlistEmpty()`, and `renderWatchlistList(symbols, quotesMap)` in `src/ui.js`. `renderWatchlistRow` returns an `<li data-symbol="...">` with: ticker symbol bold on the left, price formatted to 2 decimal places and % change right-aligned, positive % change styled `color: var(--color-positive)` and negative `color: var(--color-negative)`, a remove button `<button data-action="remove" data-symbol="...">×</button>`. `renderWatchlistEmpty` returns a `<li class="empty-state">` with text "Search for a ticker above to add it to your watchlist." `renderWatchlistList(symbols, quotesMap)` clears `#watchlist-list` and rebuilds it — calls `renderWatchlistEmpty()` if `symbols` is empty, else calls `renderWatchlistRow` for each symbol using `quotesMap[symbol]`.
- [X] T013 [P] [US1] Implement `renderSearchResults(results, watchlist)` and `renderSearchEmpty(query)` in `src/ui.js`. `renderSearchResults` replaces the contents of `#search-results` with one `<li>` per result showing ticker symbol, company name, exchange, and an "Add" button `<button data-action="add" data-symbol="...">` — if `watchlist.includes(result.symbol)` the button is disabled and labeled "Added". `renderSearchEmpty(query)` sets `#search-results` innerHTML to a single `<li class="empty-state">No results found for '${query}'.</li>`.
- [X] T014 [US1] Wire search input in `src/main.js`: import `searchStocks` from `./api.js` and `getWatchlist` from `./watchlist.js`. Add `input` event listener on `#search-input` with a 300ms debounce (use `clearTimeout`/`setTimeout` pattern). On fire: if query is empty clear `#search-results`; else call `searchStocks(query, getApiKey())` — on success call `renderSearchResults(results, getWatchlist())`; on `ApiError`: status 401/403 → `showView('settings')` with auth message; status 429 → show rate-limit `<li>` in `#search-results`; status 0 → show connection error `<li>`.
- [X] T015 [US1] Wire "Add to Watchlist" button in `src/main.js`: add delegated `click` listener on `#search-results` that matches `[data-action="add"]`. Call `addToWatchlist(symbol)` from `watchlist.js` — if `success: false` insert the returned `message` as a temporary `<span class="feedback-msg">` next to the button (remove after 2s); if `success: true` call `loadWatchlistPrices()` (defined in T016) and re-render search results via `renderSearchResults` so the button shows "Added".
- [X] T016 [US1] Implement `async function loadWatchlistPrices()` in `src/main.js`: call `getWatchlist()`, if empty call `renderWatchlistList([], {})` and clear `#last-updated`, else call `fetchQuote(symbol, getApiKey())` for each symbol via `Promise.all` (individual failures resolve as `null` — don't let one bad quote abort the rest), build a `quotesMap` keyed by symbol, call `renderWatchlistList(symbols, quotesMap)`, call `setLastUpdatedAt(new Date().toISOString())`, and update `#last-updated` text to `"Last updated ${new Date().toLocaleTimeString()}"`. Call `loadWatchlistPrices()` from the `DOMContentLoaded` handler when the API key is present. Attach it to `#refresh-btn` click event.
- [X] T017 [US1] Wire remove button in `src/main.js`: add delegated `click` listener on `#watchlist-list` matching `[data-action="remove"]`. Call `removeFromWatchlist(symbol)`, then call `renderWatchlistList(getWatchlist(), latestQuotesMap)` (keep a module-level `latestQuotesMap` variable updated by `loadWatchlistPrices`). If the removed symbol equals the currently selected ticker (track in a module-level `selectedSymbol` variable), clear all four `#detail-panel` sections with empty content.
- [X] T018 [P] [US1] Add sidebar and search/watchlist CSS to `style.css`: `#search-input` full-width with padding and border-bottom; `#search-results` as absolute-positioned dropdown or static list below input with `background: var(--color-surface)`, max-height + overflow-y scroll; each result `<li>` as flex row with symbol bold, name muted, "Add"/"Added" button right-aligned; `#watchlist-list` list-style none; each watchlist `<li>` as flex row with symbol on left and price+% on right, remove button appearing on hover; `.empty-state` centered muted text; `#sidebar-footer` flex row between timestamp and refresh button.

**Checkpoint**: V2 (add AAPL, persist across reload), V3 (remove), V8 (refresh + timestamp) from quickstart.md all pass.

---

## Phase 4: User Story 2 — Stock Overview (Priority: P2)

**Goal**: Clicking a watchlist ticker loads the Overview section with company name, price, % change, market cap, P/E ratio, sector, and description. Missing fields show "—".

**Independent Test**: Add one stock, click it — all 7 Overview fields render within 3 seconds; missing fields show "—", no crash (V4 in quickstart.md).

- [X] T019 [P] [US2] Implement `export async function fetchProfile(symbol, apiKey)` in `src/api.js` per contract: `GET ${BASE_URL}/profile/${encodeURIComponent(symbol)}?apikey=${apiKey}` via `_apiFetch`. FMP returns array; extract `data[0]` and return `{ companyName: data[0].companyName ?? null, sector: data[0].sector ?? null, description: data[0].description ?? null }`. Throws `ApiError` on failure.
- [X] T020 [US2] Implement `renderOverview(stock)` and `renderOverviewError(message)` in `src/ui.js`. `stock` is the merged object from `fetchQuote` + `fetchProfile`. `renderOverview` populates `#overview-section` with: company name as `<h2>`, price as `$X.XX` (or `"—"`), % change with sign and `var(--color-positive)`/`var(--color-negative)`, market cap formatted (e.g., `"$2.95T"` for > 1e12, `"$295B"` for > 1e9, `"$2.95M"` for > 1e6), P/E ratio to 1 decimal, sector as a label pill, description paragraph. Any `null` or `undefined` field renders as `"—"`. `renderOverviewError(message)` replaces `#overview-section` content with an `<p class="error-msg">` containing `message`.
- [X] T021 [US2] Wire ticker click in `src/main.js`: add delegated `click` listener on `#watchlist-list` matching `li[data-symbol]`. Set `selectedSymbol` to the clicked symbol. Show a loading placeholder in `#overview-section`. Run `await Promise.all([fetchQuote(symbol, apiKey), fetchProfile(symbol, apiKey)])`. Merge the two results: `{ ...quote, ...profile }`. Call `renderOverview(merged)`.
- [X] T022 [US2] Add `ApiError` handling to the ticker-select handler in `src/main.js`: status 401/403 → `showView('settings')` + `renderSettingsPanel(getApiKey(), 'Invalid or expired API key.')` (will be wired in US5, stub the call for now); status 429 → `renderOverviewError('Rate limit reached. Please wait before refreshing.')` ; status 0 → `renderOverviewError('Could not load data. Check your connection.')` ; other → `renderOverviewError('Data unavailable for this ticker.')`.
- [X] T023 [P] [US2] Add Overview CSS to `style.css`: `#overview-section` padding and bottom border; company name `<h2>` large with muted subtitle; metrics as a flex-wrap row of labeled stat blocks (price large, others smaller); sector as an inline badge; description paragraph with `line-height: 1.6` and `max-width: 70ch`; `"—"` values in `var(--color-muted)`; `.error-msg` in `var(--color-negative)` with icon prefix.

**Checkpoint**: V4 from quickstart.md passes — Overview loads within 3 seconds, missing fields show "—", no crash.

---

## Phase 5: User Story 3 — Price Chart (Priority: P3)

**Goal**: Interactive line chart with 365-day history; crosshair/tooltip, scroll-zoom, drag-pan; 1W/1M/3M/1Y buttons update the visible window instantly from cached data.

**Independent Test**: Click each of the four range buttons in sequence — chart updates each time within 2 seconds (V5 in quickstart.md).

- [X] T024 [US3] Implement `export async function fetchPriceHistory(symbol, apiKey, timeseries = 365)` in `src/api.js` per contract: `GET ${BASE_URL}/historical-price-full/${encodeURIComponent(symbol)}?timeseries=${timeseries}&apikey=${apiKey}` via `_apiFetch`. Extract `data.historical`, **reverse** the array (FMP returns newest-first; Lightweight Charts requires oldest-first). Return `PriceCandle[]` with `{ time: record.date, open: record.open, high: record.high, low: record.low, close: record.close, volume: record.volume }`. Throws `ApiError` on failure.
- [X] T025 [US3] Implement `export function initChart(containerId)` in `src/chart.js`: get the container element by ID, call `LightweightCharts.createChart(container, { crosshair: { mode: LightweightCharts.CrosshairMode.Normal }, layout: { background: { color: '#0f172a' }, textColor: '#94a3b8' }, grid: { vertLines: { color: '#1e293b' }, horzLines: { color: '#1e293b' } }, width: container.clientWidth, height: 300 })`. Call `chart.addLineSeries({ color: '#3b82f6', lineWidth: 2 })`. Subscribe `chart.subscribeCrosshairMove(handler)` to update a `<div id="chart-tooltip" style="position:absolute">` tooltip div showing formatted price and date when `param.seriesPrices.size > 0`. Return `{ chart, series }`.
- [X] T026 [US3] Implement `export function setChartData(series, candles)` in `src/chart.js`: call `series.setData(candles.map(c => ({ time: c.time, value: c.close })))`. Call `series.parent().timeScale().fitContent()` to show the full dataset initially.
- [X] T027 [US3] Implement `export function setChartRange(chart, days)` in `src/chart.js`: compute `to` as today's date formatted `"YYYY-MM-DD"` and `from` as today minus `days` days (use `new Date()` arithmetic). Call `chart.timeScale().setVisibleRange({ from, to })`. Export constants `export const RANGE_1W = 7`, `RANGE_1M = 30`, `RANGE_3M = 90`, `RANGE_1Y = 365`.
- [X] T028 [US3] Implement `export function attachResizeObserver(chart, container)` in `src/chart.js`: if `ResizeObserver` is available create one that calls `chart.resize(container.clientWidth, container.clientHeight)` on size change; otherwise fall back to a debounced `window.resize` event listener (200ms debounce). Call `attachResizeObserver` after `initChart` is called.
- [X] T029 [US3] Add chart section to `src/ui.js`: `renderChartSection(containerId)` creates `<div id="${containerId}" style="position:relative"></div>` and time-range button row `<div class="range-buttons">` with four `<button data-range="7">1W</button>`, `<button data-range="30">1M</button>`, `<button data-range="90">3M</button>`, `<button data-range="365">1Y</button>` — replace `#chart-section` contents with these. `renderChartError(message)` replaces `#chart-section` with `<p class="error-msg">`. Wire in `src/main.js`: in the ticker-select handler (T021), after rendering Overview, call `renderChartSection('price-chart-container')`, then call `fetchPriceHistory(symbol, apiKey)`, on success call `initChart('price-chart-container')` (store returned `{ chart, series }` in module scope), call `setChartData(series, candles)`, call `setChartRange(chart, RANGE_1Y)`, call `attachResizeObserver(chart, container)`. Delegate `.range-buttons` button `click` on `#chart-section` → `setChartRange(chart, Number(btn.dataset.range))` and update active button class.
- [X] T030 [P] [US3] Add chart CSS to `style.css`: `#chart-section` with `position: relative` and bottom margin; `.range-buttons` as `display: flex; gap: 0.5rem; margin-bottom: 0.5rem`; range buttons as small outlined pills with `.active` class filled; `#chart-tooltip` as `position: absolute; pointer-events: none; background: var(--color-surface); border: 1px solid #334155; padding: 0.25rem 0.5rem; font-size: 0.75rem; border-radius: 4px; z-index: 10`.

**Checkpoint**: V5 from quickstart.md passes — chart renders, crosshair and tooltip work, all 4 range buttons update the view within 2 seconds.

---

## Phase 6: User Story 4 — Fundamentals & Earnings Calendar (Priority: P4)

**Goal**: Fundamentals section shows 5 TTM metrics with correct units; Earnings Calendar table shows up to 4 past + 2 upcoming rows with EPS data and surprise %.

**Independent Test**: Select AAPL — Fundamentals shows all 5 metrics, Earnings Calendar shows ≥ 4 historical rows (V6 and V7 in quickstart.md).

- [X] T031 [P] [US4] Implement `export async function fetchRatios(symbol, apiKey)` in `src/api.js` per contract: `GET ${BASE_URL}/ratios-ttm/${encodeURIComponent(symbol)}?apikey=${apiKey}` via `_apiFetch`. Extract `data[0]` and return `{ revenuePerShareTTM: data[0].revenuePerShareTTM ?? null, netIncomePerShareTTM: data[0].netIncomePerShareTTM ?? null, grossProfitMarginTTM: data[0].grossProfitMarginTTM ?? null, operatingProfitMarginTTM: data[0].operatingProfitMarginTTM ?? null, debtEquityRatioTTM: data[0].debtEquityRatioTTM ?? null }`. Throws `ApiError` on failure.
- [X] T032 [P] [US4] Implement `export async function fetchEarnings(symbol, apiKey)` in `src/api.js` per contract: `GET ${BASE_URL}/historical/earning_calendar/${encodeURIComponent(symbol)}?apikey=${apiKey}` via `_apiFetch`. For each record compute `surprisePct`: `(record.eps != null && record.epsEstimated != null && record.epsEstimated !== 0) ? ((record.eps - record.epsEstimated) / Math.abs(record.epsEstimated)) * 100 : null`. Return array of `{ date: record.date, epsEstimate: record.epsEstimated ?? null, epsActual: record.eps ?? null, surprisePct }`. Throws `ApiError` on failure.
- [X] T033 [US4] Implement `renderFundamentals(ratios)` and `renderFundamentalsError(message)` in `src/ui.js`. `renderFundamentals` populates `#fundamentals-section` with a `<dl>` or grid of 5 metric rows using labels from data-model.md: "Revenue/Share (TTM)" = `revenuePerShareTTM` formatted as `$X.XX`; "EPS (TTM)" = `netIncomePerShareTTM` as `$X.XX`; "Gross Margin %" = `grossProfitMarginTTM * 100` formatted as `X.X%`; "Operating Margin %" = `operatingProfitMarginTTM * 100` as `X.X%`; "Debt/Equity" = `debtEquityRatioTTM` as `X.XX`. Any `null` value renders as `"—"`. `renderFundamentalsError` shows `message` in `#fundamentals-section`.
- [X] T034 [US4] Implement `renderEarningsTable(records)` and `renderEarningsError(message)` in `src/ui.js`. Apply selection logic from data-model.md: sort full array by `date` descending; take up to 2 records where `epsActual === null` (upcoming — re-sort these ascending by date); take up to 4 records where `epsActual !== null` (past, descending). Render merged list (upcoming first) as `<table>` with columns: Date | EPS Estimate | EPS Actual | Surprise %. Null `epsActual` and `surprisePct` render as `"—"`. Positive `surprisePct` colored `var(--color-positive)`, negative `var(--color-negative)`. Upcoming rows get a `class="upcoming"` for visual distinction.
- [X] T035 [US4] Extend the ticker-select handler in `src/main.js` to load fundamentals and earnings in parallel with the existing quote+profile fetch. Replace the `Promise.all([fetchQuote, fetchProfile])` call with `Promise.all([fetchQuote, fetchProfile, fetchRatios, fetchEarnings])`. On success call `renderFundamentals(ratios)` and `renderEarningsTable(earnings)`. On `ApiError` from either call: same status routing as T022 but render errors in `#fundamentals-section` and `#earnings-section` independently (a ratios failure should not hide the earnings table and vice versa). Handle individual fetch failures with `Promise.allSettled` if needed to keep each section independent.
- [X] T036 [P] [US4] Add Fundamentals and Earnings CSS to `style.css`: `#fundamentals-section` with a 2-column definition list grid or `<dl>` with `dt` label in muted color and `dd` value in normal weight; `#earnings-section` with `<table>` full-width, `th` muted background, `td` padded; `.upcoming` rows in italics or with a "Upcoming" badge; positive/negative surprise values colored; section `<h3>` headings for each section.

**Checkpoint**: V6 and V7 from quickstart.md pass — 5 fundamentals metrics render (missing show "—"), earnings table shows correct rows with "—" for upcoming actuals.

---

## Phase 7: User Story 5 — API Key Settings (Priority: P5)

**Goal**: First-time user sees Settings panel on load, enters API key, saves it, and sees the dashboard. Saved key persists as masked field. Invalid key redirects back to Settings with an inline error.

**Independent Test**: Clear browser storage, open app — Settings panel is the active view. Enter valid key, save — dashboard loads with prices (V1 in quickstart.md).

- [X] T037 [US5] Add Settings panel HTML inside `<div id="settings-panel">` in `index.html`: `<div class="settings-card">` containing `<h2>Settings</h2>`, `<p>Enter your Financial Modeling Prep API key to load stock data. Get a free key at financialmodelingprep.com.</p>`, `<input id="api-key-input" type="password" placeholder="Your FMP API key" autocomplete="off">`, `<button id="save-api-key-btn">Save Key</button>`, `<p id="settings-error" class="error-msg" hidden></p>`. Also add a `<button id="nav-settings-btn" title="Settings">⚙</button>` in the sidebar footer area of `index.html`.
- [X] T038 [P] [US5] Implement `renderSettingsPanel(currentKey, errorMessage)` in `src/ui.js`: set `#api-key-input.value = currentKey ?? ''` (password type masks it automatically — do not clear the input if a key exists, so the user sees dots indicating a key is saved); if `errorMessage` is non-null, un-hide `#settings-error` and set its `textContent`; if null, hide `#settings-error`.
- [X] T039 [US5] Wire Settings panel in `src/main.js`: (a) `#save-api-key-btn` click: trim `#api-key-input.value`, if empty show "Please enter an API key." in `#settings-error` and return; else call `setApiKey(trimmedKey)`, hide `#settings-error`, call `showView('dashboard')`, call `loadWatchlistPrices()`; (b) `#nav-settings-btn` click: call `showView('settings')`, call `renderSettingsPanel(getApiKey(), null)`; (c) complete the `showView('settings')` call in `T008` to also invoke `renderSettingsPanel(getApiKey(), null)` — this pre-fills the masked field on every visit; (d) in T022's 401/403 branch: call `showView('settings')`, call `renderSettingsPanel(getApiKey(), 'Invalid or expired API key. Please enter a valid key.')`.
- [X] T040 [P] [US5] Add Settings panel CSS to `style.css`: `#settings-panel { display: flex; align-items: center; justify-content: center; height: 100vh; background: var(--color-bg); }`, `.settings-card { background: var(--color-surface); padding: 2rem; border-radius: 8px; width: 100%; max-width: 480px; }`, `#api-key-input { width: 100%; padding: 0.5rem 0.75rem; margin: 1rem 0; background: var(--color-bg); border: 1px solid #334155; color: var(--color-text); border-radius: 4px; font-size: 1rem; }`, `#save-api-key-btn { width: 100%; padding: 0.625rem; background: #3b82f6; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 1rem; }`, `#settings-error { color: var(--color-negative); margin-top: 0.5rem; font-size: 0.875rem; }`.
- [X] T041 [US5] Manual first-run validation in `src/main.js`: confirm the `DOMContentLoaded` handler (T008) works end-to-end — clear `localStorage` in browser DevTools, reload, verify Settings panel appears, enter a valid FMP API key, click Save, verify prices load and the watchlist panel is active, close and reopen the tab, verify the key is pre-filled as masked dots and `loadWatchlistPrices()` fires automatically.

**Checkpoint**: V1 (first-run setup), V9 (invalid key redirect), and V10 (new user full flow under 60 seconds) from quickstart.md pass.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Robustness audit, error-routing completeness, and final validation across all stories.

- [X] T042 Audit all `renderX()` functions in `src/ui.js` for null-safety: verify every nullable field from data-model.md renders `"—"` (not `undefined`, `null`, `"null"`, or blank). Check explicitly: `price`, `changesPercentage`, `marketCap`, `pe`, `companyName`, `sector`, `description` in `renderOverview`; `revenuePerShareTTM`, `netIncomePerShareTTM`, `grossProfitMarginTTM`, `operatingProfitMarginTTM`, `debtEquityRatioTTM` in `renderFundamentals`; `epsEstimate`, `epsActual`, `surprisePct` in `renderEarningsTable`. Fix any field that does not render `"—"` correctly.
- [X] T043 Audit all `ApiError` catch blocks in `src/main.js`: for every async call path (search, watchlist price load, ticker-select combined fetch), confirm status 401/403 → Settings redirect, 429 → rate-limit message in the relevant section, 0 → connection error message, other 4xx/5xx → generic "Data unavailable" message. Add any missing handlers. Verify that an error in one section (e.g., price history) does not suppress data in a sibling section (e.g., Overview).
- [X] T044 [P] Run quickstart.md validation scenarios V1–V10 in Chrome: for each scenario follow the steps and verify all expected outcomes. Record any failures and fix them before marking T044 complete.
- [X] T045 [P] Cross-browser smoke test: open `http://localhost:5173` in Firefox and Safari (or Edge); verify the two-column layout renders, chart loads, watchlist persists, and no console errors appear. Fix any browser-specific issues found.
- [X] T046 Final build verification: run `npm run build` and confirm it completes without errors; check the `dist/` output directory contains `index.html`, a bundled JS file, and `style.css`; serve `dist/` with `npm run preview` and verify the production build behaves identically to the dev build for the primary flows (add stock, select ticker, chart renders).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on Phase 1 — **BLOCKS all user stories**
- **Phase 3 (US1)**: Depends on Phase 2
- **Phase 4 (US2)**: Depends on Phase 2; reuses `fetchQuote` from T011 (US1) — implement US1 first or implement T011 before T019
- **Phase 5 (US3)**: Depends on Phase 2; wires into the same ticker-select handler built in Phase 4
- **Phase 6 (US4)**: Depends on Phase 2; extends the same `Promise.all` ticker-select handler from Phase 4/5
- **Phase 7 (US5)**: Depends on Phase 2 (`watchlist.js` key functions); integrates with Phase 3's `loadWatchlistPrices` entry point
- **Phase 8 (Polish)**: Depends on Phases 3–7 complete

### User Story Dependencies

| Story | Can start after | Integrates with |
|-------|----------------|-----------------|
| US1 (P1) | Phase 2 | — |
| US2 (P2) | Phase 2 | Reuses `fetchQuote` (US1 T011) |
| US3 (P3) | Phase 2 | Extends ticker-select handler (US2 T021) |
| US4 (P4) | Phase 2 | Extends same `Promise.all` (US2/US3 T021/T029) |
| US5 (P5) | Phase 2 | Calls `loadWatchlistPrices` (US1 T016), settings error routing (US2 T022) |

### Parallel Opportunities Within Each Phase

- **Phase 1**: T002, T003, T004 in parallel (different files)
- **Phase 2**: T007 and T009 can start in parallel after T006 and T008 begin
- **Phase 3**: T011, T012, T013, T018 in parallel; T010 → T014/T015/T016/T017 sequentially
- **Phase 4**: T019, T023 in parallel with T020; T021 → T022 sequentially
- **Phase 5**: T025, T026, T027, T028 in parallel after T024; T029 → T030 in parallel
- **Phase 6**: T031, T032, T036 in parallel; T033, T034 in parallel; T035 sequential after T031+T032
- **Phase 7**: T038, T040 in parallel with T037; T039 sequential after T037+T038
- **Phase 8**: T044 and T045 in parallel after T042+T043

---

## Parallel Example: Phase 3 (US1)

```
# These 5 tasks can run in parallel once T007 (_apiFetch) is complete:
T011: fetchQuote() in src/api.js
T012: renderWatchlistRow / renderWatchlistList in src/ui.js
T013: renderSearchResults / renderSearchEmpty in src/ui.js
T018: Sidebar + watchlist CSS in style.css
T010: searchStocks() in src/api.js  (also parallelizable)

# Then sequentially (each depends on tasks above):
T014: Wire search debounce in src/main.js  (needs T010, T013)
T015: Wire "Add" button in src/main.js     (needs T014, T012)
T016: loadWatchlistPrices() in src/main.js (needs T011, T012)
T017: Wire remove button in src/main.js    (needs T016)
```

---

## Implementation Strategy

### MVP First (Phases 1–3 Only)

1. Complete Phase 1 (Setup) — Vite running, HTML shell, CSS vars
2. Complete Phase 2 (Foundational) — `watchlist.js`, `ApiError`, view routing, HTML layout
3. Complete Phase 3 (US1) — search, add, remove, persist, price refresh
4. **VALIDATE**: Run V2, V3, V8 from quickstart.md manually
5. **MVP COMPLETE**: Persistent watchlist with live prices is independently valuable

### Incremental Delivery

1. Phases 1+2 → Foundation ready
2. +Phase 3 → **MVP**: persistent watchlist with prices (V2, V3, V8)
3. +Phase 4 → Add: company overview on ticker click (V4)
4. +Phase 5 → Add: interactive price chart with time ranges (V5)
5. +Phase 6 → Add: fundamentals + earnings calendar (V6, V7)
6. +Phase 7 → Add: polished settings panel + first-run flow (V1, V9, V10)
7. +Phase 8 → Polish: null audit, error routing, cross-browser, build verification

---

## Notes

- `[P]` tasks touch different files — no merge conflicts when run in parallel
- `[USn]` labels trace every task to its story in spec.md for acceptance validation
- `src/main.js` is the orchestrator — it imports from all four modules and wires every event listener
- `_apiFetch` in `src/api.js` centralizes the FMP silent rate-limit detection (`{"Error Message": "Limit Reach."}`) — all 6 API functions route through it, never call `fetch()` directly
- FMP free tier is 250 requests/day — limit API calls during development; use a single real ticker (e.g., AAPL) for most testing
- No automated test framework; all validation is manual per quickstart.md scenarios V1–V10
- The `Promise.all` for ticker-select grows across phases (US2 adds quote+profile, US3 adds price history, US4 adds ratios+earnings) — use `Promise.allSettled` in T035 to keep section errors independent
