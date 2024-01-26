package build.tools.configure.core;

public class Option {
    private final String option;
    private final String prefix;
    private final boolean requireArgument;
    private final boolean allowDisable;

    public Option(String option, String prefix, boolean requireArgument, boolean allowDisable) {
        this.option = option;
        this.prefix = prefix;
        this.requireArgument = requireArgument;
        this.allowDisable = allowDisable;
    }

    public static Option registerWith(String option, boolean requireArgument, boolean allowDisable) {
        return new Option(option, "with", requireArgument, allowDisable);
    }

    public static boolean isPresent(String name) {
        return true;
    }

    public static Option registerEnable(String option) {
        return new Option(option, "enable", false, true);
    }

    public String getValue() {
        // return null for unregistered options
        return "";
    }

    public boolean hasValue() {
        return false;
    }

    public String getFlagName() {
        return "--" + prefix + "-" + option;
    }

    public boolean isEnabled() {
        return false;
    }
}
