const WATCHLIST_KEY = 'watchlist';
const API_KEY_KEY = 'fmp_api_key';
const UPDATED_AT_KEY = 'watchlist_updated_at';

export function getWatchlist() {
  try {
    return JSON.parse(localStorage.getItem(WATCHLIST_KEY) || '[]');
  } catch {
    return [];
  }
}

export function setWatchlist(arr) {
  localStorage.setItem(WATCHLIST_KEY, JSON.stringify(arr));
}

export function addToWatchlist(symbol) {
  const list = getWatchlist();
  if (!list.includes(symbol)) {
    list.push(symbol);
    setWatchlist(list);
  }
}

export function removeFromWatchlist(symbol) {
  setWatchlist(getWatchlist().filter(s => s !== symbol));
}

export function isInWatchlist(symbol) {
  return getWatchlist().includes(symbol);
}

export function getApiKey() {
  return localStorage.getItem(API_KEY_KEY) || null;
}

export function setApiKey(key) {
  localStorage.setItem(API_KEY_KEY, key);
}

export function getLastUpdatedAt() {
  return localStorage.getItem(UPDATED_AT_KEY) || null;
}

export function setLastUpdatedAt(ts) {
  localStorage.setItem(UPDATED_AT_KEY, ts);
}
