import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Benchmarks the five sorting algorithms against the same extracted numeric data
 * and reports the fastest correctly sorted result.
 */
public final class PerformanceEvaluator {
    private static final List<SortTask> SORT_TASKS = Arrays.asList(
            new SortTask("Insertion Sort", SortingAlgorithms::insertionSort),
            new SortTask("Shell Sort", SortingAlgorithms::shellSort),
            new SortTask("Merge Sort", SortingAlgorithms::mergeSort),
            new SortTask("Quick Sort", SortingAlgorithms::quickSort),
            new SortTask("Heap Sort", SortingAlgorithms::heapSort)
    );

    private PerformanceEvaluator() {
    }

    public static EvaluationResult evaluate(Dataset dataset, int columnIndex) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null.");
        }
        if (columnIndex < 0 || columnIndex >= dataset.getColumnCount()) {
            throw new IllegalArgumentException("The selected column index is invalid.");
        }
        if (!CsvLoader.isNumericColumn(dataset, columnIndex)) {
            throw new IllegalArgumentException("The selected column is not numeric.");
        }

        CsvLoader.Extraction extraction = CsvLoader.extractColumn(dataset, columnIndex);
        Entry[] originalEntries = extraction.entries.clone();
        List<AlgorithmResult> results = new ArrayList<>();
        List<AlgorithmResult> validResults = new ArrayList<>();

        for (SortTask sortTask : SORT_TASKS) {
            Entry[] workingCopy = cloneEntries(originalEntries);
            long startNanos = System.nanoTime();
            sortTask.sorter.sort(workingCopy);
            long durationNanos = System.nanoTime() - startNanos;

            String explanation;
            boolean correct = isCorrectResult(originalEntries, workingCopy);
            if (correct) {
                explanation = "Correct result";
            } else {
                explanation = describeFailure(originalEntries, workingCopy);
            }

            AlgorithmResult result = new AlgorithmResult(
                    sortTask.name,
                    durationNanos,
                    correct,
                    explanation,
                    workingCopy
            );
            results.add(result);
            if (correct) {
                validResults.add(result);
            }
        }

        AlgorithmResult winner = null;
        if (!validResults.isEmpty()) {
            winner = Collections.min(validResults, Comparator
                    .comparingLong(AlgorithmResult::getDurationNanos)
                    .thenComparing(AlgorithmResult::getAlgorithmName));
        }

        List<String[]> sortedRows = new ArrayList<>();
        if (winner != null) {
            sortedRows = buildSortedRows(dataset, winner.getSortedEntries());
        }

        return new EvaluationResult(results, winner, sortedRows, extraction.skipped);
    }

    public static List<String[]> buildSortedRows(Dataset dataset, Entry[] sortedEntries) {
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset must not be null.");
        }
        if (sortedEntries == null) {
            throw new IllegalArgumentException("Sorted entries must not be null.");
        }

        List<String[]> rows = new ArrayList<>(sortedEntries.length);
        for (Entry entry : sortedEntries) {
            if (entry == null) {
                throw new IllegalArgumentException("Sorted entries must not contain null values.");
            }
            if (entry.rowIndex < 0 || entry.rowIndex >= dataset.getRows().size()) {
                throw new IllegalArgumentException("Encountered a row index outside the dataset bounds.");
            }
            rows.add(dataset.getRows().get(entry.rowIndex).clone());
        }
        return rows;
    }

    private static Entry[] cloneEntries(Entry[] source) {
        Entry[] copy = new Entry[source.length];
        for (int i = 0; i < source.length; i++) {
            copy[i] = source[i];
        }
        return copy;
    }

    private static boolean isCorrectResult(Entry[] original, Entry[] sorted) {
        if (original.length != sorted.length) {
            return false;
        }

        double[] originalByRow = new double[findMaxRowIndex(original) + 1];
        Arrays.fill(originalByRow, Double.NaN);
        for (Entry entry : original) {
            originalByRow[entry.rowIndex] = entry.key;
        }

        boolean[] seen = new boolean[originalByRow.length];
        for (Entry entry : sorted) {
            if (entry == null) {
                return false;
            }
            if (entry.rowIndex < 0 || entry.rowIndex >= originalByRow.length) {
                return false;
            }
            if (seen[entry.rowIndex]) {
                return false;
            }
            if (Double.isNaN(originalByRow[entry.rowIndex])) {
                return false;
            }
            if (Double.compare(originalByRow[entry.rowIndex], entry.key) != 0) {
                return false;
            }
            seen[entry.rowIndex] = true;
        }

        for (Entry entry : original) {
            if (!seen[entry.rowIndex]) {
                return false;
            }
        }

        return SortingAlgorithms.isSorted(sorted);
    }

    private static String describeFailure(Entry[] original, Entry[] sorted) {
        if (original.length != sorted.length) {
            return "Result length mismatch.";
        }
        if (!SortingAlgorithms.isSorted(sorted)) {
            return "Returned values are not in ascending order.";
        }
        double[] originalByRow = new double[findMaxRowIndex(original) + 1];
        Arrays.fill(originalByRow, Double.NaN);
        for (Entry entry : original) {
            originalByRow[entry.rowIndex] = entry.key;
        }

        boolean[] seen = new boolean[originalByRow.length];
        for (Entry entry : sorted) {
            if (entry == null) {
                return "Null result entry detected.";
            }
            if (entry.rowIndex < 0 || entry.rowIndex >= originalByRow.length) {
                return "Row index is outside the original dataset.";
            }
            if (seen[entry.rowIndex]) {
                return "Duplicate row detected in the sorted output.";
            }
            if (Double.isNaN(originalByRow[entry.rowIndex])) {
                return "Result contains a row that was not part of the original selection.";
            }
            if (Double.compare(originalByRow[entry.rowIndex], entry.key) != 0) {
                return "A selected row value no longer matches its original row.";
            }
            seen[entry.rowIndex] = true;
        }

        for (Entry entry : original) {
            if (!seen[entry.rowIndex]) {
                return "A row was lost during sorting.";
            }
        }
        return "Validation failed.";
    }

    private static int findMaxRowIndex(Entry[] entries) {
        int max = -1;
        for (Entry entry : entries) {
            if (entry != null && entry.rowIndex > max) {
                max = entry.rowIndex;
            }
        }
        return max;
    }

    public static final class AlgorithmResult {
        private final String algorithmName;
        private final long durationNanos;
        private final boolean correct;
        private final String explanation;
        private final Entry[] sortedEntries;

        public AlgorithmResult(String algorithmName, long durationNanos, boolean correct,
                              String explanation, Entry[] sortedEntries) {
            this.algorithmName = algorithmName;
            this.durationNanos = durationNanos;
            this.correct = correct;
            this.explanation = explanation;
            this.sortedEntries = sortedEntries.clone();
        }

        public String getAlgorithmName() {
            return algorithmName;
        }

        public long getDurationNanos() {
            return durationNanos;
        }

        public boolean isCorrect() {
            return correct;
        }

        public String getExplanation() {
            return explanation;
        }

        public Entry[] getSortedEntries() {
            return sortedEntries.clone();
        }

        public String getDisplayTime() {
            double value = durationNanos / 1_000_000.0;
            if (durationNanos < 1_000) {
                return durationNanos + " ns";
            }
            if (durationNanos < 1_000_000) {
                return String.format("%.2f us", durationNanos / 1000.0);
            }
            return String.format("%.3f ms", value);
        }
    }

    public static final class EvaluationResult {
        private final List<AlgorithmResult> results;
        private final AlgorithmResult winner;
        private final List<String[]> sortedRows;
        private final int skippedRows;

        public EvaluationResult(List<AlgorithmResult> results, AlgorithmResult winner,
                                List<String[]> sortedRows, int skippedRows) {
            this.results = Collections.unmodifiableList(new ArrayList<>(results));
            this.winner = winner;
            this.sortedRows = Collections.unmodifiableList(new ArrayList<>(sortedRows));
            this.skippedRows = skippedRows;
        }

        public List<AlgorithmResult> getResults() {
            return results;
        }

        public AlgorithmResult getWinner() {
            return winner;
        }

        public boolean hasWinner() {
            return winner != null;
        }

        public String getWinnerName() {
            return winner == null ? "No valid winner" : winner.getAlgorithmName();
        }

        public List<String[]> getSortedRows() {
            return sortedRows;
        }

        public int getSkippedRows() {
            return skippedRows;
        }
    }

    private static final class SortTask {
        private final String name;
        private final SortFunction sorter;

        private SortTask(String name, SortFunction sorter) {
            this.name = name;
            this.sorter = sorter;
        }
    }

    @FunctionalInterface
    private interface SortFunction {
        void sort(Entry[] entries);
    }
}
