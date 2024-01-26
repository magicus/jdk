package build.tools.configure.nativelibs;

import build.tools.configure.core.LibraryInfo;

public class LibFontconfig {
    public static void setupFontconfig() {
        LibraryInfo cups = LibraryInfo.register("fontconfig", null, "fontconfig/fontconfig.h", getFontconfigAvailable(), getFontconfigRequired());

        cups.checkOptions();

    }

    private static boolean getFontconfigRequired() {
        return true;
    }

    private static boolean getFontconfigAvailable() {
        return true;
    }

    /*
################################################################################
# Setup fontconfig
################################################################################
AC_DEFUN_ONCE([LIB_SETUP_FONTCONFIG],
[
  AC_ARG_WITH(fontconfig, [AS_HELP_STRING([--with-fontconfig],
      [specify prefix directory for the fontconfig package
      (expecting the headers under PATH/include)])])
  AC_ARG_WITH(fontconfig-include, [AS_HELP_STRING([--with-fontconfig-include],
      [specify directory for the fontconfig include files])])

  if test "x$NEEDS_LIB_FONTCONFIG" = xfalse; then
    if (test "x${with_fontconfig}" != x && test "x${with_fontconfig}" != xno) || \
        (test "x${with_fontconfig_include}" != x && test "x${with_fontconfig_include}" != xno); then
      AC_MSG_WARN([[fontconfig not used, so --with-fontconfig[-*] is ignored]])
    fi
    FONTCONFIG_CFLAGS=
  else
    FONTCONFIG_FOUND=no

    if test "x${with_fontconfig}" = xno || test "x${with_fontconfig_include}" = xno; then
      AC_MSG_ERROR([It is not possible to disable the use of fontconfig. Remove the --without-fontconfig option.])
    fi

    if test "x${with_fontconfig}" != x; then
      AC_MSG_CHECKING([for fontconfig headers])
      if test -s "${with_fontconfig}/include/fontconfig/fontconfig.h"; then
        FONTCONFIG_CFLAGS="-I${with_fontconfig}/include"
        FONTCONFIG_FOUND=yes
        AC_MSG_RESULT([$FONTCONFIG_FOUND])
      else
        AC_MSG_ERROR([Can't find 'include/fontconfig/fontconfig.h' under ${with_fontconfig} given with the --with-fontconfig option.])
      fi
    fi
    if test "x${with_fontconfig_include}" != x; then
      AC_MSG_CHECKING([for fontconfig headers])
      if test -s "${with_fontconfig_include}/fontconfig/fontconfig.h"; then
        FONTCONFIG_CFLAGS="-I${with_fontconfig_include}"
        FONTCONFIG_FOUND=yes
        AC_MSG_RESULT([$FONTCONFIG_FOUND])
      else
        AC_MSG_ERROR([Can't find 'fontconfig/fontconfig.h' under ${with_fontconfig_include} given with the --with-fontconfig-include option.])
      fi
    fi
    if test "x$FONTCONFIG_FOUND" = xno; then
      # Are the fontconfig headers installed in the default /usr/include location?
      AC_CHECK_HEADERS([fontconfig/fontconfig.h], [
          FONTCONFIG_FOUND=yes
          FONTCONFIG_CFLAGS=
          DEFAULT_FONTCONFIG=yes
      ])
    fi
    if test "x$FONTCONFIG_FOUND" = xno; then
      HELP_MSG_MISSING_DEPENDENCY([fontconfig])
      AC_MSG_ERROR([Could not find fontconfig! $HELP_MSG ])
    fi
  fi

  AC_SUBST(FONTCONFIG_CFLAGS)
])

     */
}
