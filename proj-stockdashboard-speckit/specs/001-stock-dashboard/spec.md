# Feature Specification: Stock Analysis Dashboard

**Feature Branch**: `001-stock-dashboard`

**Created**: 2026-07-12

**Status**: Draft

**Input**: User description: "Build a personal stock analysis dashboard..."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Watchlist Management (Priority: P1)

A user opens the app and uses a search box to look up a stock by ticker symbol or
company name. The app returns matching results and the user adds one or more stocks to
a persistent sidebar watchlist. When the user closes and reopens the browser, their
watchlist is exactly as they left it. Each watchlist entry displays the ticker symbol,
current price, and intraday % change with green (positive) or red (negative) color
coding.

**Why this priority**: The watchlist is the entry point for every other feature. Without
it, no stock can be selected and no detail data can be viewed. It also delivers
stand-alone value as a personalized stock tracker.

**Independent Test**: Open the app with no data, search "AAPL", add it to the watchlist,
close the browser, reopen — AAPL row is still visible with price and % change.

**Acceptance Scenarios**:

1. **Given** the watchlist is empty, **When** the user types a ticker symbol in the
   search box, **Then** matching results appear within 2 seconds.
2. **Given** a search result is displayed for a stock not yet in the watchlist, **When**
   the user clicks "Add to Watchlist", **Then** the stock appears in the sidebar watchlist
   with ticker, current price, and color-coded % change.
3. **Given** a search result is displayed for a stock already in the watchlist, **When**
   the user clicks "Add to Watchlist", **Then** a message appears: "[TICKER] is already in
   your watchlist" and no duplicate entry is added.
4. **Given** a stock is in the watchlist, **When** the user removes it, **Then** it
   disappears from the sidebar immediately.
5. **Given** a populated watchlist, **When** the user closes and reopens the browser tab,
   **Then** all previously added stocks are still present with fresh price data.
6. **Given** the watchlist is empty, **When** the user views the sidebar, **Then** a
   prompt reads: "Search for a ticker above to add it to your watchlist."

---

### User Story 2 - Stock Overview (Priority: P2)

A user selects a ticker from the watchlist. The main panel loads an Overview section
showing the company name, current price, intraday % change, market cap, P/E ratio,
sector, and a brief company description.

**Why this priority**: The overview is the first thing a user wants after selecting a
stock — it answers "what is this company and how is it trading right now?"

**Independent Test**: Add one stock, select it — Overview section renders all six data
points plus the company description without errors.

**Acceptance Scenarios**:

1. **Given** a watchlist with at least one stock, **When** the user clicks a ticker,
   **Then** the Overview section displays company name, price, intraday % change, market
   cap, P/E ratio, sector, and a one-paragraph company description within 3 seconds.
2. **Given** the Overview is loaded, **When** data for a field is unavailable, **Then**
   that field shows "—" rather than crashing or showing a blank.
3. **Given** the API key is missing, **When** the user clicks any ticker, **Then** the
   app redirects to the Settings panel with a message explaining the key is required.

---

### User Story 3 - Price Chart (Priority: P3)

A user viewing the detail panel sees an interactive price chart in the Price Chart
section. They can switch between four time ranges — 1W, 1M, 3M, and 1Y — to explore
historical price performance.

**Why this priority**: Charts are the core analytical tool. After confirming what the
company is (Overview), the user wants to understand price history.

**Independent Test**: Select any watchlisted stock, click each of the four range buttons
in sequence — chart updates each time with the correct date window.

**Acceptance Scenarios**:

1. **Given** a stock is selected, **When** the Price Chart section loads, **Then** a chart
   with historical price data is displayed supporting: hover crosshair with price/date
   tooltip, scroll-to-zoom, and drag-to-pan.
2. **Given** the chart is displayed, **When** the user clicks a time range button
   (1W, 1M, 3M, or 1Y), **Then** the chart updates to show that period within 2 seconds.
3. **Given** price history data cannot be retrieved, **When** the chart section loads,
   **Then** a clear error message is shown in place of the chart.

---

### User Story 4 - Fundamentals & Earnings Calendar (Priority: P4)

A user views the Fundamentals section showing five key financial metrics (revenue TTM,
EPS, gross margin %, operating margin %, and debt-to-equity ratio). They also see an
Earnings Calendar table listing the last 4 and next 2 earnings dates with EPS estimates,
actuals, and surprise percentages.

**Why this priority**: These two sections are research tools that complement price data.
They can be implemented together as they share a single data-load cycle for the selected
ticker.

**Independent Test**: Select a stock with public financials — Fundamentals shows all five
metrics and Earnings Calendar shows a table with at least 4 historical rows.

**Acceptance Scenarios**:

1. **Given** a stock is selected, **When** the Fundamentals section loads, **Then**
   revenue (TTM), EPS, gross margin %, operating margin %, and debt-to-equity are each
   displayed with their labels and units.
2. **Given** a stock is selected, **When** the Earnings Calendar section loads, **Then**
   a table appears with up to 4 past and up to 2 upcoming earnings rows, each showing
   date, EPS estimate, EPS actual, and surprise %.
3. **Given** an upcoming earnings row, **When** actual EPS is not yet known, **Then** the
   actual and surprise % columns show "—" rather than empty or error states.
4. **Given** fundamentals data is unavailable for a stock, **When** the section loads,
   **Then** each missing metric shows "—" with no crash.

---

### User Story 5 - API Key Settings (Priority: P5)

A first-time user opens the app and sees a Settings panel (or is redirected there). They
enter their external data service API key, save it, and the app begins loading live stock
data. The key persists so they do not need to re-enter it on future visits.

**Why this priority**: Without an API key, no data can be loaded. This story is a
prerequisite blocker that must be resolved before any data story works, but as a UI
feature it is lower priority because it is a one-time setup action.

**Independent Test**: Clear all browser storage, open the app — Settings panel appears or
is prompted. Enter a valid API key, save it, close and reopen — key is pre-filled and
stock data loads.

**Acceptance Scenarios**:

1. **Given** no API key is saved, **When** the app loads, **Then** the Settings panel is
   the active view and an explanatory message tells the user they need to enter a key.
2. **Given** the Settings panel is open, **When** the user enters a key and clicks Save,
   **Then** the key is saved and the app transitions to the main dashboard view.
3. **Given** a saved API key, **When** the user reopens the browser, **Then** the key is
   pre-filled as a masked (password-style) field in the Settings panel and data loads
   automatically.
4. **Given** the user submits an invalid API key, **When** the first data request returns
   an authentication error, **Then** the app redirects to Settings with an inline error
   message.

---

### Edge Cases

- What happens when the watchlist is empty and the user opens the app? → Prompt:
  "Search for a ticker above to add it to your watchlist."
- What happens when a search query returns no results? → Message: "No results found for
  '[query]'."
- What happens when the data service rate limit is exceeded? → User-visible error:
  "Rate limit reached. Please wait before refreshing."
- What happens when a stock has no available fundamentals or earnings data? → Each
  missing field shows "—"; the section remains visible but partially populated.
- What happens when the user's internet connection drops mid-session? → Data sections
  display a "Could not load data. Check your connection." error message.
- What happens when the user tries to add a ticker already in the watchlist? → The add
  action is blocked and a message informs the user: "[TICKER] is already in your
  watchlist." No duplicate entry is created.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to search for stocks by ticker symbol or company name.
- **FR-002**: Search results MUST appear within 2 seconds of the user typing.
- **FR-003**: Users MUST be able to add a stock from search results to the watchlist. If
  the stock is already in the watchlist, the action MUST be blocked and a message MUST
  inform the user: "[TICKER] is already in your watchlist."
- **FR-004**: Users MUST be able to remove a stock from the watchlist.
- **FR-005**: The watchlist MUST persist across browser sessions without data loss.
- **FR-006**: Each watchlist row MUST display the ticker symbol, current price, and
  intraday % change with green/red color coding.
- **FR-007**: Selecting a watchlist entry MUST load a detail view in the main panel.
- **FR-008**: The Overview section MUST display: company name, current price, intraday
  % change, market cap, P/E ratio, sector, and a one-paragraph company description.
- **FR-009**: The Price Chart section MUST display a historical price chart supporting
  hover crosshair with price/date tooltip, scroll-to-zoom, and drag-to-pan.
- **FR-010**: The Price Chart MUST offer time range controls for 1W, 1M, 3M, and 1Y.
- **FR-011**: The Fundamentals section MUST display: revenue (TTM), EPS, gross margin %,
  operating margin %, and debt-to-equity ratio.
- **FR-012**: The Earnings Calendar MUST display a table of the last 4 and next 2 earnings
  dates with EPS estimate, EPS actual, and surprise % for each row.
- **FR-013**: The Settings panel MUST allow users to enter and save a data service API key.
  When a saved key is pre-filled, it MUST be rendered as a masked (password-style) field.
- **FR-014**: When no API key is saved, the app MUST direct the user to the Settings panel
  before any data request is attempted.
- **FR-015**: When an invalid or expired API key is detected, the app MUST redirect to
  Settings with an explanatory inline error message.
- **FR-016**: All data MUST be fetched at runtime from an external service; no pre-bundled
  data is permitted.
- **FR-017**: All empty states, API errors, and rate-limit responses MUST surface a clear,
  user-readable message rather than crashing or showing a blank section.
- **FR-018**: Watchlist row prices MUST be fetched on page load and refreshed when the
  user activates the manual refresh control. A "last updated" timestamp MUST be visible
  so the user can assess data currency.

### Key Entities

- **Stock**: A publicly traded company identified by a ticker symbol. Carries a company
  name, current price, intraday % change, market cap, P/E ratio, sector, and description.
- **Watchlist**: An ordered collection of Stock entries saved by the user. Persists between
  sessions.
- **Price History**: A time-series of a stock's price data (date, open, high, low, close)
  used to render the price chart.
- **Fundamentals**: A set of trailing financial metrics for a stock: revenue (TTM), EPS,
  gross margin %, operating margin %, and debt-to-equity ratio.
- **Earnings Record**: A single earnings report entry containing earnings date, EPS
  estimate, EPS actual, and surprise %. A stock has multiple historical records plus
  up to two upcoming (estimate-only) entries.
- **API Configuration**: The user's credentials for accessing the external data service.
  Stored client-side and used to authorize all data requests.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A new user with no saved data can add their first stock and view its full
  detail (Overview through Earnings Calendar) within 60 seconds of opening the app.
- **SC-002**: The application runs entirely within the user's browser; no server-side
  code is executed for any user-initiated action.
- **SC-003**: Watchlist entries and the API key survive browser tab close and reopen with
  100% fidelity (no data loss).
- **SC-004**: Every error state (API failure, rate limit, invalid key, missing data) is
  surfaced as a readable user message within 3 seconds.
- **SC-005**: Time range switches on the price chart respond and re-render within 2
  seconds of the user's click.
- **SC-006**: A user arriving with a valid API key and a populated watchlist sees their
  watchlist rendered within 3 seconds of page load.

## Assumptions

- **Data freshness**: There is no automatic background polling. Watchlist row prices and
  detail-view data are both fetched on page load and when the user activates the manual
  refresh control. A "last updated" timestamp is shown so the user knows data currency.
  No data is fetched automatically after the initial page load.
- **Search behavior**: The search box queries by both ticker symbol and company name.
  Results appear with a short debounce delay as the user types.
- **Chart type**: Either candlestick or line chart format is acceptable; the choice is
  left to implementation provided the chart is interactive and supports time range
  switching.
- **Mobile layout**: The app targets desktop browsers as the primary viewport. A
  functional (non-optimized) mobile layout is acceptable but not a success criterion.
- **Rate-limit handling**: When the external data service rate limit is exceeded, the app
  displays a human-readable error message; there is no automatic retry with exponential
  backoff in v1.
- **Watchlist ordering**: Stocks are displayed in the order they were added. Drag-to-
  reorder is out of scope for v1.
- **Authentication**: The app does not implement user accounts or multi-device sync.
  All data is local to the browser and device.

## Clarifications

### Session 2026-07-12

- Q: If a user tries to add a ticker already in the watchlist, what should happen? → A: Prevent the add with user feedback — display "[TICKER] is already in your watchlist"; do not create a duplicate entry.
- Q: What does "interactive" mean for the price chart? → A: Hover crosshair with price/date tooltip + scroll-to-zoom + drag-to-pan.
- Q: When a saved API key is pre-filled in Settings, how should it be displayed? → A: Masked as a password-style field (dots/asterisks); user can clear and re-enter a new key.
- Q: When do watchlist row prices refresh during a session? → A: On page load and when the user activates the manual refresh control; no automatic background polling.
