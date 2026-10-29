import java.util.Arrays;
import java.util.List;

public final class PerformanceEvaluatorTest {
    private PerformanceEvaluatorTest() {
    }

    public static void main(String[] args) {
        Dataset dataset = new Dataset(
                "test.csv",
                Arrays.asList("Name", "Age", "Salary"),
                new java.util.ArrayList<>(Arrays.asList(
                        new String[] {"Amal", "25", "50000"},
                        new String[] {"Nimal", "30", "40000"},
                        new String[] {"Kamal", "22", "60000"}
                ))
        );

        PerformanceEvaluator.EvaluationResult result = PerformanceEvaluator.evaluate(dataset, 2);
        assertResultCount(result, 5, "Expected one benchmark result per sorting algorithm");
        assertWinnerIsPresent(result, "Expected a valid winner from the correct results");
        assertSortedRowsPreserveRows(result, dataset, "Sorted rows should keep the original row structure");

        Dataset partialNumericDataset = new Dataset(
                "partial.csv",
                Arrays.asList("Name", "Value"),
                new java.util.ArrayList<>(Arrays.asList(
                        new String[] {"A", "8"},
                        new String[] {"B", "not-a-number"},
                        new String[] {"C", "3"},
                        new String[] {"D", "2"},
                        new String[] {"E", "9"},
                        new String[] {"F", "1"},
                        new String[] {"G", "4"},
                        new String[] {"H", "7"},
                        new String[] {"I", "6"},
                        new String[] {"J", "5"}
                ))
        );
        PerformanceEvaluator.EvaluationResult partialResult =
                PerformanceEvaluator.evaluate(partialNumericDataset, 1);
        if (partialResult.getSkippedRows() != 1 || partialResult.getSortedRows().size() != 9) {
            throw new AssertionError("Invalid numeric values should be counted and excluded from sorted rows.");
        }

        Dataset invalidDataset = new Dataset(
                "invalid.csv",
                Arrays.asList("Value"),
                new java.util.ArrayList<>(Arrays.asList(
                        new String[] {"five"},
                        new String[] {"ten"}
                ))
        );
        try {
            PerformanceEvaluator.evaluate(invalidDataset, 0);
            throw new AssertionError("Invalid numeric column should be rejected.");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        System.out.println("All performance evaluator tests passed.");
    }

    private static void assertResultCount(PerformanceEvaluator.EvaluationResult result, int expected, String message) {
        if (result.getResults().size() != expected) {
            throw new AssertionError(message);
        }
    }

    private static void assertWinnerIsPresent(PerformanceEvaluator.EvaluationResult result, String message) {
        if (result.getWinner() == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertSortedRowsPreserveRows(PerformanceEvaluator.EvaluationResult result, Dataset dataset, String message) {
        List<String[]> sortedRows = result.getSortedRows();
        if (sortedRows.size() != dataset.getRowCount()) {
            throw new AssertionError(message);
        }
        String[] first = sortedRows.get(0);
        if (!"Nimal".equals(first[0]) || !"40000".equals(first[2])) {
            throw new AssertionError(message);
        }
    }
}
