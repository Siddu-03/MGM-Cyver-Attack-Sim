# CyberSim MGM — Cyberattack Simulation Platform
## MGM Resorts Breach Case Study | September 2023

A Java Swing desktop application that simulates the 7-stage attack chain of
the MGM Resorts International cyberattack attributed to Scattered Spider /
ALPHV/BlackCat. Designed for academic and professional cybersecurity training.

---

## Requirements

- **Java 17 or later** (Java 21 recommended)
- No external libraries required — pure Java SE

---

## Quick Start (Prebuilt JAR)

```bash
java -jar CyberSimMGM.jar
```

The `demo_files/` folder is created automatically in the current working directory.

---

## Build from Source

```bash
# 1. Compile
mkdir -p out
javac -d out src/cybersim/*.java

# 2. Package as executable JAR
jar --create --file CyberSimMGM.jar --main-class cybersim.MainApp -C out .

# 3. Run
java -jar CyberSimMGM.jar
```

---

## Project Structure

```
CyberSimMGM/
├── CyberSimMGM.jar              ← Prebuilt executable JAR
├── README.md
├── demo_files/                  ← Simulated corporate files (auto-created on run)
│   ├── employee_records.txt
│   ├── financial_report_Q3_2023.txt
│   └── guest_reservations.txt
└── src/
    └── cybersim/
        ├── MainApp.java         ← Entry point
        ├── Dashboard.java       ← Main UI window (provided)
        ├── AttackSimulator.java ← 7-stage attack engine (provided)
        ├── MitrePanel.java      ← MITRE ATT&CK mapping tab (provided)
        ├── LogManager.java      ← Logging engine (generated)
        ├── FileManager.java     ← Demo file management (generated)
        ├── UITheme.java         ← Colours, fonts, component factories (generated)
        └── LogPanel.java        ← Scrollable event log viewer (generated)
```

---

## How to Use

1. **Launch** — run `java -jar CyberSimMGM.jar`
2. **Read the alert** — the attacker session warning explains the scenario
3. **Overview tab** — review the session and scenario summary cards
4. **Click "Simulate Attack"** — confirm the dialog to start
5. **Watch the MITRE ATT&CK tab** — rows highlight live as each technique fires
6. **Security Log tab** — colour-coded event timeline (INFO / WARN / ALERT)
7. **Click any MITRE row** — a popup shows the full technique description
8. **Click "Reset & Recover"** — restores demo files and clears highlighting

---

## Attack Stages Simulated

| Stage | Tactic             | MITRE ID   | Description                            |
|-------|--------------------|------------|----------------------------------------|
| 1     | Reconnaissance     | T1593.002  | LinkedIn OSINT — employee identification |
| 2     | Social Engineering | T1566/T1111| Helpdesk vishing call — MFA reset       |
| 3     | Initial Access     | T1078      | Login with valid compromised credentials |
| 4     | Privilege Escalation | T1548    | Okta super-admin misconfiguration abuse |
| 5     | Lateral Movement   | T1021      | VMware vCenter — 1,300+ ESXi servers   |
| 6     | Exfiltration       | T1041      | 6 TB of PII/financial data to C2       |
| 7     | Impact             | T1486      | ALPHV/BlackCat ransomware deployment   |

---

## Safety Notice

This application performs **no real malicious activity**.
- The "file encryption" simulation overwrites demo text files with a ransom note string
- No real encryption algorithms are used
- No network connections are made
- All activity is confined to the `demo_files/` directory

---

## Academic Context

Developed as a case-study tool for the course:
**Incident Response and Digital Forensics**
Ras Al Khaimah Medical and Health Sciences University (RVU)

Reference: MGM Resorts SEC 8-K filing (September 2023),
MITRE ATT&CK Framework (attack.mitre.org), CISA Advisory AA23-353A.
