export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

const BASE_URL = 'https://financialmodelingprep.com/api/v3';
const STABLE_URL = 'https://financialmodelingprep.com/stable';

async function _apiFetch(path, apiKey, baseUrl = STABLE_URL) {
  const sep = path.includes('?') ? '&' : '?';
  const url = `${baseUrl}${path}${sep}apikey=${encodeURIComponent(apiKey)}`;
  let res;
  try {
    res = await fetch(url);
  } catch {
    throw new ApiError('Network error. Check your connection.', 0);
  }
  if (res.status === 401 || res.status === 403) {
    throw new ApiError('Invalid or unauthorized API key.', res.status);
  }
  if (res.status === 429) {
    throw new ApiError('Rate limit exceeded. Please wait a moment.', 429);
  }
  if (!res.ok) {
    throw new ApiError(`Request failed (${res.status}).`, res.status);
  }
  const data = await res.json();
  if (data && typeof data === 'object' && !Array.isArray(data) && data['Error Message']) {
    throw new ApiError('Rate limit exceeded. Please wait a moment.', 429);
  }
  return data;
}

export async function searchStocks(query, apiKey) {
  const data = await _apiFetch(`/search-symbol?query=${encodeURIComponent(query)}`, apiKey);
  return Array.isArray(data) ? data : [];
}

export async function fetchQuote(symbol, apiKey) {
  const data = await _apiFetch(`/quote?symbol=${encodeURIComponent(symbol)}`, apiKey);
  if (!Array.isArray(data) || !data[0]) throw new ApiError('No quote data found.', 404);
  return data[0];
}

export async function fetchProfile(symbol, apiKey) {
  const data = await _apiFetch(`/profile?symbol=${encodeURIComponent(symbol)}`, apiKey);
  if (!Array.isArray(data) || !data[0]) throw new ApiError('No profile data found.', 404);
  return data[0];
}

export async function fetchRatios(symbol, apiKey) {
  const data = await _apiFetch(`/ratios-ttm?symbol=${encodeURIComponent(symbol)}`, apiKey);
  if (!Array.isArray(data) || !data[0]) throw new ApiError('No ratios data found.', 404);
  return data[0];
}

export async function fetchPriceHistory(symbol, apiKey) {
  const data = await _apiFetch(
    `/historical-price-eod/full?symbol=${encodeURIComponent(symbol)}&timeseries=365`,
    apiKey
  );
  const historical = data && data.historical;
  if (!Array.isArray(historical) || historical.length === 0) {
    throw new ApiError('No price history found.', 404);
  }
  // FMP returns newest-first; reverse for LightweightCharts (oldest-first required)
  return historical.slice().reverse().map(c => ({
    time: c.date,
    open: c.open,
    high: c.high,
    low: c.low,
    close: c.close,
    volume: c.volume,
  }));
}

export async function fetchEarnings(symbol, apiKey) {
  const data = await _apiFetch(`/earnings?symbol=${encodeURIComponent(symbol)}`, apiKey);
  const records = Array.isArray(data) ? data : (data && data.historical) || [];

  // Sort descending by date
  const sorted = records.slice().sort((a, b) => (b.date > a.date ? 1 : -1));

  const upcoming = sorted
    .filter(r => r.epsActual === null || r.epsActual === undefined)
    .slice(0, 2);
  const past = sorted
    .filter(r => r.epsActual !== null && r.epsActual !== undefined)
    .slice(0, 4);

  const pastWithSurprise = past.map(r => {
    let surprisePct = null;
    if (r.epsEstimated != null && r.epsEstimated !== 0) {
      surprisePct = ((r.epsActual - r.epsEstimated) / Math.abs(r.epsEstimated)) * 100;
    }
    return { ...r, surprisePct };
  });

  // Upcoming sorted ascending, past already descending
  const upcomingAsc = upcoming.slice().sort((a, b) => (a.date > b.date ? 1 : -1));
  return [...upcomingAsc, ...pastWithSurprise];
}
