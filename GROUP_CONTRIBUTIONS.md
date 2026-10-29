# Member responsibilities and boundaries

- **Member 1 — Dataset/file handling:** `Entry.java`, `Dataset.java`, `CsvLoader.java`. Owns validation, CSV parsing, numeric-column detection and extraction.
- **Member 2 — Sorting algorithms:** `SortingAlgorithms.java`. Owns the five hand-written algorithms and their correctness checks.
- **Member 3 — Evaluation and UI:** `PerformanceEvaluator.java`, `MainFrame.java`, `Main.java`, plus integration tests/docs. Owns timing, ranking, GUI workflow, background execution and final integration.

## Shared contract
- `CsvLoader.load(File)` returns a `Dataset`.
- `CsvLoader.extractColumn(Dataset, int)` returns `CsvLoader.Extraction` containing `Entry[]` and a skipped count.
- `Entry.key` is the numeric sort key; `Entry.rowIndex` points to the original CSV row.
- Every method in `SortingAlgorithms` sorts `Entry[]` in ascending order by `key`.
- Member 3's evaluator clones the input for each algorithm and measures only the sort call.

## Suggested commits
1. `feat: add CSV loading and dataset handling`
2. `feat: implement five sorting algorithms`
3. `feat: add benchmark evaluator and Swing interface`
4. `test: verify sorting correctness and CSV edge cases`

Members should pull the latest shared branch before starting. Use pull requests or ask another member to review each contribution. Avoid committing generated `out/` class files.
