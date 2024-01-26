package build.tools.configure.deps;

public class PlatformTools
{
    // cmd.exe, cygpath
    // lsb_release

    /*
  if test "x$OPENJDK_TARGET_OS" = "xmacosx"; then
    UTIL_REQUIRE_PROGS(DSYMUTIL, dsymutil)
    AC_MSG_CHECKING([if dsymutil supports --reproducer option])
    if $DSYMUTIL --help | $GREP -q '\--reproducer '; then
      AC_MSG_RESULT([yes])
      # --reproducer option is supported
      # set "--reproducer Off" to prevent unnecessary temporary
      # directories creation
      DSYMUTIL="$DSYMUTIL --reproducer Off"
    else
      # --reproducer option isn't supported
      AC_MSG_RESULT([no])
    fi
    UTIL_REQUIRE_PROGS(MIG, mig)
    UTIL_REQUIRE_PROGS(XATTR, xattr)
    UTIL_LOOKUP_PROGS(CODESIGN, codesign)
    UTIL_REQUIRE_PROGS(SETFILE, SetFile)
  fi
  if ! test "x$OPENJDK_TARGET_OS" = "xwindows"; then
    UTIL_REQUIRE_PROGS(ULIMIT, ulimit)
  fi
     */
}
