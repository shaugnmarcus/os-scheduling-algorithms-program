
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class DiskSchedulingScreen extends JFrame {

    private MainMenu mainMenu;

    private JComboBox<String> algorithmBox;
    private JComboBox<String> directionBox;
    private JTextField headField;
    private JTextField lastTrackField;
    private JTextField requestField;
    private DefaultTableModel stepTableModel;
    private JTextArea summaryArea;
    private GraphPanel graphPanel;

    public DiskSchedulingScreen(MainMenu menu) {
        mainMenu = menu;

        setTitle("Disk Scheduling");
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                mainMenu.setVisible(true);
            }
        });

        setLayout(new BorderLayout());
        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildResultPanel(), BorderLayout.CENTER);
    }

    // ---------- building the screen ----------

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(3, 1));

        JPanel settingsRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsRow.add(new JLabel("Algorithm:"));
        algorithmBox = new JComboBox<>(new String[] {"SSTF", "SCAN"});
        settingsRow.add(algorithmBox);

        settingsRow.add(new JLabel("SCAN direction:"));
        directionBox = new JComboBox<>(new String[] {"Toward higher tracks", "Toward lower tracks"});
        directionBox.setEnabled(false);
        settingsRow.add(directionBox);

        settingsRow.add(new JLabel("Starting head position:"));
        headField = new JTextField(5);
        settingsRow.add(headField);

        settingsRow.add(new JLabel("Last track of the disk:"));
        lastTrackField = new JTextField("199", 5);
        settingsRow.add(lastTrackField);

        algorithmBox.addActionListener(e -> {
            boolean isScan = algorithmBox.getSelectedIndex() == 1;
            directionBox.setEnabled(isScan);
        });

        JPanel requestRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        requestRow.add(new JLabel("Request queue (separate with commas or spaces):"));
        requestField = new JTextField(50);
        requestRow.add(requestField);

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
        topPanel.add(requestRow);
        topPanel.add(buttonRow);
        return topPanel;
    }

    private JPanel buildResultPanel() {
        JPanel resultPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // left side: step table and summary
        JPanel leftSide = new JPanel(new BorderLayout(5, 5));
        stepTableModel = new DefaultTableModel(new String[] {
                "Step", "From", "To", "Head Movement", "Note"}, 0);
        JTable stepTable = new JTable(stepTableModel);
        stepTable.setDefaultEditor(Object.class, null);

        summaryArea = new JTextArea(10, 30);
        summaryArea.setEditable(false);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);

        leftSide.add(new JScrollPane(stepTable), BorderLayout.CENTER);
        leftSide.add(new JScrollPane(summaryArea), BorderLayout.SOUTH);

        // right side: graph of the head path
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.add(new JLabel("Head path (each row going down is the next step)"), BorderLayout.NORTH);
        graphPanel = new GraphPanel();
        rightSide.add(new JScrollPane(graphPanel), BorderLayout.CENTER);

        resultPanel.add(leftSide);
        resultPanel.add(rightSide);
        return resultPanel;
    }

    // ---------- button actions ----------

    private void loadSampleData() {
        algorithmBox.setSelectedIndex(0);
        directionBox.setSelectedIndex(0);
        headField.setText("53");
        lastTrackField.setText("199");
        requestField.setText("98, 183, 37, 122, 14, 124, 65, 67, 22, 190, "
                + "5, 150, 78, 111, 45, 170, 8, 135, 60, 101");
        clearResults();
    }

    private void resetEverything() {
        algorithmBox.setSelectedIndex(0);
        directionBox.setSelectedIndex(0);
        headField.setText("");
        lastTrackField.setText("199");
        requestField.setText("");
        clearResults();
    }

    private void runScheduler() {
        // check the last track of the disk
        String lastTrackText = lastTrackField.getText().trim();
        if (lastTrackText.equals("")) {
            showError("Please enter the last track of the disk (for example 199).");
            return;
        }
        int lastTrack;
        try {
            lastTrack = Integer.parseInt(lastTrackText);
        } catch (NumberFormatException e) {
            showError("The last track must be a whole number.");
            return;
        }
        if (lastTrack < 1 || lastTrack > 9999) {
            showError("The last track must be between 1 and 9999.");
            return;
        }

        // check the starting head position
        String headText = headField.getText().trim();
        if (headText.equals("")) {
            showError("Please enter the starting head position.");
            return;
        }
        int startingHead;
        try {
            startingHead = Integer.parseInt(headText);
        } catch (NumberFormatException e) {
            showError("The starting head position must be a whole number.");
            return;
        }
        if (startingHead < 0 || startingHead > lastTrack) {
            showError("The starting head position must be between 0 and " + lastTrack + ".");
            return;
        }

        // check the request queue
        String requestText = requestField.getText().trim();
        if (requestText.equals("")) {
            showError("Please enter at least one request.");
            return;
        }
        String[] pieces = requestText.split("[,\\s]+");
        List<Integer> requests = new ArrayList<>();
        for (String piece : pieces) {
            if (piece.equals("")) {
                continue;
            }
            int trackNumber;
            try {
                trackNumber = Integer.parseInt(piece);
            } catch (NumberFormatException e) {
                showError("\"" + piece + "\" is not a whole number. Use numbers separated by commas or spaces.");
                return;
            }
            if (trackNumber < 0 || trackNumber > lastTrack) {
                showError("Request " + trackNumber + " is outside the disk. Tracks go from 0 to " + lastTrack + ".");
                return;
            }
            requests.add(trackNumber);
        }
        if (requests.size() == 0) {
            showError("Please enter at least one request.");
            return;
        }

        // run the chosen algorithm
        DiskResult result;
        String algorithmName;
        if (algorithmBox.getSelectedIndex() == 0) {
            SstfScheduler scheduler = new SstfScheduler();
            result = scheduler.run(startingHead, requests);
            algorithmName = "SSTF (Shortest Seek Time First)";
        } else {
            ScanScheduler scheduler = new ScanScheduler();
            boolean movingUp = directionBox.getSelectedIndex() == 0;
            result = scheduler.run(startingHead, requests, lastTrack, movingUp);
            if (movingUp) {
                algorithmName = "SCAN (starting toward higher tracks)";
            } else {
                algorithmName = "SCAN (starting toward lower tracks)";
            }
        }

        showResult(result, algorithmName, lastTrack);
    }

    // ---------- showing things on the screen ----------

    private void showResult(DiskResult result, String algorithmName, int lastTrack) {
        stepTableModel.setRowCount(0);
        for (int i = 0; i < result.movements.size(); i++) {
            stepTableModel.addRow(new Object[] {
                    i + 1,
                    result.path.get(i),
                    result.path.get(i + 1),
                    result.movements.get(i),
                    result.notes.get(i)});
        }

        String seekSequence = "";
        for (int i = 0; i < result.path.size(); i++) {
            seekSequence += result.path.get(i);
            if (i < result.path.size() - 1) {
                seekSequence += " -> ";
            }
        }

        String summary = "Algorithm: " + algorithmName + "\n\n";
        summary += "Seek sequence: " + seekSequence + "\n\n";
        summary += "Total head movement: " + result.totalMovement + "\n";
        summary += "Number of requests: " + result.requestsServed + "\n";
        summary += "Average seek distance: " + String.format("%.2f", result.averageSeekDistance)
                + "  (total movement / number of requests)\n";
        summaryArea.setText(summary);

        graphPanel.setPath(result.path, lastTrack);
    }

    private void clearResults() {
        stepTableModel.setRowCount(0);
        summaryArea.setText("");
        graphPanel.setPath(new ArrayList<Integer>(), 199);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---------- the head path graph ----------

    private class GraphPanel extends JPanel {
        private List<Integer> path = new ArrayList<>();
        private int lastTrack = 199;
        private int panelWidth = 600;
        private int sideMargin = 30;
        private int topMargin = 45;
        private int stepHeight = 25;

        public GraphPanel() {
            setPreferredSize(new Dimension(panelWidth, 100));
        }

        public void setPath(List<Integer> newPath, int newLastTrack) {
            path = newPath;
            lastTrack = newLastTrack;
            setPreferredSize(new Dimension(panelWidth, topMargin + path.size() * stepHeight + 20));
            revalidate();
            repaint();
        }

        // converts a track number into an x position on the panel
        private int getX(int track) {
            return sideMargin + track * (panelWidth - 2 * sideMargin) / lastTrack;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (path.size() == 0) {
                return;
            }

            // ruler on top showing the track numbers
            int rulerY = topMargin - 12;
            g.setColor(Color.BLACK);
            g.drawLine(getX(0), rulerY, getX(lastTrack), rulerY);

            int tickGap = 1;
            if (lastTrack > 500) {
                tickGap = 100;
            } else if (lastTrack > 100) {
                tickGap = 25;
            } else if (lastTrack > 20) {
                tickGap = 10;
            }
            for (int track = 0; track <= lastTrack; track += tickGap) {
                int x = getX(track);
                g.drawLine(x, rulerY - 4, x, rulerY + 4);
                g.drawString(String.valueOf(track), x - 8, rulerY - 8);
            }

            // the path: one point for every step
            for (int i = 0; i < path.size(); i++) {
                int x = getX(path.get(i));
                int y = topMargin + i * stepHeight;

                if (i > 0) {
                    int previousX = getX(path.get(i - 1));
                    int previousY = topMargin + (i - 1) * stepHeight;
                    g.setColor(Color.BLUE);
                    g.drawLine(previousX, previousY, x, y);
                }

                if (i == 0) {
                    g.setColor(Color.RED);     // starting position
                } else {
                    g.setColor(Color.BLACK);
                }
                g.fillOval(x - 4, y - 4, 8, 8);
                g.setColor(Color.BLACK);
                g.drawString(String.valueOf(path.get(i)), x + 7, y + 4);
            }
        }
    }
}