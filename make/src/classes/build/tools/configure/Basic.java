package build.tools.configure;

import java.util.List;

public class Basic {
    public static void init(Conf conf, List<String> commandLines) {
        // Save the original command line. This is passed to us by the wrapper configure script.
        AC.subst(conf, "CONFIGURE_COMMAND_LINE", String.join(" ", commandLines));
        // We might have the original command line if the wrapper was called by some
        // other script.
        AC.subst(conf, "REAL_CONFIGURE_COMMAND_EXEC_SHORT");
        AC.subst(conf, "REAL_CONFIGURE_COMMAND_EXEC_FULL");
        AC.subst(conf, "REAL_CONFIGURE_COMMAND_LINE");
        // AUTOCONF might be set in the environment by the user. Preserve for "make reconfigure".
        AC.subst(conf, "AUTOCONF");
        // Save the path variable before it gets changed
        String originalPath = Shell.env("PATH");
        AC.subst(conf, "ORIGINAL_PATH", originalPath);
        String dateWhenConfigured = Shell.date();
        AC.subst(conf, "DATE_WHEN_CONFIGURED", dateWhenConfigured);
        AC.msgNotice("Configuration created at " + dateWhenConfigured);
    }
}
