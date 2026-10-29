import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TaskFlowGUI - the Swing dashboard for TaskFlow.
 *
 * Start it with:  java TaskFlowGUI
 * This window never calls the backend classes directly. Every action goes through TaskFlowService.
 */
public class TaskFlowGUI extends JFrame {

    // ---- colours --------------------------------------------------------
    private static final Color NAVY = new Color(33, 47, 90);
    private static final Color SUBTITLE = new Color(190, 200, 225);
    private static final Color GOOD = new Color(0, 110, 40);
    private static final Color BAD = new Color(170, 20, 30);

    private static final Font BASE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    // ---- data -----------------------------------------------------------
    private final TaskFlowService service = new TaskFlowService();

    // ---- widgets that need updating after actions ----------------------
    private final TaskTableModel tableModel = new TaskTableModel();
    private final Map<String, JLabel> cardValues = new LinkedHashMap<>();
    private final JTextField idField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JComboBox<DependencyOption> dependencyBox = new JComboBox<>();
    private final JLabel statusLabel = new JLabel("Ready.");
    private final JTextArea logArea = new JTextArea();
    private final JTextArea outputArea = new JTextArea();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final JLabel logPathLabel = new JLabel();

    public TaskFlowGUI() {
        super("TaskFlow - Workflow Engine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBorder(new EmptyBorder(10, 16, 10, 16));
        body.add(buildSummaryPanel(), BorderLayout.NORTH);
        body.add(buildMainSplit(), BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        configureWindow();
        refreshAll();
    }

    /** Window size (never bigger than the screen), minimum size, centring and the first divider position. */
    private void configureWindow() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int width = Math.min(1100, (int) (screen.width * 0.9));
        int height = Math.min(760, (int) (screen.height * 0.9));
        setSize(width, height);
        setMinimumSize(new Dimension(840, 580));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                mainSplit.setDividerLocation(0.62);
            }
        });
    }

    // ==================================================================
    // Building the screen
    // ==================================================================

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("TaskFlow");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Java Workflow Engine  -  plan tasks, manage dependencies, execute workflows");
        subtitle.setFont(BASE_FONT);
        subtitle.setForeground(SUBTITLE);

        header.add(title);
        header.add(subtitle);
        return header;
    }

    private JPanel buildSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 10, 0));

        addCard(panel, "Total", new Color(52, 73, 120));
        addCard(panel, TaskFlowService.PENDING, new Color(110, 118, 130));
        addCard(panel, TaskFlowService.RUNNING, new Color(30, 110, 190));
        addCard(panel, TaskFlowService.COMPLETED, new Color(40, 140, 70));
        addCard(panel, TaskFlowService.FAILED, new Color(190, 50, 50));
        addCard(panel, TaskFlowService.SKIPPED, new Color(200, 130, 20));
        return panel;
    }

    private void addCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(color);
        card.setBorder(new EmptyBorder(6, 10, 6, 10));

        JLabel value = new JLabel("0", SwingConstants.CENTER);
        value.setFont(new Font("Segoe UI", Font.BOLD, 26));
        value.setForeground(Color.WHITE);

        JLabel name = new JLabel(title, SwingConstants.CENTER);
        name.setFont(BASE_FONT);
        name.setForeground(Color.WHITE);

        card.add(value);
        card.add(name);
        parent.add(card);
        cardValues.put(title, value);
    }

    private JPanel buildMiddlePanel() {
        JPanel middle = new JPanel(new BorderLayout(12, 0));

        // --- task table ---
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFont(BASE_FONT);
        table.getTableHeader().setFont(BOLD_FONT);
        table.setFillsViewportHeight(true);
        table.setPreferredScrollableViewportSize(new Dimension(450, 170));
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setMinWidth(105);   // keeps "Completed" readable
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusRenderer());

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Tasks"));
        tableScroll.setMinimumSize(new Dimension(320, 120));
        middle.add(tableScroll, BorderLayout.CENTER);

        // --- add task form (inside a scroll pane, so it is never clipped) ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(0, 0, 4, 0));

        idField.setFont(BASE_FONT);
        nameField.setFont(BASE_FONT);
        dependencyBox.setFont(BASE_FONT);
        idField.setColumns(8);
        nameField.setColumns(8);
        // Fixed width for the drop-down, so a long task name cannot make the form wider.
        dependencyBox.setPrototypeDisplayValue(new DependencyOption(0, "Task 99 - Sample task"));

        addFormRow(form, 0, "Task ID", idField);
        addFormRow(form, 1, "Task name", nameField);
        addFormRow(form, 2, "Depends on", dependencyBox);

        JButton addButton = new JButton("Add Task");
        addButton.setFont(BOLD_FONT);
        addButton.addActionListener(e -> handleAddTask());
        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 3;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.fill = GridBagConstraints.HORIZONTAL;
        buttonConstraints.insets = new Insets(8, 8, 4, 8);
        form.add(addButton, buttonConstraints);

        // empty filler pushes the form to the top
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridx = 0;
        filler.gridy = 4;
        filler.gridwidth = 2;
        filler.weighty = 1;
        form.add(new JPanel(), filler);

        idField.addActionListener(e -> handleAddTask());
        nameField.addActionListener(e -> handleAddTask());

        JScrollPane formScroll = new JScrollPane(form);
        formScroll.setBorder(BorderFactory.createTitledBorder("Add Task"));
        formScroll.setPreferredSize(new Dimension(360, 100));
        formScroll.setMinimumSize(new Dimension(280, 100));
        middle.add(formScroll, BorderLayout.EAST);
        return middle;
    }

    private void addFormRow(JPanel form, int row, String labelText, Component field) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.WEST;
        labelConstraints.insets = new Insets(6, 8, 2, 8);
        JLabel label = new JLabel(labelText);
        label.setFont(BASE_FONT);
        form.add(label, labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(6, 0, 2, 8);
        form.add(field, fieldConstraints);
    }

    /** Tasks on top, logs below. The user can drag the divider to give either part more room. */
    private JSplitPane buildMainSplit() {
        JPanel upper = new JPanel(new BorderLayout(0, 8));
        upper.add(buildMiddlePanel(), BorderLayout.CENTER);
        upper.add(buildActionPanel(), BorderLayout.SOUTH);

        mainSplit.setTopComponent(upper);
        mainSplit.setBottomComponent(buildTabs());
        mainSplit.setResizeWeight(0.62);
        mainSplit.setContinuousLayout(true);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(null);
        return mainSplit;
    }

    /** Action buttons plus the one-line result message. */
    private JPanel buildActionPanel() {
        JPanel actions = new JPanel(new BorderLayout(0, 6));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        buttons.add(makeButton("Execute Workflow", e -> handleExecute()));
        buttons.add(makeButton("Reset Statuses", e -> handleReset()));
        buttons.add(makeButton("Check Cycles", e -> handleCheckCycles()));
        buttons.add(makeButton("Save Tasks", e -> handleSave()));
        buttons.add(makeButton("Load Tasks", e -> handleLoad()));

        statusLabel.setFont(BOLD_FONT);
        actions.add(buttons, BorderLayout.CENTER);
        actions.add(statusLabel, BorderLayout.SOUTH);
        return actions;
    }

    /** The "Execution Logs" and "Run Output" tabs. */
    private JTabbedPane buildTabs() {
        logArea.setEditable(false);
        logArea.setFont(MONO_FONT);
        outputArea.setEditable(false);
        outputArea.setFont(MONO_FONT);

        // Shows exactly which file is read and cleared, so a wrong folder is easy to spot.
        logPathLabel.setFont(MONO_FONT.deriveFont(11f));
        logPathLabel.setForeground(Color.DARK_GRAY);
        logPathLabel.setBorder(new EmptyBorder(2, 4, 4, 4));
        logPathLabel.setText("Log file: " + service.getLogFilePath());
        logPathLabel.setToolTipText(service.getLogFilePath());

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(logPathLabel, BorderLayout.NORTH);
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        JPanel logButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        logButtons.add(makeButton("Refresh", e -> handleRefreshLogs()));
        logButtons.add(makeButton("Clear Logs", e -> handleClearLogs()));
        logPanel.add(logButtons, BorderLayout.SOUTH);

        tabs.addTab("Execution Logs", logPanel);
        tabs.addTab("Run Output", new JScrollPane(outputArea));
        tabs.setMinimumSize(new Dimension(300, 120));
        return tabs;
    }

    private JButton makeButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        button.setFont(BASE_FONT);
        button.addActionListener(listener);
        return button;
    }

    // ==================================================================
    // Button handlers
    // ==================================================================

    private void handleAddTask() {
        int id;
        try {
            id = Integer.parseInt(idField.getText().trim());
        } catch (NumberFormatException ex) {
            showStatus(false, "Task ID must be a whole number.");
            return;
        }

        DependencyOption selected = (DependencyOption) dependencyBox.getSelectedItem();
        int dependencyId = (selected == null) ? -1 : selected.id;

        TaskFlowService.Result result = service.addTask(id, nameField.getText(), dependencyId);
        if (result.isSuccess()) {
            idField.setText("");
            nameField.setText("");
        }
        showResult(result);
        refreshAll();
        idField.requestFocusInWindow();
    }

    private void handleExecute() {
        TaskFlowService.Result result = service.executeWorkflow();
        appendOutput("Execute Workflow", result.getConsoleOutput());
        showResult(result);
        refreshAll();
        tabs.setSelectedIndex(0);
    }

    private void handleReset() {
        showResult(service.resetStatuses());
        refreshAll();
    }

    private void handleCheckCycles() {
        showResult(service.checkCircularDependencies());
    }

    private void handleSave() {
        TaskFlowService.Result result = service.saveTasks();
        appendOutput("Save Tasks", result.getConsoleOutput());
        showResult(result);
    }

    private void handleLoad() {
        if (service.getTaskCount() > 0) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Loading will replace the tasks currently shown. Continue?",
                    "Load Tasks", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
        }
        TaskFlowService.Result result = service.loadTasks();
        appendOutput("Load Tasks", result.getConsoleOutput());
        showResult(result);
        refreshAll();
    }

    private void handleRefreshLogs() {
        refreshLogs();
        showStatus(true, "Logs refreshed.");
    }

    private void handleClearLogs() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Clear the whole execution log? This cannot be undone.",
                "Clear Logs", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) {
            return;
        }
        TaskFlowService.Result result = service.clearLogs();
        appendOutput("Clear Logs", result.getConsoleOutput());
        showResult(result);
        refreshLogs();
    }

    // ==================================================================
    // Updating what the user sees
    // ==================================================================

    private void refreshAll() {
        tableModel.fireTableDataChanged();
        updateSummary();
        refreshDependencyOptions();
        refreshLogs();
    }

    private void updateSummary() {
        for (Map.Entry<String, JLabel> entry : cardValues.entrySet()) {
            String name = entry.getKey();
            int value = name.equals("Total") ? service.getTaskCount() : service.countByStatus(name);
            entry.getValue().setText(String.valueOf(value));
        }
    }

    /** Rebuilds the "Depends on" drop-down from the current tasks, keeping the user's choice if possible. */
    private void refreshDependencyOptions() {
        DependencyOption previous = (DependencyOption) dependencyBox.getSelectedItem();
        int previousId = (previous == null) ? -1 : previous.id;

        DefaultComboBoxModel<DependencyOption> model = new DefaultComboBoxModel<>();
        DependencyOption none = new DependencyOption(-1, "None");
        model.addElement(none);
        DependencyOption toSelect = none;

        for (int i = 0; i < service.getTaskCount(); i++) {
            Task task = service.getTask(i);
            DependencyOption option = new DependencyOption(task.getTaskId(),
                    "Task " + task.getTaskId() + " - " + task.getTaskName());
            model.addElement(option);
            if (option.id == previousId) {
                toSelect = option;
            }
        }
        dependencyBox.setModel(model);
        dependencyBox.setSelectedItem(toSelect);
    }

    private void refreshLogs() {
        logArea.setText(service.readLogs());
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void appendOutput(String title, String text) {
        outputArea.append("=== " + title + " ===\n");
        if (text != null && !text.isEmpty()) {
            outputArea.append(text + "\n");
        }
        outputArea.append("\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }

    private void showResult(TaskFlowService.Result result) {
        showStatus(result.isSuccess(), result.getMessage());
    }

    private void showStatus(boolean good, String message) {
        statusLabel.setForeground(good ? GOOD : BAD);
        statusLabel.setText(message);
        statusLabel.setToolTipText(message);
    }

    // ==================================================================
    // Small helper classes
    // ==================================================================

    /** One entry in the "Depends on" drop-down. toString() is what the user sees. */
    private static class DependencyOption {
        final int id;
        final String label;

        DependencyOption(int id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    /** Feeds the JTable with data from the service. Read-only. */
    private class TaskTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Name", "Depends on", "Status"};

        @Override
        public int getRowCount() {
            return service.getTaskCount();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            Task task = service.getTask(row);
            if (task == null) {
                return "";
            }
            switch (column) {
                case 0:
                    return task.getTaskId();
                case 1:
                    return task.getTaskName();
                case 2:
                    return service.getDependencyLabel(row);
                case 3:
                    return task.getStatus();
                default:
                    return "";
            }
        }
    }

    /** Colours the Status cell depending on the status text. */
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(BOLD_FONT);

            if (!isSelected) {
                String status = String.valueOf(value);
                if (TaskFlowService.COMPLETED.equals(status)) {
                    setBackground(new Color(198, 239, 206));
                    setForeground(new Color(0, 97, 0));
                } else if (TaskFlowService.FAILED.equals(status)) {
                    setBackground(new Color(255, 199, 206));
                    setForeground(new Color(156, 0, 6));
                } else if (TaskFlowService.SKIPPED.equals(status)) {
                    setBackground(new Color(255, 235, 156));
                    setForeground(new Color(156, 87, 0));
                } else if (TaskFlowService.RUNNING.equals(status)) {
                    setBackground(new Color(189, 215, 238));
                    setForeground(new Color(31, 78, 121));
                } else {
                    setBackground(new Color(230, 230, 230));
                    setForeground(new Color(64, 64, 64));
                }
            }
            return cell;
        }
    }

    // ==================================================================
    // Start the application
    // ==================================================================

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is unavailable, Java's default look is used. Nothing else to do.
        }

        SwingUtilities.invokeLater(() -> new TaskFlowGUI().setVisible(true));
    }
}