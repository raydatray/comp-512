#!/bin/bash
# trim_logs.sh
# Keeps only lines containing the '`' character from each log file.
# Creates a duplicate file with "-trimmed" appended to its name.

# Exit immediately if a command fails
set -e

for file in game-*-processinfo-.log; do
  # Skip if no files found
  [ -e "$file" ] || { echo "No matching log files found."; exit 1; }

  # Construct output filename
  base="${file%.log}"
  trimmed="trimmed-${base}.log"

  echo "Trimming $file -> $trimmed"

  # Keep only lines containing backtick character
  grep -E '(`|paxos\.GCLReader run FINE: Received)' "$file" > "$trimmed"
done

echo "✅ Done. Trimmed files created."
