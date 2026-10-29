import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PerformanceEvaluator {

    public enum Algorithm {
        INSERTION("Insertion Sort", SortingAlgorithms::insertionSort),
        SHELL("Shell Sort", SortingAlgorithms::shellSort),
        MERGE("Merge Sort", SortingAlgorithms::mergeSort),
        QUICK("Quick Sort", SortingAlgorithms::quickSort),
        HEAP("Heap Sort", SortingAlgorithms::heapSort);

        public final String displayName;
        private final Consumer<Entry[]> sorter;

        Algorithm(String displayName, Consumer<Entry[]> sorter) {
            this.displayName = displayName;
            this.sorter = sorter;
        }
    }

    public static class Result {
        public final Algorithm algorithm;
        public final long nanos;
        public final boolean correct;
        public final Entry[] sortedData;

        Result(Algorithm algorithm, long nanos, boolean correct, Entry[] sortedData) {
            this.algorithm = algorithm;
            this.nanos = nanos;
            this.correct = correct;
            this.sortedData = sortedData;
        }

        public double millis() { return nanos / 1_000_000.0; }
    }

    public static List<Result> evaluateAll(Entry[] original, Consumer<String> progress) {
        List<Result> results = new ArrayList<>();
        for (Algorithm alg : Algorithm.values()) {
            if (progress != null) progress.accept("Running " + alg.displayName + "...");
            Entry[] copy = original.clone();

            long start = System.nanoTime();
            alg.sorter.accept(copy);
            long elapsed = System.nanoTime() - start;

            results.add(new Result(alg, elapsed, SortingAlgorithms.isSorted(copy), copy));
        }
        return results;
    }

    public static Result findBest(List<Result> results) {
        Result best = null;
        for (Result r : results) {
            if (!r.correct) continue;
            if (best == null || r.nanos < best.nanos) best = r;
        }
        return best;
    }
}
