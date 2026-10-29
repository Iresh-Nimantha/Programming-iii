
# Java Application for Sorting Algorithm Performance Evaluation

This project combines CSV handling, sorting implementations, performance benchmarking, and a Java Swing GUI for comparing five sorting algorithms on numeric CSV columns.

## Java version and project structure

- Java version: Java 8 or later is supported.
- Project layout:
  - `src/Entry.java` stores a numeric value and its original CSV row index.
  - `src/Dataset.java` stores CSV headers and rows.
  - `src/CsvLoader.java` validates and parses CSV files.
  - `src/SortingAlgorithms.java` implements insertion, shell, merge, quick, and heap sort.
  - `src/PerformanceEvaluator.java` benchmarks each algorithm and selects the fastest correct result.
  - `src/MainFrame.java` provides the GUI.
  - `src/Main.java` launches the application.
  - `src/SortingAlgorithmsTest.java` verifies sorting correctness.
  - `src/PerformanceEvaluatorTest.java` verifies benchmark behavior.

## Stage 1 — Member 1

**Responsibility:** CSV validation/loading, dataset representation, numeric-column detection and extraction while retaining original row indexes.

Files: `Entry.java`, `Dataset.java`, `CsvLoader.java`, `Member1Demo.java`.

Run from this folder:

```bat
javac -d out src\*.java
java -cp out Member1Demo sample\students_sample.csv
```

`CsvLoader.load(File)` validates the file and parses headers/rows. `isNumericColumn(Dataset, int)` identifies numeric columns. `extractColumn(Dataset, int)` returns `Entry[]` and a skipped-row count. `Entry.rowIndex` allows the GUI to display complete CSV rows in sorted order.

## Stage 2 — Member 2

`SortingAlgorithms` provides in-place ascending sorts for extracted `Entry[]` values while preserving each entry's original `rowIndex`:

```java
SortingAlgorithms.insertionSort(entries);
SortingAlgorithms.shellSort(entries);
SortingAlgorithms.mergeSort(entries);
SortingAlgorithms.quickSort(entries);
SortingAlgorithms.heapSort(entries);
boolean sorted = SortingAlgorithms.isSorted(entries);
```

Run the dependency-free sorting tests after compilation:

```bat
javac -d out src\*.java
java -cp out SortingAlgorithmsTest
```

## Stage 3 — Member 3

The desktop application window is titled **Sorting Algorithm Performance Evaluator** and uses a light, academic-style Swing layout. It allows a user to upload a CSV file, review the dataset, choose from columns labeled Numeric or Text, and benchmark the five sorting algorithms.

### Benchmark method

The benchmark copies the original `Entry[]` for each algorithm, measures time with `System.nanoTime()`, validates the sorted output independently, and excludes incorrect results from the fastest-correct winner. The GUI ranks valid results by execution time, highlights the best valid result, displays a progress indicator while using `SwingWorker`, and displays sorted full rows while preserving row integrity. Empty or malformed numeric values are excluded from sorting and reported after the analysis. Datasets above 50,000 records trigger a warning because Insertion Sort may take a long time.

### Running the application

```bat
javac -d out src\*.java
java -cp out Main
```

On Windows, `run.bat` compiles and launches the program. On macOS/Linux, run `./run.sh`.

Then:

1. Click `Choose CSV File`.
2. Choose a CSV file like `sample\students_sample.csv`.
3. Select a numeric column from the drop-down list.
4. Click `Run Performance Analysis` to benchmark the algorithms.
5. Review the benchmark table and the sorted dataset.

## Algorithms included

- Insertion Sort
- Shell Sort
- Merge Sort
- Quick Sort
- Heap Sort

## Tests

Run the complete Java test set from the project root:

```bat
javac -d out src\*.java
java -cp out SortingAlgorithmsTest
java -cp out PerformanceEvaluatorTest
```

## Limitations and assumptions

- The project assumes CSV files are UTF-8 encoded and use standard comma-separated values.
- Rows with empty or malformed numeric values are skipped when building the sortable numeric column.
- Very small datasets can produce noisy timing measurements; the GUI still reports timings in microseconds or milliseconds for readability.
- The application is built without third-party UI libraries and uses standard Swing components.
