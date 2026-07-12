export const RANGE_1W = 7;
export const RANGE_1M = 30;
export const RANGE_3M = 90;
export const RANGE_1Y = 365;

export function initChart(containerId) {
  const container = document.getElementById(containerId);

  const chart = LightweightCharts.createChart(container, {
    width: container.clientWidth,
    height: 300,
    layout: {
      background: { color: '#1a1a2e' },
      textColor: '#e0e0e0',
    },
    grid: {
      vertLines: { color: '#2a2a4a' },
      horzLines: { color: '#2a2a4a' },
    },
    crosshair: {
      mode: LightweightCharts.CrosshairMode.Normal,
    },
    rightPriceScale: {
      borderColor: '#2a2a4a',
    },
    timeScale: {
      borderColor: '#2a2a4a',
      timeVisible: true,
    },
  });

  const series = chart.addLineSeries({
    color: '#4f7cff',
    lineWidth: 2,
  });

  const tooltip = document.createElement('div');
  tooltip.className = 'chart-tooltip';
  tooltip.style.cssText =
    'position:absolute;display:none;padding:6px 10px;background:#1e1e3a;border:1px solid #4f7cff;border-radius:4px;font-size:12px;color:#e0e0e0;pointer-events:none;z-index:10;';
  container.style.position = 'relative';
  container.appendChild(tooltip);

  chart.subscribeCrosshairMove(param => {
    if (!param.point || !param.time) {
      tooltip.style.display = 'none';
      return;
    }
    const data = param.seriesData.get(series);
    if (!data) {
      tooltip.style.display = 'none';
      return;
    }
    tooltip.style.display = 'block';
    tooltip.textContent = `${param.time}  $${Number(data.value).toFixed(2)}`;
    const x = Math.min(param.point.x + 12, container.clientWidth - 120);
    const y = Math.max(param.point.y - 30, 4);
    tooltip.style.left = `${x}px`;
    tooltip.style.top = `${y}px`;
  });

  return { chart, series };
}

export function setChartData(series, candles) {
  series.setData(candles.map(c => ({ time: c.time, value: c.close })));
}

export function setChartRange(chart, days) {
  try {
    const to = new Date();
    const from = new Date();
    from.setDate(from.getDate() - days);
    const fmt = d => d.toISOString().slice(0, 10);
    chart.timeScale().setVisibleRange({ from: fmt(from), to: fmt(to) });
  } catch {
    chart.timeScale().fitContent();
  }
}

export function attachResizeObserver(chart, containerId) {
  const container = document.getElementById(containerId);
  if (!container || typeof ResizeObserver === 'undefined') return;
  const ro = new ResizeObserver(entries => {
    for (const entry of entries) {
      chart.applyOptions({ width: entry.contentRect.width });
    }
  });
  ro.observe(container);
}
