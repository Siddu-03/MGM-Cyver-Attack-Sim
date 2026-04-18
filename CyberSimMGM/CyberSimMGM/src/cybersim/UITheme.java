package cybersim;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * UITheme - Centralised design token library for the CyberSim application.
 *
 * Provides colours, fonts, and factory methods for styled Swing components
 * so all panels share a consistent professional appearance without repeating
 * styling code throughout the codebase.
 */
public final class UITheme {

    private UITheme() {}   // utility class — no instances

    // -------------------------------------------------------------------------
    // Colour Palette
    // -------------------------------------------------------------------------

    // Backgrounds
    public static final Color BG_WHITE       = new Color(0xFFFFFF);
    public static final Color BG_PANEL       = new Color(0xF8FAFC);
    public static final Color BG_SIDEBAR     = new Color(0xF1F5F9);

    // Accent colours
    public static final Color ACCENT_NAVY    = new Color(0x1E3A5F);   // top-bar / header
    public static final Color ACCENT_BLUE    = new Color(0x2563EB);   // primary action / links
    public static final Color ACCENT_BLUE_LT = new Color(0xEFF6FF);   // light blue fill
    public static final Color ACCENT_RED     = new Color(0xDC2626);   // danger / attack

    // Status indicators
    public static final Color STATUS_OK          = new Color(0x16A34A);   // green — secure
    public static final Color STATUS_COMPROMISED = new Color(0xDC2626);   // red — compromised
    public static final Color STATUS_WARNING     = new Color(0xD97706);   // amber — warning

    // Text
    public static final Color TEXT_PRIMARY   = new Color(0x1E293B);
    public static final Color TEXT_SECONDARY = new Color(0x64748B);
    public static final Color TEXT_MUTED     = new Color(0x94A3B8);

    // Borders / dividers
    public static final Color BORDER_COLOR   = new Color(0xE2E8F0);

    // -------------------------------------------------------------------------
    // Typography
    // -------------------------------------------------------------------------

    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Monospaced", Font.PLAIN, 12);
    public static final Font FONT_MONO_SM = new Font("Monospaced", Font.PLAIN, 11);

    // -------------------------------------------------------------------------
    // Frame helpers
    // -------------------------------------------------------------------------

    /** Sets the frame's content-pane background to BG_WHITE. */
    public static void applyBackground(JFrame frame) {
        frame.getContentPane().setBackground(BG_WHITE);
    }

    /** Centers the frame on the primary screen. */
    public static void centerOnScreen(JFrame frame) {
        frame.setLocationRelativeTo(null);
    }

    // -------------------------------------------------------------------------
    // Section card factory
    // -------------------------------------------------------------------------

    /**
     * Creates a titled section panel (card) used in the sidebar and other areas.
     * The caller should set a layout and add child components after this call.
     */
    public static JPanel sectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBackground(BG_WHITE);
        panel.setBorder(buildCardBorder(title));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return panel;
    }

    private static Border buildCardBorder(String title) {
        Border outer = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            title,
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            FONT_HEADING,
            TEXT_SECONDARY
        );
        Border inner = BorderFactory.createEmptyBorder(8, 10, 10, 10);
        return BorderFactory.createCompoundBorder(outer, inner);
    }

    // -------------------------------------------------------------------------
    // Button factory methods
    // -------------------------------------------------------------------------

    /**
     * Creates the primary (blue) action button.
     * Use for main positive actions such as "Reset & Recover".
     */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADING);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_BLUE);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        applyHoverEffect(btn, ACCENT_BLUE, new Color(0x1D4ED8));
        return btn;
    }

    /**
     * Creates the danger (red) button.
     * Use for destructive or high-impact actions such as "Simulate Attack".
     */
    public static JButton dangerButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_HEADING);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_RED);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        applyHoverEffect(btn, ACCENT_RED, new Color(0xB91C1C));
        return btn;
    }

    /**
     * Creates a secondary (outline-style) button.
     * Use for optional actions such as "View Demo Files" or "Open Log Window".
     */
    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BG_PANEL);
        btn.setOpaque(true);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        applyHoverEffect(btn, BG_PANEL, ACCENT_BLUE_LT);
        return btn;
    }

    // -------------------------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------------------------

    private static void applyHoverEffect(JButton btn, Color normal, Color hover) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(normal);
            }
        });
    }
}
