package build.tools.configure;

public class AC {
    public static void subst(Conf conf, String variable, String content) {
        conf.put(variable, content);
    }

    public static void subst(Conf conf, String variable) {
        String content = System.getenv(variable);
        // FIXME: Non-existent variable?
        conf.put(variable, content);
    }

    public static void msgNotice(String message) {
        System.out.println(message);
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
}
