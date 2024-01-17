package build.tools.configure;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
}
