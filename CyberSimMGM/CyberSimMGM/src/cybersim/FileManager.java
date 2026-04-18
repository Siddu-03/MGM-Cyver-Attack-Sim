package cybersim;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FileManager - Manages the demo_files directory used during attack simulation.
 *
 * Three sample corporate files are created on first run, each containing
 * realistic-looking sensitive data. During the attack simulation,
 * encryptFiles() overwrites them with a ransomware notice; restoreFiles()
 * resets them to their original content.
 *
 * SAFETY: No actual encryption is performed. File contents are replaced
 * with placeholder text to simulate the effect for demonstration purposes.
 */
public class FileManager {

    // -------------------------------------------------------------------------
    // Demo file definitions — filename → original content
    // -------------------------------------------------------------------------

    private static final Map<String, String> DEMO_FILES = new LinkedHashMap<>();

    static {
        DEMO_FILES.put("employee_records.txt",
            "MGM RESORTS INTERNATIONAL — EMPLOYEE RECORDS (CONFIDENTIAL)\n" +
            "=============================================================\n\n" +
            "Record ID : EMP-00234\n" +
            "Name      : David Chen\n" +
            "Department: Finance\n" +
            "Title     : Senior Financial Analyst\n" +
            "Access    : AD Group — FinanceAdmins (elevated)\n" +
            "MFA Token : Okta Push  (device: iPhone 14 Pro)\n\n" +
            "Record ID : EMP-00891\n" +
            "Name      : Sarah Mitchell\n" +
            "Department: IT Operations\n" +
            "Title     : Systems Administrator\n" +
            "Access    : AD Group — SysAdmins, vCenter-Admins\n" +
            "MFA Token : Okta Push  (device: Pixel 7)\n\n" +
            "[... 14,208 additional records — CLASSIFIED LEVEL 3 ...]\n"
        );

        DEMO_FILES.put("financial_report_Q3_2023.txt",
            "MGM RESORTS INTERNATIONAL — Q3 2023 FINANCIAL REPORT (RESTRICTED)\n" +
            "====================================================================\n\n" +
            "Total Revenue      : $3,826,100,000\n" +
            "Casino Revenue     : $1,602,400,000\n" +
            "Hotel Revenue      :   $944,700,000\n" +
            "Entertainment Rev  :   $411,200,000\n" +
            "Net Income         :   $328,900,000\n\n" +
            "Las Vegas Strip Properties — EBITDA Breakdown\n" +
            "  Bellagio          : $287M\n" +
            "  MGM Grand         : $241M\n" +
            "  Mandalay Bay      : $198M\n" +
            "  Aria              : $176M\n\n" +
            "REGULATORY NOTE: This document is subject to SEC disclosure rules.\n" +
            "Unauthorised distribution is a federal offence.\n"
        );

        DEMO_FILES.put("guest_reservations.txt",
            "MGM RESORTS — GUEST RESERVATION DATABASE EXPORT (CONFIDENTIAL)\n" +
            "================================================================\n\n" +
            "ReservationID  | GuestName         | Email                  | CreditCard       | Dates\n" +
            "---------------|-------------------|------------------------|------------------|-------------------\n" +
            "RES-20230901-A | James R. Morrison | jmorrison@example.com  | ****-****-**-4821| Sep 15 – Sep 19\n" +
            "RES-20230901-B | Priya Nair        | pnair@example.com      | ****-****-**-7703| Sep 15 – Sep 22\n" +
            "RES-20230901-C | Carlos Mendez     | cmendez@example.com    | ****-****-**-3390| Sep 16 – Sep 18\n" +
            "RES-20230901-D | Yuki Tanaka       | ytanaka@example.com    | ****-****-**-5512| Sep 16 – Sep 23\n\n" +
            "[... 37,421 additional guest records — PII DATA — GDPR/CCPA PROTECTED ...]\n\n" +
            "NOTICE: Exfiltration of this data constitutes a reportable breach under\n" +
            "Nevada Revised Statutes 603A and the EU General Data Protection Regulation.\n"
        );
    }

    // -------------------------------------------------------------------------
    // Instance state
    // -------------------------------------------------------------------------

    private final String demoDir;

    // Ransomware notice written over files during simulation
    private static final String RANSOM_NOTE =
        "!!! YOUR FILES HAVE BEEN ENCRYPTED !!!\n" +
        "========================================\n\n" +
        "All corporate files on this system have been encrypted by\n" +
        "ALPHV/BlackCat ransomware (variant: SCATTERED SPIDER affiliate).\n\n" +
        "Your organisation's data (approx. 6 TB) has been exfiltrated\n" +
        "to our secure servers. Failure to comply will result in\n" +
        "full public release of all employee PII, financial records,\n" +
        "and guest reservation data.\n\n" +
        "RANSOM DEMAND : USD $15,000,000 (fifteen million)\n" +
        "PAYMENT METHOD: Monero (XMR)\n" +
        "DEADLINE      : 72 hours from encryption timestamp\n\n" +
        "Contact: alphv-support@[redacted].onion\n\n" +
        "====[ FOR MGM RESORTS MANAGEMENT ONLY ]====\n" +
        "This is a demonstration file for the CyberSim MGM Case Study.\n" +
        "No real encryption has been applied. Run 'Reset & Recover' to restore.\n";

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param demoDir  Absolute or relative path to the demo_files directory.
     *                 The directory (and its files) are created if absent.
     */
    public FileManager(String demoDir) {
        this.demoDir = demoDir;
        initialiseFiles();
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Returns the absolute path of the demo files directory. */
    public String getDemoDir() {
        return new File(demoDir).getAbsolutePath();
    }

    /** Returns an array of demo file names (not full paths). */
    public String[] getFileNames() {
        return DEMO_FILES.keySet().toArray(new String[0]);
    }

    /**
     * Overwrites all demo files with the ransomware notice.
     * Called by AttackSimulator at the end of the attack chain.
     */
    public void encryptFiles() throws IOException {
        for (String name : DEMO_FILES.keySet()) {
            File f = new File(demoDir, name);
            Files.writeString(f.toPath(), RANSOM_NOTE, StandardCharsets.UTF_8);
        }
    }

    /**
     * Restores all demo files to their original content.
     * Called by Dashboard when the user clicks "Reset & Recover".
     */
    public void restoreFiles() throws IOException {
        for (Map.Entry<String, String> entry : DEMO_FILES.entrySet()) {
            File f = new File(demoDir, entry.getKey());
            Files.writeString(f.toPath(), entry.getValue(), StandardCharsets.UTF_8);
        }
    }

    // -------------------------------------------------------------------------
    // Initialisation helper
    // -------------------------------------------------------------------------

    /**
     * Creates the demo_files directory and all demo files if they do not exist.
     * If a file already exists, its content is left unchanged.
     */
    private void initialiseFiles() {
        File dir = new File(demoDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (Map.Entry<String, String> entry : DEMO_FILES.entrySet()) {
            File f = new File(dir, entry.getKey());
            if (!f.exists()) {
                try {
                    Files.writeString(f.toPath(), entry.getValue(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    System.err.println("[FileManager] Could not create " + f.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
