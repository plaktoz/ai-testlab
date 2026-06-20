#!/usr/bin/env bash

PORT=8000
PIDS=$(lsof -ti:$PORT 2>/dev/null)

if [ -z "$PIDS" ]; then
  echo "Nothing running on port $PORT."
  exit 0
fi

kill $PIDS
echo "Backend stopped (port $PORT, PID $PIDS)."
