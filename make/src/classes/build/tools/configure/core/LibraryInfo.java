package build.tools.configure.core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LibraryInfo {
    private final String flagName;
    private final String includeTestName;
    private final boolean available;
    private final boolean required;
    private final String libName;
    private final Option baseOpt;
    private final Option includeOpt;
    private final Option libOpt;

    private LibraryFlags flags;
    private LibraryPaths paths;

    public LibraryInfo(String flagName, String libName, String includeTestName, boolean available, boolean required) {
        this.flagName = flagName;
        this.libName = libName;
        this.includeTestName = includeTestName;
        this.available = available;
        this.required = required;
        this.baseOpt = Option.registerWith(flagName, true, false);
        this.includeOpt = Option.registerWith(flagName + "-include", true, false);
        if (libName != null) {
            this.libOpt = Option.registerWith(flagName + "-lib", true, false);
        } else {
            this.libOpt = null;
        }
    }

    public static LibraryInfo register(String name, String libName, String includeTestName, boolean available, boolean required) {
        return new LibraryInfo(name, libName, includeTestName, available, required);
    }

    public static String tryLocateLibrary(String libPath, String libraryName) {
        String platformLibraryFileName = getPlatformLibraryFileName(libraryName);

        // Concatenate
        Path libraryPath = Paths.get(libPath, platformLibraryFileName);
        if (!Shell.fileExists(libraryPath.toString())) {
            AC.fatal("Cannot locate " + platformLibraryFileName + " in " + libPath);
            return null;
        }

        try {
            Path absolutePath = libraryPath.toRealPath();
            return absolutePath.toString();
        } catch (IOException e) {
            AC.fatal("Failed to rewrite path " + libraryPath, e);
            return null;
        }
    }

    private static String getPlatformLibraryFileName(String libraryName) {
        if (Shell.isWindows()) {
            return libraryName + ".dll";
        } else if (Shell.isMacOS()) {
            return "lib" + libraryName + ".dylib";
        } else  {
            return "lib" + libraryName + ".so";
        }
    }

    public LibraryFlags getFlags() {
        return flags;
    }


    public void checkOptions() {
        LibraryFlags flags = null;

        // Check if options make sense
        if (!available) {
            for (Option opt : new Option[]{baseOpt, includeOpt, libOpt}) {
                if (opt.hasValue()) {
                    AC.warn("You specified " + opt.getFlagName() + " but the " + flagName + " library is not available on this platform");
                    AC.msgNotice("The value of " + opt.getFlagName() + " is completely ignored");
                }
            }
            return;
        }


        paths = getPathsFromOptions();

        if ((paths == null) && available) {
                flags = autoDetect();

        }
        
        if ((flags == null) && required) {
            AC.fatal("The " + flagName + " library is required but could not be found");
            return;
        }
        
        // Now that we have established the paths, check if the library is working
        tryCompileLibrary(flags);
        this.flags = flags;
    }

    private LibraryPaths getPathsFromOptions() {
        LibraryPaths paths = getPathFromOptions();
        if (paths != null) {
            // Check if the directories exist
            if (verifyPath(paths)) {
                return paths;
            }
        }
        return null;
    }

    private boolean verifyPath(LibraryPaths paths) {
        if (!Shell.dirExists(paths.includeDir)) {
            AC.fatal("The directory " + paths.includeDir + " does not exist");
            return false;
        }
        if (paths.libDir != null && !Shell.dirExists(paths.libDir)) {
            AC.fatal("The directory " + paths.libDir + " does not exist");
            return false;
        }

        // Check if include file exists
        Path includeFile = Paths.get(paths.includeDir, includeTestName);
        if (!Shell.fileExists(includeFile.toString())) {
            AC.fatal("Cannot locate " + includeTestName + " in " + paths.includeDir);
            return false;
        }
        return true;
    }

    private LibraryPaths getPathFromOptions() {
        if (baseOpt.hasValue()) {
            String includeDir = baseOpt.getValue() + "/include";
            if (includeOpt.hasValue()) {
                if (libOpt.hasValue()) {
                    AC.warn("The value of " + baseOpt.getFlagName() + " is overridden by " + includeOpt.getFlagName() +  " and " + libOpt.getFlagName() + " and is completely ignored");
                } else if (libName == null) {
                    AC.warn("The value of " + baseOpt.getFlagName() + " is overridden by " + includeOpt.getFlagName() + " and is completely ignored");
                }
                includeDir = includeOpt.getValue();
            }

            String libDir = baseOpt.getValue() + "/lib";
            if (libOpt.hasValue()) {
                libDir = libOpt.getValue();
            }
            if (libName == null) {
                libDir = null;
            }
            return new LibraryPaths(includeDir, libDir, libName);
        } else if (includeOpt.hasValue() || libOpt.hasValue()) {
            if (includeOpt.hasValue()) {
                if (libOpt.hasValue()) {
                    return new LibraryPaths(includeOpt.getValue(), libOpt.getValue(), libName);
                } else if (libName == null) {
                    // only include available
                    AC.warn("You specified --with-" + flagName + "-lib but not --with-" + flagName + "-include, or vice versa");
                    return new LibraryPaths(includeOpt.getValue(), null, libName);
                } else {
                    AC.fatal("You specified --with-" + flagName + "-include but not --with-" + flagName + "-lib");
                }
            } else if (includeOpt.hasValue()) {
                // libOpt has value, but not includeOpt
                AC.fatal("You specified --with-" + flagName + "-lib but not --with-" + flagName + "-include");
            }
        }
        return null;
    }

    private boolean checkLibraryFilesPresent(LibraryPaths paths, boolean printWarning) {
        Path includeFile = Paths.get(paths.includeDir, includeTestName);
        if (!Shell.fileExists(includeFile.toString())) {
            if (printWarning) {
                AC.error("Cannot locate " + includeTestName + " in " + paths.includeDir);
            }
            return false;
        }
        return false;
    }

    private void tryCompileLibrary(LibraryFlags flags) {
        String testInclude = includeTestName;

        String includeLine = "#include <" + testInclude + ">";

        String compileCommandLine = "$CC -c $CFLAGS $CPPFLAGS conftest.c";

        tryLinkLibrary(flags);


    }

    public void tryLinkLibrary(LibraryFlags flags) {
    }

    private LibraryFlags autoDetect() {
        LibraryPaths myPaths;

        LibraryDescriptor desc = detectFromPkgConfig();
        if (desc != null) {
            return desc;
        }

        myPaths = detectFromSysroot();
        if (checkLibraryFilesPresent(myPaths, false)) {
            paths = myPaths;
            return myPaths;
        }
        for (LibraryPaths path : getWellKnownPaths()) {
            if (checkLibraryFilesPresent(path, false)) {
                paths = myPaths;
                return path;
            }
        }
        return null;
    }

    protected List<String> getWellKnownIncludeDirs() {
        return List.of();
    }

    protected LibraryPaths[] getWellKnownPaths() {
        return new LibraryPaths[0];
    }

    private LibraryPaths detectFromSysroot() {
        if (AC.sysroot != null) {
            String includeDir = AC.sysroot + "/usr/include";
            String libDir = AC.sysroot + "/usr/lib";
            LibraryPaths path = new LibraryPaths(includeDir, libDir, libName);
            return path;
        }

        return null;
    }

    private LibraryDescriptor detectFromPkgConfig() {
        // make sure not to run on windows, since we can't trust if it is mingw or what..
        /*
when cross-compiling, set the following:

export PKG_CONFIG_PATH=
export PKG_CONFIG_LIBDIR=${SYSROOT}/usr/lib/pkgconfig:${SYSROOT}/usr/share/pkgconfig
export PKG_CONFIG_SYSROOT_DIR=${SYSROOT}

         */
        int result = Shell.execute("pkg-config", "--exists", flagName);
        if (result != 0) {
            return null;
        }
        ShellCapture captureCflags = Shell.capture("pkg-config", "--cflags", flagName);
        if (result != 0) {
            AC.warn("pkg-config --exists " + flagName + " succeeded but pkg-config --cflags " + flagName + " failed");
        }
        ShellCapture captureLibs = Shell.capture("pkg-config", "--libs", flagName);
        if (result != 0) {
            AC.warn("pkg-config --exists " + flagName + " succeeded but pkg-config --libs " + flagName + " failed");
        }
        return new LibraryDescriptor(captureCflags.stdout(), captureLibs.stdout());
    }

    public String getFlagName() {
        return flagName;
    }

    public String getLibraryName() {
        return libName;
    }

    public record LibraryPaths(String includeDir, String libDir, String libName) implements LibraryFlags{
        @Override
        public String cflags() {
            return "-I" + includeDir;
        }

        @Override
        public String libs() {
            return "-L" + libDir + " -l" + libName;
        }

        @Override
        public String getLibPath() {
            return libDir;
        }

        @Override
        public LibraryFlags appendCflags(String extraCflags) {
            return new ExtendedLibraryPaths(includeDir, libDir, libName, extraCflags);
        }

        private record ExtendedLibraryPaths(String includeDir, String libDir, String libName,
                                            String extraCflags) implements LibraryFlags {
            @Override
            public String cflags() {
                return "-I" + includeDir + " " + extraCflags;
            }

            @Override
            public String libs() {
                return "-L" + libDir + " -l" + libName;
            }

            @Override
            public String getLibPath() {
                return libDir;
            }

            @Override
            public LibraryFlags appendCflags(String s) {
                return new ExtendedLibraryPaths(includeDir, libDir, libName, extraCflags + " " + s);
            }
        }
    }
}
