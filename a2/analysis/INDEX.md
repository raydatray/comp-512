# Paxos Log Analysis Tools - Index

## Quick Start

**To analyze your logs right now:**
```bash
cd /Users/ray/comp-512/a2
python3 analysis/analyze_logs.py
```

That's all you need! The script works with any number of log files.

## Files in This Directory

| File | Purpose | When to Use |
|------|---------|-------------|
| **analyze_logs.py** | Universal analyzer | **Use this for everything** |
| QUICKSTART.md | Quick start guide | First time using the tools |
| README.md | Detailed documentation | Understanding metrics & options |
| SUMMARY.md | Analysis of current logs | See what your logs show |
| run_analysis.sh | Shell wrapper | Alternative to Python command |

## What Gets Analyzed

The analyzer automatically detects and processes:
- ✅ Consensus result logs (`game-*-N.log`)
- ✅ Process info logs (`*processinfo*.log`, `*.cleaned`)
- ✅ Any number of players/processes
- ✅ Different test configurations

## Output Includes

### Research Question 1: Impact of Players & Intervals
- Throughput (moves/second)
- System efficiency (%)  
- Contention assessment

### Research Question 2: Fairness
- Fairness score (0-1)
- Per-player move distribution
- Identification of systematic bias

### Research Question 3: Maximum Throughput
- Current vs theoretical throughput
- Distance from saturation
- Capacity headroom

## Common Commands

```bash
# Basic analysis
python3 analysis/analyze_logs.py

# Specify directory
python3 analysis/analyze_logs.py --dir /path/to/logs

# Save JSON output
python3 analysis/analyze_logs.py --output results.json

# Using shell script
./analysis/run_analysis.sh
```

## Understanding Results

**System Efficiency**
- `< 10%` → Severe contention problems
- `10-50%` → Can handle shorter intervals
- `> 50%` → Good utilization

**Fairness Score**
- `> 0.9` → Excellent
- `0.7-0.9` → Good
- `0.5-0.7` → Moderate
- `< 0.5` → Poor (unfair)

## Key Findings from Your Logs

Based on the logs analyzed:

1. **9-player test**: Perfect fairness (score 1.0), 360 moves, all players equal
2. **3-player test (in process logs)**: Severe unfairness, only 0.1% efficiency
3. **Critical bug**: Proposer 1 made 80,686 attempts with 0 successes

See `SUMMARY.md` for complete analysis and recommendations.

## No Dependencies Required

All scripts use only Python 3 standard library - no pip install needed!

## Support

- Questions about metrics → See `README.md`
- First time user → See `QUICKSTART.md`  
- Understanding your results → See `SUMMARY.md`
- Script help → Run with `--help` flag
