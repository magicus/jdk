#
# Copyright (c) 2011, 2022, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the LICENSE file that accompanied this code.
#
# This code is distributed in the hope that it will be useful, but WITHOUT
# ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
# FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
# version 2 for more details (a copy is included in the LICENSE file that
# accompanied this code).
#
# You should have received a copy of the GNU General Public License version
# 2 along with this work; if not, write to the Free Software Foundation,
# Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
#
# Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
# or visit www.oracle.com if you need additional information or have any
# questions.
#

# All valid JVM variants
VALID_JVM_VARIANTS="server client minimal core zero custom"

###############################################################################
# Check which variant of the JVM that we want to build. Available variants are:
#   server: normal interpreter, and a tiered C1/C2 compiler
#   client: normal interpreter, and C1 (no C2 compiler)
#   minimal: reduced form of client with optional features stripped out
#   core: normal interpreter only, no compiler
#   zero: C++ based interpreter only, no compiler
#   custom: baseline JVM with no default features
#
AC_DEFUN_ONCE([HOTSPOT_SETUP_JVM_VARIANT],
[
  AC_ARG_WITH([jvm-variant], [AS_HELP_STRING([--with-jvm-variant],
      [Which JVM variant to build (server client minimal core zero custom)
      @<:@server@:>@])])

  AC_ARG_WITH([jvm-variants], [AS_HELP_STRING([--with-jvm-variants],
      [(DEPRECATED) JVM variants to build, separated by commas (server client minimal core
      zero custom) @<:@server@:>@])])

  if test "x$with_jvm_variant" != x && test "x$with_jvm_variants" != x; then
    AC_MSG_ERROR([Cannot specify both --with-jvm-variant and --with-jvm-variants])
  fi

  AC_MSG_CHECKING([which variant of the JVM to build])
  if test "x$with_jvm_variants" = x && test "x$with_jvm_variant" = x; then
    # No option given, use "server" as default
    JVM_VARIANT="server"
    AC_MSG_RESULT([$JVM_VARIANT] (default))
  elif test "x$with_jvm_variants" != x; then
    if [ [[ ! "$with_jvm_variants" =~ "," ]] ]; then
      JVM_VARIANT="$with_jvm_variants"
      AC_MSG_RESULT([$JVM_VARIANT])
      AC_MSG_WARN([--with-jvm-variants is deprecated, use --with-jvm-variant instead])
    else
      # Multiple variants requested. This is deprecated, but we support it for
      # now by creating a separate configuration for each "extra" variant.

      jvm_variants_list=`$ECHO $with_jvm_variants | $SED -e 's/,/ /g'`

      # We treat the first variant as the main variant, to be built by this
      # configuration. The remaining variants are built separately.
      JVM_VARIANT=`$ECHO $jvm_variants_list | $CUT -d " " -f 1`
      AC_MSG_RESULT([$JVM_VARIANT])

      BUILD_EXTRA_JVM_VARIANTS=`$ECHO $jvm_variants_list | $CUT -d " " -f 2-`
      # Check that the extra variants are valid
      UTIL_GET_NON_MATCHING_VALUES(INVALID_VARIANTS, $BUILD_EXTRA_JVM_VARIANTS, \
          $VALID_JVM_VARIANTS)
      if test "x$INVALID_VARIANTS" != x; then
        AC_MSG_NOTICE([Unknown variant(s) specified: "$INVALID_VARIANTS"])
        AC_MSG_NOTICE([The available JVM variants are: "$VALID_JVM_VARIANTS"])
        AC_MSG_ERROR([Cannot continue])
      fi

      # Replace the commas with AND for use in the build directory name.
      JVM_VARIANT_NAME=`$ECHO "$with_jvm_variants" | $SED -e 's/,/AND/g'`

      AC_MSG_WARN([--with-jvm-variants is deprecated, use --with-jvm-variant and --with-jvm-imports instead])
    fi
  else
    JVM_VARIANT="$with_jvm_variant"
    AC_MSG_RESULT([$JVM_VARIANT])
  fi

  if test "x$JVM_VARIANT_NAME" = x; then
    JVM_VARIANT_NAME="$JVM_VARIANT"
  fi

  UTIL_GET_NON_MATCHING_VALUES(INVALID_VARIANT, $JVM_VARIANT, \
      $VALID_JVM_VARIANTS)
  if test "x$INVALID_VARIANT" != x; then
    AC_MSG_NOTICE([Unknown variant specified: "$INVALID_VARIANT"])
    AC_MSG_NOTICE([The available JVM variants are: "$VALID_JVM_VARIANTS"])
    AC_MSG_ERROR([Cannot continue])
  fi

  AC_MSG_CHECKING([for additional JVMs to build])
  if test "x$BUILD_EXTRA_JVM_VARIANTS" != x; then
    AC_MSG_RESULT([$BUILD_EXTRA_JVM_VARIANTS])
  else
    AC_MSG_RESULT([none])
  fi

  AC_SUBST(JVM_VARIANT)
  AC_SUBST(BUILD_EXTRA_JVM_VARIANTS)
])

###############################################################################
# Misc hotspot setup that does not fit elsewhere.
#
AC_DEFUN_ONCE([HOTSPOT_SETUP_MISC],
[
  if test "x$JVM_VARIANT" = xzero; then
    # zero behaves as a platform and rewrites these values. This is a bit weird.
    # But when building zero, we never build any other variants so it works.
    HOTSPOT_TARGET_CPU=zero
    HOTSPOT_TARGET_CPU_ARCH=zero
  fi


  AC_ARG_WITH([hotspot-build-time], [AS_HELP_STRING([--with-hotspot-build-time],
  [timestamp to use in hotspot version string, empty means determined at build time @<:@source-date/empty@:>@])])

  AC_MSG_CHECKING([what hotspot build time to use])

  if test "x$with_hotspot_build_time" != x; then
    HOTSPOT_BUILD_TIME="$with_hotspot_build_time"
    AC_MSG_RESULT([$HOTSPOT_BUILD_TIME (from --with-hotspot-build-time)])
  else
    if test "x$SOURCE_DATE" = xupdated; then
      HOTSPOT_BUILD_TIME=""
      AC_MSG_RESULT([determined at build time (default)])
    else
      # If we have a fixed value for SOURCE_DATE, use it as default
      HOTSPOT_BUILD_TIME="$SOURCE_DATE_ISO_8601"
      AC_MSG_RESULT([$HOTSPOT_BUILD_TIME (from --with-source-date)])
    fi
  fi

  AC_SUBST(HOTSPOT_BUILD_TIME)


  # Override hotspot cpu definitions for ARM platforms
  if test "x$OPENJDK_TARGET_CPU" = xarm; then
    HOTSPOT_TARGET_CPU=arm_32
    HOTSPOT_TARGET_CPU_DEFINE="ARM32"
  fi
])
