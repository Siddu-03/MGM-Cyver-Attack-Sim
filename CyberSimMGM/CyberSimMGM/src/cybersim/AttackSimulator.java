package cybersim;

import javax.swing.*;
import java.util.function.Consumer;

/**
 * AttackSimulator - Orchestrates the multi-stage cyberattack simulation.
 *
 * Simulates the 7-stage attack chain observed in the MGM Resorts breach (Sept 2023),
 * attributed to ALPHV/BlackCat ransomware group working with Scattered Spider.
 *
 * Each stage logs events, updates a progress bar, highlights the corresponding
 * MITRE ATT&CK row in MitrePanel, and pauses briefly for live demo pacing.
 *
 * SAFETY: No real malware. File "encryption" = text overwrite only.
 */
public class AttackSimulator {

    private final LogManager logManager;
    private final FileManager fileManager;
    private final JProgressBar progressBar;
    private final JLabel statusLabel;
    private final Consumer<Boolean> onComplete; // called with true = success

    /**
     * Optional reference to MitrePanel.
     * When set, each attack stage triggers a corresponding MITRE row highlight.
     * May be null if the panel is not available (backward-compatible).
     */
    private MitrePanel mitrePanel;

    // Attack stages with delay in milliseconds
    private static final Object[][] STAGES = {
        // { delay_ms, log_level, message, progress_pct }
        { 1000, LogManager.Level.INFO,    "Stage 1/7 — Reconnaissance: Harvesting LinkedIn profiles for MGM employees...",                     10 },
        { 1400, LogManager.Level.INFO,    "Stage 1/7 — Reconnaissance: Target identified: David Chen (Finance, elevated access)",                18 },
        { 1200, LogManager.Level.WARNING, "Stage 2/7 — Social Engineering: Attacker calls MGM IT Helpdesk, impersonates David Chen",             26 },
        { 1000, LogManager.Level.WARNING, "Stage 2/7 — Social Engineering: Helpdesk operator deceived — identity not properly verified",         32 },
        { 1200, LogManager.Level.ALERT,   "Stage 3/7 — MFA Bypass: Helpdesk resets MFA for target account — AUTHENTICATION CONTROL DEFEATED",   40 },
        { 1000, LogManager.Level.ALERT,   "Stage 3/7 — MFA Bypass: Attacker gains valid session token — logged in as E-1003",                   46 },
        { 1400, LogManager.Level.ALERT,   "Stage 4/7 — Privilege Escalation: Exploiting misconfigured Okta tenant — requesting ADMIN token",    54 },
        { 1000, LogManager.Level.ALERT,   "Stage 4/7 — Privilege Escalation: Domain Admin rights obtained — full AD access granted",             60 },
        { 1200, LogManager.Level.ALERT,   "Stage 5/7 — Lateral Movement: Scanning internal subnets (10.10.20.0/24, 10.10.50.0/24)",            68 },
        { 1000, LogManager.Level.ALERT,   "Stage 5/7 — Lateral Movement: Compromising 1,300+ ESXi servers via vCenter — CRITICAL",              75 },
        { 1400, LogManager.Level.ALERT,   "Stage 6/7 — Data Exfiltration: Copying employee PII, financial records, reservations data...",        82 },
        { 1000, LogManager.Level.ALERT,   "Stage 6/7 — Data Exfiltration: 6 TB of data transferred to external C2 server",                      86 },
        { 1200, LogManager.Level.ALERT,   "Stage 7/7 — Ransomware Deployment: ALPHV/BlackCat payload deployed across 1,300+ hosts",             92 },
        { 1000, LogManager.Level.ALERT,   "Stage 7/7 — Ransomware Deployment: Files ENCRYPTED — ransom note dropped ($15M demand)",             96 },
        {  800, LogManager.Level.ALERT,   "ATTACK COMPLETE — Systems compromised. Estimated impact: $100M+ in losses",                          100 },
    };

    public AttackSimulator(
            LogManager logManager,
            FileManager fileManager,
            JProgressBar progressBar,
            JLabel statusLabel,
            Consumer<Boolean> onComplete) {
        this.logManager  = logManager;
        this.fileManager = fileManager;
        this.progressBar = progressBar;
        this.statusLabel = statusLabel;
        this.onComplete  = onComplete;
    }

    /**
     * Attach a MitrePanel so that each attack stage automatically highlights
     * its corresponding MITRE ATT&CK technique row during simulation.
     */
    public void setMitrePanel(MitrePanel panel) {
        this.mitrePanel = panel;
    }

    /**
     * Run the attack simulation on a background thread.
     * UI updates are dispatched back to the Event Dispatch Thread.
     */
    public void run() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                logManager.alert("=== ATTACK SIMULATION INITIATED ===");
                logManager.info("Scenario: MGM Resorts breach — September 2023 (ALPHV/Scattered Spider)");

                for (int stageIndex = 0; stageIndex < STAGES.length; stageIndex++) {
                    Object[] stage  = STAGES[stageIndex];
                    int delay       = (int)    stage[0];
                    LogManager.Level level = (LogManager.Level) stage[1];
                    String message  = (String) stage[2];
                    int progress    = (int)    stage[3];

                    Thread.sleep(delay);

                    // Log entry
                    logManager.log(level, message);

                    // Capture loop variable for EDT lambda
                    final int currentStage = stageIndex;
                    final int p = progress;
                    final String msg = message;

                    SwingUtilities.invokeLater(() -> {
                        // Update progress bar and status label
                        progressBar.setValue(p);
                        String display = msg.length() > 70 ? msg.substring(0, 67) + "..." : msg;
                        statusLabel.setText(display);

                        // Highlight the corresponding MITRE ATT&CK row (if panel is attached)
                        if (mitrePanel != null) {
                            mitrePanel.highlightStage(currentStage);
                        }
                    });
                }

                // Simulate file encryption
                Thread.sleep(600);
                logManager.alert("Encrypting demo files in: " + fileManager.getDemoDir());
                try {
                    fileManager.encryptFiles();
                    logManager.alert("File encryption complete — " +
                        fileManager.getFileNames().length + " corporate files encrypted");
                } catch (Exception e) {
                    logManager.warning("File simulation error: " + e.getMessage());
                }

                return null;
            }

            @Override
            protected void done() {
                onComplete.accept(true);
            }
        };

        worker.execute();
    }
}
