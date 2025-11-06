# Log Analysis Tools

This directory contains Python scripts to analyze your Paxos consensus logs and answer the research questions about performance, fairness, and throughput.

## Quick Start

```bash
cd /Users/ray/comp-512/a2
python3 analysis/analyze_logs.py
```

The analyzer works with **any number of log files** - it automatically detects what you have and provides comprehensive analysis.

## What You'll Get

The analysis answers three key research questions:

1. **How do the number of players and interval affect performance?**
   - Throughput measurements (moves/second)
   - System efficiency percentage
   - Contention assessment

2. **Do all processes have the same rate at which moves are accepted?**
   - Fairness score (1.0 = perfect fairness)
   - Per-player success rates
   - Identification of any systematic bias

3. **What is the maximum throughput the system can process?**
   - Current vs theoretical maximum throughput
   - Efficiency at current interval
   - Distance from saturation

## Documentation

All documentation is in the `analysis/` directory:

- **`analysis/INDEX.md`** - Main index and quick reference
- **`analysis/QUICKSTART.md`** - Getting started guide
- **`analysis/SUMMARY.md`** - Analysis of your current logs with recommendations

## Key Findings

Based on your current logs:

✅ **9-player test**: Perfect fairness (1.0 score), 360 successful moves
❌ **3-player test**: Critical bug - Proposer 1 starved with 80,686 failed attempts
⚠️  **Efficiency**: Only 0.1% of theoretical maximum (severe contention)

See `analysis/SUMMARY.md` for complete analysis and fix recommendations.

## Requirements

- Python 3.6 or newer
- No external dependencies (uses only standard library)

## Support

For detailed help:
```bash
python3 analysis/analyze_logs.py --help
```

Or read the documentation files in `analysis/`.
