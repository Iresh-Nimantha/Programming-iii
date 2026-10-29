# Sorting Algorithm Performance Evaluator
IC4302 – Programming III | Group Assignment | University of Colombo

A Java Swing application that loads a CSV file, lets the user pick a numeric column,
sorts it with five hand-written algorithms, times each one and announces the fastest.

## How to run
Requires JDK 17+ (tested on JDK 21).

    # compile
    mkdir out
    javac -d out src/*.java
    # run
    java -cp out Main

Or in an IDE (IntelliJ / Eclipse / NetBeans): create a project, add the files in `src/`, run `Main`.
A sample file is in `sample/students_sample.csv`. More datasets: https://www.kaggle.com/datasets

Optional correctness tests:  `javac -d out src/*.java test/SortTest.java && java -cp out SortTest`
(run from the project folder)

## Files and suggested member split
| File | Purpose | Suggested owner |
|---|---|---|
| `Dataset.java`, `CsvLoader.java`, `Entry.java` | CSV upload, parsing, numeric-column detection, error handling | Member 1 |
| `SortingAlgorithms.java` | Insertion, Shell, Merge, Quick, Heap sort | Member 2 |
| `PerformanceEvaluator.java`, `MainFrame.java`, `Main.java` | Timing, best-algorithm logic, GUI | Member 3 |

(Group size is up to 3. If you work alone or in a pair, just cover the parts you present.)

## How it maps to the requirements
| Requirement | Where |
|---|---|
| Upload .csv, display columns | `MainFrame.chooseFile()` + `CsvLoader.load()` – drop-down lists every column, marked numeric / non-numeric |
| Select a (numeric) column | Non-numeric columns are rejected with a message (`CsvLoader.extractColumn`) |
| 5 sorting algorithms | `SortingAlgorithms` (no built-in sort used) |
| Execution time of each + best | `PerformanceEvaluator.evaluateAll()` / `findBest()` – table ranked fastest first, best row highlighted green |
| GUI | Swing; sorting runs in a `SwingWorker` so the window never freezes |
| Edge cases | wrong extension, empty file, header-only file, empty/invalid cells (skipped and reported), non-numeric column, no numeric columns, very large data (warning for Insertion Sort) |

## Design notes (useful for the viva / presentation)
- Each algorithm sorts its **own copy** of the same data, so timings are fair.
- Timing uses `System.nanoTime()` around only the sort call (not file loading or GUI work).
- Items are `Entry(key, rowIndex)` objects, so after sorting, the **whole row** can be shown in sorted order.
- Every result is verified with `isSorted()`; a wrong result cannot win "best".
- Quick Sort: median-of-three pivot + Hoare partitioning, recursing on the smaller half → no stack overflow on sorted/duplicate data.
- Shell Sort uses Knuth's gap sequence (1, 4, 13, 40…). Merge Sort uses one reusable aux array.
- Complexities: Insertion O(n²); Shell ≈ O(n^1.3–n^1.5); Merge, Heap O(n log n); Quick O(n log n) average, O(n²) worst.
- Typical result: Quick or Merge is fastest, Insertion is by far the slowest on large data (≈3.5 s vs ≈12–15 ms for 100,000 values in our test).

## Presentation checklist (Zoom recording)
1. Each member: "I'm <name>, my student number is <number>." then explain their code.
2. Demo: upload CSV → show columns → select numeric column → Run Sorting → show times and best algorithm.
3. Also show an error case (e.g., choose a non-numeric column or a non-CSV file).
4. Upload code + recording to Moodle before the deadline.

**Before submitting:** replace the placeholders with your real group member names / student numbers and make sure each of you can explain the code you present.
