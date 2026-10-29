import java.util.List;

public class Dataset {
    private final String fileName;
    private final List<String> headers;
    private final List<String[]> rows;

    public Dataset(String fileName, List<String> headers, List<String[]> rows) {
        this.fileName = fileName;
        this.headers = headers;
        this.rows = rows;
    }

    public String getFileName()        { return fileName; }
    public List<String> getHeaders()   { return headers; }
    public List<String[]> getRows()    { return rows; }
    public int getColumnCount()        { return headers.size(); }
    public int getRowCount()           { return rows.size(); }
}
