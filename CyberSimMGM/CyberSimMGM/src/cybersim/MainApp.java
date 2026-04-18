package cybersim;

import javax.swing.*;
import java.awt.*;

/**
 * MainApp - Application entry point for the CyberSim MGM Case Study platform.
 *
 * Bootstraps the three core services (LogManager, FileManager) and launches
 * the main Dashboard on the Swing Event Dispatch Thread.
 *
 * Default scenario: attacker session established via social-engineering bypass.
 */
public class MainApp {

    /** Relative path from the working directory to the demo files folder. */
    private static final String DEMO_DIR      = "demo_files";

    /** Simulated employee whose identity was stolen via social engineering. */
    private static final String DEFAULT_USER  = "employee";

    /** When true the Dashboard starts in attacker mode (MFA-bypassed session). */
    private static final boolean ATTACKER_MODE = true;

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        // Apply a clean system-neutral look-and-feel before any UI is built
        applyLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            // ── 1. Initialise back-end services ──────────────────────────────
            LogManager  logManager  = new LogManager();
            FileManager fileManager = new FileManager(DEMO_DIR);

            logManager.info("CyberSim MGM Case Study — application started");
            logManager.info("Demo files directory: " + fileManager.getDemoDir());
            logManager.info("Files present: " + fileManager.getFileNames().length);

            // ── 2. Launch the Dashboard ───────────────────────────────────────
            Dashboard dashboard = new Dashboard(
                DEFAULT_USER, ATTACKER_MODE, logManager, fileManager
            );
            dashboard.show();
        });
    }

    // -------------------------------------------------------------------------
    // Look-and-feel helper
    // -------------------------------------------------------------------------

    /**
     * Attempts to enable anti-aliased text rendering and applies the cross-platform
     * (Metal) look-and-feel so that the custom UITheme colours render consistently
     * across Windows, macOS, and Linux.
     */
    private static void applyLookAndFeel() {
        // Enable sub-pixel / greyscale anti-aliasing for text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Cross-platform L&F gives us the most predictable rendering
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            // If this fails the default L&F is used — acceptable fallback
            System.err.println("[MainApp] Could not set look-and-feel: " + e.getMessage());
        }

        // Global defaults for consistent typography across all components
        UIManager.put("Button.font",          UITheme.FONT_BODY);
        UIManager.put("Label.font",           UITheme.FONT_BODY);
        UIManager.put("TabbedPane.font",      UITheme.FONT_HEADING);
        UIManager.put("Table.font",           UITheme.FONT_BODY);
        UIManager.put("TableHeader.font",     UITheme.FONT_HEADING);
        UIManager.put("TextField.font",       UITheme.FONT_BODY);
        UIManager.put("TextArea.font",        UITheme.FONT_MONO);
        UIManager.put("OptionPane.messageFont", UITheme.FONT_BODY);
        UIManager.put("OptionPane.buttonFont",  UITheme.FONT_HEADING);

        // Flatten button borders so UITheme's custom backgrounds show correctly
        UIManager.put("Button.border",
            BorderFactory.createEmptyBorder(6, 12, 6, 12));
    }
}
