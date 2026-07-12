# Contract: watchlist.js

**Module**: `src/watchlist.js`
**Role**: All localStorage read/write operations for the watchlist and the FMP API key.
No other module may read or write `localStorage["watchlist"]` or `localStorage["fmp_api_key"]`
directly.

---

## localStorage Keys (internal)

| Key                      | Value type  | Purpose                                   |
|--------------------------|-------------|-------------------------------------------|
| `"watchlist"`            | JSON string | Ordered `string[]` of ticker symbols      |
| `"fmp_api_key"`          | string      | Raw FMP API key entered by user           |
| `"watchlist_updated_at"` | string      | ISO 8601 timestamp of last price refresh  |

---

## Exports

### `getWatchlist()`

Return the current watchlist as an ordered array of ticker symbols.

```js
/**
 * @returns {string[]}  — ordered array of uppercase ticker symbols; empty array if none saved
 */
export function getWatchlist()
```

**Behaviour**: Reads `localStorage["watchlist"]`, parses JSON. Returns `[]` on missing key,
invalid JSON, or non-array value (never throws).

---

### `addToWatchlist(symbol)`

Attempt to add a ticker to the watchlist.

```js
/**
 * @param {string} symbol  — uppercase ticker, e.g. "AAPL"
 * @returns {{ success: boolean, message: string | null }}
 *   success: true  → symbol added; message is null
 *   success: false → symbol already present; message is "[TICKER] is already in your watchlist."
 */
export function addToWatchlist(symbol)
```

**Behaviour**:
1. Call `isInWatchlist(symbol)`.
2. If already present: return `{ success: false, message: "${symbol} is already in your watchlist." }`.
3. Otherwise: append `symbol` to the array and persist. Return `{ success: true, message: null }`.

**Constraint**: Duplicate symbols are never added (FR-003).

---

### `removeFromWatchlist(symbol)`

Remove a ticker from the watchlist.

```js
/**
 * @param {string} symbol
 * @returns {void}
 */
export function removeFromWatchlist(symbol)
```

**Behaviour**: Filters the current array, writes the result back. No-op if symbol not present.

---

### `isInWatchlist(symbol)`

Check whether a ticker is already in the watchlist.

```js
/**
 * @param {string} symbol
 * @returns {boolean}
 */
export function isInWatchlist(symbol)
```

---

### `getApiKey()`

Return the saved FMP API key.

```js
/**
 * @returns {string | null}  — the saved key, or null if not set
 */
export function getApiKey()
```

**Behaviour**: Returns `null` if `localStorage["fmp_api_key"]` is missing or empty string.

---

### `setApiKey(key)`

Save the FMP API key to localStorage.

```js
/**
 * @param {string} key  — raw API key string
 * @returns {void}
 */
export function setApiKey(key)
```

---

### `getLastUpdatedAt()`

Return the ISO 8601 timestamp of the last watchlist price refresh.

```js
/**
 * @returns {string | null}  — ISO 8601 string, or null if never refreshed
 */
export function getLastUpdatedAt()
```

---

### `setLastUpdatedAt(isoString)`

Persist the timestamp of the most recent watchlist price refresh.

```js
/**
 * @param {string} isoString  — value from new Date().toISOString()
 * @returns {void}
 */
export function setLastUpdatedAt(isoString)
```
