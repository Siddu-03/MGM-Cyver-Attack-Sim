package cybersim;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

/**
 * MitrePanel - MITRE ATT&CK Framework Mapping Panel.
 *
 * Displays a professional table that maps each simulated attack stage to its
 * corresponding MITRE ATT&CK tactic, technique name, and technique ID.
 *
 * MITRE ATT&CK (https://attack.mitre.org) is the industry-standard knowledge base
 * of adversary tactics and techniques used by security professionals worldwide.
 *
 * Features:
 *   - Color-coded rows by tactic category
 *   - Dynamic row highlighting as each attack stage fires during simulation
 *   - Click any row for a detailed technique description popup
 *   - Tooltips on each cell with additional context
 *   - Auto-scrolls to the active technique during simulation
 *   - Reset capability to clear highlighting after recovery
 */
public class MitrePanel extends JPanel {

    // -------------------------------------------------------------------------
    // MITRE ATT&CK Data Model
    // -------------------------------------------------------------------------

    /**
     * Represents one row in the MITRE ATT&CK mapping table.
     * Each entry links a simulation stage to a real MITRE technique.
     */
    public static class MitreEntry {
        public final int    rowIndex;       // Position in table (0-based)
        public final String tactic;         // MITRE Tactic (column 1)
        public final String simulationEvent;// What the simulation does (column 2)
        public final String techniqueName;  // MITRE Technique name (column 3)
        public final String techniqueId;    // MITRE Technique ID, e.g. T1078 (column 4)
        public final String description;    // Full description shown in popup
        public final String url;            // MITRE ATT&CK URL for reference
        public final Color  tacticColor;    // Row background tint for this tactic

        public MitreEntry(int rowIndex, String tactic, String simulationEvent,
                          String techniqueName, String techniqueId,
                          String description, String url, Color tacticColor) {
            this.rowIndex        = rowIndex;
            this.tactic          = tactic;
            this.simulationEvent = simulationEvent;
            this.techniqueName   = techniqueName;
            this.techniqueId     = techniqueId;
            this.description     = description;
            this.url             = url;
            this.tacticColor     = tacticColor;
        }
    }

    // -------------------------------------------------------------------------
    // MITRE ATT&CK Mapping Data
    // Each entry corresponds to one or more stages of the MGM attack simulation.
    // Row indices map to the STAGES array in AttackSimulator.
    // -------------------------------------------------------------------------

    /** Maps AttackSimulator stage index → MitreEntry row index to highlight. */
    public static final Map<Integer, Integer> STAGE_TO_MITRE_ROW = new HashMap<>();

    /** The complete MITRE mapping table definition. */
    public static final MitreEntry[] MITRE_ENTRIES = {

        // Row 0 — Reconnaissance
        new MitreEntry(0,
            "Reconnaissance",
            "LinkedIn OSINT — harvesting employee names, departments, roles",
            "Search Open Websites / Domains",
            "T1593.002",
            "WHAT: Adversaries search freely available websites (LinkedIn, company pages) " +
            "to identify employees and organizational structure.\n\n" +
            "HOW IN MGM BREACH: Scattered Spider used LinkedIn to identify a specific MGM " +
            "Finance employee with elevated system access, then used that information to " +
            "convincingly impersonate them in a phone call.\n\n" +
            "MITRE TACTIC: Reconnaissance (TA0043)\n" +
            "DETECTION: Monitor for bulk LinkedIn scraping; train employees on oversharing.\n" +
            "MITIGATION: Limit public exposure of org charts and employee directories.",
            "https://attack.mitre.org/techniques/T1593/002/",
            new Color(0xEFF6FF)   // light blue
        ),

        // Row 1 — Initial Access via Valid Accounts
        new MitreEntry(1,
            "Initial Access",
            "Login with valid employee credentials after social engineering",
            "Valid Accounts",
            "T1078",
            "WHAT: Adversaries use legitimate credentials to gain initial access, " +
            "bypassing many security controls since the login appears genuine.\n\n" +
            "HOW IN MGM BREACH: After convincing the helpdesk to reset MFA, the attacker " +
            "used the victim employee's actual username and authenticated session to log in " +
            "to MGM's Okta identity platform.\n\n" +
            "MITRE TACTIC: Initial Access (TA0001)\n" +
            "DETECTION: Anomalous login locations, times, or device fingerprints.\n" +
            "MITIGATION: Conditional access policies, device trust, login anomaly detection.",
            "https://attack.mitre.org/techniques/T1078/",
            new Color(0xFFF7ED)   // light orange
        ),

        // Row 2 — Social Engineering / MFA Bypass
        new MitreEntry(2,
            "Defense Evasion",
            "IT Helpdesk phone call — attacker impersonates employee to bypass MFA",
            "Multi-Factor Auth Interception / Social Engineering",
            "T1566 / T1111",
            "WHAT: T1566 (Phishing) covers social engineering broadly; T1111 covers " +
            "adversaries defeating MFA through deception rather than technical exploits.\n\n" +
            "HOW IN MGM BREACH: The attacker called MGM's IT helpdesk, claimed to be a " +
            "stranded employee whose phone was dead, and requested an MFA reset. The " +
            "operator did not follow strict identity verification procedures.\n\n" +
            "MITRE TACTIC: Defense Evasion (TA0005) + Initial Access (TA0001)\n" +
            "DETECTION: Flag all helpdesk MFA resets; require secondary manager approval.\n" +
            "MITIGATION: Never reset MFA via phone; use in-person or video verification only.",
            "https://attack.mitre.org/techniques/T1111/",
            new Color(0xFEF9C3)   // light yellow
        ),

        // Row 3 — Privilege Escalation
        new MitreEntry(3,
            "Privilege Escalation",
            "Okta misconfiguration exploited — Domain Admin token obtained",
            "Abuse Elevation Control Mechanism",
            "T1548",
            "WHAT: After gaining initial access, adversaries exploit misconfigurations " +
            "or weak privilege controls to escalate from a standard user to an administrator.\n\n" +
            "HOW IN MGM BREACH: The attacker leveraged a misconfigured Okta super-admin " +
            "role that had not been scoped correctly, gaining Domain Admin privileges over " +
            "MGM's Active Directory environment.\n\n" +
            "MITRE TACTIC: Privilege Escalation (TA0004)\n" +
            "DETECTION: Alert on new admin role assignments; privileged access workstations.\n" +
            "MITIGATION: Principle of least privilege; regular access reviews; PAM solutions.",
            "https://attack.mitre.org/techniques/T1548/",
            new Color(0xFFF1F2)   // light red
        ),

        // Row 4 — Lateral Movement
        new MitreEntry(4,
            "Lateral Movement",
            "ESXi hypervisor scanning — 1,300+ servers compromised via vCenter",
            "Remote Services",
            "T1021",
            "WHAT: Adversaries move through a network by leveraging remote services " +
            "(RDP, SSH, vCenter) to access and compromise additional systems.\n\n" +
            "HOW IN MGM BREACH: With Domain Admin access, the attacker connected to " +
            "VMware vCenter and gained control of over 1,300 ESXi hypervisor servers, " +
            "effectively controlling all virtual machines across MGM's data centers.\n\n" +
            "MITRE TACTIC: Lateral Movement (TA0008)\n" +
            "DETECTION: Unusual vCenter API calls; new admin connections to hypervisors.\n" +
            "MITIGATION: Network segmentation; privileged access only from jump servers.",
            "https://attack.mitre.org/techniques/T1021/",
            new Color(0xF0FDF4)   // light green
        ),

        // Row 5 — Data Exfiltration
        new MitreEntry(5,
            "Exfiltration",
            "6 TB of PII, financial records, and reservations data exfiltrated to C2",
            "Exfiltration Over C2 Channel",
            "T1041",
            "WHAT: After collecting target data, adversaries transfer it to external " +
            "attacker-controlled infrastructure over the established command-and-control channel.\n\n" +
            "HOW IN MGM BREACH: Scattered Spider exfiltrated approximately 6 TB of data " +
            "including guest PII, employee records, and financial data before deploying " +
            "ransomware — giving them double leverage: ransom + data leak threat.\n\n" +
            "MITRE TACTIC: Exfiltration (TA0010)\n" +
            "DETECTION: DLP tools; anomalous outbound traffic volume; UEBA alerts.\n" +
            "MITIGATION: Data loss prevention; egress filtering; network traffic analysis.",
            "https://attack.mitre.org/techniques/T1041/",
            new Color(0xFDF4FF)   // light purple
        ),

        // Row 6 — Ransomware / Impact
        new MitreEntry(6,
            "Impact",
            "ALPHV/BlackCat ransomware deployed — files encrypted across 1,300+ hosts",
            "Data Encrypted for Impact",
            "T1486",
            "WHAT: Adversaries encrypt files on target systems to deny access, then " +
            "demand a ransom payment for the decryption key.\n\n" +
            "HOW IN MGM BREACH: The ALPHV/BlackCat ransomware payload was deployed " +
            "across all compromised ESXi servers simultaneously, encrypting VM disk " +
            "images and taking down slot machines, hotel check-in systems, and ATMs. " +
            "MGM refused to pay the $15 million ransom.\n\n" +
            "MITRE TACTIC: Impact (TA0040)\n" +
            "DETECTION: Honeypot files; file system activity monitoring; canary tokens.\n" +
            "MITIGATION: Offline backups; immutable backup storage; rapid IR playbooks.",
            "https://attack.mitre.org/techniques/T1486/",
            new Color(0xFFF1F2)   // light red (impact = most severe)
        ),
    };

    // -------------------------------------------------------------------------
    // Stage → Row mapping (which MITRE row to highlight when a stage fires)
    // Maps AttackSimulator STAGES array index to MITRE_ENTRIES row index
    // -------------------------------------------------------------------------
    static {
        STAGE_TO_MITRE_ROW.put(0,  0);   // Stage 1 first log  → Reconnaissance
        STAGE_TO_MITRE_ROW.put(1,  0);   // Stage 1 second log → Reconnaissance (still)
        STAGE_TO_MITRE_ROW.put(2,  2);   // Stage 2 first log  → Social Engineering
        STAGE_TO_MITRE_ROW.put(3,  2);   // Stage 2 second log → Social Engineering
        STAGE_TO_MITRE_ROW.put(4,  1);   // Stage 3 MFA bypass → Valid Accounts (T1078)
        STAGE_TO_MITRE_ROW.put(5,  1);   // Stage 3 session    → Valid Accounts
        STAGE_TO_MITRE_ROW.put(6,  3);   // Stage 4 first log  → Privilege Escalation
        STAGE_TO_MITRE_ROW.put(7,  3);   // Stage 4 second log → Privilege Escalation
        STAGE_TO_MITRE_ROW.put(8,  4);   // Stage 5 first log  → Lateral Movement
        STAGE_TO_MITRE_ROW.put(9,  4);   // Stage 5 second log → Lateral Movement
        STAGE_TO_MITRE_ROW.put(10, 5);   // Stage 6 exfil      → Exfiltration
        STAGE_TO_MITRE_ROW.put(11, 5);   // Stage 6 volume     → Exfiltration
        STAGE_TO_MITRE_ROW.put(12, 6);   // Stage 7 deploy     → Impact / Ransomware
        STAGE_TO_MITRE_ROW.put(13, 6);   // Stage 7 encrypt    → Impact / Ransomware
        STAGE_TO_MITRE_ROW.put(14, 6);   // Attack complete    → Impact / Ransomware
    }

    // -------------------------------------------------------------------------
    // Table Column Indices
    // -------------------------------------------------------------------------
    private static final int COL_TACTIC    = 0;
    private static final int COL_EVENT     = 1;
    private static final int COL_TECHNIQUE = 2;
    private static final int COL_ID        = 3;

    // -------------------------------------------------------------------------
    // Instance State
    // -------------------------------------------------------------------------
    private final LogManager logManager;
    private JTable table;
    private DefaultTableModel tableModel;
    private MitreTableRenderer renderer;
    private int activeRow = -1;  // Currently highlighted row (-1 = none)

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public MitrePanel(LogManager logManager) {
        this.logManager = logManager;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG_WHITE);

        add(buildHeaderBar(),  BorderLayout.NORTH);
        add(buildTableArea(),  BorderLayout.CENTER);
        add(buildLegend(),     BorderLayout.SOUTH);
    }

    // -------------------------------------------------------------------------
    // UI Construction
    // -------------------------------------------------------------------------

    /** Top bar with title and description. */
    private JPanel buildHeaderBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(UITheme.BG_PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);

        JLabel title = new JLabel("MITRE ATT&CK® Framework Mapping");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel(
            "MGM Resorts Breach — September 2023  |  Tactic → Technique mapping per simulated attack stage");
        subtitle.setFont(UITheme.FONT_SMALL);
        subtitle.setForeground(UITheme.TEXT_SECONDARY);

        left.add(title);
        left.add(subtitle);

        // Right: link badge
        JLabel badge = new JLabel("attack.mitre.org  ↗");
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setForeground(UITheme.ACCENT_BLUE);
        badge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.ACCENT_BLUE, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        badge.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        badge.setToolTipText("https://attack.mitre.org — The industry standard adversary knowledge base");

        bar.add(left,  BorderLayout.WEST);
        bar.add(badge, BorderLayout.EAST);
        return bar;
    }

    /** Scrollable table area with custom renderer. */
    private JScrollPane buildTableArea() {
        // Column headers
        String[] columns = { "MITRE Tactic", "Simulation Event", "ATT&CK Technique", "Technique ID" };

        // Build table model (non-editable)
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        // Populate rows from MITRE_ENTRIES
        for (MitreEntry entry : MITRE_ENTRIES) {
            tableModel.addRow(new Object[]{
                entry.tactic,
                entry.simulationEvent,
                entry.techniqueName,
                entry.techniqueId
            });
        }

        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(46);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(UITheme.BG_WHITE);
        table.setFillsViewportHeight(true);

        // Custom renderer for color-coding and highlighting
        renderer = new MitreTableRenderer();
        for (int col = 0; col < table.getColumnCount(); col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(renderer);
        }

        // Column widths
        table.getColumnModel().getColumn(COL_TACTIC).setPreferredWidth(140);
        table.getColumnModel().getColumn(COL_EVENT).setPreferredWidth(310);
        table.getColumnModel().getColumn(COL_TECHNIQUE).setPreferredWidth(220);
        table.getColumnModel().getColumn(COL_ID).setPreferredWidth(100);

        // Style the header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(UITheme.ACCENT_NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);

        // Click row → show technique detail popup
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && row < MITRE_ENTRIES.length) {
                    showTechniqueDetail(MITRE_ENTRIES[row]);
                }
            }

            // Tooltip on hover
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0 && row < MITRE_ENTRIES.length) {
                    table.setToolTipText("Click for full technique details: " +
                        MITRE_ENTRIES[row].techniqueId + " — " + MITRE_ENTRIES[row].techniqueName);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        scroll.setBackground(UITheme.BG_WHITE);
        scroll.getViewport().setBackground(UITheme.BG_WHITE);
        return scroll;
    }

    /** Bottom legend explaining color scheme. */
    private JPanel buildLegend() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        bar.setBackground(UITheme.BG_PANEL);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        bar.add(legendItem(new Color(0xEFF6FF), "Reconnaissance"));
        bar.add(legendItem(new Color(0xFFF7ED), "Initial Access"));
        bar.add(legendItem(new Color(0xFEF9C3), "Defense Evasion"));
        bar.add(legendItem(new Color(0xFFF1F2), "Privilege Esc. / Impact"));
        bar.add(legendItem(new Color(0xF0FDF4), "Lateral Movement"));
        bar.add(legendItem(new Color(0xFDF4FF), "Exfiltration"));

        // Active highlight swatch
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(1, 20));
        sep.setBackground(UITheme.BORDER_COLOR);
        bar.add(sep);

        bar.add(legendItem(new Color(0xFEF08A), "▶ Active Stage (live)"));
        bar.add(new JLabel(" — Click any row for technique details"));

        return bar;
    }

    private JPanel legendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        item.setOpaque(false);

        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(UITheme.TEXT_SECONDARY);

        item.add(swatch);
        item.add(lbl);
        return item;
    }

    // -------------------------------------------------------------------------
    // Public API — called by AttackSimulator during simulation
    // -------------------------------------------------------------------------

    /**
     * Highlight the MITRE row corresponding to the given simulator stage index.
     * Called from AttackSimulator on the EDT for each attack stage.
     *
     * @param simulatorStageIndex — index into AttackSimulator.STAGES[]
     */
    public void highlightStage(int simulatorStageIndex) {
        Integer mitreRow = STAGE_TO_MITRE_ROW.get(simulatorStageIndex);
        if (mitreRow == null) return;

        activeRow = mitreRow;
        renderer.setActiveRow(activeRow);
        table.repaint();

        // Auto-scroll table to show the active row
        Rectangle cellRect = table.getCellRect(activeRow, 0, true);
        table.scrollRectToVisible(cellRect);

        // Log the MITRE technique activation
        MitreEntry entry = MITRE_ENTRIES[activeRow];
        logManager.alert("MITRE " + entry.techniqueId + " [" + entry.tactic + "] — " + entry.techniqueName);
    }

    /**
     * Reset all highlighting — called when the system is recovered.
     */
    public void resetHighlighting() {
        activeRow = -1;
        renderer.setActiveRow(-1);
        table.repaint();
        logManager.info("MITRE ATT&CK mapping reset — all technique highlights cleared");
    }

    // -------------------------------------------------------------------------
    // Technique Detail Popup
    // -------------------------------------------------------------------------

    /**
     * Show a detailed information dialog for a clicked MITRE technique.
     * Provides academic context about the technique and how it was used in the MGM breach.
     */
    private void showTechniqueDetail(MitreEntry entry) {
        // Header panel
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 3));
        header.setBackground(UITheme.ACCENT_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        JLabel idLabel = new JLabel(entry.techniqueId + "  —  " + entry.techniqueName);
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        idLabel.setForeground(Color.WHITE);

        JLabel tacticLabel = new JLabel("Tactic: " + entry.tactic);
        tacticLabel.setFont(UITheme.FONT_BODY);
        tacticLabel.setForeground(new Color(0xA5B4C8));

        JLabel urlLabel = new JLabel(entry.url);
        urlLabel.setFont(UITheme.FONT_SMALL);
        urlLabel.setForeground(new Color(0x60A5FA));

        header.add(idLabel);
        header.add(tacticLabel);
        header.add(urlLabel);

        // Body text
        JTextArea body = new JTextArea(entry.description);
        body.setFont(UITheme.FONT_BODY);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        body.setEditable(false);
        body.setBackground(UITheme.BG_WHITE);
        body.setForeground(UITheme.TEXT_PRIMARY);
        body.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        body.setPreferredSize(new Dimension(480, 220));

        // Assemble dialog content
        JPanel content = new JPanel(new BorderLayout());
        content.add(header, BorderLayout.NORTH);
        content.add(new JScrollPane(body), BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
            this,
            content,
            "MITRE ATT&CK — Technique Detail",
            JOptionPane.PLAIN_MESSAGE
        );
    }

    // -------------------------------------------------------------------------
    // Custom Table Cell Renderer
    // -------------------------------------------------------------------------

    /**
     * MitreTableRenderer — Renders table cells with:
     *   - Per-row tactic color (default background)
     *   - Bright yellow highlight for the currently active row
     *   - Bold font for the Technique ID column
     *   - Alternating row shading when not highlighted
     */
    private static class MitreTableRenderer extends DefaultTableCellRenderer {

        // Bright yellow used when a row becomes active during simulation
        private static final Color ACTIVE_BG   = new Color(0xFEF08A);  // yellow
        private static final Color ACTIVE_FG   = new Color(0x78350F);  // dark amber text
        private static final Color ACTIVE_BORDER = new Color(0xF59E0B);

        private int activeRow = -1;

        public void setActiveRow(int row) { this.activeRow = row; }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

            JLabel label = (JLabel) c;
            label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));

            MitreEntry entry = (row < MITRE_ENTRIES.length) ? MITRE_ENTRIES[row] : null;

            if (row == activeRow) {
                // ── ACTIVE (currently firing during simulation) ──
                label.setBackground(ACTIVE_BG);
                label.setForeground(ACTIVE_FG);
                label.setFont(new Font("SansSerif", Font.BOLD, 12));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(2, 0, 2, 0, ACTIVE_BORDER),
                    BorderFactory.createEmptyBorder(3, 10, 3, 10)
                ));

                // Add "▶ ACTIVE" indicator to the tactic column
                if (column == COL_TACTIC && entry != null) {
                    label.setText("▶  " + entry.tactic);
                }

            } else if (isSelected) {
                // ── SELECTED (user clicked) ──
                label.setBackground(UITheme.ACCENT_BLUE_LT);
                label.setForeground(UITheme.ACCENT_BLUE);
                label.setFont(UITheme.FONT_BODY);

            } else if (entry != null) {
                // ── NORMAL — use tactic-specific color ──
                label.setBackground(entry.tacticColor);
                label.setForeground(UITheme.TEXT_PRIMARY);
                label.setFont(UITheme.FONT_BODY);
            }

            // Make the Technique ID column bold and monospaced for emphasis
            if (column == COL_ID) {
                label.setFont(new Font("Monospaced", Font.BOLD, 12));
                label.setForeground(row == activeRow ? ACTIVE_FG : UITheme.ACCENT_NAVY);
                label.setHorizontalAlignment(SwingConstants.CENTER);
            } else {
                label.setHorizontalAlignment(SwingConstants.LEFT);
            }

            // Tooltip per cell for extra context
            if (entry != null) {
                label.setToolTipText(switch (column) {
                    case COL_TACTIC    -> "MITRE Tactic: " + entry.tactic + " — click row for full details";
                    case COL_EVENT     -> entry.simulationEvent;
                    case COL_TECHNIQUE -> entry.techniqueName + " (" + entry.techniqueId + ")";
                    case COL_ID        -> "Reference: " + entry.url;
                    default            -> null;
                });
            }

            label.setOpaque(true);
            return label;
        }
    }
}
