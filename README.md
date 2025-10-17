# My Automation Toolkit

A collection of automation utilities built around **Simcenter STAR-CCM+** macros and small helper
scripts. These tools streamline common post-processing tasks such as exporting geometry sectors,
preparing comparison views, capturing consistent imagery, and aggregating CSV results.

## Repository structure

```
.
├── macros/                  # Java macros to be executed from STAR-CCM+
│   ├── Comparison_1.java    # Imports multiple sector CAD designs and builds comparison sections
│   ├── AutomationSupport.java  # Shared helper for directory resolution and environment overrides
│   ├── ExtractParameterValues.java  # Prints all scalar global parameters to the console
│   ├── Sector_Extraction.java       # Saves the current simulation and exports the sector CAD body
│   └── Sector_Pictures.java         # Captures identical views for each sector part
└── scripts/
    └── combine_csv.py       # CLI/GUI helper for merging CSV files
```

## Getting started

### Prerequisites

* Simcenter STAR-CCM+ with permissions to run Java macros in your simulation environment.
* Java runtime supplied with STAR-CCM+ (no separate installation is required).
* Python 3.8+ with `pandas` installed for the CSV helper. Optional: `tkinter` for the file picker GUI
  (already bundled with many Python distributions).

### Installing the macros

1. Copy the files inside `macros/` into your STAR-CCM+ macro directory (e.g. `~/STAR-CCM+/macros`).
2. Launch STAR-CCM+, open your simulation, and use **Tools → Macros → Record/Play** to load the
   desired macro.
3. Update the relevant environment variables (see below) or adjust the scene/body names in the code
   if your project structure differs from the defaults supplied in the macros.

### Configuration overrides

Set one or more of the following environment variables before launching STAR-CCM+ to redirect where
the automation writes and reads files:

| Variable | Purpose | Default |
| --- | --- | --- |
| `STAR_AUTOMATION_DESIGNS_DIR` | Location of imported/exported design CAD files. | `/nfs/nnwork011/106300/03_Simulations/Sector/V2410/Designs/` |
| `STAR_AUTOMATION_EXPORT_DIR` | Directory used when saving simulations or exporting CAD. | Same as designs directory |
| `STAR_AUTOMATION_IMAGES_DIR` | Output folder for rendered PNG images. | Same as designs directory |

Providing these overrides keeps the macros configuration-free while still making them portable across
projects and file servers.

### Running the CSV combiner

```bash
python scripts/combine_csv.py file_a.csv file_b.csv -o combined.csv
```

If you omit the file arguments the script falls back to a file picker (when `tkinter` is available).
Use `--no-header` when combining files that lack header rows.

## Current automation features

* **Sector export automation** – cleans the current solution, synchronises Star-ICE settings, and
  exports both the `.sim` snapshot and `.x_b` CAD body in a single run.
* **Bulk CAD comparison builder** – imports sequential sector designs, generates sections, and sets
  up per-design displayers for side-by-side evaluation.
* **Batch scene capture** – applies a fixed camera setup and prints consistent PNG renders for each
  sector part.
* **CSV consolidation helper** – merges multiple CSV files from GUI or CLI input into a single
  dataset for downstream analytics.

## Roadmap and improvement ideas

* Parameterise export directories and resource names through configuration files or environment
  variables so the macros can be reused across multiple programmes without editing the source.
* Add logging wrappers around macro operations to aid troubleshooting during automated STAR-CCM+
  runs.
* Extend `Comparison_1` to read the list of CAD identifiers from a CSV or simulation table instead
  of the current fixed `P1..P8` pattern.
* Generate summary reports (images + tables) directly from STAR-CCM+ and store them alongside the
  exported CAD.
* Package the Python helper as a lightweight CLI tool (e.g. via `pipx`) and include unit tests for
  the data-loading utilities.
* Introduce CI linting (SpotBugs/Checkstyle for Java, Ruff for Python) to keep the automation suite
  consistent as it grows.

## Contribution guidelines

* Keep paths, scene names, and part identifiers configurable whenever possible.
* Document any environment-specific assumptions (server paths, licensing setup, etc.) within code
  comments and this README.
* Prefer extracting helper methods over duplicating STAR-CCM+ API calls—many macros share the same
  setup steps.

Feel free to open an issue or PR when you extend the automation workflow so future collaborators
understand the new capabilities.
