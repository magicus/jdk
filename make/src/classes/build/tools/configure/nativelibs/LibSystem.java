package build.tools.configure.nativelibs;

public class LibSystem {
    // small special libraries
    // hotspot linux: -lrt
    // hotspot aix :-lperfstat
    // hotspot zero (most pf): -latomic
    // hotspot linux && aix: -lpthread
    // hotspot windows: a whole bunch of system libs

    //  libm (the maths library)
    /*
        AC_CHECK_LIB(m, cos, [], [
        AC_MSG_NOTICE([Maths library was not found])
    ])
     */

    // libdl (the dynamic linker library)
    /*
     AC_CHECK_LIB(dl, dlopen)

     */
}
