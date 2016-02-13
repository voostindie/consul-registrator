package nl.ulso.consul.registrator;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static nl.ulso.consul.registrator.Logger.Level.*;
import static nl.ulso.consul.registrator.Logger.Level.INFO;

/**
 * Minimal logger; no outside dependencies or complex configurations: just logging to System.out.
 */
class Logger {

    private static final String APPLICATION_NAME = "CONSUL-AGENT";


    enum Level {DEBUG, INFO, SILENT, ERROR}

    private static PrintStream out = System.out;
    private static Level level = INFO;

    static void setOut(PrintStream out) {
        Logger.out = out;
    }

    static void setLogLevel(Level level) {
        Logger.level = level;
    }

    static Level getLogLevel() {
        return level;
    }

    static void info(String line, Object... arguments) {
        if (SILENT == level) {
            return;
        }
        print(INFO, line, arguments);
    }

    static void debug(String line, Object... arguments) {
        if (SILENT == level || INFO == level) {
            return;
        }
        print(DEBUG, line, arguments);
    }

    static void error(String line, Object... arguments) {
        print(ERROR, line, arguments);
    }

    private static void print(Level level, String line, Object[] arguments) {
        out.print(new SimpleDateFormat("yyyy-MM-dd HH:MM:ss.SSS").format(new Date()));
        out.print(" ");
        out.print(APPLICATION_NAME);
        out.print(" ");
        out.print(String.format("%5s", level));
        out.print(" - ");
        out.println(String.format(line, arguments));
    }
}
