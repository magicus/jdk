package build.tools.config.prerequisites;

public enum OperatingSystem {
    LINUX(OperatingSystemType.UNIX),
    MACOS(OperatingSystemType.UNIX),
    AIX(OperatingSystemType.UNIX),
    WINDOWS(OperatingSystemType.WINDOWS),
    UNKNOWN(OperatingSystemType.UNIX);

    private final OperatingSystemType operatingSystemType;

    OperatingSystem(OperatingSystemType operatingSystemType) {

        this.operatingSystemType = operatingSystemType;
    }
}
