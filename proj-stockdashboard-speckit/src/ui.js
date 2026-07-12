export function escHtml(str) {
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function fmtPrice(v) {
  if (v == null) return '—';
  return `$${Number(v).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function fmtPct(v) {
  if (v == null) return '—';
  const n = Number(v);
  return `${n >= 0 ? '+' : ''}${n.toFixed(2)}%`;
}

function fmtMarketCap(v) {
  if (v == null) return '—';
  const n = Number(v);
  if (n >= 1e12) return `$${(n / 1e12).toFixed(2)}T`;
  if (n >= 1e9) return `$${(n / 1e9).toFixed(2)}B`;
  if (n >= 1e6) return `$${(n / 1e6).toFixed(2)}M`;
  return `$${n.toLocaleString('en-US')}`;
}

function fmtNum(v, decimals = 2) {
  if (v == null) return '—';
  return Number(v).toFixed(decimals);
}

function fmtDollar(v) {
  if (v == null) return '—';
  return `$${Number(v).toFixed(2)}`;
}

export function renderWatchlistList(symbols, quotesMap, activeSymbol = null) {
  const el = document.getElementById('watchlist-list');
  if (!el) return;

  if (!symbols || symbols.length === 0) {
    el.innerHTML = '<li class="placeholder">Your watchlist is empty.</li>';
    return;
  }

  el.innerHTML = symbols
    .map(sym => {
      const q = quotesMap[sym];
      const price = q ? fmtPrice(q.price) : '—';
      const chg = q ? q.changesPercentage : null;
      const chgStr = chg != null ? fmtPct(chg) : '—';
      const chgClass = chg != null ? (chg >= 0 ? 'positive' : 'negative') : '';
      const activeClass = sym === activeSymbol ? ' active' : '';
      return `<li class="watchlist-item${activeClass}" data-symbol="${escHtml(sym)}">
  <div class="watchlist-item-info">
    <span class="watchlist-item-symbol">${escHtml(sym)}</span>
    <div class="watchlist-item-price">
      <span class="watchlist-price-value">${price}</span>
      <span class="watchlist-price-change ${chgClass}">${chgStr}</span>
    </div>
  </div>
  <button class="btn-remove" data-symbol="${escHtml(sym)}" title="Remove">✕</button>
</li>`;
    })
    .join('');
}

export function renderSearchResults(results, watchlist) {
  const el = document.getElementById('search-results');
  if (!el) return;

  if (!results || results.length === 0) {
    el.innerHTML = '<div class="placeholder">No results found.</div>';
    return;
  }

  el.innerHTML = results
    .map(r => {
      const sym = r.symbol || '';
      const name = r.name || r.companyName || '';
      const inList = watchlist && watchlist.includes(sym);
      const btnLabel = inList ? 'Added' : '+ Add';
      const btnDisabled = inList ? ' disabled' : '';
      return `<div class="search-result-item">
  <div class="search-result-info">
    <span class="search-result-symbol">${escHtml(sym)}</span>
    <span class="search-result-name">${escHtml(name)}</span>
  </div>
  <button class="btn-add-watchlist" data-symbol="${escHtml(sym)}"${btnDisabled}>${btnLabel}</button>
</div>`;
    })
    .join('');
}

export function renderOverview(quote, profile) {
  const el = document.getElementById('overview-content');
  if (!el) return;

  const name = profile?.companyName || quote?.name || quote?.symbol || '—';
  const symbol = quote?.symbol || '—';
  const price = fmtPrice(quote?.price);
  const chg = quote?.changesPercentage;
  const chgStr = chg != null ? fmtPct(chg) : '—';
  const chgClass = chg != null ? (chg >= 0 ? 'positive' : 'negative') : '';
  const mktCap = fmtMarketCap(quote?.marketCap);
  const pe = quote?.pe != null ? fmtNum(quote.pe) : '—';
  const sector = profile?.sector || '—';
  const desc = profile?.description || '';

  el.innerHTML = `<div class="overview-header">
  <div class="overview-company">
    <div class="overview-company-name">${escHtml(name)}</div>
    <div class="overview-symbol">${escHtml(symbol)} &middot; ${escHtml(sector)}</div>
  </div>
  <div class="overview-price-block">
    <div class="overview-price">${price}</div>
    <div class="overview-change ${chgClass}">${chgStr}</div>
  </div>
</div>
<div class="overview-meta">
  <div class="meta-item">
    <span class="meta-label">Market Cap</span>
    <span class="meta-value">${mktCap}</span>
  </div>
  <div class="meta-item">
    <span class="meta-label">P/E Ratio</span>
    <span class="meta-value">${pe}</span>
  </div>
</div>
${desc ? `<p class="overview-description">${escHtml(desc)}</p>` : ''}`;
}

export function renderFundamentals(ratios) {
  const el = document.getElementById('fundamentals-content');
  if (!el) return;

  const rev = fmtDollar(ratios?.revenuePerShareTTM);
  const eps = fmtDollar(ratios?.netIncomePerShareTTM);
  const gm = ratios?.grossProfitMarginTTM != null
    ? `${(ratios.grossProfitMarginTTM * 100).toFixed(2)}%`
    : '—';
  const om = ratios?.operatingProfitMarginTTM != null
    ? `${(ratios.operatingProfitMarginTTM * 100).toFixed(2)}%`
    : '—';
  const de = ratios?.debtEquityRatioTTM != null ? fmtNum(ratios.debtEquityRatioTTM) : '—';

  el.innerHTML = `<div class="fundamentals-grid">
  <div class="fundamental-item">
    <span class="fundamental-label">Revenue / Share (TTM)</span>
    <span class="fundamental-value">${rev}</span>
  </div>
  <div class="fundamental-item">
    <span class="fundamental-label">EPS (TTM)</span>
    <span class="fundamental-value">${eps}</span>
  </div>
  <div class="fundamental-item">
    <span class="fundamental-label">Gross Margin</span>
    <span class="fundamental-value">${gm}</span>
  </div>
  <div class="fundamental-item">
    <span class="fundamental-label">Operating Margin</span>
    <span class="fundamental-value">${om}</span>
  </div>
  <div class="fundamental-item">
    <span class="fundamental-label">Debt / Equity</span>
    <span class="fundamental-value">${de}</span>
  </div>
</div>`;
}

export function renderEarningsTable(earnings) {
  const el = document.getElementById('earnings-content');
  if (!el) return;

  if (!earnings || earnings.length === 0) {
    el.innerHTML = '<p class="placeholder">No earnings data available.</p>';
    return;
  }

  const rows = earnings
    .map(r => {
      const isUpcoming = r.epsActual === null || r.epsActual === undefined;
      const rowClass = isUpcoming ? ' class="upcoming-row"' : '';
      const actual = isUpcoming ? '—' : fmtDollar(r.epsActual);
      const estimate = r.epsEstimated != null ? fmtDollar(r.epsEstimated) : '—';
      let surpriseCell = '<td>—</td>';
      if (!isUpcoming && r.surprisePct != null) {
        const cls = r.surprisePct >= 0 ? 'surprise-positive' : 'surprise-negative';
        surpriseCell = `<td class="${cls}">${r.surprisePct >= 0 ? '+' : ''}${r.surprisePct.toFixed(1)}%</td>`;
      }
      return `<tr${rowClass}>
    <td>${escHtml(r.date || '—')}</td>
    <td>${estimate}</td>
    <td>${actual}</td>
    ${surpriseCell}
  </tr>`;
    })
    .join('');

  el.innerHTML = `<table class="earnings-table">
  <thead>
    <tr>
      <th>Date</th>
      <th>Est. EPS</th>
      <th>Actual EPS</th>
      <th>Surprise</th>
    </tr>
  </thead>
  <tbody>${rows}</tbody>
</table>`;
}

export function renderSettingsPanel(apiKey, errorMsg) {
  const input = document.getElementById('api-key-input');
  const msg = document.getElementById('settings-message');

  if (input) {
    input.value = apiKey || '';
  }

  if (msg) {
    if (errorMsg) {
      msg.textContent = errorMsg;
      msg.className = 'error';
    } else {
      msg.textContent = 'Enter your FMP API key. It is stored only in your browser.';
      msg.className = '';
    }
  }
}
