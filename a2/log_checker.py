import glob
import os
import re


def get_log_files():
    """Finds log files that strictly match 'game-<num>-<num>-<num>.log'."""
    pattern = re.compile(r"^game-\d+-\d+-\d+\.log$")
    return [f for f in glob.glob("game-*-*-*.log") if pattern.match(f)]


def read_log_files(files):
    """Reads log files into a dictionary, skipping the first line."""
    print(files)
    logs = {}
    for f in files:
        with open(f, "r") as file:
            # store the lines (minus the first) and the original file path
            logs[f] = file.readlines()[1:]
    return logs


def compare_logs(logs):
    """
    Compares log files iteratively.

    In each step, it finds the shortest log, verifies that all other logs match
    up to that length, and then removes the shortest from the set to continue
    checking the rest.
    """
    if not logs:
        print("No log files found.")
        return

    # Convert to a list of tuples (filepath, lines) for easier processing
    remaining_logs = list(logs.items())

    while remaining_logs:
        # Find the shortest log in the current list of remaining logs
        shortest_log_path, shortest_log_lines = min(
            remaining_logs, key=lambda item: len(item[1])
        )
        shortest_length = len(shortest_log_lines)

        if shortest_length == 0:
            print(
                f"Log file {shortest_log_path} is empty, removing it from comparison."
            )
            remaining_logs = [
                (path, lines)
                for path, lines in remaining_logs
                if path != shortest_log_path
            ]
            continue

        print(
            f"\n--- Verifying logs up to line {shortest_length} (length of {os.path.basename(shortest_log_path)}) ---\n"
        )

        # The first shortest log is our reference for this round
        reference_lines = shortest_log_lines

        # Compare all remaining logs against the reference for the length of the shortest one
        for i in range(shortest_length):
            for log_path, log_lines in remaining_logs:
                if log_lines[i] != reference_lines[i]:
                    print(
                        f"Mismatch found in file {os.path.basename(log_path)} at line {i + 1}:"
                    )
                    print(f"  Expected: {reference_lines[i].strip()}")
                    print(f"  Got:      {log_lines[i].strip()}")
                    return  # Stop on first error

        print(
            f"All currently checked logs are consistent up to line {shortest_length}."
        )

        # Prepare for the next iteration:
        # Keep only the logs that were longer than the one we just used as a reference.
        new_remaining_logs = []
        for path, lines in remaining_logs:
            if len(lines) > shortest_length:
                new_remaining_logs.append((path, lines))

        if not new_remaining_logs:
            print("\n--- All logs have been successfully checked. ---")

        remaining_logs = new_remaining_logs


if __name__ == "__main__":
    log_files = get_log_files()
    logs_content = read_log_files(log_files)
    compare_logs(logs_content)
