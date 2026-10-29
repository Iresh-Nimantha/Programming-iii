import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class MainFrame extends JFrame {

    private static final int PREVIEW_ROWS = 1000;
    private static final int INSERTION_WARN_SIZE = 50_000;

    private final JButton uploadButton = new JButton("Upload CSV");
    private final JButton sortButton = new JButton("Run Sorting");
    private final JLabel fileLabel = new JLabel("No file selected");
    private final JComboBox<String> columnBox = new JComboBox<>();
    private final JLabel statusLabel = new JLabel("Upload a .csv file to begin.");
    private final JLabel bestLabel = new JLabel(" ");
    private final JProgressBar progressBar = new JProgressBar();

    private final DefaultTableModel resultModel = new DefaultTableModel(
            new String[]{"Rank", "Algorithm", "Time (ms)", "Time (ns)", "Sorted correctly?"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable resultTable = new JTable(resultModel);

    private final DefaultTableModel dataModel = new DefaultTableModel() {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable dataTable = new JTable(dataModel);
    private final JLabel dataTitle = new JLabel("Dataset preview");

    private Dataset dataset;
    private boolean[] numericColumn;
    private int bestRowInTable = -1;

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        buildUi();
        wireEvents();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBorder(BorderFactory.createTitledBorder("1. Dataset & column"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        g.gridx = 0; g.gridy = 0;
        controls.add(uploadButton, g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        controls.add(fileLabel, g);

        g.gridx = 0; g.gridy = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        controls.add(new JLabel("Column to sort:"), g);
        g.gridx = 1; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        controls.add(columnBox, g);
        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        controls.add(sortButton, g);

        columnBox.setEnabled(false);
        sortButton.setEnabled(false);
        sortButton.setFont(sortButton.getFont().deriveFont(Font.BOLD));

        resultTable.setRowHeight(24);
        resultTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                if (!sel) {
                    boolean best = row == bestRowInTable;
                    c.setBackground(best ? new Color(200, 240, 200) : Color.WHITE);
                    c.setFont(c.getFont().deriveFont(best ? Font.BOLD : Font.PLAIN));
                }
                return c;
            }
        });
        JScrollPane resultScroll = new JScrollPane(resultTable);
        resultScroll.setPreferredSize(new Dimension(100, 170));

        bestLabel.setFont(bestLabel.getFont().deriveFont(Font.BOLD, 15f));
        bestLabel.setForeground(new Color(0, 110, 0));

        JPanel resultPanel = new JPanel(new BorderLayout(4, 4));
        resultPanel.setBorder(BorderFactory.createTitledBorder("2. Performance evaluation"));
        resultPanel.add(resultScroll, BorderLayout.CENTER);
        resultPanel.add(bestLabel, BorderLayout.SOUTH);

        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JPanel dataPanel = new JPanel(new BorderLayout(4, 4));
        dataPanel.setBorder(BorderFactory.createTitledBorder("3. Data"));
        dataPanel.add(dataTitle, BorderLayout.NORTH);
        dataPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.add(resultPanel, BorderLayout.NORTH);
        center.add(dataPanel, BorderLayout.CENTER);

        JPanel status = new JPanel(new BorderLayout(8, 0));
        status.add(statusLabel, BorderLayout.CENTER);
        progressBar.setPreferredSize(new Dimension(180, 16));
        progressBar.setVisible(false);
        status.add(progressBar, BorderLayout.EAST);

        root.add(controls, BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(status, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        uploadButton.addActionListener(e -> chooseFile());
        sortButton.addActionListener(e -> runSorting());
    }

    private void chooseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a CSV file");
        chooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try {
            Dataset ds = CsvLoader.load(file);
            dataset = ds;
            numericColumn = new boolean[ds.getColumnCount()];

            columnBox.removeAllItems();
            int firstNumeric = -1;
            for (int c = 0; c < ds.getColumnCount(); c++) {
                numericColumn[c] = CsvLoader.isNumericColumn(ds, c);
                if (numericColumn[c] && firstNumeric < 0) firstNumeric = c;
                columnBox.addItem(ds.getHeaders().get(c) + (numericColumn[c] ? "  (numeric)" : "  (non-numeric)"));
            }
            columnBox.setEnabled(true);
            sortButton.setEnabled(true);
            if (firstNumeric >= 0) columnBox.setSelectedIndex(firstNumeric);

            fileLabel.setText(ds.getFileName() + "  -  " + ds.getRowCount() + " rows, "
                    + ds.getColumnCount() + " columns");
            clearResults();
            showRows(ds, null, "Dataset preview (original order)");
            statusLabel.setText(firstNumeric >= 0
                    ? "File loaded. Select a numeric column and click 'Run Sorting'."
                    : "File loaded, but it has no numeric column - sorting is not possible.");
        } catch (Exception ex) {
            showError("Could not load file", ex.getMessage());
        }
    }

    private void runSorting() {
        if (dataset == null) {
            showError("No dataset", "Please upload a CSV file first.");
            return;
        }
        final int col = columnBox.getSelectedIndex();
        if (col < 0 || !numericColumn[col]) {
            showError("Invalid column", "Please select a numeric column to sort.");
            return;
        }

        final CsvLoader.Extraction extraction;
        try {
            extraction = CsvLoader.extractColumn(dataset, col);
        } catch (Exception ex) {
            showError("Cannot sort column", ex.getMessage());
            return;
        }

        if (extraction.entries.length > INSERTION_WARN_SIZE) {
            int ans = JOptionPane.showConfirmDialog(this,
                    "This column has " + extraction.entries.length + " values.\n"
                  + "Insertion Sort is O(n^2) and may take a long time.\nContinue?",
                    "Large dataset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ans != JOptionPane.YES_OPTION) return;
        }

        setBusy(true);
        clearResults();

        new SwingWorker<List<PerformanceEvaluator.Result>, String>() {
            @Override
            protected List<PerformanceEvaluator.Result> doInBackground() {
                return PerformanceEvaluator.evaluateAll(extraction.entries, msg -> publish(msg));
            }

            @Override
            protected void process(List<String> chunks) {
                statusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                setBusy(false);
                try {
                    showResults(get(), col, extraction);
                } catch (Exception ex) {
                    showError("Sorting failed", String.valueOf(ex.getCause() != null ? ex.getCause() : ex));
                }
            }
        }.execute();
    }

    private void showResults(List<PerformanceEvaluator.Result> results, int col,
                             CsvLoader.Extraction extraction) {
        PerformanceEvaluator.Result best = PerformanceEvaluator.findBest(results);

        List<PerformanceEvaluator.Result> ranked = new java.util.ArrayList<>(results);
        ranked.sort((a, b) -> Long.compare(a.nanos, b.nanos));
        bestRowInTable = ranked.indexOf(best);

        int rank = 1;
        for (PerformanceEvaluator.Result r : ranked) {
            resultModel.addRow(new Object[]{
                    rank++, r.algorithm.displayName,
                    String.format("%.4f", r.millis()),
                    String.format("%,d", r.nanos),
                    r.correct ? "Yes" : "NO - error"});
        }

        if (best != null) {
            bestLabel.setText("Best algorithm: " + best.algorithm.displayName
                    + "  (" + String.format("%.4f", best.millis()) + " ms)");
            showRows(dataset, best.sortedData,
                    "Dataset sorted by '" + dataset.getHeaders().get(col) + "' (ascending)");
        }

        String msg = "Done. Sorted " + extraction.entries.length + " values.";
        if (extraction.skipped > 0) {
            msg += " " + extraction.skipped + " row(s) with empty/invalid values were skipped.";
        }
        statusLabel.setText(msg);
        resultTable.repaint();
    }

    private void showRows(Dataset ds, Entry[] order, String title) {
        dataModel.setColumnIdentifiers(ds.getHeaders().toArray());
        dataModel.setRowCount(0);
        int total = order == null ? ds.getRowCount() : order.length;
        int limit = Math.min(total, PREVIEW_ROWS);
        for (int i = 0; i < limit; i++) {
            String[] row = order == null ? ds.getRows().get(i) : ds.getRows().get(order[i].rowIndex);
            dataModel.addRow(row);
        }
        dataTitle.setText(title + (total > limit ? "  - showing first " + limit + " of " + total + " rows" : ""));
        for (int c = 0; c < dataTable.getColumnCount(); c++) {
            dataTable.getColumnModel().getColumn(c).setPreferredWidth(120);
        }
    }

    private void clearResults() {
        resultModel.setRowCount(0);
        bestRowInTable = -1;
        bestLabel.setText(" ");
    }

    private void setBusy(boolean busy) {
        uploadButton.setEnabled(!busy);
        sortButton.setEnabled(!busy);
        columnBox.setEnabled(!busy);
        progressBar.setIndeterminate(busy);
        progressBar.setVisible(busy);
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
    }

    private void showError(String title, String message) {
        statusLabel.setText(title + ": " + message);
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
}
