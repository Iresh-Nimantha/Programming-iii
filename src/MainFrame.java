import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 * Swing GUI for sorting algorithm benchmarking and result inspection.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final JFileChooser fileChooser = new JFileChooser();
    private final JLabel fileLabel = new JLabel("No file selected");
    private final JLabel summaryLabel = new JLabel("Dataset summary: no file loaded");
    private final JLabel fastestLabel = new JLabel("Fastest correct algorithm: waiting for a run");
    private final JComboBox<String> columnSelector = new JComboBox<>();
    private final JButton loadButton = new JButton("Upload CSV");
    private final JButton runButton = new JButton("Run Sorting");
    private final JButton clearButton = new JButton("Clear/Reset");
    private final JTextArea statusArea = new JTextArea(5, 40);
    private final JTable previewTable = new JTable();
    private final JTable resultsTable = new JTable();
    private final JTable sortedTable = new JTable();

    private Dataset currentDataset;
    private int selectedColumn = -1;
    private SwingWorker<PerformanceEvaluator.EvaluationResult, Void> benchmarkWorker;

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluation");
        configureLookAndFeel();
        initializeLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1200, 840));
        pack();
        setLocationRelativeTo(null);
        setStatus("Ready. Upload a CSV file to begin.");
        updateRunButtonState();
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Ignore look-and-feel issues because the app still works with Swing defaults.
        }
    }

    private void initializeLayout() {
        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        JPanel uploadPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        uploadPanel.add(loadButton);
        uploadPanel.add(fileLabel);
        fileLabel.setFont(fileLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(uploadPanel);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        summaryPanel.add(summaryLabel);
        topPanel.add(summaryPanel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        columnSelector.setPrototypeDisplayValue("Salary");
        columnSelector.addActionListener(this::handleColumnSelection);
        controlPanel.add(new JLabel("Numeric column:"));
        controlPanel.add(columnSelector);
        controlPanel.add(runButton);
        controlPanel.add(clearButton);
        topPanel.add(controlPanel);

        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        fastestLabel.setFont(fastestLabel.getFont().deriveFont(Font.BOLD, 14f));
        fastestLabel.setForeground(new Color(0, 90, 180));
        resultPanel.add(fastestLabel);
        resultPanel.add(Box.createVerticalStrut(8));

        JLabel previewTitle = new JLabel("Dataset preview");
        previewTitle.setFont(previewTitle.getFont().deriveFont(Font.BOLD));
        resultPanel.add(previewTitle);
        resultPanel.add(new JScrollPane(previewTable));

        JPanel lowerPanel = new JPanel(new BorderLayout(12, 12));
        lowerPanel.add(resultPanel, BorderLayout.NORTH);

        JPanel benchmarkPanel = new JPanel(new BorderLayout());
        benchmarkPanel.setBorder(BorderFactory.createTitledBorder("Benchmark results"));
        benchmarkPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
        lowerPanel.add(benchmarkPanel, BorderLayout.CENTER);

        JPanel sortedPanel = new JPanel(new BorderLayout());
        sortedPanel.setBorder(BorderFactory.createTitledBorder("Sorted dataset"));
        sortedPanel.add(new JScrollPane(sortedTable), BorderLayout.CENTER);
        lowerPanel.add(sortedPanel, BorderLayout.SOUTH);

        root.add(topPanel, BorderLayout.NORTH);
        root.add(lowerPanel, BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        statusArea.setBackground(new Color(245, 245, 245));
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        root.add(statusPanel, BorderLayout.SOUTH);

        addActionListeners();
        setContentPane(root);
    }

    private void addActionListeners() {
        loadButton.addActionListener(this::loadDatasetAction);
        runButton.addActionListener(this::runBenchmarkAction);
        clearButton.addActionListener(this::clearAction);
    }

    private void loadDatasetAction(ActionEvent event) {
        int option = fileChooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = fileChooser.getSelectedFile();
        if (selected == null) {
            setError("No file was selected.");
            return;
        }

        try {
            Dataset dataset = CsvLoader.load(selected);
            currentDataset = dataset;
            selectedColumn = -1;
            setStatus("Loaded file: " + selected.getName());
            fileLabel.setText(selected.getName());
            summaryLabel.setText("Dataset summary: " + dataset.getRowCount() + " rows, "
                    + dataset.getColumnCount() + " columns");
            updatePreviewTable(dataset);
            populateColumnSelector(dataset);
            populateResultTable(new String[][] {}, new String[] {"Algorithm", "Execution time", "Correctness", "Performance result"});
            showSortedRows(new ArrayList<>());
            fastestLabel.setText("Fastest correct algorithm: waiting for a run");
        } catch (Exception ex) {
            setError(ex.getMessage());
            currentDataset = null;
            selectedColumn = -1;
            clearTables();
            fileLabel.setText("No file selected");
            summaryLabel.setText("Dataset summary: no file loaded");
            populateColumnSelector(null);
        }
        updateRunButtonState();
    }

    private void populateColumnSelector(Dataset dataset) {
        columnSelector.removeAllItems();
        if (dataset == null) {
            columnSelector.addItem("No dataset loaded");
            columnSelector.setEnabled(false);
            return;
        }

        List<String> names = new ArrayList<>();
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            if (CsvLoader.isNumericColumn(dataset, i)) {
                names.add(dataset.getHeaders().get(i));
            }
        }

        if (names.isEmpty()) {
            columnSelector.addItem("No numeric columns");
            columnSelector.setEnabled(false);
            selectedColumn = -1;
            setStatus("No numeric columns were found in the selected file.");
            return;
        }

        for (String name : names) {
            columnSelector.addItem(name);
        }
        columnSelector.setEnabled(true);
        selectedColumn = 0;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            if (CsvLoader.isNumericColumn(dataset, i)) {
                selectedColumn = i;
                break;
            }
        }
        setStatus("Numeric columns loaded. Select a column to benchmark.");
    }

    private void handleColumnSelection(ActionEvent event) {
        if (currentDataset == null) {
            return;
        }
        String selected = (String) columnSelector.getSelectedItem();
        if (selected == null || "No dataset loaded".equals(selected) || "No numeric columns".equals(selected)) {
            selectedColumn = -1;
            updateRunButtonState();
            return;
        }

        for (int i = 0; i < currentDataset.getColumnCount(); i++) {
            if (currentDataset.getHeaders().get(i).equals(selected) && CsvLoader.isNumericColumn(currentDataset, i)) {
                selectedColumn = i;
                setStatus("Selected column: " + selected);
                updateRunButtonState();
                return;
            }
        }

        selectedColumn = -1;
        updateRunButtonState();
    }

    private void runBenchmarkAction(ActionEvent event) {
        if (currentDataset == null || selectedColumn < 0 || benchmarkWorker != null && !benchmarkWorker.isDone()) {
            setError("Please load a valid dataset and choose a numeric column before running the benchmark.");
            return;
        }

        setStatus("Running sorting benchmark on column '" + currentDataset.getHeaders().get(selectedColumn) + "'...");
        setControlsEnabled(false);
        fastestLabel.setText("Fastest correct algorithm: running...");

        benchmarkWorker = new SwingWorker<PerformanceEvaluator.EvaluationResult, Void>() {
            @Override
            protected PerformanceEvaluator.EvaluationResult doInBackground() throws Exception {
                return PerformanceEvaluator.evaluate(currentDataset, selectedColumn);
            }

            @Override
            protected void done() {
                try {
                    PerformanceEvaluator.EvaluationResult result = get();
                    updateBenchmarkResults(result);
                    if (result.hasWinner()) {
                        fastestLabel.setText("Fastest correct algorithm for this run: " + result.getWinnerName());
                    } else {
                        fastestLabel.setText("No valid winner: all algorithms failed validation.");
                    }
                    setStatus("Benchmark complete.");
                } catch (Exception ex) {
                    setError("Benchmark failed: " + ex.getMessage());
                    fastestLabel.setText("Fastest correct algorithm: unavailable");
                } finally {
                    setControlsEnabled(true);
                    benchmarkWorker = null;
                }
            }
        };
        benchmarkWorker.execute();
    }

    private void updateBenchmarkResults(PerformanceEvaluator.EvaluationResult result) {
        List<PerformanceEvaluator.AlgorithmResult> algorithmResults = result.getResults();
        Object[][] data = new Object[algorithmResults.size()][4];
        for (int i = 0; i < algorithmResults.size(); i++) {
            PerformanceEvaluator.AlgorithmResult algorithmResult = algorithmResults.get(i);
            data[i][0] = algorithmResult.getAlgorithmName();
            data[i][1] = algorithmResult.getDisplayTime();
            data[i][2] = algorithmResult.isCorrect() ? "Correct" : "Incorrect";
            data[i][3] = algorithmResult.isCorrect() ? algorithmResult.getExplanation() : "Excluded: " + algorithmResult.getExplanation();
        }
        populateResultTable(data, new String[] {"Algorithm", "Execution time", "Correctness", "Performance result"});
        showSortedRows(result.getSortedRows());
    }

    private void clearAction(ActionEvent event) {
        currentDataset = null;
        selectedColumn = -1;
        fileLabel.setText("No file selected");
        summaryLabel.setText("Dataset summary: no file loaded");
        fastestLabel.setText("Fastest correct algorithm: waiting for a run");
        populateColumnSelector(null);
        clearTables();
        setStatus("Cleared. Upload a CSV file to continue.");
        updateRunButtonState();
    }

    private void clearTables() {
        updatePreviewTable(null);
        populateResultTable(new String[][] {}, new String[] {"Algorithm", "Execution time", "Correctness", "Performance result"});
        showSortedRows(new ArrayList<>());
    }

    private void updatePreviewTable(Dataset dataset) {
        if (dataset == null) {
            previewTable.setModel(new DefaultTableModel(new Object[][] {}, new Object[] {"No data"}));
            return;
        }

        List<String> headers = dataset.getHeaders();
        List<String[]> rows = dataset.getRows();
        int maxRows = Math.min(rows.size(), 20);
        Object[][] previewRows = new Object[maxRows][headers.size()];
        for (int row = 0; row < maxRows; row++) {
            String[] sourceRow = rows.get(row);
            for (int col = 0; col < headers.size(); col++) {
                previewRows[row][col] = sourceRow[col];
            }
        }
        previewTable.setModel(new DefaultTableModel(previewRows, headers.toArray(new String[0])));
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void populateResultTable(Object[][] rows, String[] headers) {
        resultsTable.setModel(new DefaultTableModel(rows, headers));
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    }

    private void showSortedRows(List<String[]> rows) {
        if (currentDataset == null || currentDataset.getHeaders().isEmpty()) {
            sortedTable.setModel(new DefaultTableModel(new Object[][] {}, new Object[] {"No result"}));
            return;
        }

        Object[][] data = new Object[rows.size()][currentDataset.getHeaders().size()];
        for (int row = 0; row < rows.size(); row++) {
            String[] source = rows.get(row);
            for (int col = 0; col < currentDataset.getHeaders().size(); col++) {
                data[row][col] = source[col];
            }
        }
        sortedTable.setModel(new DefaultTableModel(data, currentDataset.getHeaders().toArray(new String[0])));
        sortedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void setControlsEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
        columnSelector.setEnabled(enabled && currentDataset != null && columnSelector.getItemCount() > 0 && selectedColumn >= 0);
        runButton.setEnabled(enabled && currentDataset != null && selectedColumn >= 0);
        clearButton.setEnabled(enabled);
    }

    private void updateRunButtonState() {
        boolean canRun = currentDataset != null && selectedColumn >= 0 && (columnSelector.getSelectedItem() != null)
                && !("No dataset loaded".equals(columnSelector.getSelectedItem()))
                && !("No numeric columns".equals(columnSelector.getSelectedItem()));
        runButton.setEnabled(canRun);
    }

    private void setStatus(String message) {
        statusArea.setText(message);
    }

    private void setError(String message) {
        statusArea.setText("Error: " + message);
        statusArea.setForeground(Color.RED);
        JOptionPane.showMessageDialog(this, message, "Sorting App Error", JOptionPane.ERROR_MESSAGE);
    }
}
