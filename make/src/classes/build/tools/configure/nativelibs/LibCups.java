package build.tools.configure.nativelibs;

import build.tools.configure.core.LibraryInfo;

public class LibCups {
    public static void setupCups() {
        // -- no pkg-config! instead, maybe use cups-config: --libs --cflags --ldflags
        LibraryInfo cups = LibraryInfo.register("cups", null, "cups/cups.h", getCupsAvailable(), getCupsRequired());

        cups.checkOptions();

    }

    private static boolean getCupsRequired() {
        return true;
    }

    private static boolean getCupsAvailable() {
        return true;
    }

    /*
################################################################################
# Setup cups (Common Unix Printing System)
################################################################################
AC_DEFUN_ONCE([LIB_SETUP_CUPS],
[
  AC_ARG_WITH(cups, [AS_HELP_STRING([--with-cups],
      [specify prefix directory for the cups package
      (expecting the headers under PATH/include)])])
  AC_ARG_WITH(cups-include, [AS_HELP_STRING([--with-cups-include],
      [specify directory for the cups include files])])

  if test "x$NEEDS_LIB_CUPS" = xfalse; then
    if (test "x${with_cups}" != x && test "x${with_cups}" != xno) || \
        (test "x${with_cups_include}" != x && test "x${with_cups_include}" != xno); then
      AC_MSG_WARN([[cups not used, so --with-cups[-*] is ignored]])
    fi
    CUPS_CFLAGS=
  else
    CUPS_FOUND=no

    if test "x${with_cups}" = xno || test "x${with_cups_include}" = xno; then
      AC_MSG_ERROR([It is not possible to disable the use of cups. Remove the --without-cups option.])
    fi

    if test "x${with_cups}" != x; then
      AC_MSG_CHECKING([for cups headers])
      if test -s "${with_cups}/include/cups/cups.h"; then
        CUPS_CFLAGS="-I${with_cups}/include"
        CUPS_FOUND=yes
        AC_MSG_RESULT([$CUPS_FOUND])
      else
        AC_MSG_ERROR([Can't find 'include/cups/cups.h' under ${with_cups} given with the --with-cups option.])
      fi
    fi
    if test "x${with_cups_include}" != x; then
      AC_MSG_CHECKING([for cups headers])
      if test -s "${with_cups_include}/cups/cups.h"; then
        CUPS_CFLAGS="-I${with_cups_include}"
        CUPS_FOUND=yes
        AC_MSG_RESULT([$CUPS_FOUND])
      else
        AC_MSG_ERROR([Can't find 'cups/cups.h' under ${with_cups_include} given with the --with-cups-include option.])
      fi
    fi
    if test "x$CUPS_FOUND" = xno; then
      # Are the cups headers installed in the default AIX or /usr/include location?
      if test "x$OPENJDK_TARGET_OS" = "xaix"; then
        AC_CHECK_HEADERS([/opt/freeware/include/cups/cups.h /opt/freeware/include/cups/ppd.h], [
            CUPS_FOUND=yes
            CUPS_CFLAGS="-I/opt/freeware/include"
            DEFAULT_CUPS=yes
        ])
      else
        AC_CHECK_HEADERS([cups/cups.h cups/ppd.h], [
            CUPS_FOUND=yes
            CUPS_CFLAGS=
            DEFAULT_CUPS=yes
        ])
      fi
    fi
    if test "x$CUPS_FOUND" = xno; then
      HELP_MSG_MISSING_DEPENDENCY([cups])
      AC_MSG_ERROR([Could not find cups! $HELP_MSG ])
    fi
  fi

  AC_SUBST(CUPS_CFLAGS)
])

     */
}
