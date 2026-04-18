package cybersim;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

/**
 * Dashboard - Main control panel shown after successful authentication.
 *
 * Layout (v2 — with MITRE ATT&CK tab):
 *   ┌──────────────────────────────────────────────────┐
 *   │  Top Bar: branding, user info, system status     │
 *   ├──────────────────────────────────────────────────┤
 *   │  Left sidebar │  JTabbedPane                     │
 *   │  (controls)   │  Tab 1: Overview                 │
 *   │               │  Tab 2: Security Event Log       │
 *   │               │  Tab 3: MITRE ATT&CK Mapping     │
 *   └──────────────────────────────────────────────────┘
 *
 * The MITRE tab highlights rows automatically during attack simulation,
 * and all three tabs share the same LogManager and FileManager instances.
 */
public class Dashboard {

    private final String username;
    private final boolean attackerMode;
    private final LogManager logManager;
    private final FileManager fileManager;

    private JFrame frame;
    private JLabel systemStatusLabel;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JButton attackBtn;
    private JButton resetBtn;
    private JTabbedPane tabbedPane;
    private MitrePanel mitrePanel;

    private boolean attackInProgress = false;
    private boolean attackComplete   = false;

    public Dashboard(String username, boolean attackerMode,
                     LogManager logManager, FileManager fileManager) {
        this.username     = username;
        this.attackerMode = attackerMode;
        this.logManager   = logManager;
        this.fileManager  = fileManager;
    }

    public void show() {
        frame = new JFrame("MGM Resorts — Security Operations Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1050, 680);
        frame.setMinimumSize(new Dimension(900, 580));
        UITheme.applyBackground(frame);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG_WHITE);
        root.add(buildTopBar(),   BorderLayout.NORTH);
        root.add(buildMainArea(), BorderLayout.CENTER);

        frame.setContentPane(root);
        UITheme.centerOnScreen(frame);
        frame.setVisible(true);

        logManager.info("Dashboard session started — user: " + username +
            (attackerMode
                ? " [SESSION ORIGIN: Social Engineering Bypass — ATTACKER]"
                : " [LEGITIMATE SESSION]"));

        if (attackerMode) showAttackerAlert();
    }

    // =========================================================================
    // TOP BAR
    // =========================================================================

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.ACCENT_NAVY);
        bar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel brand = new JLabel("MGM RESORTS  |  Security Operations Center");
        brand.setFont(new Font("SansSerif", Font.BOLD, 15));
        brand.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setBackground(UITheme.ACCENT_NAVY);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.BOLD, 14));
        dot.setForeground(UITheme.STATUS_OK);

        systemStatusLabel = new JLabel("System: SECURE");
        systemStatusLabel.setFont(UITheme.FONT_HEADING);
        systemStatusLabel.setForeground(UITheme.STATUS_OK);

        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        statusBox.setBackground(UITheme.ACCENT_NAVY);
        statusBox.add(dot);
        statusBox.add(systemStatusLabel);

        JLabel userLbl = new JLabel("👤  " + username +
            (attackerMode ? "  [⚠ ATTACKER SESSION]" : "  [Authenticated]"));
        userLbl.setFont(UITheme.FONT_BODY);
        userLbl.setForeground(attackerMode ? new Color(0xFBBF24) : new Color(0xA5B4C8));

        right.add(statusBox);
        right.add(userLbl);

        bar.add(brand, BorderLayout.WEST);
        bar.add(right,  BorderLayout.EAST);
        return bar;
    }

    // =========================================================================
    // MAIN AREA — sidebar + tabbed pane
    // =========================================================================

    private JSplitPane buildMainArea() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            buildSidebar(),
            buildTabbedPane()
        );
        split.setDividerLocation(295);
        split.setResizeWeight(0.28);
        split.setBorder(null);
        split.setDividerSize(4);
        return split;
    }

    // =========================================================================
    // SIDEBAR
    // =========================================================================

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 10));

        panel.add(buildSessionCard());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildAttackCard());
        panel.add(Box.createVerticalStrut(12));
        panel.add(buildRecoveryCard());
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel buildSessionCard() {
        JPanel card = UITheme.sectionPanel("Session Information");
        card.setLayout(new GridLayout(4, 1, 0, 4));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (attackerMode) card.setBackground(new Color(0xFFF1F2));

        card.add(infoRow("User:",         username));
        card.add(infoRow("Department:",   "Finance (impersonated)"));
        card.add(infoRow("Access Level:", attackerMode ? "ADMIN (social eng.)" : "STANDARD"));
        card.add(infoRow("MFA:",          attackerMode ? "⚠ BYPASSED" : "✔ Push Notification"));
        return card;
    }

    private JPanel buildAttackCard() {
        JPanel card = UITheme.sectionPanel("Attack Simulation Engine");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>Runs the 7-stage MGM breach chain.<br>" +
            "Log + MITRE table update live.</html>");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setForeground(UITheme.STATUS_COMPROMISED);
        progressBar.setBackground(new Color(0xFEE2E2));
        progressBar.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressLabel = new JLabel("Ready to simulate attack");
        progressLabel.setFont(UITheme.FONT_SMALL);
        progressLabel.setForeground(UITheme.TEXT_SECONDARY);
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        attackBtn = UITheme.dangerButton("▶  Simulate Attack");
        attackBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        attackBtn.addActionListener(e -> startAttack());

        JButton viewFiles = UITheme.secondaryButton("📂  View Demo Files");
        viewFiles.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewFiles.addActionListener(e -> openDemoFolder());

        card.add(desc);
        card.add(Box.createVerticalStrut(8));
        card.add(progressBar);
        card.add(Box.createVerticalStrut(4));
        card.add(progressLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(attackBtn);
        card.add(Box.createVerticalStrut(6));
        card.add(viewFiles);
        return card;
    }

    private JPanel buildRecoveryCard() {
        JPanel card = UITheme.sectionPanel("Incident Response");
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html>Restore files and reset all<br>indicators to pre-attack state.</html>");
        desc.setFont(UITheme.FONT_SMALL);
        desc.setForeground(UITheme.TEXT_SECONDARY);
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        resetBtn = UITheme.primaryButton("🔄  Reset & Recover");
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.setEnabled(false);
        resetBtn.addActionListener(e -> resetSystem());

        JButton logWin = UITheme.secondaryButton("📋  Open Log Window");
        logWin.setAlignmentX(Component.LEFT_ALIGNMENT);
        logWin.addActionListener(e -> new LogPanel(logManager).openStandaloneWindow());

        card.add(desc);
        card.add(Box.createVerticalStrut(10));
        card.add(resetBtn);
        card.add(Box.createVerticalStrut(6));
        card.add(logWin);
        return card;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
        lbl.setForeground(UITheme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(90, 18));
        JLabel val = new JLabel(value);
        val.setFont(UITheme.FONT_SMALL);
        val.setForeground(UITheme.TEXT_PRIMARY);
        row.add(lbl);
        row.add(val);
        return row;
    }

    // =========================================================================
    // TABBED PANE — three tabs
    // =========================================================================

    private JTabbedPane buildTabbedPane() {
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(UITheme.FONT_HEADING);
        tabbedPane.setBackground(UITheme.BG_WHITE);
        tabbedPane.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UITheme.BORDER_COLOR));

        // Tab 1 — Overview dashboard cards
        tabbedPane.addTab("  📊  Overview  ", buildOverviewTab());

        // Tab 2 — Live security event log
        tabbedPane.addTab("  📋  Security Log  ", new LogPanel(logManager));

        // Tab 3 — MITRE ATT&CK mapping (the new panel)
        mitrePanel = new MitrePanel(logManager);
        tabbedPane.addTab("  🛡  MITRE ATT&CK  ", mitrePanel);
        tabbedPane.setBackgroundAt(2, new Color(0xEFF6FF));
        tabbedPane.setForegroundAt(2, UITheme.ACCENT_BLUE);

        return tabbedPane;
    }

    // =========================================================================
    // TAB 1: OVERVIEW
    // =========================================================================

    private JPanel buildOverviewTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.BG_WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(buildStatCard("Authentication Status",
            attackerMode ? "⚠ Compromised" : "✔ Secure",
            attackerMode ? "Session via social engineering MFA bypass"
                         : "Multi-factor authentication verified",
            attackerMode ? UITheme.STATUS_COMPROMISED : UITheme.STATUS_OK), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(buildStatCard("Active Session",
            username,
            "Department: Finance  |  Access: " + (attackerMode ? "ADMIN (escalated)" : "Standard"),
            UITheme.ACCENT_BLUE), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(buildStatCard("Demo Files",
            fileManager.getFileNames().length + " corporate files",
            "Location: " + fileManager.getDemoDir(),
            UITheme.STATUS_OK), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(buildStatCard("Simulation Scenario",
            "MGM Resorts Breach",
            "September 2023  |  ALPHV/BlackCat + Scattered Spider",
            UITheme.ACCENT_NAVY), gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.weighty = 0.25;
        panel.add(buildInstructionCard(), gbc);

        return panel;
    }

    private JPanel buildStatCard(String title, String value, String sub, Color accent) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        JLabel t = new JLabel(title.toUpperCase());
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(UITheme.TEXT_SECONDARY);
        JLabel v = new JLabel(value);
        v.setFont(new Font("SansSerif", Font.BOLD, 17));
        v.setForeground(accent);
        JLabel s = new JLabel(sub);
        s.setFont(UITheme.FONT_SMALL);
        s.setForeground(UITheme.TEXT_SECONDARY);
        card.add(t); card.add(v); card.add(s);
        return card;
    }

    private JPanel buildInstructionCard() {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UITheme.ACCENT_BLUE_LT);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT_BLUE, 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel icon = new JLabel("ℹ");
        icon.setFont(new Font("SansSerif", Font.BOLD, 22));
        icon.setForeground(UITheme.ACCENT_BLUE);

        JPanel text = new JPanel(new GridLayout(2, 1, 0, 3));
        text.setOpaque(false);

        JLabel heading = new JLabel("Demo walkthrough");
        heading.setFont(UITheme.FONT_HEADING);
        heading.setForeground(UITheme.ACCENT_BLUE);

        JLabel steps = new JLabel(
            "1. Click 'Simulate Attack' in the sidebar  →  " +
            "2. The MITRE ATT&CK tab opens automatically — watch techniques highlight live  →  " +
            "3. Click any row for a full technique description  →  " +
            "4. Switch to Security Log for the full event timeline  →  " +
            "5. Click 'Reset & Recover' to restore");
        steps.setFont(UITheme.FONT_SMALL);
        steps.setForeground(UITheme.ACCENT_BLUE);

        text.add(heading);
        text.add(steps);

        card.add(icon, BorderLayout.WEST);
        card.add(text, BorderLayout.CENTER);
        return card;
    }

    // =========================================================================
    // ACTIONS
    // =========================================================================

    private void startAttack() {
        if (attackInProgress || attackComplete) return;

        int confirm = JOptionPane.showConfirmDialog(frame,
            "This will simulate a full ransomware attack chain (MGM Resorts breach).\n\n" +
            "• 7 attack stages logged in real time\n" +
            "• MITRE ATT&CK tab highlights each technique as it fires\n" +
            "• Demo files will be 'encrypted' (safe simulation)\n" +
            "• Takes approximately 20 seconds\n\n" +
            "Tip: The MITRE ATT&CK tab will open automatically so you can watch live.",
            "Confirm Attack Simulation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        attackInProgress = true;
        attackBtn.setEnabled(false);
        attackBtn.setText("⏳  Attack in Progress...");

        // Auto-switch to MITRE tab so audience sees live row highlighting
        tabbedPane.setSelectedIndex(2);

        AttackSimulator simulator = new AttackSimulator(
            logManager, fileManager, progressBar, progressLabel,
            success -> {
                attackInProgress = false;
                attackComplete   = true;
                updateSystemCompromised();
            }
        );
        simulator.setMitrePanel(mitrePanel);
        simulator.run();
    }

    private void updateSystemCompromised() {
        systemStatusLabel.setText("System: COMPROMISED ⚠");
        systemStatusLabel.setForeground(UITheme.STATUS_COMPROMISED);
        progressLabel.setText("Attack complete — system compromised");
        attackBtn.setText("✘  Attack Complete");
        resetBtn.setEnabled(true);

        JOptionPane.showMessageDialog(frame,
            "⚠  ATTACK SIMULATION COMPLETE\n\n" +
            "All 7 stages executed and mapped to MITRE ATT&CK:\n\n" +
            "  T1593.002  Reconnaissance — LinkedIn OSINT\n" +
            "  T1078      Initial Access — Valid Accounts\n" +
            "  T1566/T1111 Defense Evasion — MFA Bypass\n" +
            "  T1548      Privilege Escalation — Okta misconfiguration\n" +
            "  T1021      Lateral Movement — ESXi/vCenter\n" +
            "  T1041      Exfiltration — 6 TB over C2\n" +
            "  T1486      Impact — Data Encrypted for Impact\n\n" +
            "Click any row in the MITRE ATT&CK tab for technique details.\n" +
            "Click 'Reset & Recover' when you are ready to restore the system.",
            "Attack Simulation Complete",
            JOptionPane.ERROR_MESSAGE
        );
    }

    private void resetSystem() {
        logManager.info("=== INCIDENT RESPONSE INITIATED ===");
        logManager.info("Stage 1: Isolating compromised network segments...");
        logManager.info("Stage 2: Revoking all active attacker sessions...");
        logManager.info("Stage 3: Restoring files from verified clean backups...");

        try {
            fileManager.restoreFiles();
            logManager.info("File restoration complete — all " +
                fileManager.getFileNames().length + " files recovered");
            logManager.info("Stage 4: System status reset — normal operations resumed");
            logManager.info("=== SYSTEM RECOVERY COMPLETE ===");

            // Clear MITRE highlights
            mitrePanel.resetHighlighting();

            // Reset controls
            attackComplete = false;
            progressBar.setValue(0);
            progressLabel.setText("System recovered — ready for next simulation");
            attackBtn.setEnabled(true);
            attackBtn.setText("▶  Simulate Attack");
            resetBtn.setEnabled(false);
            systemStatusLabel.setText("System: SECURE");
            systemStatusLabel.setForeground(UITheme.STATUS_OK);

            JOptionPane.showMessageDialog(frame,
                "✔  SYSTEM RECOVERY SUCCESSFUL\n\n" +
                "• All demo files restored to original content\n" +
                "• MITRE ATT&CK highlighting cleared\n" +
                "• Log history preserved for forensic review\n\n" +
                "The system is ready for another demonstration.",
                "Recovery Complete",
                JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {
            logManager.alert("Recovery error: " + e.getMessage());
            JOptionPane.showMessageDialog(frame,
                "Recovery error:\n" + e.getMessage(),
                "Recovery Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDemoFolder() {
        try {
            java.awt.Desktop.getDesktop().open(new File(fileManager.getDemoDir()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame,
                "Demo files are located at:\n" + fileManager.getDemoDir(),
                "Demo Files Location", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAttackerAlert() {
        Timer timer = new Timer(800, e -> JOptionPane.showMessageDialog(frame,
            "⚠  ATTACKER SESSION DETECTED\n\n" +
            "This session was established via social engineering MFA bypass.\n" +
            "In a real environment, this would go UNDETECTED without proper controls.\n\n" +
            "The attacker now has full admin access to all corporate systems.\n\n" +
            "Click the MITRE ATT&CK tab to review applicable techniques,\n" +
            "then click 'Simulate Attack' to see what happens next.",
            "🚨 Social Engineering Access Detected",
            JOptionPane.WARNING_MESSAGE
        ));
        timer.setRepeats(false);
        timer.start();
    }
}
