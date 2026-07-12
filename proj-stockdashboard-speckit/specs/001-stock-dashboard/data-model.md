# Data Model: Stock Analysis Dashboard

**Branch**: `001-stock-dashboard` | **Date**: 2026-07-12

---

## Entities

### 1. WatchlistEntry

The atomic unit of the watchlist — a ticker symbol saved by the user.

| Field    | Type     | Constraints                        | Source           |
|----------|----------|------------------------------------|------------------|
| `symbol` | `string` | Uppercase, 1–5 chars, non-empty    | User input / FMP |

**Persistence**: Watchlist is stored as `string[]` in `localStorage["watchlist"]`.

**Identity rule**: `symbol` is the unique key. Duplicate symbols are rejected (FR-003).

**Ordering**: Insertion order is preserved. Drag-to-reorder is out of scope for v1.

**State transitions**:
```
[not in watchlist] --addToWatchlist(symbol)--> [in watchlist]
[in watchlist]     --removeFromWatchlist(symbol)--> [not in watchlist]
```

---

### 2. Stock (runtime — not persisted)

Aggregated per-ticker data assembled from two FMP calls (`/quote` + `/profile`).

| Field             | Type             | Nullable | FMP Source Field          |
|-------------------|------------------|----------|---------------------------|
| `symbol`          | `string`         | No       | `quote[0].symbol`         |
| `companyName`     | `string`         | Yes      | `profile[0].companyName`  |
| `price`           | `number`         | Yes      | `quote[0].price`          |
| `changePercent`   | `number`         | Yes      | `quote[0].changesPercentage` |
| `marketCap`       | `number`         | Yes      | `quote[0].marketCap`      |
| `peRatio`         | `number`         | Yes      | `quote[0].pe`             |
| `sector`          | `string`         | Yes      | `profile[0].sector`       |
| `description`     | `string`         | Yes      | `profile[0].description`  |

**Null handling**: Any nullable field with a missing, `null`, or `undefined` value renders as
`"—"` in the UI (FR-017, US2 scenario 2).

---

### 3. PriceCandle (runtime — not persisted)

One OHLCV record in a stock's price history.

| Field    | Type     | Nullable | FMP Source Field    | Notes                            |
|----------|----------|----------|---------------------|----------------------------------|
| `time`   | `string` | No       | `historical[n].date`| `"YYYY-MM-DD"` — Lightweight Charts format |
| `open`   | `number` | No       | `historical[n].open` |                                  |
| `high`   | `number` | No       | `historical[n].high` |                                  |
| `low`    | `number` | No       | `historical[n].low`  |                                  |
| `close`  | `number` | No       | `historical[n].close`|                                  |
| `volume` | `number` | Yes      | `historical[n].volume`|                                 |

**Important**: FMP returns history newest-first. The array must be **reversed** before calling
`series.setData()` (Lightweight Charts requires oldest-first order).

**Scale**: Up to 365 records per fetch (`timeseries=365`). All four time ranges (1W/1M/3M/1Y) are
served from this single array by adjusting the chart's visible window.

---

### 4. Fundamentals (runtime — not persisted)

Financial ratios from `/ratios-ttm/{symbol}`.

| Field                 | Type     | Nullable | FMP Source Field               | Display label          |
|-----------------------|----------|----------|--------------------------------|------------------------|
| `revenuePerShareTTM`  | `number` | Yes      | `revenuePerShareTTM`           | Revenue/Share (TTM)    |
| `epsTTM`              | `number` | Yes      | `netIncomePerShareTTM`         | EPS (TTM)              |
| `grossMarginPct`      | `number` | Yes      | `grossProfitMarginTTM × 100`   | Gross Margin %         |
| `operatingMarginPct`  | `number` | Yes      | `operatingProfitMarginTTM × 100` | Operating Margin %   |
| `debtToEquity`        | `number` | Yes      | `debtEquityRatioTTM`           | Debt/Equity            |

**Unit conversion**: `grossProfitMarginTTM` and `operatingProfitMarginTTM` are returned as
decimals (e.g., `0.4531`). Multiply by 100 and display with one decimal place (e.g., `45.3%`).

**Null handling**: Any missing metric renders as `"—"` (FR-017, US4 scenario 4).

---

### 5. EarningsRecord (runtime — not persisted)

One entry in a stock's earnings calendar from `/historical/earning_calendar/{symbol}`.

| Field          | Type             | Nullable | FMP Source Field   | Notes                              |
|----------------|------------------|----------|--------------------|------------------------------------|
| `date`         | `string`         | No       | `date`             | `"YYYY-MM-DD"`                     |
| `epsEstimate`  | `number`         | Yes      | `epsEstimated`     |                                    |
| `epsActual`    | `number`         | Yes      | `eps`              | `null` for future/unreported dates |
| `surprisePct`  | `number \| null` | Yes      | computed           | See formula below                  |
| `isFuture`     | `boolean`        | No       | computed           | `epsActual === null`               |

**Surprise % formula**:
```
if (epsActual != null && epsEstimate != null && epsEstimate !== 0):
  surprisePct = ((epsActual - epsEstimate) / Math.abs(epsEstimate)) * 100
else:
  surprisePct = null
```

**Display rules**:
- `epsActual = null` → show `"—"` in Actual and Surprise % columns (US4 scenario 3).
- `epsEstimate = null` → show `"—"` in Estimate column.

**Selection logic**: From the full endpoint response:
1. Sort by `date` descending.
2. Take up to 2 records where `epsActual === null` (upcoming).
3. Take up to 4 records where `epsActual !== null` (past).
4. Merge into a single table: upcoming rows first (sorted ascending by date), then past rows
   (sorted descending by date).

---

### 6. SearchResult (runtime — not persisted)

One result from `/search?query={q}&limit=10`.

| Field              | Type     | Nullable | FMP Source Field      |
|--------------------|----------|----------|-----------------------|
| `symbol`           | `string` | No       | `symbol`              |
| `name`             | `string` | Yes      | `name`                |
| `exchangeShortName`| `string` | Yes      | `exchangeShortName`   |

---

### 7. APIConfig (persisted)

| Field    | Type          | Nullable | localStorage key    |
|----------|---------------|----------|---------------------|
| `apiKey` | `string`      | Yes      | `"fmp_api_key"`     |

**Presence check**: If `localStorage.getItem("fmp_api_key")` returns `null` or empty string, the
app must redirect to the Settings panel before any data request (FR-014).

---

## localStorage Schema Summary

| Key                    | Type       | Value                              |
|------------------------|------------|------------------------------------|
| `"watchlist"`          | JSON string | `string[]` — ordered ticker symbols |
| `"fmp_api_key"`        | string      | raw FMP API key                    |
| `"watchlist_updated_at"`| string     | ISO 8601 timestamp of last price refresh |

---

## App State Transitions

```
[no API key saved]
    │  (app loads with no key in localStorage)
    ▼
[Settings panel — active view]
    │  (user enters key and clicks Save)
    ▼
[main dashboard — empty watchlist]
    │  (user searches and adds stocks)
    ▼
[main dashboard — populated watchlist, first ticker selected]
    │  (page load or manual refresh)
    ▼
[watchlist prices loaded, detail view active]
```

**Error states** (can occur from any data-loaded state):
- API 401/403 → redirect to Settings with error message
- API 429 or `"Limit Reach."` body → show rate-limit error in affected section
- Network failure → show connection error in affected section
- Empty array response → show `"—"` per field (not a hard error)
