import glob
import os
import re
from datetime import datetime
from typing import List, Tuple


def get_log_files():
    """Get all log files matching the pattern."""
    pattern = re.compile(r"^game-22-99-.*\.\d{5}-[1-9]-processinfo-\.log\.cleaned$")
    all_files = glob.glob("game-*")
    matching_files = [f for f in all_files if pattern.match(f)]
    return matching_files


def read_log_file(filename):
    """Read a single log file and return its lines."""
    with open(filename, "r") as f:
        return f.readlines()


def check_confirm_time_differences(
    log_lines: List[str],
) -> List[Tuple[str, str, float]]:
    """
    Parse log lines to find all 'Received confirm' messages and calculate
    time differences between consecutive confirms.

    Args:
        log_lines: List of log line strings

    Returns:
        List of tuples containing (timestamp1, timestamp2, time_diff_seconds)
    """
    confirms = []

    # Extract all confirm timestamps
    for line in log_lines:
        if "Received confirm" in line and "paxos.Acceptor handleConfirm INFO:" in line:
            # Parse the timestamp
            # Example: "Nov 06, 2025 4:39:50.190174000 P.M."
            parts = line.split(" paxos.Acceptor")
            if len(parts) > 0:
                timestamp_str = parts[0].strip()
                try:
                    # Handle nanoseconds - need to truncate to microseconds
                    # Use regex to extract and replace the fractional seconds
                    import re

                    match = re.match(r"(.*\.)(\d+)(\s+[AP]\.M\.)", timestamp_str)
                    if match:
                        before = match.group(
                            1
                        )  # Everything up to and including the decimal
                        fractional = match.group(2)  # The digits after decimal
                        ampm = match.group(3)  # AM/PM part

                        # Truncate to 6 digits (microseconds)
                        microseconds = fractional[:6]
                        # Remove periods from A.M./P.M. for strptime
                        ampm_clean = ampm.replace(".", "")
                        ts_clean = f"{before}{microseconds}{ampm_clean}"
                    else:
                        # Try to clean up A.M./P.M. even if regex doesn't match
                        ts_clean = timestamp_str.replace("A.M.", "AM").replace(
                            "P.M.", "PM"
                        )

                    # Handle AM/PM format (without periods)
                    dt = datetime.strptime(ts_clean, "%b %d, %Y %I:%M:%S.%f %p")
                    confirms.append((timestamp_str, dt))
                except ValueError as e:
                    print(f"Warning: Could not parse timestamp '{timestamp_str}': {e}")
                    print(f"Debug: cleaned timestamp = '{ts_clean}'")
                    continue

    # Calculate differences between consecutive confirms
    differences = []
    for i in range(len(confirms) - 1):
        ts1_str, dt1 = confirms[i]
        ts2_str, dt2 = confirms[i + 1]

        time_diff = (dt2 - dt1).total_seconds()
        differences.append((ts1_str, ts2_str, time_diff))

    return differences


def analyze_single_file(filename):
    """Analyze confirms in a single log file."""
    print(f"\n{'=' * 80}")
    print(f"Analyzing: {filename}")
    print(f"{'=' * 80}")

    lines = read_log_file(filename)
    differences = check_confirm_time_differences(lines)

    if differences:
        print(f"Found {len(differences)} consecutive confirm pairs")

        # Show first few examples
        print("\nFirst 5 time differences:")
        print("-" * 80)
        for i, (ts1, ts2, diff) in enumerate(differences[:5], 1):
            print(f"{i}. {diff:.6f} seconds ({diff * 1000:.2f} ms)")

        if len(differences) > 5:
            print(f"... and {len(differences) - 5} more")

        # Statistics
        diffs = [d[2] for d in differences]
        avg_diff = sum(diffs) / len(diffs)
        min_diff = min(diffs)
        max_diff = max(diffs)

        print(f"\nSTATISTICS:")
        print(f"  Total confirms:          {len(differences) + 1}")
        print(
            f"  Average time difference: {avg_diff:.6f} seconds ({avg_diff * 1000:.2f} ms)"
        )
        print(
            f"  Minimum time difference: {min_diff:.6f} seconds ({min_diff * 1000:.2f} ms)"
        )
        print(
            f"  Maximum time difference: {max_diff:.6f} seconds ({max_diff * 1000:.2f} ms)"
        )

        return {
            "filename": filename,
            "count": len(differences) + 1,
            "avg": avg_diff,
            "min": min_diff,
            "max": max_diff,
        }
    else:
        print("No consecutive confirms found in this log file.")
        return None


def main():
    """Main function to process all matching log files."""
    log_files = get_log_files()

    if not log_files:
        print("No log files found matching the pattern.")
        print("Pattern: game-22-99-.*\\.\\d{5}-[1-9]-processinfo-\\.log\\.cleaned")
        return

    print(f"Found {len(log_files)} matching log files:")
    for f in log_files:
        print(f"  - {f}")

    # Analyze each file
    results = []
    for log_file in log_files:
        result = analyze_single_file(log_file)
        if result:
            results.append(result)

    # Summary across all files
    if results:
        print(f"\n{'=' * 80}")
        print("SUMMARY ACROSS ALL FILES")
        print(f"{'=' * 80}")
        for result in results:
            print(f"\n{result['filename']}:")
            print(f"  Total confirms: {result['count']}")
            print(f"  Avg diff: {result['avg']:.6f}s ({result['avg'] * 1000:.2f}ms)")
            print(f"  Min diff: {result['min']:.6f}s ({result['min'] * 1000:.2f}ms)")
            print(f"  Max diff: {result['max']:.6f}s ({result['max'] * 1000:.2f}ms)")


if __name__ == "__main__":
    main()
