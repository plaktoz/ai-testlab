# Contract: api.js

**Module**: `src/api.js`
**Role**: All FMP REST API fetch wrappers. Every network call to FMP MUST go through this module.
No other module may call `fetch()` directly against the FMP base URL.

---

## Base URL

```
https://financialmodelingprep.com/api/v3
```

Auth is appended as a query parameter on every call: `?apikey={key}` or `&apikey={key}`.

---

## Exports

### `searchStocks(query, apiKey)`

Search for stocks by ticker symbol or company name.

```js
/**
 * @param {string} query    — partial ticker or company name (min 1 char)
 * @param {string} apiKey   — FMP API key from localStorage
 * @returns {Promise<SearchResult[]>}
 */
export async function searchStocks(query, apiKey)
```

**Endpoint**: `GET /search?query={query}&limit=10&apikey={apiKey}`

**Returns** (on success):
```js
[
  { symbol: string, name: string, exchangeShortName: string }
]
```

**Returns** (on error): throws `ApiError` (see Error contract below).

---

### `fetchQuote(symbol, apiKey)`

Fetch current price, % change, market cap, and P/E for one ticker.

```js
/**
 * @param {string} symbol   — uppercase ticker, e.g. "AAPL"
 * @param {string} apiKey
 * @returns {Promise<QuoteData>}
 */
export async function fetchQuote(symbol, apiKey)
```

**Endpoint**: `GET /quote/{symbol}?apikey={apiKey}`

**Returns** (on success):
```js
{
  symbol: string,
  price: number | null,
  changesPercentage: number | null,
  marketCap: number | null,
  pe: number | null
}
```

**Returns** (on error): throws `ApiError`.

---

### `fetchProfile(symbol, apiKey)`

Fetch company name, sector, and description for one ticker.

```js
/**
 * @param {string} symbol
 * @param {string} apiKey
 * @returns {Promise<ProfileData>}
 */
export async function fetchProfile(symbol, apiKey)
```

**Endpoint**: `GET /profile/{symbol}?apikey={apiKey}`

**Returns** (on success):
```js
{
  companyName: string | null,
  sector: string | null,
  description: string | null
}
```

**Returns** (on error): throws `ApiError`.

---

### `fetchRatios(symbol, apiKey)`

Fetch trailing-twelve-month financial ratios for one ticker.

```js
/**
 * @param {string} symbol
 * @param {string} apiKey
 * @returns {Promise<RatiosData>}
 */
export async function fetchRatios(symbol, apiKey)
```

**Endpoint**: `GET /ratios-ttm/{symbol}?apikey={apiKey}`

**Returns** (on success):
```js
{
  revenuePerShareTTM: number | null,
  netIncomePerShareTTM: number | null,    // EPS TTM
  grossProfitMarginTTM: number | null,    // decimal, e.g. 0.453
  operatingProfitMarginTTM: number | null,// decimal
  debtEquityRatioTTM: number | null
}
```

**Returns** (on error): throws `ApiError`.

---

### `fetchPriceHistory(symbol, apiKey, timeseries = 365)`

Fetch historical OHLCV data for the chart.

```js
/**
 * @param {string} symbol
 * @param {string} apiKey
 * @param {number} [timeseries=365]  — number of trading days to fetch
 * @returns {Promise<PriceCandle[]>}
 */
export async function fetchPriceHistory(symbol, apiKey, timeseries = 365)
```

**Endpoint**: `GET /historical-price-full/{symbol}?timeseries={timeseries}&apikey={apiKey}`

**Returns** (on success) — **already sorted oldest-first** (reversed from FMP response):
```js
[
  { time: string, open: number, high: number, low: number, close: number, volume: number }
]
// time format: "YYYY-MM-DD"
```

**Important**: This function reverses the FMP array (which is newest-first) before returning, so
callers can pass the result directly to `series.setData()`.

**Returns** (on error): throws `ApiError`.

---

### `fetchEarnings(symbol, apiKey)`

Fetch historical and upcoming earnings records for one ticker.

```js
/**
 * @param {string} symbol
 * @param {string} apiKey
 * @returns {Promise<EarningsRecord[]>}
 */
export async function fetchEarnings(symbol, apiKey)
```

**Endpoint**: `GET /historical/earning_calendar/{symbol}?apikey={apiKey}`

**Returns** (on success) — raw records, **not pre-filtered**; caller selects last 4 past + next 2
upcoming:
```js
[
  {
    date: string,           // "YYYY-MM-DD"
    epsEstimate: number | null,
    epsActual: number | null,   // null for future/unreported
    surprisePct: number | null  // computed by this function; null if either eps value is missing
  }
]
```

**Returns** (on error): throws `ApiError`.

---

## Error Contract

All functions throw an `ApiError` object on non-2xx HTTP responses or network failure.

```js
class ApiError extends Error {
  constructor(message, status)
  // message: string  — human-readable description
  // status:  number  — HTTP status code (0 for network failure)
}
```

**Callers must catch `ApiError`** and route based on `status`:

| `status`     | UI action                                              |
|--------------|--------------------------------------------------------|
| 401 or 403   | Redirect to Settings panel with auth error message     |
| 429          | Show rate-limit error in the affected section          |
| 0            | Show "Could not load data. Check your connection."     |
| Other (4xx/5xx) | Show generic "Data unavailable" error in section    |

**Special case**: FMP sometimes returns HTTP 200 with body `{"Error Message": "Limit Reach."}`.
`api.js` MUST detect this pattern and throw `ApiError` with `status = 429`.
