package build.tools.configure.core;

public record LibraryDescriptor(String cflags, String libs) implements LibraryFlags {
    @Override
    public String getLibPath() {
        // Locate "-L" in libs and return the path after it
        int index = libs.lastIndexOf("-L");
        if (index != -1) {
            return libs.substring(index + 2);
        }

        return null;
    }

    @Override
    public LibraryFlags appendCflags(String s) {
        return new LibraryDescriptor(cflags + " " + s, libs);
    }
}
