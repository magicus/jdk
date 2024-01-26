package build.tools.configure.core;

public interface LibraryFlags {
    String cflags();

    String libs();

    String getLibPath();

    LibraryFlags appendCflags(String s);
}
