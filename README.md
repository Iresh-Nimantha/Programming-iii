
# Stage 1 — Member 01

**Responsibility:** CSV file validation/loading, dataset representation, numeric-column detection and extracting numeric values while retaining original row indexes.

Files: `Entry.java`, `Dataset.java`, `CsvLoader.java`, `Member1Demo.java`.

Run from this folder:

```bat
javac -d out src\*.java
java -cp out Member1Demo sample\students_sample.csv
```

`CsvLoader.load(File)` validates the file and parses headers/rows. `isNumericColumn(Dataset, int)` identifies numeric columns. `extractColumn(Dataset, int)` returns `Entry[]` and a skipped-row count. `Entry.rowIndex` allows the later GUI to display complete CSV rows in sorted order.


