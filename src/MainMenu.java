import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("Scheduling Program");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("CPU and Disk Scheduling", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 70, 30, 70));

        JButton cpuButton = new JButton("CPU Scheduling");
        JButton diskButton = new JButton("Disk Scheduling");
        JButton aboutButton = new JButton("About");
        JButton exitButton = new JButton("Exit");

        Font buttonFont = new Font("Arial", Font.PLAIN, 14);
        cpuButton.setFont(buttonFont);
        diskButton.setFont(buttonFont);
        aboutButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        cpuButton.addActionListener(e -> {
            CpuSchedulingScreen screen = new CpuSchedulingScreen(this);
            setVisible(false);
            screen.setVisible(true);
        });

        diskButton.addActionListener(e -> {
            DiskSchedulingScreen screen = new DiskSchedulingScreen(this);
            setVisible(false);
            screen.setVisible(true);
        });

        aboutButton.addActionListener(e -> {
            String aboutText = "Group 4\n\n"
                    + "Members:\n"
                    + "- Theodore Sam Serrano (SJF)\n"
                    + "- Lucky Emmanuel Cardenas (Round Robin)\n"
                    + "- Graciela Anne Tumbaga (SSTF)\n"
                    + "- Shaugn Marcus Concepcion (SCAN)\n";
            JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
        });

        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(cpuButton);
        buttonPanel.add(diskButton);
        buttonPanel.add(aboutButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.CENTER);
    }
}