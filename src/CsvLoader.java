import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class CsvLoader {

    
    private static final double NUMERIC_THRESHOLD = 0.9;

    
    public static class Extraction {
        public final Entry[] entries;
        public final int skipped; 

        public Extraction(Entry[] entries, int skipped) {
            this.entries = entries;
            this.skipped = skipped;
        }
    }

    

    public static Dataset load(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("The selected file does not exist.");
        }
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("Invalid file type. Please select a .csv file.");
        }
        if (file.length() == 0) {
            throw new IllegalArgumentException("The selected file is empty.");
        }

        List<List<String>> records;
        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            records = parse(reader);
        }

        if (records.isEmpty()) {
            throw new IllegalArgumentException("The CSV file contains no data.");
        }

        
        List<String> headers = new ArrayList<>(records.get(0));
        for (int i = 0; i < headers.size(); i++) {
            String h = headers.get(i).trim();
            headers.set(i, h.isEmpty() ? "Column " + (i + 1) : h);
        }
        if (records.size() < 2) {
            throw new IllegalArgumentException("The CSV file has a header but no data rows.");
        }

        
        int cols = headers.size();
        List<String[]> rows = new ArrayList<>(records.size() - 1);
        for (int r = 1; r < records.size(); r++) {
            List<String> rec = records.get(r);
            String[] row = new String[cols];
            for (int c = 0; c < cols; c++) {
                row[c] = c < rec.size() ? rec.get(c).trim() : "";
            }
            rows.add(row);
        }
        return new Dataset(file.getName(), headers, rows);
    }

    
    private static List<List<String>> parse(Reader reader) throws IOException {
        List<List<String>> records = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean fieldStarted = false;

        int ch;
        boolean first = true;
        while ((ch = reader.read()) != -1) {
            if (first) {                      
                first = false;
                if (ch == '\uFEFF') continue;
            }
            char c = (char) ch;
            if (inQuotes) {
                if (c == '"') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next == '"') {         
                        field.append('"');
                    } else {                   
                        inQuotes = false;
                        if (next != -1) {
                            reader.reset();
                        }
                    }
                } else {
                    field.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    fieldStarted = true;
                } else if (c == ',') {
                    current.add(field.toString());
                    field.setLength(0);
                    fieldStarted = true;
                } else                                 if (c == '\n') {
                    if (fieldStarted || field.length() > 0 || !current.isEmpty()) {
                        current.add(field.toString());
                        records.add(current);
                    }
                    current = new ArrayList<>();
                    field.setLength(0);
                    fieldStarted = false;
                } else if (c != '\r') {
                    field.append(c);
                    fieldStarted = true;
                }
            }
        }
        
        if (fieldStarted || field.length() > 0 || !current.isEmpty()) {
            current.add(field.toString());
            records.add(current);
        }
        return records;
    }

    
    public static boolean isNumericColumn(Dataset ds, int col) {
        int nonEmpty = 0, numeric = 0;
        for (String[] row : ds.getRows()) {
            String v = row[col];
            if (v.isEmpty()) continue;
            nonEmpty++;
            if (parseNumber(v) != null) numeric++;
        }
        return nonEmpty > 0 && (double) numeric / nonEmpty >= NUMERIC_THRESHOLD;
    }

    
    private static Double parseNumber(String s) {
        try {
            double d = Double.parseDouble(s.replace(",", "").replace(" ", ""));
            return (Double.isNaN(d) || Double.isInfinite(d)) ? null : d;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    
    public static Extraction extractColumn(Dataset ds, int col) {
        if (!isNumericColumn(ds, col)) {
            throw new IllegalArgumentException(
                "Column '" + ds.getHeaders().get(col) + "' is not numeric. Please select a numeric column.");
        }
        List<Entry> list = new ArrayList<>(ds.getRowCount());
        int skipped = 0;
        for (int i = 0; i < ds.getRowCount(); i++) {
            String v = ds.getRows().get(i)[col];
            Double d = v.isEmpty() ? null : parseNumber(v);
            if (d == null) {
                skipped++;
            } else {
                list.add(new Entry(d, i));
            }
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("The selected column has no valid numeric values.");
        }
        return new Extraction(list.toArray(new Entry[0]), skipped);
    }
}
