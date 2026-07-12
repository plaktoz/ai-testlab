import {
  getWatchlist,
  addToWatchlist,
  removeFromWatchlist,
  getApiKey,
  setApiKey,
  getLastUpdatedAt,
  setLastUpdatedAt,
} from './watchlist.js';
import {
  ApiError,
  searchStocks,
  fetchQuote,
  fetchProfile,
  fetchRatios,
  fetchPriceHistory,
  fetchEarnings,
} from './api.js';
import {
  initChart,
  setChartData,
  setChartRange,
  attachResizeObserver,
  RANGE_1Y,
} from './chart.js';
import {
  renderWatchlistList,
  renderSearchResults,
  renderOverview,
  renderFundamentals,
  renderEarningsTable,
  renderSettingsPanel,
} from './ui.js';

const RANGE_MAP = { '1W': 7, '1M': 30, '3M': 90, '1Y': 365 };

let currentChart = null;
let currentSeries = null;
let selectedSymbol = null;
const latestQuotesMap = {};

function showView(name) {
  ['settings', 'dashboard', 'empty'].forEach(v => {
    const el = document.getElementById(`${v}-view`);
    if (el) el.classList.toggle('hidden', v !== name);
  });
}

function showDetailSection(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('hidden');
}

function setActiveRangeButton(rangeStr) {
  document.querySelectorAll('.range-btn').forEach(btn => {
    btn.classList.toggle('active', btn.dataset.range === rangeStr);
  });
}

async function loadWatchlistPrices() {
  const symbols = getWatchlist();
  const apiKey = getApiKey();
  if (!symbols.length || !apiKey) {
    renderWatchlistList(symbols, latestQuotesMap, selectedSymbol);
    return;
  }

  const results = await Promise.allSettled(symbols.map(sym => fetchQuote(sym, apiKey)));

  let authFailed = false;
  results.forEach((res, i) => {
    if (res.status === 'fulfilled') {
      latestQuotesMap[symbols[i]] = res.value;
    } else if (res.reason instanceof ApiError) {
      const status = res.reason.status;
      if (status === 401 || status === 403) authFailed = true;
    }
  });

  if (authFailed) {
    showView('settings');
    renderSettingsPanel(apiKey, 'Invalid API key. Please update it.');
    return;
  }

  const now = new Date().toISOString();
  setLastUpdatedAt(now);

  const updatedEl = document.getElementById('watchlist-updated');
  if (updatedEl) {
    const ts = getLastUpdatedAt();
    updatedEl.textContent = ts ? `Updated ${new Date(ts).toLocaleTimeString()}` : '';
  }

  renderWatchlistList(symbols, latestQuotesMap, selectedSymbol);
}

function handleAuthError(err, apiKey) {
  showView('settings');
  renderSettingsPanel(apiKey, `Invalid or unauthorized API key. Please update it.`);
}

async function selectTicker(symbol) {
  selectedSymbol = symbol;
  const apiKey = getApiKey();

  showView('dashboard');
  renderWatchlistList(getWatchlist(), latestQuotesMap, selectedSymbol);

  // Reset sections
  const overviewContent = document.getElementById('overview-content');
  if (overviewContent) overviewContent.innerHTML = '<p class="loading-msg">Loading overview…</p>';

  const chartError = document.getElementById('chart-error');
  if (chartError) { chartError.textContent = ''; chartError.style.display = 'none'; }

  const fundContent = document.getElementById('fundamentals-content');
  if (fundContent) fundContent.innerHTML = '<p class="loading-msg">Loading fundamentals…</p>';

  const earnContent = document.getElementById('earnings-content');
  if (earnContent) earnContent.innerHTML = '<p class="loading-msg">Loading earnings…</p>';

  showDetailSection('chart-section');
  showDetailSection('fundamentals-section');
  showDetailSection('earnings-section');

  // Destroy old chart
  if (currentChart) {
    currentChart.remove();
    currentChart = null;
    currentSeries = null;
  }
  const chartContainer = document.getElementById('chart-container');
  if (chartContainer) chartContainer.innerHTML = '';

  // Overview: quote + profile
  try {
    const [quote, profile] = await Promise.all([
      fetchQuote(symbol, apiKey),
      fetchProfile(symbol, apiKey),
    ]);
    renderOverview(quote, profile);
  } catch (err) {
    if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
      handleAuthError(err, apiKey);
      return;
    }
    if (overviewContent) {
      overviewContent.innerHTML = `<p class="error-msg">${err.message || 'Failed to load overview.'}</p>`;
    }
  }

  // Chart
  try {
    const candles = await fetchPriceHistory(symbol, apiKey);
    const { chart, series } = initChart('chart-container');
    currentChart = chart;
    currentSeries = series;
    setChartData(series, candles);
    chart.timeScale().fitContent();
    setChartRange(chart, RANGE_1Y);
    setActiveRangeButton('1Y');
    attachResizeObserver(chart, 'chart-container');
  } catch (err) {
    if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
      handleAuthError(err, apiKey);
      return;
    }
    if (chartError) {
      chartError.textContent = err.message || 'Failed to load chart.';
      chartError.style.display = 'block';
    }
  }

  // Fundamentals + Earnings (independent, use allSettled)
  const [ratiosResult, earningsResult] = await Promise.allSettled([
    fetchRatios(symbol, apiKey),
    fetchEarnings(symbol, apiKey),
  ]);

  if (ratiosResult.status === 'fulfilled') {
    renderFundamentals(ratiosResult.value);
  } else {
    const err = ratiosResult.reason;
    if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
      handleAuthError(err, apiKey);
      return;
    }
    if (fundContent) {
      fundContent.innerHTML = `<p class="error-msg">${err.message || 'Failed to load fundamentals.'}</p>`;
    }
  }

  if (earningsResult.status === 'fulfilled') {
    renderEarningsTable(earningsResult.value);
  } else {
    const err = earningsResult.reason;
    if (earnContent) {
      earnContent.innerHTML = `<p class="error-msg">${err.message || 'Failed to load earnings.'}</p>`;
    }
  }
}

let searchTimer = null;

document.addEventListener('DOMContentLoaded', () => {
  const apiKey = getApiKey();

  if (apiKey) {
    showView('dashboard');
    loadWatchlistPrices();
  } else {
    showView('settings');
    renderSettingsPanel(null, null);
  }

  // Settings button
  document.getElementById('btn-settings')?.addEventListener('click', () => {
    showView('settings');
    renderSettingsPanel(getApiKey(), null);
  });

  // Save API key
  document.getElementById('btn-save-key')?.addEventListener('click', () => {
    const input = document.getElementById('api-key-input');
    const key = input?.value?.trim();
    if (!key) {
      renderSettingsPanel(getApiKey(), 'Please enter an API key.');
      return;
    }
    setApiKey(key);
    renderSettingsPanel(key, null);
    const msg = document.getElementById('settings-message');
    if (msg) {
      msg.textContent = 'API key saved!';
      msg.className = '';
    }
    // After saving, go to dashboard
    setTimeout(() => {
      showView('dashboard');
      loadWatchlistPrices();
    }, 800);
  });

  // Search input (debounced 300ms)
  const searchInput = document.getElementById('search-input');
  const searchResults = document.getElementById('search-results');

  searchInput?.addEventListener('input', () => {
    clearTimeout(searchTimer);
    const q = searchInput.value.trim();
    if (!q) {
      searchResults?.classList.add('hidden');
      return;
    }
    searchTimer = setTimeout(async () => {
      const key = getApiKey();
      if (!key) return;
      try {
        const results = await searchStocks(q, key);
        renderSearchResults(results, getWatchlist());
        searchResults?.classList.remove('hidden');
      } catch (err) {
        if (err instanceof ApiError && (err.status === 401 || err.status === 403)) {
          handleAuthError(err, key);
        }
      }
    }, 300);
  });

  // Hide search results on blur
  searchInput?.addEventListener('blur', () => {
    setTimeout(() => searchResults?.classList.add('hidden'), 200);
  });

  // Search results delegation: add to watchlist
  searchResults?.addEventListener('click', e => {
    const btn = e.target.closest('.btn-add-watchlist');
    if (!btn || btn.disabled) return;
    const sym = btn.dataset.symbol;
    if (!sym) return;
    addToWatchlist(sym);
    btn.disabled = true;
    btn.textContent = 'Added';
    loadWatchlistPrices();
  });

  // Watchlist delegation: remove and select
  document.getElementById('watchlist-list')?.addEventListener('click', e => {
    const removeBtn = e.target.closest('.btn-remove');
    if (removeBtn) {
      const sym = removeBtn.dataset.symbol;
      removeFromWatchlist(sym);
      if (selectedSymbol === sym) {
        selectedSymbol = null;
        showView('empty');
      }
      loadWatchlistPrices();
      return;
    }
    const item = e.target.closest('.watchlist-item');
    if (item) {
      selectTicker(item.dataset.symbol);
    }
  });

  // Range buttons
  document.getElementById('chart-range-buttons')?.addEventListener('click', e => {
    const btn = e.target.closest('.range-btn');
    if (!btn || !currentChart) return;
    const rangeStr = btn.dataset.range;
    const days = RANGE_MAP[rangeStr];
    if (!days) return;
    setActiveRangeButton(rangeStr);
    setChartRange(currentChart, days);
  });

  // Refresh button
  document.getElementById('btn-refresh')?.addEventListener('click', () => {
    loadWatchlistPrices();
  });
});
