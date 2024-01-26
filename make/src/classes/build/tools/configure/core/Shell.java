package build.tools.configure.core;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Shell {
    public static String env(String variable) {
        String value = System.getenv(variable);
        return value;
    }

    public static String date() {
        ZonedDateTime currentDate = ZonedDateTime.now();
        // Emulate output of unix date command
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("E MMM d HH:mm:ss z yyyy");
        return formatter.format(currentDate);
    }

    public static boolean dirExists(String includeDir) {
    }

    public static int execute(String command, String... arguments) {
        return 0;
    }

    public static ShellCapture capture(String command, String... arguments) {
    }

    public static boolean fileExists(String string) {
        return true;
    }

    public static boolean isWindows() {
        return false;
    }

    public static boolean isMacOS() {
        return true;
    }
}
