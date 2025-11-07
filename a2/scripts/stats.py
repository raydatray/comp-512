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


def count_proposer_tries(log_lines: List[str]) -> Tuple[List[int], float]:
    """
    Count how many tries it takes for a proposer to confirm its move.

    Args:
        log_lines: List of log line strings

    Returns:
        Tuple of (list of try counts, average tries)
    """
    # Track ongoing paxos rounds by move identifier
    ongoing_rounds = {}  # key: move_id, value: (start_time, try_count)
    completed_tries = []

    for line in log_lines:
        # Check for start of new round
        if "Trying `new round of Paxos` for move" in line:
            # Extract move identifier
            match = re.search(
                r"GameMove\[id=Identifier\[playerNum=(\d+), turn=(\d+)\]", line
            )
            if match:
                player_num = match.group(1)
                turn = match.group(2)
                move_id = f"p{player_num}_t{turn}"

                # Initialize or increment try count
                if move_id not in ongoing_rounds:
                    ongoing_rounds[move_id] = 1
                else:
                    ongoing_rounds[move_id] += 1

        # Check for lost propose phase (failed attempt, will retry)
        elif (
            "Lost propose phase" in line and "paxos.Proposer runInstance INFO:" in line
        ):
            # This is just a failed attempt, the move will be retried
            # We don't need to do anything here since we already counted the try
            pass

        # Check for successful confirmation
        elif "Confirming move" in line and "paxos.Proposer runInstance INFO:" in line:
            # Extract move identifier
            match = re.search(
                r"GameMove\[id=Identifier\[playerNum=(\d+), turn=(\d+)\]", line
            )
            if match:
                player_num = match.group(1)
                turn = match.group(2)
                move_id = f"p{player_num}_t{turn}"

                # Record the number of tries it took
                if move_id in ongoing_rounds:
                    completed_tries.append(ongoing_rounds[move_id])
                    del ongoing_rounds[move_id]

    # Calculate average
    avg_tries = sum(completed_tries) / len(completed_tries) if completed_tries else 0

    return completed_tries, avg_tries


def calculate_moves_per_second(log_lines: List[str]) -> Tuple[float, int, float]:
    """
    Calculate how many moves are being accepted and delivered per second.

    Args:
        log_lines: List of log line strings

    Returns:
        Tuple of (moves_per_second, total_moves, duration_seconds)
    """
    move_timestamps = []

    for line in log_lines:
        if "comp512.ti.TreasureIsland move INFO: Move request: player number" in line:
            # Parse the timestamp
            parts = line.split(" comp512.ti.TreasureIsland")
            if len(parts) > 0:
                timestamp_str = parts[0].strip()
                try:
                    # Handle nanoseconds - truncate to microseconds
                    match = re.match(r"(.*\.)(\d+)(\s+[AP]\.M\.)", timestamp_str)
                    if match:
                        before = match.group(1)
                        fractional = match.group(2)
                        ampm = match.group(3)

                        microseconds = fractional[:6]
                        ampm_clean = ampm.replace(".", "")
                        ts_clean = f"{before}{microseconds}{ampm_clean}"
                    else:
                        ts_clean = timestamp_str.replace("A.M.", "AM").replace(
                            "P.M.", "PM"
                        )

                    dt = datetime.strptime(ts_clean, "%b %d, %Y %I:%M:%S.%f %p")
                    move_timestamps.append(dt)
                except ValueError as e:
                    continue

    if len(move_timestamps) < 2:
        return 0.0, len(move_timestamps), 0.0

    # Calculate duration from first to last move
    duration = (move_timestamps[-1] - move_timestamps[0]).total_seconds()
    total_moves = len(move_timestamps)
    moves_per_second = total_moves / duration if duration > 0 else 0

    return moves_per_second, total_moves, duration


def analyze_single_file(filename):
    """Analyze confirms in a single log file."""
    print(f"\n{'=' * 80}")
    print(f"Analyzing: {filename}")
    print(f"{'=' * 80}")

    lines = read_log_file(filename)
    differences = check_confirm_time_differences(lines)

    result = {}
    result["filename"] = filename

    # Analyze confirm time differences
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

        print(f"\nCONFIRM TIME STATISTICS:")
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

        result["confirm_count"] = len(differences) + 1
        result["confirm_avg"] = avg_diff
        result["confirm_min"] = min_diff
        result["confirm_max"] = max_diff
    else:
        print("No consecutive confirms found in this log file.")
        result["confirm_count"] = 0

    # Analyze proposer tries
    print(f"\nPROPOSER TRIES STATISTICS:")
    tries_list, avg_tries = count_proposer_tries(lines)
    if tries_list:
        print(f"  Total successful moves:  {len(tries_list)}")
        print(f"  Average tries per move:  {avg_tries:.2f}")
        print(f"  Min tries:               {min(tries_list)}")
        print(f"  Max tries:               {max(tries_list)}")

        # Show distribution
        from collections import Counter

        distribution = Counter(tries_list)
        print(f"  Distribution:")
        for tries in sorted(distribution.keys()):
            count = distribution[tries]
            percentage = (count / len(tries_list)) * 100
            print(f"    {tries} tries: {count} moves ({percentage:.1f}%)")

        result["proposer_tries_avg"] = avg_tries
        result["proposer_tries_min"] = min(tries_list)
        result["proposer_tries_max"] = max(tries_list)
        result["proposer_successful_moves"] = len(tries_list)
    else:
        print("  No completed paxos rounds found.")
        result["proposer_tries_avg"] = 0

    # Analyze moves per second
    print(f"\nMOVES PER SECOND STATISTICS:")
    moves_per_sec, total_moves, duration = calculate_moves_per_second(lines)
    if total_moves > 0:
        print(f"  Total moves delivered:   {total_moves}")
        print(f"  Duration:                {duration:.2f} seconds")
        print(f"  Moves per second:        {moves_per_sec:.2f}")

        result["moves_per_second"] = moves_per_sec
        result["total_moves"] = total_moves
        result["duration"] = duration
    else:
        print("  No moves found.")
        result["moves_per_second"] = 0
        result["total_moves"] = 0

    return (
        result
        if result.get("confirm_count", 0) > 0 or result.get("proposer_tries_avg", 0) > 0
        else result
    )


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

            if result.get("confirm_count", 0) > 0:
                print(f"  Confirm Statistics:")
                print(f"    Total confirms: {result['confirm_count']}")
                print(
                    f"    Avg diff: {result['confirm_avg']:.6f}s ({result['confirm_avg'] * 1000:.2f}ms)"
                )
                print(
                    f"    Min diff: {result['confirm_min']:.6f}s ({result['confirm_min'] * 1000:.2f}ms)"
                )
                print(
                    f"    Max diff: {result['confirm_max']:.6f}s ({result['confirm_max'] * 1000:.2f}ms)"
                )

            if result.get("proposer_tries_avg", 0) > 0:
                print(f"  Proposer Statistics:")
                print(f"    Successful moves: {result['proposer_successful_moves']}")
                print(f"    Avg tries: {result['proposer_tries_avg']:.2f}")
                print(f"    Min tries: {result['proposer_tries_min']}")
                print(f"    Max tries: {result['proposer_tries_max']}")

            if result.get("total_moves", 0) > 0:
                print(f"  Throughput Statistics:")
                print(f"    Total moves: {result['total_moves']}")
                print(f"    Duration: {result['duration']:.2f}s")
                print(f"    Moves/sec: {result['moves_per_second']:.2f}")


if __name__ == "__main__":
    main()
