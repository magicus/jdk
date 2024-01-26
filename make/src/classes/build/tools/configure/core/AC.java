package build.tools.configure.core;

import java.io.IOException;

public class AC {
    public static String sysroot;

    public static void subst(Conf conf, String variable, String content) {
        conf.put(variable, content);
    }

    public static void subst(Conf conf, String variable) {
        String content = System.getenv(variable);
        // FIXME: Non-existent variable?
        conf.put(variable, content);
    }

    public static void msgNotice(String message) {
        System.out.println("configure: " + message);
    }

    public static String progGrep() {
        return "grep";
    }

    public static String progEGrep() {
        return "grep -E";
    }

    public static String progFGrep() {
        return "grep -F";
    }

    public static String progSed() {
        return "sed";
    }

    public static void warn(String s) {
    }

    public static void fatal(String s) {
    }

    public static void error(String s) {
    }

    public static boolean tryCompile(String testProgram, LibraryFlags flags) {
        return false;
    }

    public static void fatal(String s, IOException e) {
    }
}
