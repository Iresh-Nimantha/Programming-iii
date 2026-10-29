// Tests for the sorting algorithms.
public final class SortingAlgorithmsTest {
    private static final String[] ALGORITHM_NAMES = {
        "insertion", "shell", "merge", "quick", "heap"
    };

    private SortingAlgorithmsTest() {
    }

    public static void main(String[] args) {
        for (int algorithm = 0; algorithm < ALGORITHM_NAMES.length; algorithm++) {
            runCases(algorithm);
        }
        System.out.println("All sorting algorithm tests passed.");
    }

    private static void runCases(int algorithm) {
        assertSorted(algorithm, new double[0]);
        assertSorted(algorithm, new double[] {42.5});
        assertSorted(algorithm, new double[] {-3, -1, 0, 2.5, 9});
        assertSorted(algorithm, new double[] {9, 2.5, 0, -1, -3});
        assertSorted(algorithm, new double[] {4, 1, 4, -2, 1, 4, 0});
        assertSorted(algorithm, new double[] {7, 7, 7, 7, 7});

        double[] large = new double[5000];
        for (int i = 0; i < large.length; i++) {
            large[i] = ((i * 37) % 101) - 50.5;
        }
        assertSorted(algorithm, large);
    }

    private static void assertSorted(int algorithm, double[] keys) {
        Entry[] entries = new Entry[keys.length];
        for (int i = 0; i < keys.length; i++) {
            entries[i] = new Entry(keys[i], i);
        }
        double[] expected = expectedKeys(keys);
        SortingFunction sorter = sorterFor(algorithm);
        sorter.sort(entries);
        check(SortingAlgorithms.isSorted(entries),
                ALGORITHM_NAMES[algorithm] + " did not sort the array");
        check(entries.length == keys.length,
                ALGORITHM_NAMES[algorithm] + " changed the array length");
        for (int i = 0; i < entries.length; i++) {
            check(Double.compare(entries[i].key, expected[i]) == 0,
                    ALGORITHM_NAMES[algorithm] + " produced an unexpected order");
        }
        checkPermutation(entries, keys);
    }

    private static double[] expectedKeys(double[] keys) {
        double[] expected = keys.clone();
        for (int i = 0; i < expected.length - 1; i++) {
            int minimum = i;
            for (int j = i + 1; j < expected.length; j++) {
                if (Double.compare(expected[j], expected[minimum]) < 0) {
                    minimum = j;
                }
            }
            double temporary = expected[i];
            expected[i] = expected[minimum];
            expected[minimum] = temporary;
        }
        return expected;
    }

    private static void checkPermutation(Entry[] entries, double[] originalKeys) {
        boolean[] seen = new boolean[originalKeys.length];
        for (Entry entry : entries) {
            check(entry.rowIndex >= 0 && entry.rowIndex < originalKeys.length,
                    "row index moved outside the original dataset");
            check(!seen[entry.rowIndex], "an original row was duplicated");
            check(Double.compare(entry.key, originalKeys[entry.rowIndex]) == 0,
                    "value became disconnected from its original row index");
            seen[entry.rowIndex] = true;
        }
        for (boolean wasSeen : seen) {
            check(wasSeen, "an original row was lost");
        }
    }

    private static SortingFunction sorterFor(int algorithm) {
        switch (algorithm) {
            case 0: return SortingAlgorithms::insertionSort;
            case 1: return SortingAlgorithms::shellSort;
            case 2: return SortingAlgorithms::mergeSort;
            case 3: return SortingAlgorithms::quickSort;
            case 4: return SortingAlgorithms::heapSort;
            default: throw new AssertionError("Unknown algorithm");
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    @FunctionalInterface
    private interface SortingFunction {
        void sort(Entry[] entries);
    }
}
