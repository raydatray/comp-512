# Paxos Log Analysis - Quick Start Guide

## TL;DR - Fastest Way to Analyze Your Logs

```bash
cd /Users/ray/comp-512/a2
python3 analysis/analyze_logs.py
```

That's it! The script will automatically find and analyze all your logs.

---

## What You'll Get

The analysis answers three research questions:

### 1. **Impact of Players and Intervals**
   - How throughput changes with different configurations
   - System efficiency percentage
   - Whether you can reduce intervals further

### 2. **Fairness Among Processes**
   - Which players/proposers are getting their moves accepted
   - Fairness score (1.0 = perfect, 0.0 = unfair)
   - Identification of any systematic bias

### 3. **Maximum Throughput**
   - Current throughput vs theoretical maximum
   - How close you are to system capacity
   - When throughput starts to flatten

---

## Common Usage Patterns

### Analyze Specific Directory
```bash
python3 analysis/analyze_logs.py --dir /path/to/logs
```

### Save Results to JSON
```bash
python3 analysis/analyze_logs.py --output results.json
```

### Analyze Multiple Test Runs Separately

First, organize your logs:
```bash
python3 analysis/organize_logs.py
# This shows you what test runs were detected (dry run)

python3 analysis/organize_logs.py --execute
# This actually organizes them into separate directories
```

Then analyze each run:
```bash
python3 analysis/analyze_logs.py --dir organized_logs/run01_* --output run01.json
python3 analysis/analyze_logs.py --dir organized_logs/run02_* --output run02.json
```

---

## Understanding the Output

### Key Metrics to Look For

**System Efficiency**
- **< 10%**: Severe contention problems
- **10-50%**: System working but can be improved
- **> 50%**: Good utilization

**Fairness Score**
- **> 0.9**: Excellent - all players equal
- **0.7-0.9**: Good - minor variations
- **0.5-0.7**: Moderate - some players advantaged
- **< 0.5**: Poor - systematic bias present

**Success Rate per Proposer**
- Should be similar across all proposers (within 20%)
- Large differences indicate unfairness
- 0% for any proposer = critical bug

---

## Troubleshooting

### "No log files found"
- Check you're in the right directory
- Logs should match patterns: `game-*-*.log` or `*processinfo*.log`

### Script is slow on large files
Use the efficient version:
```bash
python3 analysis/analyze_paxos_efficient.py --dir . --output results.json
```

### Mixed results from different tests
Separate test runs first:
```bash
python3 analysis/organize_logs.py --execute
```

---

## What the Analysis Detects

The script automatically finds:
- ✅ Any number of player/process logs
- ✅ Consensus results (agreed-upon moves)
- ✅ Process info (proposals, attempts, conflicts)
- ✅ Configuration (interval, max moves)
- ✅ Timing and throughput
- ✅ Fairness metrics

---

## Example Output Interpretation

```
📊 OVERALL STATISTICS:
  • Total proposals sent: 80,829
  • Successful moves: 3
  • System efficiency: 0.3%
```
**Translation:** System is experiencing severe contention. Only 3 out of 80,829 proposals succeeded.

```
⚖️  FAIRNESS ANALYSIS:
  • Fairness score: 1.000 (1.0 = perfect)
  • All processes have identical performance
```
**Translation:** Perfect fairness - all players getting equal share of successful moves.

```
🔍 Q3: Maximum Throughput Capacity
  • Current throughput: 0.09 moves/sec
  • Theoretical max: 30.00 moves/sec
  • Achieving 0.3% of theoretical maximum
  • System far from saturation - interval could be reduced
```
**Translation:** System has huge headroom theoretically, but contention is preventing progress.

---

## Files in This Directory

- **`analyze_logs.py`** ← Use this one (universal analyzer)
- `analyze_paxos_efficient.py` - For very large files
- `analyze_consensus_results.py` - Consensus-only analysis
- `organize_logs.py` - Separate test runs
- `README.md` - Detailed documentation
- `SUMMARY.md` - Analysis of your current logs
- `QUICKSTART.md` - This file

---

## Next Steps After Analysis

1. **If fairness score < 0.5:** Fix systematic bias in Paxos implementation
2. **If efficiency < 10%:** Add backoff/randomization to reduce contention
3. **If one proposer has 0% success:** Debug ballot comparison logic
4. **If efficiency > 50%:** Try shorter intervals to increase throughput

---

## Need Help?

1. Check `README.md` for detailed explanations
2. Check `SUMMARY.md` for analysis of your current results
3. All scripts have `--help` options
