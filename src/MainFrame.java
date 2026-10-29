import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/** Swing interface for comparing sorting algorithms on CSV data. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final int LARGE_DATASET_WARNING_THRESHOLD = 50000;

    private static final Color NAVY = new Color(28, 48, 77);
    private static final Color BLUE = new Color(45, 105, 170);
    private static final Color BACKGROUND = new Color(244, 247, 251);
    private static final Color BORDER = new Color(218, 226, 236);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color SUCCESS = new Color(31, 123, 77);
    private static final Color SUCCESS_BACKGROUND = new Color(226, 246, 234);
    private static final Color ERROR = new Color(180, 55, 55);
    private static final Color WARNING = new Color(165, 105, 20);

    private final JFileChooser fileChooser = new JFileChooser();
    private final JLabel fileNameLabel = new JLabel("No file selected");
    private final JLabel fileStatusLabel = new JLabel("Choose a CSV file to begin");
    private final JLabel rowCountLabel = new JLabel("Records Loaded: —");
    private final JLabel selectedColumnLabel = new JLabel("Numeric Column: —");
    private final JLabel algorithmCountLabel = new JLabel("Algorithms Tested: 5");
    private final JLabel bestAlgorithmLabel = new JLabel("Run an analysis to see the fastest correct algorithm.");
    private final JLabel bestTimeLabel = new JLabel("—");
    private final JLabel bestSummaryLabel = new JLabel("Results are measured for the selected dataset and column.");
    private final JLabel statusLabel = new JLabel("Ready. Choose a CSV dataset to begin.");
    private final JLabel progressLabel = new JLabel(" ");
    private final JLabel selectedColumnCaption = new JLabel("Selected Column: —");
    private final JComboBox<ColumnOption> columnSelector = new JComboBox<>();
    private final JButton loadButton = new RoundedButton("Choose CSV File");
    private final JButton runButton = new RoundedButton("\u25B6  Run Performance Analysis");
    private final JButton clearButton = new RoundedButton("Reset");
    private final JProgressBar progressBar = new JProgressBar();
    private final JTable previewTable = new JTable();
    private final JTable resultsTable = new JTable();
    private final JTable sortedTable = new JTable();
    private final JTextArea eventArea = new JTextArea(2, 20);

    private Dataset currentDataset;
    private int selectedColumn = -1;
    private SwingWorker<PerformanceEvaluator.EvaluationResult, Void> benchmarkWorker;

    public MainFrame() {
        super("Sorting Algorithm Performance Evaluator");
        configureLookAndFeel();
        initializeLayout();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setStatus("Ready. Choose a CSV dataset to begin.", MUTED);
        updateRunButtonState();
    }

    private void configureLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception exception) {
            System.err.println("Could not apply the system look and feel: " + exception.getMessage());
        }
    }

    private void initializeLayout() {
        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(BACKGROUND);
        root.setBorder(BorderFactory.createEmptyBorder(14, 18, 12, 18));

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(createHeader());
        north.add(Box.createVerticalStrut(10));
        north.add(createFileAndSelectionPanel());

        root.add(north, BorderLayout.NORTH);
        root.add(createMainContent(), BorderLayout.CENTER);
        root.add(createFooter(), BorderLayout.SOUTH);

        addActionListeners();
        setContentPane(root);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(12, 4));
        panel.setBackground(NAVY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel title = new JLabel("Sorting Algorithm Performance Evaluator");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));
        JLabel subtitle = new JLabel("Upload a CSV dataset, select a numeric column, compare sorting algorithm performance, and view sorted results.");
        subtitle.setForeground(new Color(220, 230, 242));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        labels.add(title);
        labels.add(Box.createVerticalStrut(5));
        labels.add(subtitle);
        panel.add(labels, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFileAndSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        JPanel fileCard = createCard("FILE", new BorderLayout(12, 6));
        styleButton(loadButton, false);
        loadButton.setIcon(UIManager.getIcon("FileView.fileIcon"));
        fileCard.add(loadButton, BorderLayout.WEST);
        JPanel fileInfo = new JPanel();
        fileInfo.setOpaque(false);
        fileInfo.setLayout(new BoxLayout(fileInfo, BoxLayout.Y_AXIS));
        fileNameLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        fileStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fileInfo.add(fileNameLabel);
        fileInfo.add(Box.createVerticalStrut(4));
        fileInfo.add(fileStatusLabel);
        fileCard.add(fileInfo, BorderLayout.CENTER);

        JPanel columnCard = createCard("COLUMN SELECTION", new BorderLayout(10, 4));
        JPanel selection = new JPanel(new BorderLayout(6, 4));
        selection.setOpaque(false);
        columnSelector.setFont(new Font("SansSerif", Font.PLAIN, 13));
        columnSelector.setPreferredSize(new Dimension(240, 32));
        selectedColumnCaption.setForeground(MUTED);
        selectedColumnCaption.setFont(new Font("SansSerif", Font.PLAIN, 11));
        selection.add(columnSelector, BorderLayout.CENTER);
        selection.add(selectedColumnCaption, BorderLayout.SOUTH);
        columnCard.add(selection, BorderLayout.CENTER);
        panel.add(fileCard, BorderLayout.CENTER);
        panel.add(columnCard, BorderLayout.EAST);
        return panel;
    }

    private JPanel createMainContent() {
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);

        JPanel actionRow = createCard("SORTING CONTROLS", new BorderLayout(12, 4));
        JPanel runControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
        runControls.setOpaque(false);
        styleButton(runButton, true);
        styleButton(clearButton, false);
        runControls.add(runButton);
        runControls.add(clearButton);
        JLabel warning = new JLabel("Large datasets (>50,000 records) may take longer because Insertion Sort is quadratic.");
        warning.setFont(new Font("SansSerif", Font.PLAIN, 11));
        warning.setForeground(WARNING);
        actionRow.add(runControls, BorderLayout.WEST);
        actionRow.add(warning, BorderLayout.CENTER);
        content.add(actionRow, BorderLayout.NORTH);

        JPanel upper = new JPanel(new BorderLayout(10, 0));
        upper.setOpaque(false);
        upper.add(createResultsPanel(), BorderLayout.CENTER);
        upper.add(createBestPanel(), BorderLayout.EAST);

        JTabbedPane dataTabs = new JTabbedPane();
        dataTabs.setFont(new Font("SansSerif", Font.BOLD, 12));
        dataTabs.addTab("Sorted Data Preview", createTableScrollPane(sortedTable));
        dataTabs.addTab("Original Dataset Preview", createTableScrollPane(previewTable));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upper, dataTabs);
        split.setResizeWeight(0.42);
        split.setDividerLocation(260);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setOpaque(false);
        content.add(split, BorderLayout.CENTER);
        return content;
    }

    private JPanel createResultsPanel() {
        JPanel panel = createCard("PERFORMANCE RESULTS", new BorderLayout(0, 6));
        resultsTable.setModel(new DefaultTableModel(new Object[0][0],
                new Object[] {"Rank", "Algorithm", "Execution Time (ms)", "Status"}));
        configureTable(resultsTable);
        panel.add(createTableScrollPane(resultsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBestPanel() {
        JPanel panel = createCard("BEST ALGORITHM", new BorderLayout(6, 8));
        panel.setPreferredSize(new Dimension(270, 10));
        JLabel trophy = new JLabel("Best Performing Algorithm");
        trophy.setForeground(SUCCESS);
        trophy.setFont(new Font("SansSerif", Font.BOLD, 14));
        bestAlgorithmLabel.setFont(new Font("SansSerif", Font.BOLD, 19));
        bestAlgorithmLabel.setForeground(NAVY);
        bestTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        bestTimeLabel.setForeground(BLUE);
        bestSummaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bestSummaryLabel.setForeground(MUTED);
        bestSummaryLabel.setVerticalAlignment(SwingConstants.TOP);
        JPanel detail = new JPanel();
        detail.setOpaque(false);
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.add(trophy);
        detail.add(Box.createVerticalStrut(15));
        detail.add(new JLabel("Algorithm"));
        detail.add(bestAlgorithmLabel);
        detail.add(Box.createVerticalStrut(12));
        detail.add(new JLabel("Execution Time"));
        detail.add(bestTimeLabel);
        detail.add(Box.createVerticalStrut(12));
        detail.add(new JLabel("Performance Summary"));
        detail.add(Box.createVerticalStrut(4));
        detail.add(bestSummaryLabel);
        panel.add(detail, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 6));
        footer.setOpaque(false);
        JPanel activity = createCard("PROGRESS & MESSAGES", new BorderLayout(10, 4));
        JPanel progress = new JPanel(new BorderLayout(8, 0));
        progress.setOpaque(false);
        progressBar.setPreferredSize(new Dimension(170, 15));
        progressBar.setStringPainted(false);
        progress.add(progressBar, BorderLayout.WEST);
        progress.add(progressLabel, BorderLayout.CENTER);
        activity.add(progress, BorderLayout.NORTH);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JPanel messages = new JPanel(new BorderLayout(0, 4));
        messages.setOpaque(false);
        messages.add(statusLabel, BorderLayout.NORTH);
        eventArea.setEditable(false);
        eventArea.setLineWrap(true);
        eventArea.setWrapStyleWord(true);
        eventArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        eventArea.setBackground(Color.WHITE);
        messages.add(new JScrollPane(eventArea), BorderLayout.CENTER);
        activity.add(messages, BorderLayout.CENTER);
        footer.add(activity, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        stats.add(createStat("RECORDS LOADED", rowCountLabel));
        stats.add(createStat("NUMERIC COLUMN", selectedColumnLabel));
        stats.add(createStat("ALGORITHMS TESTED", algorithmCountLabel));
        footer.add(stats, BorderLayout.SOUTH);
        return footer;
    }

    private JPanel createStat(String title, JLabel value) {
        JPanel panel = createCard(title, new BorderLayout());
        value.setFont(new Font("SansSerif", Font.BOLD, 12));
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCard(String title, java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        javax.swing.border.TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER), title);
        titled.setTitleFont(new Font("SansSerif", Font.BOLD, 10));
        titled.setTitleColor(MUTED);
        panel.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(5, 7, 7, 7)));
        return panel;
    }

    private JScrollPane createTableScrollPane(JTable table) {
        configureTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private void configureTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(23);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(232, 237, 243));
        table.setShowVerticalLines(false);
        table.setAutoCreateRowSorter(false);
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(new Color(235, 241, 248));
        header.setForeground(NAVY);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    private void styleButton(JButton button, boolean primary) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(9, 15, 9, 15));
        button.setBackground(primary ? BLUE : Color.WHITE);
        button.setForeground(primary ? Color.WHITE : NAVY);
    }

    private void addActionListeners() {
        loadButton.addActionListener(this::loadDatasetAction);
        runButton.addActionListener(this::runBenchmarkAction);
        clearButton.addActionListener(this::clearAction);
        columnSelector.addActionListener(this::handleColumnSelection);
    }

    private void loadDatasetAction(ActionEvent event) {
        int option = fileChooser.showOpenDialog(this);
        if (option != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = fileChooser.getSelectedFile();
        try {
            Dataset dataset = CsvLoader.load(selected);
            currentDataset = dataset;
            selectedColumn = -1;
            fileNameLabel.setText(selected.getName());
            fileStatusLabel.setText("CSV Loaded Successfully");
            fileStatusLabel.setForeground(SUCCESS);
            rowCountLabel.setText(String.format("%,d", dataset.getRowCount()));
            updatePreviewTable(dataset);
            populateColumnSelector(dataset);
            resetBenchmarkViews();
            addMessage("CSV loaded: " + dataset.getRowCount() + " records and "
                    + dataset.getColumnCount() + " columns.");
            setStatus("CSV loaded successfully.", SUCCESS);
            updateRunButtonState();
        } catch (Exception exception) {
            currentDataset = null;
            selectedColumn = -1;
            fileNameLabel.setText("No file selected");
            fileStatusLabel.setText("CSV could not be loaded");
            fileStatusLabel.setForeground(ERROR);
            rowCountLabel.setText("—");
            selectedColumnLabel.setText("—");
            populateColumnSelector(null);
            clearTables();
            setStatus(messageForLoadError(exception), ERROR);
            addMessage(messageForLoadError(exception));
            JOptionPane.showMessageDialog(this, messageForLoadError(exception),
                    "Unable to Load CSV", JOptionPane.ERROR_MESSAGE);
            updateRunButtonState();
        }
    }

    private String messageForLoadError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return "The selected CSV could not be loaded.";
        }
        if (message.toLowerCase().contains("file type")) {
            return "Invalid file type. Only CSV files are allowed.";
        }
        if (message.toLowerCase().contains("empty")) {
            return "CSV file is empty.";
        }
        if (message.toLowerCase().contains("header") && message.toLowerCase().contains("no data")) {
            return "CSV contains only headers and no data.";
        }
        return message;
    }

    private void populateColumnSelector(Dataset dataset) {
        columnSelector.removeAllItems();
        selectedColumn = -1;
        if (dataset == null) {
            columnSelector.addItem(new ColumnOption("No dataset loaded", -1, false));
            columnSelector.setEnabled(false);
            selectedColumnCaption.setText("Selected Column: —");
            return;
        }

        int firstNumericColumn = -1;
        for (int i = 0; i < dataset.getColumnCount(); i++) {
            boolean numeric = CsvLoader.isNumericColumn(dataset, i);
            columnSelector.addItem(new ColumnOption(dataset.getHeaders().get(i), i, numeric));
            if (numeric && firstNumericColumn < 0) {
                firstNumericColumn = i;
            }
        }

        columnSelector.setEnabled(dataset.getColumnCount() > 0);
        if (firstNumericColumn >= 0) {
            for (int i = 0; i < columnSelector.getItemCount(); i++) {
                if (columnSelector.getItemAt(i).columnIndex == firstNumericColumn) {
                    columnSelector.setSelectedIndex(i);
                    break;
                }
            }
            selectedColumn = firstNumericColumn;
            updateSelectedColumnLabels();
        } else {
            selectedColumnCaption.setText("Selected Column: None (numeric columns not detected)");
            setStatus("No numeric columns were detected in this dataset.", WARNING);
        }
    }

    private void handleColumnSelection(ActionEvent event) {
        ColumnOption option = (ColumnOption) columnSelector.getSelectedItem();
        if (currentDataset == null || option == null || option.columnIndex < 0) {
            selectedColumn = -1;
            updateRunButtonState();
            return;
        }
        if (!option.numeric) {
            if (selectedColumn >= 0) {
                resetBenchmarkViews();
            }
            selectedColumn = -1;
            selectedColumnCaption.setText("Selected Column: " + option.name + " (Text)");
            selectedColumnLabel.setText("—");
            setStatus("Selected column is not numeric. Please choose a numeric column.", WARNING);
            updateRunButtonState();
            return;
        }

        if (selectedColumn != option.columnIndex) {
            resetBenchmarkViews();
        }
        selectedColumn = option.columnIndex;
        updateSelectedColumnLabels();
        setStatus("Selected numeric column: " + option.name, MUTED);
        updateRunButtonState();
    }

    private void updateSelectedColumnLabels() {
        String name = selectedColumn >= 0 && currentDataset != null
                ? currentDataset.getHeaders().get(selectedColumn) : "—";
        selectedColumnCaption.setText("Selected Column: " + name);
        selectedColumnLabel.setText(name);
    }

    private void runBenchmarkAction(ActionEvent event) {
        if (currentDataset == null || selectedColumn < 0) {
            setStatus("Please load a dataset and select a numeric column before running analysis.", WARNING);
            return;
        }
        if (benchmarkWorker != null && !benchmarkWorker.isDone()) {
            setStatus("A performance analysis is already running.", WARNING);
            return;
        }
        if (currentDataset.getRowCount() > LARGE_DATASET_WARNING_THRESHOLD) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Dataset contains over 50,000 records. Insertion Sort may take a long time. Continue?",
                    "Large Dataset Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                setStatus("Analysis cancelled.", MUTED);
                return;
            }
        }

        final Dataset datasetToAnalyze = currentDataset;
        final int columnToAnalyze = selectedColumn;
        setControlsEnabled(false);
        progressBar.setIndeterminate(true);
        progressLabel.setText("Running sorting algorithms in background...");
        setStatus("Running sorting algorithms in background...", BLUE);
        bestAlgorithmLabel.setText("Analysis in progress...");
        bestTimeLabel.setText("—");
        bestSummaryLabel.setText("Each algorithm sorts an independent copy of the selected numeric data.");

        benchmarkWorker = new SwingWorker<PerformanceEvaluator.EvaluationResult, Void>() {
            @Override
            protected PerformanceEvaluator.EvaluationResult doInBackground() {
                return PerformanceEvaluator.evaluate(datasetToAnalyze, columnToAnalyze);
            }

            @Override
            protected void done() {
                try {
                    PerformanceEvaluator.EvaluationResult result = get();
                    displayBenchmarkResult(result, datasetToAnalyze, columnToAnalyze);
                } catch (Exception exception) {
                    setStatus("Sorting failed: " + exception.getMessage(), ERROR);
                    addMessage("Sorting failed: " + exception.getMessage());
                    bestAlgorithmLabel.setText("Analysis unavailable");
                    bestTimeLabel.setText("—");
                } finally {
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(0);
                    progressLabel.setText(" ");
                    benchmarkWorker = null;
                    setControlsEnabled(true);
                    updateRunButtonState();
                }
            }
        };
        benchmarkWorker.execute();
    }

    private void displayBenchmarkResult(PerformanceEvaluator.EvaluationResult result,
                                       Dataset dataset, int columnIndex) {
        List<PerformanceEvaluator.AlgorithmResult> algorithms = new ArrayList<>(result.getResults());
        algorithms.sort(Comparator
                .comparing(PerformanceEvaluator.AlgorithmResult::isCorrect).reversed()
                .thenComparingLong(PerformanceEvaluator.AlgorithmResult::getDurationNanos)
                .thenComparing(PerformanceEvaluator.AlgorithmResult::getAlgorithmName));

        String winner = result.hasWinner() ? result.getWinnerName() : null;
        Object[][] tableRows = new Object[algorithms.size()][4];
        for (int i = 0; i < algorithms.size(); i++) {
            PerformanceEvaluator.AlgorithmResult algorithm = algorithms.get(i);
            tableRows[i][0] = i + 1;
            tableRows[i][1] = algorithm.getAlgorithmName();
            tableRows[i][2] = String.format(Locale.ROOT, "%.6f", algorithm.getDurationNanos() / 1_000_000.0);
            tableRows[i][3] = algorithm.isCorrect() ? "Correct" : "Invalid";
        }
        resultsTable.setModel(new DefaultTableModel(tableRows,
                new Object[] {"Rank", "Algorithm", "Execution Time (ms)", "Status"}));
        resultsTable.setDefaultRenderer(Object.class, resultRenderer(winner));
        configureTable(resultsTable);

        showSortedRows(dataset, result.getSortedRows());
        if (result.hasWinner()) {
            PerformanceEvaluator.AlgorithmResult best = result.getWinner();
            double milliseconds = best.getDurationNanos() / 1_000_000.0;
            bestAlgorithmLabel.setText(best.getAlgorithmName());
            bestTimeLabel.setText(String.format(Locale.ROOT, "%.6f ms", milliseconds));
            bestSummaryLabel.setText(best.getAlgorithmName()
                    + " had the shortest execution time among algorithms that passed validation.");
            setStatus("Sorting completed successfully.", SUCCESS);
            addMessage("Sorting completed successfully.");
        } else {
            bestAlgorithmLabel.setText("No valid result");
            bestTimeLabel.setText("—");
            bestSummaryLabel.setText("All algorithm outputs failed correctness validation.");
            setStatus("No algorithm produced a valid sorted result.", ERROR);
            addMessage("No valid sorted result was produced.");
        }

        if (result.getSkippedRows() > 0) {
            String skipped = result.getSkippedRows() + " invalid or empty numeric values were skipped.";
            addMessage(skipped);
            setStatus(skipped, WARNING);
        }
        rowCountLabel.setText(String.format("%,d", dataset.getRowCount()));
        selectedColumnLabel.setText(dataset.getHeaders().get(columnIndex));
        algorithmCountLabel.setText(Integer.toString(result.getResults().size()));
    }

    private DefaultTableCellRenderer resultRenderer(final String winner) {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        table, value, selected, focus, row, column);
                String algorithm = String.valueOf(table.getValueAt(row, 1));
                if (winner != null && winner.equals(algorithm)) {
                    component.setBackground(selected ? table.getSelectionBackground() : SUCCESS_BACKGROUND);
                    component.setForeground(selected ? table.getSelectionForeground() : SUCCESS);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    component.setBackground(selected ? table.getSelectionBackground() : Color.WHITE);
                    component.setForeground(selected ? table.getSelectionForeground() : NAVY);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                return component;
            }
        };
    }

    private void clearAction(ActionEvent event) {
        if (benchmarkWorker != null && !benchmarkWorker.isDone()) {
            setStatus("Wait for the running analysis to finish before resetting.", WARNING);
            return;
        }
        currentDataset = null;
        selectedColumn = -1;
        fileNameLabel.setText("No file selected");
        fileStatusLabel.setText("Choose a CSV file to begin");
        fileStatusLabel.setForeground(MUTED);
        rowCountLabel.setText("—");
        selectedColumnLabel.setText("—");
        algorithmCountLabel.setText("5");
        populateColumnSelector(null);
        clearTables();
        resetBestPanel();
        eventArea.setText("");
        setStatus("Ready. Choose a CSV dataset to begin.", MUTED);
        updateRunButtonState();
    }

    private void resetBenchmarkViews() {
        algorithmCountLabel.setText("5");
        resultsTable.setModel(new DefaultTableModel(new Object[0][0],
                new Object[] {"Rank", "Algorithm", "Execution Time (ms)", "Status"}));
        showSortedRows(currentDataset, new ArrayList<String[]>());
        resetBestPanel();
    }

    private void resetBestPanel() {
        bestAlgorithmLabel.setText("Run an analysis to see the fastest correct algorithm.");
        bestTimeLabel.setText("—");
        bestSummaryLabel.setText("Results are measured for the selected dataset and column.");
    }

    private void clearTables() {
        previewTable.setModel(new DefaultTableModel(new Object[0][0], new Object[] {"No data"}));
        sortedTable.setModel(new DefaultTableModel(new Object[0][0], new Object[] {"No result"}));
        resultsTable.setModel(new DefaultTableModel(new Object[0][0],
                new Object[] {"Rank", "Algorithm", "Execution Time (ms)", "Status"}));
    }

    private void updatePreviewTable(Dataset dataset) {
        if (dataset == null) {
            previewTable.setModel(new DefaultTableModel(new Object[0][0], new Object[] {"No data"}));
            return;
        }
        int previewCount = Math.min(dataset.getRows().size(), 100);
        Object[][] rows = new Object[previewCount][dataset.getColumnCount()];
        for (int r = 0; r < previewCount; r++) {
            String[] source = dataset.getRows().get(r);
            for (int c = 0; c < dataset.getColumnCount(); c++) {
                rows[r][c] = source[c];
            }
        }
        previewTable.setModel(new DefaultTableModel(rows, dataset.getHeaders().toArray(new String[0])));
        configureTable(previewTable);
    }

    private void showSortedRows(Dataset dataset, List<String[]> rows) {
        if (dataset == null) {
            sortedTable.setModel(new DefaultTableModel(new Object[0][0], new Object[] {"No result"}));
            return;
        }
        Object[][] data = new Object[rows.size()][dataset.getColumnCount()];
        for (int r = 0; r < rows.size(); r++) {
            String[] source = rows.get(r);
            for (int c = 0; c < dataset.getColumnCount(); c++) {
                data[r][c] = source[c];
            }
        }
        sortedTable.setModel(new DefaultTableModel(data, dataset.getHeaders().toArray(new String[0])));
        configureTable(sortedTable);
    }

    private void setControlsEnabled(boolean enabled) {
        loadButton.setEnabled(enabled);
        columnSelector.setEnabled(enabled && currentDataset != null && currentDataset.getColumnCount() > 0);
        clearButton.setEnabled(enabled);
        runButton.setEnabled(enabled && currentDataset != null && selectedColumn >= 0);
    }

    private void updateRunButtonState() {
        boolean running = benchmarkWorker != null && !benchmarkWorker.isDone();
        runButton.setEnabled(!running && currentDataset != null && selectedColumn >= 0);
    }

    private void setStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void addMessage(String message) {
        if (eventArea.getText().isEmpty()) {
            eventArea.setText(message);
        } else {
            eventArea.append("\n" + message);
        }
    }

    private static final class ColumnOption {
        private final String name;
        private final int columnIndex;
        private final boolean numeric;

        private ColumnOption(String name, int columnIndex, boolean numeric) {
            this.name = name;
            this.columnIndex = columnIndex;
            this.numeric = numeric;
        }

        @Override
        public String toString() {
            if (columnIndex < 0) {
                return name;
            }
            return name + (numeric ? " (Numeric)" : " (Text)");
        }
    }

    private static final class RoundedButton extends JButton {
        private static final long serialVersionUID = 1L;

        private RoundedButton(String text) {
            super(text);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();
            if (!isEnabled()) {
                fill = new Color(220, 225, 232);
            } else if (getModel().isPressed()) {
                fill = fill.darker();
            } else if (getModel().isRollover()) {
                fill = fill.brighter();
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
