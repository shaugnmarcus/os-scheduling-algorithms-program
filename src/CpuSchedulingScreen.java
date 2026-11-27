
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class CpuSchedulingScreen extends JFrame {

    private MainMenu mainMenu;

    // the inputs the user entered (row 0 is P1, row 1 is P2, ...)
    private ArrayList<Integer> arrivalTimes = new ArrayList<>();
    private ArrayList<Integer> burstTimes = new ArrayList<>();

    private int largestAllowedNumber = 1000;

    private JComboBox<String> algorithmBox;
    private JTextField quantumField;
    private JTextField arrivalField;
    private JTextField burstField;
    private JTable inputTable;
    private DefaultTableModel inputTableModel;
    private DefaultTableModel resultTableModel;
    private GanttPanel ganttPanel;
    private JTextArea summaryArea;

    public CpuSchedulingScreen(MainMenu menu) {
        mainMenu = menu;

        setTitle("CPU Scheduling");
        setSize(1000, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                mainMenu.setVisible(true);
            }
        });

        setLayout(new BorderLayout());
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildResultPanel(), BorderLayout.CENTER);
    }

    // ---------- building the screen ----------

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsRow.add(new JLabel("Algorithm:"));
        algorithmBox = new JComboBox<>(new String[] {"SJF (Non-preemptive)", "Round Robin (Preemptive)"});
        settingsRow.add(algorithmBox);
        settingsRow.add(new JLabel("Time quantum (Round Robin only):"));
        quantumField = new JTextField(5);
        quantumField.setEnabled(false);
        settingsRow.add(quantumField);

        algorithmBox.addActionListener(e -> {
            boolean isRoundRobin = algorithmBox.getSelectedIndex() == 1;
            quantumField.setEnabled(isRoundRobin);
        });

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton runButton = new JButton("Run");
        JButton sampleButton = new JButton("Load Example");
        JButton resetButton = new JButton("Clear");
        JButton backButton = new JButton("Back");

        runButton.addActionListener(e -> runScheduler());
        sampleButton.addActionListener(e -> loadSampleData());
        resetButton.addActionListener(e -> resetEverything());
        backButton.addActionListener(e -> {
            dispose();
            mainMenu.setVisible(true);
        });

        buttonRow.add(runButton);
        buttonRow.add(sampleButton);
        buttonRow.add(resetButton);
        buttonRow.add(backButton);

        topPanel.add(settingsRow);
        topPanel.add(buttonRow);
        return topPanel;
    }

    private JPanel buildLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Processes"));

        JPanel entryPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        arrivalField = new JTextField();
        burstField = new JTextField();
        JButton addButton = new JButton("Add Process");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> addProcess());
        removeButton.addActionListener(e -> removeSelectedProcess());

        entryPanel.add(new JLabel("Arrival time:"));
        entryPanel.add(arrivalField);
        entryPanel.add(new JLabel("Burst time:"));
        entryPanel.add(burstField);
        entryPanel.add(addButton);
        entryPanel.add(removeButton);

        inputTableModel = new DefaultTableModel(new String[] {"Process", "Arrival Time", "Burst Time"}, 0);
        inputTable = new JTable(inputTableModel);
        inputTable.setDefaultEditor(Object.class, null);

        leftPanel.add(entryPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(inputTable), BorderLayout.CENTER);
        return leftPanel;
    }

    private JPanel buildResultPanel() {
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.setBorder(BorderFactory.createTitledBorder("Results"));

        ganttPanel = new GanttPanel();
        JScrollPane ganttScroll = new JScrollPane(ganttPanel);
        ganttScroll.setPreferredSize(new Dimension(600, 130));
        ganttScroll.setBorder(BorderFactory.createTitledBorder("Gantt Chart"));

        resultTableModel = new DefaultTableModel(new String[] {
                "Process", "Arrival", "Burst", "Completion", "Turnaround", "Waiting", "Response"}, 0);
        JTable resultTable = new JTable(resultTableModel);
        resultTable.setDefaultEditor(Object.class, null);

        summaryArea = new JTextArea(8, 30);
        summaryArea.setEditable(false);

        resultPanel.add(ganttScroll, BorderLayout.NORTH);
        resultPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultPanel.add(new JScrollPane(summaryArea), BorderLayout.SOUTH);
        return resultPanel;
    }

    // ---------- button actions ----------

    private void addProcess() {
        String arrivalText = arrivalField.getText().trim();
        String burstText = burstField.getText().trim();

        if (arrivalText.equals("") || burstText.equals("")) {
            showError("Please fill in both the arrival time and the burst time.");
            return;
        }

        int arrival;
        int burst;
        try {
            arrival = Integer.parseInt(arrivalText);
            burst = Integer.parseInt(burstText);
        } catch (NumberFormatException e) {
            showError("Arrival time and burst time must be whole numbers.");
            return;
        }

        if (arrival < 0) {
            showError("Arrival time cannot be negative.");
            return;
        }
        if (burst < 1) {
            showError("Burst time must be at least 1.");
            return;
        }
        if (arrival > largestAllowedNumber || burst > largestAllowedNumber) {
            showError("Numbers cannot be bigger than " + largestAllowedNumber + ".");
            return;
        }

        arrivalTimes.add(arrival);
        burstTimes.add(burst);
        refreshInputTable();

        arrivalField.setText("");
        burstField.setText("");
        arrivalField.requestFocus();
    }

    private void removeSelectedProcess() {
        int selectedRow = inputTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("Click a process in the table first, then press Remove Selected.");
            return;
        }
        arrivalTimes.remove(selectedRow);
        burstTimes.remove(selectedRow);
        refreshInputTable();
    }

    private void loadSampleData() {
        int[] sampleArrivals = {2, 5, 3, 2, 4, 6, 8, 9, 12, 50};
        int[] sampleBursts = {6, 2, 8, 3, 4, 1, 5, 2, 7, 3};

        arrivalTimes.clear();
        burstTimes.clear();
        for (int i = 0; i < sampleArrivals.length; i++) {
            arrivalTimes.add(sampleArrivals[i]);
            burstTimes.add(sampleBursts[i]);
        }
        refreshInputTable();
        quantumField.setText("3");
        clearResults();
    }

    private void resetEverything() {
        arrivalTimes.clear();
        burstTimes.clear();
        refreshInputTable();
        arrivalField.setText("");
        burstField.setText("");
        quantumField.setText("");
        algorithmBox.setSelectedIndex(0);
        clearResults();
    }

    private void runScheduler() {
        if (arrivalTimes.size() == 0) {
            showError("Please add at least one process first.");
            return;
        }

        boolean useRoundRobin = algorithmBox.getSelectedIndex() == 1;
        int timeQuantum = 0;

        if (useRoundRobin) {
            String quantumText = quantumField.getText().trim();
            if (quantumText.equals("")) {
                showError("Please enter a time quantum for Round Robin.");
                return;
            }
            try {
                timeQuantum = Integer.parseInt(quantumText);
            } catch (NumberFormatException e) {
                showError("Time quantum must be a whole number.");
                return;
            }
            if (timeQuantum < 1) {
                showError("Time quantum must be at least 1.");
                return;
            }
            if (timeQuantum > largestAllowedNumber) {
                showError("Time quantum cannot be bigger than " + largestAllowedNumber + ".");
                return;
            }
        }

        // make fresh process objects so running twice never reuses old results
        List<ProcessInfo> processList = new ArrayList<>();
        for (int i = 0; i < arrivalTimes.size(); i++) {
            processList.add(new ProcessInfo(i + 1, arrivalTimes.get(i), burstTimes.get(i)));
        }

        CpuResult result;
        String algorithmName;
        if (useRoundRobin) {
            RoundRobinScheduler scheduler = new RoundRobinScheduler();
            result = scheduler.run(processList, timeQuantum);
            algorithmName = "Round Robin (time quantum = " + timeQuantum + ")";
        } else {
            SjfScheduler scheduler = new SjfScheduler();
            result = scheduler.run(processList);
            algorithmName = "Shortest Job First (non-preemptive)";
        }

        showResult(result, algorithmName);
    }

    // ---------- showing things on the screen ----------

    private void refreshInputTable() {
        inputTableModel.setRowCount(0);
        for (int i = 0; i < arrivalTimes.size(); i++) {
            inputTableModel.addRow(new Object[] {"P" + (i + 1), arrivalTimes.get(i), burstTimes.get(i)});
        }
    }

    private void showResult(CpuResult result, String algorithmName) {
        resultTableModel.setRowCount(0);
        for (ProcessInfo process : result.processList) {
            resultTableModel.addRow(new Object[] {
                    "P" + process.id,
                    process.arrivalTime,
                    process.burstTime,
                    process.completionTime,
                    process.turnaroundTime,
                    process.waitingTime,
                    process.responseTime});
        }

        ganttPanel.setBlocks(result.ganttBlocks);

        String summary = "Algorithm: " + algorithmName + "\n\n";
        summary += "Average turnaround time: " + String.format("%.2f", result.averageTurnaround) + "\n";
        summary += "Average waiting time: " + String.format("%.2f", result.averageWaiting) + "\n";
        summary += "Average response time: " + String.format("%.2f", result.averageResponse) + "\n\n";
        summary += "Total time: " + result.totalTime + "   (CPU busy for " + result.busyTime + ")\n";
        summary += "CPU utilization: " + String.format("%.2f", result.cpuUtilization) + " %\n";
        summary += "Throughput: " + String.format("%.4f", result.throughput) + " processes per time unit\n";
        summaryArea.setText(summary);
    }

    private void clearResults() {
        resultTableModel.setRowCount(0);
        ganttPanel.setBlocks(new ArrayList<GanttBlock>());
        summaryArea.setText("");
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- the Gantt chart drawing ----------

    private class GanttPanel extends JPanel {
        private List<GanttBlock> blocks = new ArrayList<>();
        private int totalTime = 0;
        private int pixelsPerTimeUnit = 30;
        private int leftMargin = 20;
        private int barTop = 20;
        private int barHeight = 40;

        public GanttPanel() {
            setPreferredSize(new Dimension(400, 90));
        }

        public void setBlocks(List<GanttBlock> newBlocks) {
            blocks = newBlocks;
            totalTime = 0;
            if (blocks.size() > 0) {
                totalTime = blocks.get(blocks.size() - 1).endTime;
            }
            setPreferredSize(new Dimension(leftMargin * 2 + totalTime * pixelsPerTimeUnit, 90));
            revalidate();
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            FontMetrics fontMetrics = g.getFontMetrics();
            int numberY = barTop + barHeight + 22;

            for (GanttBlock block : blocks) {
                int x = leftMargin + block.startTime * pixelsPerTimeUnit;
                int width = (block.endTime - block.startTime) * pixelsPerTimeUnit;

                if (block.label.equals("Idle")) {
                    g.setColor(Color.LIGHT_GRAY);
                } else {
                    g.setColor(new Color(200, 220, 255));
                }
                g.fillRect(x, barTop, width, barHeight);

                g.setColor(Color.BLACK);
                g.drawRect(x, barTop, width, barHeight);

                int labelWidth = fontMetrics.stringWidth(block.label);
                g.drawString(block.label, x + (width - labelWidth) / 2, barTop + 25);

                String startText = String.valueOf(block.startTime);
                g.drawString(startText, x - fontMetrics.stringWidth(startText) / 2, numberY);
            }

            if (blocks.size() > 0) {
                String endText = String.valueOf(totalTime);
                int endX = leftMargin + totalTime * pixelsPerTimeUnit;
                g.drawString(endText, endX - fontMetrics.stringWidth(endText) / 2, numberY);

                // the time scale: one small tick for every time unit
                int scaleY = barTop + barHeight;
                g.drawLine(leftMargin, scaleY, endX, scaleY);
                for (int time = 0; time <= totalTime; time++) {
                    int tickX = leftMargin + time * pixelsPerTimeUnit;
                    g.drawLine(tickX, scaleY, tickX, scaleY + 6);
                }
            }
        }
    }
}