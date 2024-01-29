package build.tools.config.prerequisites;

public enum OperatingSystemEnv {
    LINUX(OperatingSystem.LINUX),
    MACOS(OperatingSystem.MACOS),
    AIX(OperatingSystem.AIX),
    WINDOWS_CYGWIN(OperatingSystem.WINDOWS),
    WINDOWS_MSYS2(OperatingSystem.WINDOWS),
    WINDOWS_WSL(OperatingSystem.WINDOWS),
    UNKNOWN(OperatingSystem.UNKNOWN);

    private final OperatingSystem operatingSystem;

    OperatingSystemEnv(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }
}
