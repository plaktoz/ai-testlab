#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND="$SCRIPT_DIR/frontend/index.html"

echo "Starting Kanban Task Board..."

# Start backend
cd "$BACKEND_DIR"
PYTHONPATH=. .venv/bin/uvicorn main:app --reload --port 8000 &
BACKEND_PID=$!

# Wait for backend to be ready
echo "Waiting for backend..."
for i in $(seq 1 20); do
  if curl -s http://localhost:8000/columns > /dev/null 2>&1; then
    echo "Backend ready at http://localhost:8000"
    break
  fi
  sleep 0.5
done

# Open frontend
echo "Opening frontend..."
open "$FRONTEND"

echo ""
echo "  App:     file://$FRONTEND"
echo "  API:     http://localhost:8000"
echo "  Swagger: http://localhost:8000/docs"
echo ""
echo "Backend PID: $BACKEND_PID  (press Ctrl+C or run: kill $BACKEND_PID)"

# Keep script alive so Ctrl+C stops the backend
wait $BACKEND_PID
