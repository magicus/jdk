package build.tools.configure.deps;

public class BootJdk {
    /*
    we need a separate check for the bootjdk even though we are running on java.
    the bootjdk can have restrictions on version etc that do not apply to running the
    configure program itself.

    for sanity, always start by trying to use the same java as this script is running with.

     */

    /*
    ===
    verify that all tools are present


    ===
    check which arguments are supported
     */
}
