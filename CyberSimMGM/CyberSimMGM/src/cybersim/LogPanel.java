package cybersim;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;

/**
 * LogPanel - Real-time security event log viewer.
 *
 * Extends JPanel so it can be embedded directly into a JTabbedPane tab
 * (Dashboard uses it for the "Security Log" tab) or opened as a
 * standalone window via openStandaloneWindow().
 *
 * Log entries are colour-coded by severity:
 *   INFO    → dark text on white
 *   WARNING → amber text
 *   ALERT   → red text, bold
 *
 * The panel registers itself as a LogManager.LogListener and appends
 * new entries in real time on the Event Dispatch Thread, auto-scrolling
 * to the bottom so the latest event is always visible.
 */
public class LogPanel extends JPanel {

    // -------------------------------------------------------------------------
    // Colour scheme for log levels
    // -------------------------------------------------------------------------
    private static final Color CLR_INFO    = new Color(0x1E293B);   // dark slate
    private static final Color CLR_WARNING = new Color(0xB45309);   // amber
    private static final Color CLR_ALERT   = new Color(0xDC2626);   // red
    private static final Color CLR_BG      = new Color(0x0F172A);   // near-black terminal bg
    private static final Color CLR_LINE_BG = new Color(0x1E293B);   // slightly lighter rows

    // -------------------------------------------------------------------------
    // Instance state
    // -------------------------------------------------------------------------
    private final LogManager   logManager;
    private final JTextPane    textPane;
    private final StyledDocument doc;

    // Reusable AttributeSet objects (built once, reused per entry)
    private final SimpleAttributeSet attrInfo;
    private final SimpleAttributeSet attrWarning;
    private final SimpleAttributeSet attrAlert;
    private final SimpleAttributeSet attrNewline;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public LogPanel(LogManager logManager) {
        this.logManager = logManager;

        setLayout(new BorderLayout(0, 0));
        setBackground(CLR_BG);

        // ── Build styled text pane ──
        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setBackground(CLR_BG);
        textPane.setFont(UITheme.FONT_MONO_SM);
        textPane.setForeground(CLR_INFO);
        textPane.setCaretColor(CLR_INFO);
        textPane.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        doc = textPane.getStyledDocument();

        // Pre-build attribute sets
        attrInfo    = buildAttr(CLR_INFO,    false);
        attrWarning = buildAttr(CLR_WARNING, false);
        attrAlert   = buildAttr(CLR_ALERT,   true);
        attrNewline = buildAttr(CLR_INFO,    false);

        // ── Scroll pane ──
        JScrollPane scroll = new JScrollPane(textPane,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(CLR_BG);
        scroll.getViewport().setBackground(CLR_BG);
        scroll.getVerticalScrollBar().setBackground(new Color(0x334155));

        // ── Header bar ──
        JPanel header = buildHeader();

        add(header, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);

        // Load any existing log entries (written before this panel was created)
        loadExistingLogs();

        // Register for future entries
        logManager.addListener(this::appendEntry);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Opens this log panel in its own JFrame window.
     * Useful for full-screen log review during live demonstrations.
     */
    public void openStandaloneWindow() {
        JFrame win = new JFrame("CyberSim — Security Event Log");
        win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        win.setSize(820, 560);
        win.setMinimumSize(new Dimension(600, 400));

        // Build a fresh LogPanel sharing the same LogManager
        // so it reflects current and future entries
        LogPanel standalone = new LogPanel(logManager);

        win.setContentPane(standalone);
        UITheme.centerOnScreen(win);
        win.setVisible(true);
    }

    // -------------------------------------------------------------------------
    // UI construction helpers
    // -------------------------------------------------------------------------

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 6));
        bar.setBackground(UITheme.ACCENT_NAVY);
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x334155)));

        JLabel title = new JLabel("📋  SECURITY EVENT LOG");
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Live simulation events | colour-coded by severity");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(0x94A3B8));

        // Legend swatches
        bar.add(title);
        bar.add(sub);
        bar.add(legendItem(CLR_INFO,    "INFO"));
        bar.add(legendItem(CLR_WARNING, "WARN"));
        bar.add(legendItem(CLR_ALERT,   "ALERT"));

        return bar;
    }

    private JPanel legendItem(Color colour, String label) {
        JPanel item = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 0));
        item.setOpaque(false);
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(10, 10));
        swatch.setBackground(colour);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        lbl.setForeground(new Color(0x94A3B8));
        item.add(swatch);
        item.add(lbl);
        return item;
    }

    // -------------------------------------------------------------------------
    // Log entry rendering
    // -------------------------------------------------------------------------

    /** Loads all entries already in LogManager into the text pane. */
    private void loadExistingLogs() {
        List<String> raw = logManager.getRawEntries();
        for (String entry : raw) {
            LogManager.Level level = parseLevel(entry);
            String text = stripLevel(entry);
            appendColoured(text + "\n", attributesFor(level));
        }
    }

    /** LogManager.LogListener implementation — called for each new log entry. */
    private void appendEntry(String formattedLine, LogManager.Level level) {
        SwingUtilities.invokeLater(() -> {
            appendColoured(formattedLine + "\n", attributesFor(level));
            scrollToBottom();
        });
    }

    private void appendColoured(String text, SimpleAttributeSet attrs) {
        try {
            doc.insertString(doc.getLength(), text, attrs);
        } catch (BadLocationException ex) {
            // Should never happen on a valid document
            ex.printStackTrace();
        }
    }

    private void scrollToBottom() {
        textPane.setCaretPosition(doc.getLength());
    }

    // -------------------------------------------------------------------------
    // Attribute builders
    // -------------------------------------------------------------------------

    private static SimpleAttributeSet buildAttr(Color fg, boolean bold) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setForeground(a, fg);
        StyleConstants.setBold(a, bold);
        StyleConstants.setFontFamily(a, "Monospaced");
        StyleConstants.setFontSize(a, 11);
        return a;
    }

    private SimpleAttributeSet attributesFor(LogManager.Level level) {
        return switch (level) {
            case WARNING -> attrWarning;
            case ALERT   -> attrAlert;
            default      -> attrInfo;
        };
    }

    // -------------------------------------------------------------------------
    // Raw entry parsing helpers
    // -------------------------------------------------------------------------

    /** Extract the Level from a raw "LEVEL|formattedLine" string. */
    private static LogManager.Level parseLevel(String raw) {
        int pipe = raw.indexOf('|');
        if (pipe < 0) return LogManager.Level.INFO;
        String name = raw.substring(0, pipe).trim();
        try {
            return LogManager.Level.valueOf(name);
        } catch (IllegalArgumentException e) {
            return LogManager.Level.INFO;
        }
    }

    /** Strip the "LEVEL|" prefix and return only the formatted display string. */
    private static String stripLevel(String raw) {
        int pipe = raw.indexOf('|');
        return pipe >= 0 ? raw.substring(pipe + 1) : raw;
    }
}
