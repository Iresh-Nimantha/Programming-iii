import java.io.File;
import java.util.*;

public class SortTest {
    static int fails = 0;
    static void check(boolean ok, String msg) { System.out.println((ok ? "PASS  " : "FAIL  ") + msg); if (!ok) fails++; }

    public static void main(String[] args) throws Exception {
        Random rnd = new Random(1);
        int[] sizes = {0, 1, 2, 3, 10, 1000, 5000};
        for (PerformanceEvaluator.Algorithm alg : PerformanceEvaluator.Algorithm.values()) {
            boolean all = true;
            for (int n : sizes) {
                for (int mode = 0; mode < 5; mode++) {
                    Entry[] a = new Entry[n];
                    double[] ref = new double[n];
                    for (int i = 0; i < n; i++) {
                        double v = switch (mode) { case 0 -> rnd.nextDouble() * 1000 - 500; case 1 -> i; case 2 -> n - i;
                                                   case 3 -> 5; default -> rnd.nextInt(10); };
                        a[i] = new Entry(v, i); ref[i] = v;
                    }
                    Arrays.sort(ref);
                    var res = PerformanceEvaluator.evaluateAll(a, null);
                    for (var r : res) if (r.algorithm == alg) {
                        boolean ok = r.correct && r.sortedData.length == n;
                        for (int i = 0; ok && i < n; i++) ok = r.sortedData[i].key == ref[i];
                        boolean[] seen = new boolean[n];
                        for (Entry e : r.sortedData) seen[e.rowIndex] = true;
                        for (boolean s : seen) ok &= s;
                        all &= ok;
                    }
                }
            }
            check(all, alg.displayName + " correct (sizes, sorted/reverse/duplicates/random)");
        }

        Dataset ds = CsvLoader.load(new File("sample/students_sample.csv"));
        check(ds.getRowCount() == 2000 && ds.getColumnCount() == 5, "CSV loaded 2000 rows x 5 cols (quoted names parsed)");
        check(!CsvLoader.isNumericColumn(ds, 1) && CsvLoader.isNumericColumn(ds, 4), "Name non-numeric, Salary numeric");
        var ex = CsvLoader.extractColumn(ds, 4);
        check(ex.skipped == 40 && ex.entries.length == 1960, "Empty Salary cells skipped (" + ex.skipped + ")");
        try { CsvLoader.extractColumn(ds, 1); check(false, "non-numeric rejected"); } catch (IllegalArgumentException e) { check(true, "Non-numeric column rejected: " + e.getMessage()); }
        try { CsvLoader.load(new File("README.md")); check(false, "wrong ext"); } catch (Exception e) { check(true, "Non-CSV rejected: " + e.getMessage()); }
        java.nio.file.Files.writeString(new File("out/empty.csv").toPath(), "");
        try { CsvLoader.load(new File("out/empty.csv")); check(false, "empty"); } catch (Exception e) { check(true, "Empty file rejected: " + e.getMessage()); }
        java.nio.file.Files.writeString(new File("out/hdr.csv").toPath(), "a,b\n");
        try { CsvLoader.load(new File("out/hdr.csv")); check(false, "hdr"); } catch (Exception e) { check(true, "Header-only rejected: " + e.getMessage()); }

        Entry[] big = new Entry[100000];
        for (int i = 0; i < big.length; i++) big[i] = new Entry(rnd.nextDouble(), i);
        for (var r : PerformanceEvaluator.evaluateAll(big, null)) System.out.printf("  100k: %-15s %10.2f ms correct=%b%n", r.algorithm.displayName, r.millis(), r.correct);
        System.out.println(fails == 0 ? "ALL TESTS PASSED" : fails + " FAILED");
    }
}
