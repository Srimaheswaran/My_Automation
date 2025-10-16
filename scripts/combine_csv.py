#!/usr/bin/env python3
"""Utility for combining multiple CSV files into a single consolidated dataset."""

from __future__ import annotations

import argparse
import sys
from pathlib import Path
from typing import Iterable, List

import pandas as pd

try:
    import tkinter as tk
    from tkinter import filedialog
except Exception:  # pragma: no cover - tkinter may be unavailable in headless environments
    tk = None
    filedialog = None


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Combine multiple CSV files into a single output file."
    )
    parser.add_argument(
        "files",
        nargs="*",
        help="Paths to the CSV files to combine. If omitted, a file picker dialog will open.",
    )
    parser.add_argument(
        "-o",
        "--output",
        default="CombinedData.csv",
        help="Name (or path) for the combined CSV file.",
    )
    parser.add_argument(
        "--no-header",
        action="store_true",
        help="Treat CSV files as headerless and assign generic column names.",
    )
    return parser.parse_args()


def select_files_via_dialog() -> List[Path]:
    if filedialog is None:
        raise RuntimeError("Tkinter is not available. Provide file paths as arguments instead.")

    root = tk.Tk()
    root.withdraw()
    selected = filedialog.askopenfilenames(
        title="Select CSV Files",
        filetypes=[("CSV Files", "*.csv"), ("All Files", "*.*")],
    )
    root.update()

    return [Path(path) for path in selected]


def read_csv_files(paths: Iterable[Path], assume_no_header: bool) -> pd.DataFrame:
    frames = []
    for path in paths:
        dataframe = pd.read_csv(path, header=None if assume_no_header else 'infer')
        if assume_no_header:
            dataframe.columns = [f'column_{index}' for index in range(len(dataframe.columns))]
        frames.append(dataframe)
    return pd.concat(frames, ignore_index=True)


def determine_output_path(files: List[Path], output: str) -> Path:
    output_path = Path(output)
    if not output_path.is_absolute() and files:
        return files[0].parent / output_path
    return output_path


def main() -> int:
    args = parse_args()

    try:
        files = [Path(file) for file in args.files]
        if not files:
            files = select_files_via_dialog()
    except RuntimeError as error:
        print(error, file=sys.stderr)
        return 1

    if not files:
        print("No files selected. Exiting...")
        return 0

    missing = [str(path) for path in files if not path.exists()]
    if missing:
        print("The following files could not be found:")
        for path in missing:
            print(f"  - {path}")
        return 1

    try:
        combined = read_csv_files(files, assume_no_header=args.no_header)
    except Exception as exc:  # pragma: no cover - pass through pandas errors
        print(f"Failed to read CSV files: {exc}", file=sys.stderr)
        return 1

    output_path = determine_output_path(files, args.output)
    combined.to_csv(output_path, index=False)
    print(f"Data has been combined and saved to {output_path}.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
