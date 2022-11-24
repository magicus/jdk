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
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);
        return formatter.format(currentDate);
    }
}
