package build.tools.configure.deps;

public class BasicTools {
    // basic tools
    /*
  # Bootstrapping: These tools are needed by UTIL_LOOKUP_PROGS
  AC_PATH_PROGS(BASENAME, basename)
  UTIL_CHECK_NONEMPTY(BASENAME)
  AC_PATH_PROGS(DIRNAME, dirname)
  UTIL_CHECK_NONEMPTY(DIRNAME)
  AC_PATH_PROGS(FILE, file)
  UTIL_CHECK_NONEMPTY(FILE)
  AC_PATH_PROGS(LDD, ldd)

  # Required tools
  UTIL_REQUIRE_PROGS(ECHO, echo)
  UTIL_REQUIRE_PROGS(TR, tr)
  UTIL_REQUIRE_PROGS(UNAME, uname)
  UTIL_REQUIRE_PROGS(WC, wc)

  # Required tools with some special treatment
  UTIL_REQUIRE_SPECIAL(GREP, [AC_PROG_GREP])
  UTIL_REQUIRE_SPECIAL(EGREP, [AC_PROG_EGREP])
  UTIL_REQUIRE_SPECIAL(SED, [AC_PROG_SED])


  # Required tools
  UTIL_REQUIRE_PROGS(BASH, bash)
  UTIL_REQUIRE_PROGS(CAT, cat)
  UTIL_REQUIRE_PROGS(CHMOD, chmod)
  UTIL_REQUIRE_PROGS(CP, cp)
  UTIL_REQUIRE_PROGS(CUT, cut)
  UTIL_REQUIRE_PROGS(DATE, date)
  UTIL_REQUIRE_PROGS(DIFF, gdiff diff)
  UTIL_REQUIRE_PROGS(EXPR, expr)
  UTIL_REQUIRE_PROGS(FIND, find)
  UTIL_REQUIRE_PROGS(GUNZIP, gunzip)
  UTIL_REQUIRE_PROGS(GZIP, pigz gzip)
  UTIL_REQUIRE_PROGS(HEAD, head)
  UTIL_REQUIRE_PROGS(LN, ln)
  UTIL_REQUIRE_PROGS(LS, ls)
  # gmkdir is known to be safe for concurrent invocations with -p flag.
  UTIL_REQUIRE_PROGS(MKDIR, gmkdir mkdir)
  UTIL_REQUIRE_PROGS(MKTEMP, mktemp)
  UTIL_REQUIRE_PROGS(MV, mv)
  UTIL_REQUIRE_PROGS(AWK, gawk nawk awk)
  UTIL_REQUIRE_PROGS(PRINTF, printf)
  UTIL_REQUIRE_PROGS(RM, rm)
  UTIL_REQUIRE_PROGS(RMDIR, rmdir)
  UTIL_REQUIRE_PROGS(SH, sh)
  UTIL_REQUIRE_PROGS(SORT, sort)
  UTIL_REQUIRE_PROGS(TAIL, tail)
  UTIL_REQUIRE_PROGS(TAR, gtar tar)
  UTIL_REQUIRE_PROGS(TEE, tee)
  UTIL_REQUIRE_PROGS(TOUCH, touch)
  UTIL_REQUIRE_PROGS(XARGS, xargs)

  # Required tools with some special treatment
  UTIL_REQUIRE_SPECIAL(FGREP, [AC_PROG_FGREP])

  # Optional tools, we can do without them
  UTIL_LOOKUP_PROGS(DF, df)
  UTIL_LOOKUP_PROGS(GIT, git)
  UTIL_LOOKUP_PROGS(NICE, nice)
  UTIL_LOOKUP_PROGS(READLINK, greadlink readlink)
  UTIL_LOOKUP_PROGS(WHOAMI, whoami)

  # For compare.sh only
  UTIL_LOOKUP_PROGS(CMP, cmp)
  UTIL_LOOKUP_PROGS(UNIQ, uniq)
     */

/*
  # These tools might not be installed by default,
  # need hint on how to install them.
  UTIL_REQUIRE_PROGS(UNZIP, unzip)
  # Since zip uses "ZIP" as a environment variable for passing options, we need
  # to name our variable differently, hence ZIPEXE.
  UTIL_REQUIRE_PROGS(ZIPEXE, zip)

  # Non-required basic tools

  UTIL_LOOKUP_PROGS(READELF, greadelf readelf)
  UTIL_LOOKUP_PROGS(DOT, dot)
  UTIL_LOOKUP_PROGS(STAT, stat)
  UTIL_LOOKUP_PROGS(TIME, time)
  UTIL_LOOKUP_PROGS(FLOCK, flock)
  # Dtrace is usually found in /usr/sbin, but that directory may not
  # be in the user path.
  UTIL_LOOKUP_PROGS(DTRACE, dtrace, $PATH:/usr/sbin)
  UTIL_LOOKUP_PROGS(PATCH, gpatch patch)
 */

    // check for bash options!

    // special tests for functionality in common tools:
    //  # Test if find supports -delete
    // Test which kind of tar was found
    // Test that grep supports -Fx with a list of pattern which includes null pattern.
    //  # Check if it's GNU time
    //  # Check if it's a GNU date compatible version
}
