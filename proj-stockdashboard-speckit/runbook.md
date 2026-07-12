# Runbook: Stock Dashboard

## Prerequisites

- **Node.js** 18+ and **npm** installed
- A free **Financial Modeling Prep (FMP) API key** — sign up at https://financialmodelingprep.com/developer/docs

---

## Development

### 1. Install dependencies

```bash
npm install
```

### 2. Start the dev server

```bash
npm run dev
```

The app will be available at **http://localhost:5173**.

### 3. First-run setup

On first load, the app shows the Settings screen.

1. Paste your FMP API key into the **FMP API Key** field.
2. Click **Save Key**.
3. The app redirects to the dashboard automatically.

Your API key is stored only in your browser's `localStorage` — it is never sent anywhere other than FMP API requests.

---

## Using the dashboard

| Task | How |
|------|-----|
| Search for a stock | Type a ticker or company name in the sidebar search box |
| Add to watchlist | Click **+ Add** next to a search result |
| View stock details | Click any row in your watchlist |
| Switch chart range | Click **1W / 1M / 3M / 1Y** above the chart |
| Refresh prices | Click the **⟳** button in the sidebar header |
| Update API key | Click the **⚙** (gear) button in the sidebar header |
| Remove a stock | Click **✕** on its watchlist row |

---

## Production build

### Build

```bash
npm run build
```

Output goes to `dist/`. The build bundles all JS and CSS; the TradingView chart library is loaded from CDN at runtime.

### Preview the production build locally

```bash
npm run preview
```

Serves the `dist/` folder at **http://localhost:4173**.

### Deploy

Copy the contents of `dist/` to any static file host (Netlify, Vercel, GitHub Pages, S3, nginx, etc.). No server-side runtime is required.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| "Invalid or unauthorized API key" | Wrong key or key revoked | Re-enter key in Settings |
| "Rate limit exceeded" | Free FMP plan limit hit | Wait ~1 minute, then refresh |
| "Network error. Check your connection." | No internet / FMP unreachable | Check connectivity |
| Chart doesn't render | CDN script blocked | Ensure `unpkg.com` is reachable |
| Blank page with console errors | `node_modules` missing | Run `npm install` |

---

## Project structure

```
stock-dashboard/
├── index.html          # App entry point
├── style.css           # Global styles
├── vite.config.js      # Vite config (no plugins needed)
├── package.json
└── src/
    ├── main.js         # App bootstrap, routing, event wiring
    ├── api.js          # FMP API calls (fetchQuote, fetchProfile, etc.)
    ├── watchlist.js    # localStorage helpers (watchlist + API key)
    ├── chart.js        # TradingView Lightweight Charts wrapper
    └── ui.js           # DOM rendering functions
```
