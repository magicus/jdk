package build.tools.config.prerequisites;

public class BuildHost {
    private String triplet;
    private int memory;
    private int cores;
    private OperatingSystemEnv osEnv;

    private void determineTriplet() {
        triplet = "x86_64-pc-linux-gnu"; // FIXME
    }

    private void determineMemory() {
        memory = 4096; // FIXME
    }

    private void determineCores() {
        cores = 4; // FIXME
    }

    private void determineOsEnv() {
        osEnv = OperatingSystemEnv.LINUX; // FIXME
    }

    public String getTriplet() {
        return triplet;
    }

    public int getMemoryInMB() {
        return memory;
    }

    public int getCores() {
        return cores;
    }

    public OperatingSystemEnv getOperatingSystemEnv() {
        return osEnv;
    }
}
