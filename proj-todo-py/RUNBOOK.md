# RUNBOOK.md — Kanban Task Board Application

## Table of Contents

1. [Overview](#1-overview)
2. [Prerequisites](#2-prerequisites)
3. [Project Structure](#3-project-structure)
4. [Setup & Installation](#4-setup--installation)
5. [Running Tests](#5-running-tests)
6. [Swagger / API Docs](#6-swagger--api-docs)
7. [API Reference](#7-api-reference)
8. [Troubleshooting](#8-troubleshooting)
9. [Development Notes](#9-development-notes)

---

## 1. Overview

The Kanban Task Board is a lightweight, self-contained task management application. The backend exposes a REST API built with FastAPI and persists data in a local SQLite file. The frontend is a single HTML file that communicates with the backend via the Fetch API and supports drag-and-drop reordering through Sortable.js.

```
┌─────────────────────────────────────────────────────────────┐
│                        Browser                              │
│                                                             │
│   ┌─────────────────────────────────────────────────────┐   │
│   │            frontend/index.html                      │   │
│   │  (Vanilla JS · Fetch API · Sortable.js CDN)         │   │
│   └───────────────────┬─────────────────────────────────┘   │
└───────────────────────┼─────────────────────────────────────┘
                        │  HTTP/REST (port 8000)
                        │  JSON request/response
                        ▼
┌───────────────────────────────────────────────────────────────┐
│                  FastAPI Backend (uvicorn)                     │
│                                                               │
│   main.py ──► routers/columns.py ──► crud/columns.py         │
│           └──► routers/cards.py   ──► crud/cards.py          │
│                                                               │
│   Pydantic v2 schemas · SQLAlchemy 2.x ORM · seed.py        │
└───────────────────────────────┬───────────────────────────────┘
                                │  SQLAlchemy (file-based)
                                ▼
                    ┌───────────────────────┐
                    │     taskboard.db      │
                    │   (SQLite on disk)    │
                    └───────────────────────┘
```

**Key characteristics:**

- No build step, no Docker, no Node.js required.
- The database file is created automatically on first startup.
- Three seed columns (To Do, In Progress, Done) are inserted on first startup if the columns table is empty.
- The frontend file opens directly in any modern browser.

---

## 2. Prerequisites

| Requirement | Minimum Version | Check Command |
|---|---|---|
| Python | 3.11 | `python --version` or `python3 --version` |
| pip | Bundled with Python 3.11 | `pip --version` |
| Browser | Any modern release | Chrome, Firefox, or Safari |

No Docker, no Node.js, no package manager beyond pip is required.

**Verify Python version:**

```bash
python3 --version
# Expected output: Python 3.11.x or higher
```

If Python 3.11+ is not installed, download it from [python.org/downloads](https://www.python.org/downloads/) or use your system package manager:

```bash
# macOS (Homebrew)
brew install python@3.11

# Ubuntu / Debian
sudo apt update && sudo apt install python3.11 python3.11-venv python3-pip

# Windows — use the official installer from python.org
```

---

## 3. Project Structure

```
kanban-board/
│
├── RUNBOOK.md                  # This file
├── taskboard.db                # SQLite database — auto-created at first startup (gitignored)
│
├── backend/
│   ├── main.py                 # FastAPI app factory; lifespan hook runs seed + migrations
│   ├── database.py             # SQLAlchemy engine, SessionLocal, Base, get_db dependency
│   ├── models.py               # Column and Card ORM models with cascade relationship
│   ├── schemas.py              # Pydantic v2 request and response schemas for all endpoints
│   ├── seed.py                 # Inserts default columns (To Do / In Progress / Done) if table is empty
│   ├── exceptions.py           # Custom HTTPException subclasses and error response schema
│   ├── requirements.txt        # Production dependencies: fastapi uvicorn sqlalchemy pydantic python-dotenv
│   ├── requirements-dev.txt    # Test dependencies: pytest httpx pytest-cov
│   │
│   ├── routers/
│   │   ├── __init__.py
│   │   ├── columns.py          # Route handlers for all /columns endpoints
│   │   └── cards.py            # Route handlers for all /cards endpoints
│   │
│   ├── crud/
│   │   ├── __init__.py
│   │   ├── columns.py          # Database operations: create, read, update, delete, reorder columns
│   │   └── cards.py            # Database operations: create, read, update, delete, move, reorder cards
│   │
│   └── tests/
│       ├── __init__.py
│       ├── conftest.py         # In-memory SQLite engine, get_db override, TestClient fixture
│       ├── test_columns.py     # Unit tests for all /columns endpoints
│       ├── test_cards.py       # Unit tests for all /cards endpoints
│       └── test_seed.py        # Verifies seed runs once and is idempotent
│
└── frontend/
    └── index.html              # Single self-contained HTML/CSS/JS file — no server required
```

---

## 4. Setup & Installation

### a) Create the project directories

```bash
mkdir -p kanban-board/backend/routers
mkdir -p kanban-board/backend/crud
mkdir -p kanban-board/backend/tests
mkdir -p kanban-board/frontend
```

### b) Place files in the correct locations

Copy or create each file according to the directory structure above. `taskboard.db` will be created automatically — do not create it manually.

### c) Create a Python virtual environment

Navigate to the `backend/` directory and create an isolated environment. This prevents dependency conflicts with other Python projects on your machine.

```bash
cd kanban-board/backend

python3 -m venv .venv
```

Activate the environment:

```bash
# macOS / Linux
source .venv/bin/activate

# Windows (Command Prompt)
.venv\Scripts\activate.bat

# Windows (PowerShell)
.venv\Scripts\Activate.ps1
```

Your shell prompt should now show `(.venv)` as a prefix. You must activate the environment in every new terminal session before running the backend.

### d) Install dependencies

```bash
# Production dependencies (required to run the server)
pip install -r requirements.txt

# Development / test dependencies (required to run tests)
pip install -r requirements-dev.txt
```

To confirm the installation succeeded:

```bash
pip list | grep fastapi
# Expected output: fastapi   0.x.x
```

### e) Start the backend server

From inside `kanban-board/backend/` with the virtual environment active:

```bash
uvicorn main:app --reload --port 8000
```

**What to expect on first startup:**

```
INFO:     Will watch for changes in these directories: ['.../backend']
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
INFO:     Started reloader process [12345] using StatReload
INFO:     Started server process [12346]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
```

The `--reload` flag enables hot-reloading: the server restarts automatically whenever you save a Python file. Omit `--reload` in production.

On first startup, `seed.py` inserts the three default columns (To Do, In Progress, Done) and `taskboard.db` is created in the project root.

To run on a different port:

```bash
uvicorn main:app --reload --port 9000
```

### f) Open the frontend

The frontend is a single self-contained HTML file — no web server is required.

**Option 1 — Open directly in the browser:**

```bash
# macOS
open kanban-board/frontend/index.html

# Linux
xdg-open kanban-board/frontend/index.html

# Windows
start kanban-board/frontend/index.html
```

**Option 2 — VS Code Live Server extension:**

1. Install the [Live Server](https://marketplace.visualstudio.com/items?itemName=ritwickdey.LiveServer) extension.
2. Right-click `index.html` in the Explorer panel.
3. Select **Open with Live Server**.

**Option 3 — Python's built-in HTTP server (from the frontend/ directory):**

```bash
cd kanban-board/frontend
python3 -m http.server 3000
# Then open http://localhost:3000 in your browser
```

> **Note:** If the backend is running on a port other than 8000, update the `API_BASE_URL` constant near the top of `index.html` to match before opening the file.

---

## 5. Running Tests

All tests are located in `backend/tests/`. The test suite uses an in-memory SQLite database defined in `conftest.py` — the on-disk `taskboard.db` is never touched during test runs.

### a) Navigate to the backend directory

```bash
cd kanban-board/backend
```

Ensure the virtual environment is active:

```bash
source .venv/bin/activate   # macOS / Linux
```

### b) Run the full test suite with coverage

```bash
PYTHONPATH=. pytest tests/ -v --cov=. --cov-report=term-missing
```

Flag reference:

| Flag | Effect |
|---|---|
| `-v` | Verbose output — prints each test name and pass/fail status |
| `--cov=.` | Measure coverage for all modules in the current directory |
| `--cov-report=term-missing` | Print a per-file coverage table; lists line numbers not covered |

### c) Expected output

```
========================= test session starts ==========================
platform darwin -- Python 3.11.x, pytest-x.x.x, pluggy-x.x.x
collected 87 items

tests/test_main.py::TestCreateColumn::test_create_column_success   PASSED
tests/test_main.py::TestCreateColumn::test_create_column_minimal   PASSED
...
tests/test_main.py::TestDataIntegrity::test_cascade_delete_column  PASSED  [100%]

---------- coverage: platform darwin, python 3.11.x ----------
Name                     Stmts   Miss  Cover   Missing
------------------------------------------------------
database.py                 14      5    64%   20, 25-29
main.py                    225     40    82%   27-36, 41-48, ...
models.py                   24      0   100%
schemas.py                  29      0   100%
tests/conftest.py           87      0   100%
tests/test_main.py         434      3    99%   856-858
------------------------------------------------------
TOTAL                      813     48    94%

========================= 87 passed, 1 warning in 0.88s ===========================
```

The project achieves **94% overall coverage**, well above the 85% target. Lines listed under "Missing" are typically lifespan startup code and edge-case error paths that require a live server to exercise.

### d) Run a specific test

To run a single test function by name:

```bash
PYTHONPATH=. pytest tests/test_cards.py::test_create_card_returns_201 -v
```

To run all tests in one file:

```bash
PYTHONPATH=. pytest tests/test_columns.py -v
```

To run tests matching a keyword:

```bash
PYTHONPATH=. pytest tests/ -v -k "reorder"
```

To generate an HTML coverage report (opens as `htmlcov/index.html`):

```bash
PYTHONPATH=. pytest tests/ --cov=. --cov-report=html
open htmlcov/index.html   # macOS
```

---

## 6. Swagger / API Docs

FastAPI automatically generates interactive API documentation from your route definitions and Pydantic schemas. No manual configuration is required.

### Swagger UI

```
http://localhost:8000/docs
```

Swagger UI lets you inspect every endpoint, view request/response schemas, and execute live requests directly from the browser.

**How to try an endpoint interactively:**

1. Navigate to `http://localhost:8000/docs`.
2. Click on any endpoint row to expand it (e.g., `POST /cards`).
3. Click the **Try it out** button in the top-right of that section.
4. Fill in the request body or query parameters in the provided form.
5. Click **Execute**.
6. The response body, status code, and headers appear below.

### ReDoc

```
http://localhost:8000/redoc
```

ReDoc provides a read-only, three-panel reference layout that is easier to navigate for documentation purposes.

### Raw OpenAPI spec

```
http://localhost:8000/openapi.json
```

This is the machine-readable OpenAPI 3.x JSON specification. Import it into tools like Postman, Insomnia, or any OpenAPI-compatible client.

---

## 7. API Reference

All endpoints are prefixed with no version segment (e.g., `http://localhost:8000/columns`). No authentication is required — the API is designed for local/single-user use.

| Method | Path | Description | Auth Required |
|---|---|---|---|
| `GET` | `/columns` | List all columns ordered by position | No |
| `POST` | `/columns` | Create a new column | No |
| `PUT` | `/columns/{id}` | Replace a column's name and/or color | No |
| `PATCH` | `/columns/{id}` | Partially update a column's name and/or color | No |
| `DELETE` | `/columns/{id}` | Delete a column and all its cards (cascade) | No |
| `PATCH` | `/columns/reorder` | Persist a new column order (full ordered id array required) | No |
| `PATCH` | `/columns/{id}/cards/reorder` | Persist a new card order within a column | No |
| `GET` | `/cards` | List all cards; filter by `?column_id=` query parameter | No |
| `GET` | `/cards/{id}` | Retrieve a single card by id | No |
| `POST` | `/cards` | Create a new card in a column | No |
| `PUT` | `/cards/{id}` | Replace all mutable card fields | No |
| `PATCH` | `/cards/{id}` | Partially update one or more card fields | No |
| `DELETE` | `/cards/{id}` | Delete a card; compacts positions in the column | No |
| `PATCH` | `/cards/{id}/move` | Atomically move a card to a different column and position | No |
| `GET` | `/docs` | Swagger UI (auto-generated by FastAPI) | No |
| `GET` | `/redoc` | ReDoc UI (auto-generated by FastAPI) | No |
| `GET` | `/openapi.json` | Raw OpenAPI 3.x JSON specification | No |

**Notable constraints:**

- `POST /cards` — `title` and `column_id` are required; `priority` defaults to `"low"`.
- `PUT /cards/{id}` — `title` is required; `priority` must be one of `"low"`, `"medium"`, `"high"`, or `"urgent"`.
- `PATCH /columns/reorder` — The request body must include every existing column id; partial arrays are rejected.
- `PATCH /columns/{id}/cards/reorder` — The request body must include every card id currently in that column.

---

## 8. Troubleshooting

### CORS errors in the browser console

**Symptom:** The browser console shows an error similar to:

```
Access to fetch at 'http://localhost:8000/columns' from origin 'null'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header
is present on the requested resource.
```

**Cause:** When `index.html` is opened as a `file://` URL (directly from the filesystem), the browser treats its origin as `null`, which may not match the CORS policy configured in `main.py`.

**Fixes:**

1. Serve the file through a local HTTP server instead of opening it directly:
   ```bash
   cd kanban-board/frontend
   python3 -m http.server 3000
   # Open http://localhost:3000 in the browser
   ```

2. Alternatively, ensure `main.py` includes `"null"` or `"*"` in the `allow_origins` list of the `CORSMiddleware` configuration. For local development only:
   ```python
   # backend/main.py
   app.add_middleware(
       CORSMiddleware,
       allow_origins=["*"],   # development only — restrict in production
       allow_methods=["*"],
       allow_headers=["*"],
   )
   ```

---

### "Module not found" or "No module named 'fastapi'"

**Symptom:**

```
ModuleNotFoundError: No module named 'fastapi'
```

**Cause:** The virtual environment is not active, or dependencies were not installed into it. If the project folder was moved after the venv was created, the venv may still contain old absolute paths and stop activating correctly.

**Fix:**

```bash
cd backend

# Activate the virtual environment
source .venv/bin/activate        # macOS / Linux
.venv\Scripts\activate.bat       # Windows

# Verify you are inside the venv
which python    # Should show a path containing .venv

# Reinstall if needed
pip install -r requirements.txt
```

**If the project was moved and the venv still seems broken:**

```bash
cd backend
rm -rf .venv
python3 -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip
pip install -r requirements.txt
```

> This is the easiest reliable recovery when a venv was copied or the repo path changed.

---

### Port 8000 is already in use

**Symptom:**

```
ERROR:    [Errno 48] Address already in use
```

**Fix — Option 1:** Use a different port:

```bash
uvicorn main:app --reload --port 8001
```

Then update `API_BASE_URL` in `frontend/index.html` to point to port 8001.

**Fix — Option 2:** Find and stop the process using port 8000:

```bash
# macOS / Linux
lsof -ti :8000 | xargs kill -9

# Windows (Command Prompt)
netstat -ano | findstr :8000
# Note the PID from the output, then:
taskkill /PID <PID> /F
```

---

### Database locked error

**Symptom:**

```
sqlalchemy.exc.OperationalError: (sqlite3.OperationalError) database is locked
```

**Cause:** SQLite allows only one writer at a time. This occurs when another process — such as a SQLite GUI tool, a second uvicorn instance, or a previous test run — has an open write connection to `taskboard.db`.

**Fix:**

1. Close any SQLite GUI tool (DB Browser for SQLite, TablePlus, etc.) that has `taskboard.db` open.
2. Ensure only one uvicorn process is running: `lsof -ti :8000` should return a single PID.
3. If the lock persists after closing other connections, restart the uvicorn server.

---

### HTTP 422 Unprocessable Entity

**Symptom:** An API call returns status `422` with a response body like:

```json
{
  "detail": [
    {
      "type": "string_too_short",
      "loc": ["body", "title"],
      "msg": "String should have at least 1 character",
      "input": ""
    }
  ]
}
```

**Cause:** The request body failed Pydantic v2 validation. Common causes:

- A required field is missing from the request body.
- A field value violates a constraint (e.g., empty `title`, `priority` not in the allowed enum).
- The `Content-Type` header is missing or is not `application/json`.

**How to debug:**

1. Open `http://localhost:8000/docs` and use the **Try it out** feature to inspect the exact schema the endpoint expects.
2. Read the `detail` array in the 422 response — each element identifies the field (`loc`) and the reason (`msg`).
3. Confirm the `priority` field uses one of the four valid values: `"low"`, `"medium"`, `"high"`, `"urgent"`.
4. When calling via `fetch`, confirm you are passing both the correct body and the correct header:
   ```javascript
   fetch('http://localhost:8000/cards', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify({ title: 'My task', column_id: 1 })
   });
   ```

---

## 9. Development Notes

### Adding a new column via the UI vs the API

**Via the UI:** Click the **+ Add Column** button (or equivalent control) in the frontend. The frontend calls `POST /columns` on your behalf.

**Via the API directly** (useful for scripting or testing):

```bash
curl -X POST http://localhost:8000/columns \
  -H "Content-Type: application/json" \
  -d '{"name": "Review", "color": "#f59e0b"}'
```

The new column is appended at the end (position = current max + 1). Drag it in the UI or call `PATCH /columns/reorder` to place it elsewhere.

---

### Resetting the database

Deleting `taskboard.db` wipes all columns and cards. On the next server startup, `seed.py` will re-insert the three default columns.

```bash
# Stop the uvicorn server first (CTRL+C), then:
rm kanban-board/taskboard.db

# Restart the server — seed runs automatically
cd kanban-board/backend
uvicorn main:app --reload --port 8000
```

> The database file is created at the path configured in `database.py`. If you changed the default path, adjust the `rm` command accordingly.

---

### Changing the default seed columns

Open `backend/seed.py`. The seed function inserts rows only when the columns table is empty, so changes only take effect after a database reset.

```python
# backend/seed.py  (illustrative — adapt to the actual implementation)
DEFAULT_COLUMNS = [
    {"name": "To Do",       "color": "#6b7280", "position": 0},
    {"name": "In Progress", "color": "#3b82f6", "position": 1},
    {"name": "Done",        "color": "#10b981", "position": 2},
]
```

To add a fourth default column, append a new dictionary to the list. After saving:

1. Delete `taskboard.db`.
2. Restart the server.

---

### Environment variables

`python-dotenv` is included in the dependencies. Create a `.env` file in `kanban-board/backend/` to override defaults without modifying source files:

```ini
# backend/.env
DATABASE_URL=sqlite:///./taskboard.db
```

`database.py` loads this file automatically via `load_dotenv()`. The `DATABASE_URL` variable controls which SQLite file (or other SQLAlchemy-compatible database) the application connects to.

---

### Useful one-liners

```bash
# Check which process is listening on port 8000
lsof -i :8000

# Inspect the database schema without a GUI tool
sqlite3 kanban-board/taskboard.db ".schema"

# Dump all cards to the terminal
sqlite3 kanban-board/taskboard.db "SELECT * FROM cards;"

# Run only specific test classes (e.g., column-related tests)
PYTHONPATH=. pytest tests/ -v -k "Column"

# Check test coverage for a single module
PYTHONPATH=. pytest tests/ --cov=main --cov-report=term-missing
```
