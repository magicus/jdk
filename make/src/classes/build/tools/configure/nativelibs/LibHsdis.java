package build.tools.configure.nativelibs;

public class LibHsdis {
    // capstone
/*
capstone_header="\"$CAPSTONE/include/capstone/capstone.h\""
  AC_MSG_CHECKING([capstone aarch64 arch name])
  AC_COMPILE_IFELSE([AC_LANG_PROGRAM([#include $capstone_header],[[cs_arch test = CS_ARCH_AARCH64]])],
    [
      AC_MSG_RESULT([AARCH64])
      CAPSTONE_ARCH_AARCH64_NAME="AARCH64"
    ],
    [
      AC_MSG_RESULT([ARM64])
      CAPSTONE_ARCH_AARCH64_NAME="ARM64"
    ]
  )
 */

    // llvm
/*
pkg-config do not work; use llvm-config instead...

// how to locate llvm-config

  if test "x$LLVM_DIR" = x; then
      # Macs with homebrew can have llvm in different places
      UTIL_LOOKUP_PROGS(LLVM_CONFIG, llvm-config, [$PATH:/usr/local/opt/llvm/bin:/opt/homebrew/opt/llvm/bin])
      if test "x$LLVM_CONFIG" = x; then
        AC_MSG_NOTICE([Cannot locate llvm-config which is needed for hsdis/llvm. Try using --with-llvm=<LLVM home>.])
        AC_MSG_ERROR([Cannot continue])
      fi
    else
      UTIL_LOOKUP_PROGS(LLVM_CONFIG, llvm-config, [$LLVM_DIR/bin])
      if test "x$LLVM_CONFIG" = x; then
        AC_MSG_NOTICE([Cannot locate llvm-config in $LLVM_DIR. Check your --with-llvm argument.])
        AC_MSG_ERROR([Cannot continue])
      fi
    fi

    # We need the LLVM flags and libs, and llvm-config provides them for us.
    HSDIS_CFLAGS=`$LLVM_CONFIG --cflags`
    HSDIS_LDFLAGS=`$LLVM_CONFIG --ldflags`
    HSDIS_LIBS=`$LLVM_CONFIG --libs $OPENJDK_TARGET_CPU_ARCH ${OPENJDK_TARGET_CPU_ARCH}disassembler`

=====

    # Official Windows installation of LLVM do not ship llvm-config, and self-built llvm-config
    # produced unusable output, so just ignore it on Windows.
    if ! test -e $LLVM_DIR/include/llvm-c/lto.h; then
      AC_MSG_NOTICE([$LLVM_DIR does not seem like a valid LLVM home; include dir is missing])
      AC_MSG_ERROR([Cannot continue])
    fi
    if ! test -e $LLVM_DIR/include/llvm-c/Disassembler.h; then
      AC_MSG_NOTICE([$LLVM_DIR does not point to a complete LLVM installation. ])
      AC_MSG_NOTICE([The official LLVM distribution is missing crucical files; you need to build LLVM yourself or get all include files elsewhere])
      AC_MSG_ERROR([Cannot continue])
    fi
    if ! test -e $LLVM_DIR/lib/llvm-c.lib; then
      AC_MSG_NOTICE([$LLVM_DIR does not seem like a valid LLVM home; lib dir is missing])
      AC_MSG_ERROR([Cannot continue])
    fi
    HSDIS_CFLAGS="-I$LLVM_DIR/include"
    HSDIS_LDFLAGS="-libpath:$LLVM_DIR/lib"
    HSDIS_LIBS="llvm-c.lib"
  fi
 */

    // binutils
    /*
    frekkin messs....
     */

    // hsdis
    /*
    this is not a lib per se, but a switch which activates one of the libraries above as needed.
     */

    // hsdis-bundling == bundle the generated lib with the JDK

}
