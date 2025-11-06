#!/usr/bin/env python3
"""
Universal Paxos consensus log analyzer - works with any number of logs.
Run: python3 analyze_logs.py [--dir LOG_DIRECTORY] [--output RESULTS.json]
"""

import re, sys, glob, os, json, statistics
from datetime import datetime
from collections import defaultdict

class UniversalPaxosAnalyzer:
    def parse_timestamp(self, ts):
        try:
            parts = ts.split()
            if len(parts) >= 2 and ("P.M." in parts[-1] or "A.M." in parts[-1]):
                time, meridiem = parts[0], parts[1].replace(".", "")
                h, m, s = time.split(":")
                h, m = int(h), int(m)
                sec, us = s.split(".")
                sec, us = int(sec), int(us[:6])
                if meridiem == "PM" and h != 12: h += 12
                elif meridiem == "AM" and h == 12: h = 0
                return datetime(2024, 1, 1, h, m, sec, us)
        except: pass
        return None

    def detect_logs(self, directory):
        consensus = []; process = []
        for p in ["game-*-[0-9].log", "game-*-[0-9][0-9].log"]:
            for f in glob.glob(os.path.join(directory, p)):
                if "display" not in f and "processinfo" not in f:
                    consensus.append(f)
        for p in ["*.log.cleaned", "*processinfo*.log"]:
            for f in glob.glob(os.path.join(directory, p)):
                if "display" not in f: process.append(f)
        return {"consensus": consensus, "process": process}

    def analyze_consensus(self, files):
        if not files: return {}
        result = {"num_processes": len(files), "per_player": defaultdict(lambda: {"moves": 0, "dirs": defaultdict(int)}), "total": 0}
        with open(sorted(files)[0]) as f:
            for line in f:
                m = re.search(r"Move request: player number (\d+) direction (\w+)", line)
                if m:
                    pid, dir = int(m.group(1)), m.group(2)
                    result["per_player"][pid]["moves"] += 1
                    result["per_player"][pid]["dirs"][dir] += 1
                    result["total"] += 1
        # Fairness
        counts = [p["moves"] for p in result["per_player"].values()]
        if counts:
            mean = statistics.mean(counts)
            result["fairness"] = {"mean": mean, "min": min(counts), "max": max(counts),
                                 "stdev": statistics.stdev(counts) if len(counts) > 1 else 0}
            result["fairness"]["score"] = max(0, 1 - (result["fairness"]["stdev"]/mean if mean > 0 else 0))
        return result

    def analyze_process(self, files):
        if not files: return {}
        result = {"num_processes": len(files), "config": {}, "aggregate": {"proposals": 0, "accepts": 0, "confirms": 0, "moves": 0},
                 "per_proposer": defaultdict(lambda: {"attempts": 0, "moves": 0}), "timing": {"first": None, "last": None}}
        for f in files:
            print(f"  Processing {os.path.basename(f)}...")
            with open(f) as file:
                for i, line in enumerate(file, 1):
                    if i % 100000 == 0: print(f"    {i:,} lines...")
                    if not any(k in line for k in ["AutoMoveGenerator", "sending proposes", "accepting AcceptRequest", "handling confirm", "Move request:"]): continue
                    tsm = re.match(r"^(\d+:\d+:\d+\.\d+ [AP]\.M\.)", line)
                    if not tsm: continue
                    ts = self.parse_timestamp(tsm.group(1))
                    if ts:
                        if not result["timing"]["first"] or ts < result["timing"]["first"]: result["timing"]["first"] = ts
                        if not result["timing"]["last"] or ts > result["timing"]["last"]: result["timing"]["last"] = ts
                    if "AutoMoveGenerator setup" in line:
                        m = re.search(r"maxmoves = (\d+), interval = (\d+)", line)
                        if m: result["config"]["max_moves"], result["config"]["interval_ms"] = int(m.group(1)), int(m.group(2))
                    elif "sending proposes" in line:
                        m = re.search(r"pID=(\d+)", line)
                        if m: result["per_proposer"][int(m.group(1))]["attempts"] += 1; result["aggregate"]["proposals"] += 1
                    elif "accepting AcceptRequest" in line: result["aggregate"]["accepts"] += 1
                    elif "handling confirm" in line: result["aggregate"]["confirms"] += 1
                    elif "Move request: player number" in line:
                        m = re.search(r"player number (\d+)", line)
                        if m: result["per_proposer"][int(m.group(1))]["moves"] += 1; result["aggregate"]["moves"] += 1
        # Calculate metrics
        if result["timing"]["first"] and result["timing"]["last"]:
            dur = (result["timing"]["last"] - result["timing"]["first"]).total_seconds()
            result["duration"] = dur
            if dur > 0:
                result["throughput"] = {"moves_per_sec": result["aggregate"]["moves"] / dur}
                if "interval_ms" in result["config"]:
                    theo = result["num_processes"] / (result["config"]["interval_ms"] / 1000.0)
                    result["throughput"]["theoretical"] = theo
                    result["throughput"]["efficiency"] = (result["throughput"]["moves_per_sec"] / theo) * 100 if theo > 0 else 0
        for pid, stats in result["per_proposer"].items():
            if stats["attempts"] > 0: stats["success_rate"] = stats["moves"] / stats["attempts"]
        return result

    def format_report(self, consensus, process):
        lines = ["\n" + "="*80, "PAXOS CONSENSUS LOG ANALYSIS", "="*80]
        nplayers = len(consensus.get("per_player", {})) or process.get("num_processes", 0)
        lines.append(f"\n📋 SYSTEM CONFIGURATION:\n  • Number of players/processes: {nplayers}")
        if process and "config" in process:
            if "interval_ms" in process["config"]: lines.append(f"  • Move interval: {process['config']['interval_ms']} ms")
            if "max_moves" in process["config"]: lines.append(f"  • Max moves per player: {process['config']['max_moves']}")
        if consensus and consensus.get("total", 0) > 0:
            lines.extend(["\n" + "="*80, "CONSENSUS RESULTS", "="*80, f"\n📊 OVERALL:\n  • Total moves: {consensus['total']}"])
            if "fairness" in consensus:
                f = consensus["fairness"]
                lines.append(f"\n⚖️  FAIRNESS:\n  • Mean: {f['mean']:.1f}, Min: {f['min']}, Max: {f['max']}, StdDev: {f['stdev']:.2f}")
                lines.append(f"  • Fairness score: {f['score']:.3f} (1.0 = perfect)")
                lines.append(f"  • Assessment: {'EXCELLENT' if f['score'] > 0.9 else 'GOOD' if f['score'] > 0.7 else 'MODERATE' if f['score'] > 0.5 else 'POOR'}")
            lines.append("\n👥 PER-PLAYER:")
            for pid, stats in sorted(consensus["per_player"].items(), key=lambda x: x[1]["moves"], reverse=True):
                lines.append(f"  Player {pid}: {stats['moves']} moves")
        if process and process.get("aggregate", {}).get("proposals", 0) > 0:
            agg = process["aggregate"]
            lines.extend(["\n" + "="*80, "PROCESS INFORMATION", "="*80])
            lines.append(f"\n📊 AGGREGATE:\n  • Proposals: {agg['proposals']:,}, Accepts: {agg['accepts']:,}, Confirms: {agg['confirms']:,}, Successful moves: {agg['moves']}")
            if "duration" in process: lines.append(f"  • Duration: {process['duration']:.2f} sec")
            if "throughput" in process:
                tp = process["throughput"]
                lines.append(f"\n⚡ THROUGHPUT:\n  • Moves/sec: {tp['moves_per_sec']:.2f}")
                if "theoretical" in tp: lines.append(f"  • Theoretical max: {tp['theoretical']:.2f} moves/sec")
                if "efficiency" in tp: lines.append(f"  • Efficiency: {tp['efficiency']:.1f}%")
            lines.append("\n👥 PER-PROPOSER:")
            for pid, stats in sorted(process["per_proposer"].items()):
                lines.append(f"  Proposer {pid}: {stats['attempts']:,} attempts → {stats['moves']} moves ({stats.get('success_rate', 0)*100:.1f}%)")
        lines.extend(["\n" + "="*80, "RESEARCH QUESTIONS", "="*80])
        lines.append(f"\n🔍 Q1: Impact of Players & Intervals")
        if process and "throughput" in process:
            tp = process["throughput"]
            lines.append(f"  • {nplayers} players, {process.get('config', {}).get('interval_ms', '?')}ms interval")
            lines.append(f"  • Throughput: {tp['moves_per_sec']:.2f} moves/sec, Efficiency: {tp.get('efficiency', 0):.1f}%")
            if tp.get("efficiency", 0) < 10: lines.append(f"  • SEVERE CONTENTION")
        lines.append(f"\n🔍 Q2: Fairness")
        if consensus and "fairness" in consensus:
            lines.append(f"  • Score: {consensus['fairness']['score']:.3f}")
            if consensus["fairness"]["score"] > 0.9: lines.append(f"  • Perfect fairness")
            elif consensus["fairness"]["score"] < 0.5: lines.append(f"  • UNFAIR - systematic bias present")
        lines.append(f"\n🔍 Q3: Maximum Throughput")
        if process and "throughput" in process:
            tp = process["throughput"]
            lines.append(f"  • Current: {tp['moves_per_sec']:.2f} moves/sec")
            if "theoretical" in tp:
                lines.append(f"  • Theoretical: {tp['theoretical']:.2f} moves/sec ({tp.get('efficiency', 0):.1f}% efficiency)")
                if tp.get("efficiency", 0) < 50: lines.append(f"  • FAR from saturation")
        lines.extend(["\n" + "="*80, "END OF ANALYSIS", "="*80])
        return "\n".join(lines)

def main():
    import argparse
    parser = argparse.ArgumentParser(description="Universal Paxos log analyzer")
    parser.add_argument("--dir", default=os.path.dirname(os.path.abspath(__file__)) + "/..", help="Log directory")
    parser.add_argument("--output", help="Output JSON file")
    args = parser.parse_args()
    
    analyzer = UniversalPaxosAnalyzer()
    print("Detecting logs...")
    logs = analyzer.detect_logs(args.dir)
    print(f"Found: {len(logs['consensus'])} consensus logs, {len(logs['process'])} process logs")
    
    if not logs['consensus'] and not logs['process']:
        print(f"No logs found in {args.dir}"); sys.exit(1)
    
    consensus = analyzer.analyze_consensus(logs['consensus']) if logs['consensus'] else {}
    process = analyzer.analyze_process(logs['process']) if logs['process'] else {}
    
    print(analyzer.format_report(consensus, process))
    
    if args.output:
        with open(args.output, "w") as f:
            json.dump({"consensus": consensus, "process": process}, f, indent=2, default=str)
        print(f"\nResults saved to: {args.output}")

if __name__ == "__main__":
    main()
