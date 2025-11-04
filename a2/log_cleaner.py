import textwrap
import glob
import os
import re
import sys


def get_log_files():
    """
    Finds log files that match the new distributed application log naming convention,
    e.g., 'game-22-99-Rays-MacBook-Pro.local.40122-1-processinfo-.log'.

    The search is recursive from the current directory.
    """
    # Pattern matches: game-<num>-<num>-<host.port>-<num>-processinfo-.log
    # The [A-Za-z0-9.-]+ segment safely matches the host/port block (e.g., Rays-MacBook-Pro.local.40122).
    pattern = re.compile(r"^game-\d+-\d+-[A-Za-z0-9.-]+-\d+-processinfo-\.log$")

    # Use recursive globbing (**) to find files matching 'game-*.log' anywhere in subdirectories
    found_files = glob.glob("**/game-*.log", recursive=True)

    # Filter the full paths based on the base filename matching the regex pattern
    return [f for f in found_files if pattern.match(os.path.basename(f))]


def read_log_files(files):
    """
    Reads content from multiple log files into a dictionary, skipping the first line
    of each file.

    Returns:
        dict[str, list[str]]: A dictionary mapping file paths to lists of log lines (excluding the header).
    """
    print(f"Discovered log files: {files}")
    logs = {}
    for f in files:
        try:
            with open(f, "r") as file:
                # Store the lines (minus the first, which is often a header/command line)
                logs[f] = file.readlines()[1:]
        except FileNotFoundError:
            print(f"Error: File not found: {f}")
        except Exception as e:
            print(f"Error reading file {f}: {e}")
    return logs


def remove_gcl_logs(log_lines: list[str]) -> str:
    """
    Filters out all log lines that contain the specific communication subsystem
    identifier ("comp512.gcl").

    Args:
        log_lines: A list of raw log line strings to be processed.

    Returns:
        A cleaned log string containing only the non-GCL related lines, joined by newlines.
    """
    # The identifier for the General Communication Layer (GCL) to filter out.
    # We use this as it appears in all GCL-related logs (e.g., comp512.gcl.GCL).
    GCL_FILTER_KEYWORD = "comp512.gcl"

    # Filter the lines, keeping only those that DO NOT contain the GCL keyword
    clean_lines = [line.strip() for line in log_lines if GCL_FILTER_KEYWORD not in line]

    # Join the remaining lines back together with newlines
    return "\n".join(clean_lines)


def process_and_output_logs():
    """
    Main function to find logs, process them using the log cleaner, and write
    the cleaned output to new files with a '.cleaned' suffix.
    """
    # 1. Find relevant files
    files_to_process = get_log_files()

    if not files_to_process:
        print(
            "No log files matching 'game-<num>-<num>-<num>.log' found in the current directory."
        )
        return

    # 2. Read the content (skipping the first line/header)
    all_logs = read_log_files(files_to_process)

    # 3. Process and write each file
    for original_path, lines in all_logs.items():
        if not lines:
            print(f"Skipping empty or malformed file: {original_path}")
            continue

        # Clean the logs
        cleaned_content = remove_gcl_logs(lines)

        # Define the output file path
        output_path = f"{original_path}.cleaned"

        # Write the cleaned content to the new file
        try:
            with open(output_path, "w") as outfile:
                outfile.write(cleaned_content)
            print(f"Successfully cleaned log written to: {output_path}")
        except Exception as e:
            print(f"Error writing to output file {output_path}: {e}")


if __name__ == "__main__":
    # If the user invokes this script, it will now automatically process
    # all matching log files in the current directory.
    print("--- Log Cleaning Utility Started ---")
    process_and_output_logs()
    print("--- Log Cleaning Utility Finished ---")
