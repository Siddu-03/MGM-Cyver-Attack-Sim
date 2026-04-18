package cybersim;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * LogManager - Centralised logging engine for the CyberSim platform.
 *
 * Maintains an ordered list of timestamped log entries that are written
 * during the attack simulation and incident-response phases.
 *
 * Three severity levels are provided (INFO, WARNING, ALERT) so that UI
 * components such as LogPanel can apply colour-coding per entry.
 */
public class LogManager {

    // -------------------------------------------------------------------------
    // Severity level enum — also used by AttackSimulator to tag each stage
    // -------------------------------------------------------------------------

    public enum Level {
        INFO    ("[INFO]   "),
        WARNING ("[WARN]   "),
        ALERT   ("[ALERT]  ");

        public final String prefix;
        Level(String prefix) { this.prefix = prefix; }
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final List<String>         rawEntries = new ArrayList<>();   // level|message
    private final List<LogListener>    listeners  = new ArrayList<>();

    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    // -------------------------------------------------------------------------
    // Functional interface for real-time log listeners (e.g. LogPanel)
    // -------------------------------------------------------------------------

    @FunctionalInterface
    public interface LogListener {
        void onNewEntry(String formattedLine, Level level);
    }

    // -------------------------------------------------------------------------
    // Public logging methods
    // -------------------------------------------------------------------------

    public void log(Level level, String message) {
        String timestamp = LocalTime.now().format(TIME_FMT);
        String formatted = timestamp + "  " + level.prefix + message;
        rawEntries.add(level.name() + "|" + formatted);
        for (LogListener l : listeners) {
            l.onNewEntry(formatted, level);
        }
    }

    public void info(String message)    { log(Level.INFO,    message); }
    public void warning(String message) { log(Level.WARNING, message); }
    public void alert(String message)   { log(Level.ALERT,   message); }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns all log entries as formatted strings (timestamp + level + message).
     * The raw level prefix is stripped so callers get the display string.
     */
    public List<String> getLogs() {
        List<String> display = new ArrayList<>(rawEntries.size());
        for (String raw : rawEntries) {
            int pipe = raw.indexOf('|');
            display.add(pipe >= 0 ? raw.substring(pipe + 1) : raw);
        }
        return display;
    }

    /**
     * Returns entries as Level/message pairs for colour-aware rendering.
     * Each returned String has the format: "LEVEL|formattedLine"
     */
    public List<String> getRawEntries() {
        return new ArrayList<>(rawEntries);
    }

    public int size() { return rawEntries.size(); }

    public void clear() { rawEntries.clear(); }

    // -------------------------------------------------------------------------
    // Listener registration
    // -------------------------------------------------------------------------

    public void addListener(LogListener listener)    { listeners.add(listener); }
    public void removeListener(LogListener listener) { listeners.remove(listener); }
}
