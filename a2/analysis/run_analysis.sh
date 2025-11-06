#!/bin/bash
# Simple wrapper to run Paxos log analysis

echo "=================================="
echo "Paxos Log Analysis Tool"
echo "=================================="
echo ""

# Find Python 3
PYTHON=$(which python3 2>/dev/null || which python 2>/dev/null)

if [ -z "$PYTHON" ]; then
    echo "Error: Python 3 not found"
    exit 1
fi

echo "Using: $PYTHON"
echo ""

# Default to parent directory for logs
LOG_DIR="${1:-..}"

echo "Analyzing logs in: $LOG_DIR"
echo ""

$PYTHON analyze_logs.py --dir "$LOG_DIR" --output analysis_results.json

echo ""
echo "=================================="
echo "Analysis complete!"
echo "Results saved to: analysis_results.json"
echo "=================================="
