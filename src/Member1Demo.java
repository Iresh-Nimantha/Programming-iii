import java.io.File;

/** Small console demo for Member 1's CSV loading and numeric-column detection. */
public class Member1Demo {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java Member1Demo <path-to-csv>");
            return;
        }
        Dataset dataset = CsvLoader.load(new File(args[0]));
        System.out.printf("Loaded %s: %d rows, %d columns%n",
                dataset.getFileName(), dataset.getRowCount(), dataset.getColumnCount());
        for (int c = 0; c < dataset.getColumnCount(); c++) {
            System.out.printf("%d: %-24s %s%n", c, dataset.getHeaders().get(c),
                    CsvLoader.isNumericColumn(dataset, c) ? "numeric" : "non-numeric");
        }
        System.out.println("\nFirst five rows:");
        for (int r = 0; r < Math.min(5, dataset.getRowCount()); r++) {
            System.out.println(String.join(" | ", dataset.getRows().get(r)));
        }
        for (int c = 0; c < dataset.getColumnCount(); c++) {
            if (CsvLoader.isNumericColumn(dataset, c)) {
                CsvLoader.Extraction ex = CsvLoader.extractColumn(dataset, c);
                System.out.printf("\nExample extraction: %s -> %d numeric values, %d skipped rows%n",
                        dataset.getHeaders().get(c), ex.entries.length, ex.skipped);
                break;
            }
        }
    }
}
