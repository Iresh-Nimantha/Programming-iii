/**
 * Entry.java
 * One sortable item: the numeric value taken from the selected column
 * plus the index of the CSV row it came from. Keeping the row index lets us
 * show the WHOLE row in sorted order, not just the sorted column.
 */
public class Entry {
    public final double key;   // numeric value from the selected column
    public final int rowIndex; // index of the original row in the dataset

    public Entry(double key, int rowIndex) {
        this.key = key;
        this.rowIndex = rowIndex;
    }
}
