#!/bin/bash
#
# Copyright (c) 2012, 2023, Oracle and/or its affiliates. All rights reserved.
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
#
# This code is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License version 2 only, as
# published by the Free Software Foundation.
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

###
### Ensure a basic sane environment
###

if [ "$1" != "CHECKME" ]; then
  echo "ERROR: Calling this wrapper script directly is not supported" 1>&2
  echo "Use the 'configure' script in the top-level directory instead" 1>&2
  exit 1
fi

# The next argument is the absolute top-level directory path.
# The TOPDIR variable is passed on to configure.ac.
TOPDIR="$2"
# Remove these two arguments to get to the user supplied arguments
shift
shift

if [ "$BASH" = "" ]; then
  echo "Error: This script must be run using bash" 1>&2
  exit 1
fi

if [[ "$TOPDIR" =~ .*[[:space:]]+.* ]]; then
  echo "Error: Build path containing space character is not supported" 1>&2
  exit 1
fi

# Make sure all shell commands are executed with the C locale
export LC_ALL=C

# Make sure "**" globbing works
shopt -s globstar

DEBUG="${DEBUG_CONFIGURE:-false}"

function debug() {
  if [ "$DEBUG" = "true" ]; then
    echo "configure: $*"
  fi
}

###
### Figure out where to store our output
###

CURRENT_DIR="$(pwd)"
if [ "$CURRENT_DIR" = "$TOPDIR" ]; then
  # We are running configure from the src root.
  # Create '.configure-support' under $TOPDIR/build
  support_dir="$TOPDIR/build/.configure-support"
elif [ "$CURRENT_DIR" = "$CUSTOM_ROOT" ]; then
  # We are running configure from the custom root.
  # Create '.configure-support' under $CUSTOM_ROOT/build
  support_dir="$CUSTOM_ROOT/build/.configure-support"
else
  # We are running configure from outside of the src dir.
  # Create 'configure-support' in the current directory.
  support_dir="$CURRENT_DIR/configure-support"
fi

debug "Top dir: $TOPDIR"
debug "Support dir: $support_dir"

java_source_dir="$TOPDIR/make/src/classes"
classes_dir="$support_dir/configure_classes"

###
### Find a JDK capable of compiling and running the configure tool
###

# Helper function to check if a path contains a valid JDK
# Will set $java and $javac if so
function verify_potential_jdk() {
  potential_jdk="$1"
  if [ -x "$potential_jdk/bin/java" ]; then
    if [ ! -x "$potential_jdk/bin/javac" ]; then
      debug "$potential_jdk contains bin/java but not bin/javac; ignoring"
      return
    fi
    debug "Found JDK at $potential_jdk"
    java="$potential_jdk/bin/java"
    javac="$potential_jdk/bin/javac"
    return
  fi
  if [ -x "$potential_jdk/bin/java.exe" ]; then
    if [ ! -x "$potential_jdk/bin/javac.exe" ]; then
      debug "$potential_jdk contains bin/java.exe but not bin/javac.exe; ignoring"
      return
    fi
    debug "Found JDK at $potential_jdk"
    java="$potential_jdk/bin/java.exe"
    javac="$potential_jdk/bin/javac.exe"
    return
  fi
  debug "No JDK found at $potential_jdk"
}

# Helper function to check if there is a JDK below the given directory, and if
# so, try to pick the latest.
# Will set $java and $javac if one is found
function find_best_jdk_in_dir() {
  dir_to_check="$1"
  debug "Searching for JDK in $dir_to_check"
  # This directory can contain multiple JDKs. In an effort to try to find the
  # latest first, we sort the directory in reverse order.
  possible_jdks=$(find "$dir_to_check" -type d -name bin 2> /dev/null | sort -r)
  for jdk_bin_dir in $possible_jdks ; do
    # We actually located the bin directory, so we need check one level up
    check_jdk="$(dirname "$jdk_bin_dir")"
    debug "Checking $check_jdk for JDK..."
    verify_potential_jdk "$check_jdk"
    if [ "$java" != "" ]; then
      return;
    fi
  done
}

# Helper function to check if there is a JDK below the directory point to by
# $VAR/Java, and if so, try to pick the latest.
# Will set $java and $javac if one is found
function find_best_jdk_from_variable() {
  var_to_check="$1"
  if [ -n "${!var_to_check}" ]; then
    debug "Checking environment variable $var_to_check: ${!var_to_check}"
    find_best_jdk_in_dir "${!var_to_check}/Java"
  else
    debug "Environment variable $var_to_check is not set"
  fi
}

# Start by checking if there is a boot jdk option
for option; do
  case $option in
    --bootjdk=* | --boot-jdk=* | --with-bootjdk=* | --with-boot-jdk=* | -j=*)
    # FIXME: make safe for spaces in path
      bootjdk=$(expr "X$option" : '[^=]*=\(.*\)')
      ;;
    -j ) # Save the next argument as the bootjdk
      bootjdk_next=true
      ;;
    * )
      if [ "$bootjdk_next" = "true" ]; then
        bootjdk="$option"
        bootjdk_next=false
      fi
  esac
done

if [ "$bootjdk" != "" ]; then
  debug "Trying Boot JDK specified on command line: $bootjdk"
  verify_potential_jdk "$bootjdk"
  # If the boot jdk argument is given, we require for it to be valid
  if [ "$java" = "" ]; then
    echo "Error: The specified boot jdk '$bootjdk' is invalid" 1>&2
    exit 1
  fi
fi

# If a boot JDK was not specified, try various ways to find a valid JDK
if [ "$java" = "" ]; then
  # First test: Try using $JAVA_HOME
  if [ "$JAVA_HOME" != "" ]; then
    debug "Trying JAVA_HOME: $JAVA_HOME"
    verify_potential_jdk "$JAVA_HOME"
  else
    debug "JAVA_HOME is not set"
  fi
fi

if [ "$java" = "" ]; then
  # Next test:Try using /usr/libexec/java_home (typically available on macOS)
  if [ -x /usr/libexec/java_home ]; then
    libexec_java_home="$(/usr/libexec/java_home 2> /dev/null)"
    debug "Trying /usr/libexec/java_home: $libexec_java_home"
    if [ "$libexec_java_home" != "" ]; then
      verify_potential_jdk "$libexec_java_home"
    fi
  else
    debug "No /usr/libexec/java_home found"
  fi
fi

if [ "$java" = "" ]; then
  # Next test: Is java and javac on the PATH?
  potential_java="$(command -v java || true)"
  potential_javac="$(command -v javac || true)"
  if [ -x "$potential_java" ]; then
    if [ -x "$potential_javac" ]; then
      debug "Found java and javac on the PATH"
      java="$potential_java"
      javac="$potential_javac"
    else
      debug "Found java on the PATH ($potential_java) but javac is missing"
    fi
  else
    debug "java not found on PATH"
  fi
fi

if [ "$java" = "" ]; then
  # Next test: Look in locations pointed to by well-known environment variables
  for variable in "ProgramW6432" "PROGRAMW6432" "PROGRAMFILES" "ProgramFiles"; do
    find_best_jdk_from_variable "$variable"
    if [ "$java" != "" ]; then
      break
    fi
  done
fi

if [ "$java" = "" ]; then
  # Next test: Look in well-known static locations
  for dir in "/usr/lib/jvm" "/Library/Java/JavaVirtualMachines" \
      "/System/Library/Java/JavaVirtualMachines" \
      "/cygdrive/c/Program Files/Java"; do
    find_best_jdk_in_dir "$dir"
    if [ "$java" != "" ]; then
      break
    fi
  done
fi

if [ "$java" = "" ]; then
  # We've made all possible checks
  echo "Error: Unable to find a boot JDK. Please use --with-boot-jdk or set JAVA_HOME" 1>&2
  exit 1
fi

debug "Using java: $java"
debug "Using javac: $javac"
java_opts="-Xmx512m"

###
### Compile the configure tool, if needed
###

# Check if we need to compile the configure tool
compilation_needed=false
if [ ! -d "$classes_dir" ]; then
  # Generated script is missing, so we definitely need to create it
  debug "Compiled configure dir is not present"
  compilation_needed=true
else
  # Check if the compiled main class is older than any of the java sources
  source_files="$java_source_dir/**/*.java"
  if [ "$CUSTOM_CONFIG_DIR" != "" ]; then
    custom_source_files="$CUSTOM_CONFIG_DIR/**/*.java"
  fi

  for file in $source_files $custom_source_files ; do
    if [ "$file" -nt "$classes_dir/build/tools/configure/Configure.class" ]; then
      debug "Java source file '$file' is newer than compiled configure"
      compilation_needed=true
    fi
  done
fi

if [ "$compilation_needed" = "true" ]; then
  debug "Compiling configure to $classes_dir"

  # Compile the configure tool
  if [ "$CUSTOM_CONFIG_DIR" = "" ]; then
    source_path="$java_source_dir"
    main_class_dir="$java_source_dir"
  else
    source_path="$CUSTOM_CONFIG_DIR:$java_source_dir"
    main_class_dir="$CUSTOM_CONFIG_DIR"
  fi

  mkdir -p "$classes_dir"

  debug "Running '"$javac" -d "$classes_dir" -sourcepath "$source_path" "$main_class_dir/build/tools/configure/Configure.java"'"

  "$javac" -d "$classes_dir" -sourcepath "$source_path" "$main_class_dir/build/tools/configure/Configure.java"

  # Sanity check
  if [ ! -e "$classes_dir/build/tools/configure/Configure.class" ]; then
    echo "Error: Failed to compile configure" 1>&2
    exit 1
  fi
fi

###
### Call the configure tool
###

# Setup a temporary directory
trap "cleanup" EXIT
function cleanup() {
  result_code=$?
  if [ "$tempdir" != "" ]; then
    debug "Removing temporary directory: $tempdir"
    rm -rf "$tempdir"
  fi
  if [ "$result_code" = "1" ]; then
    # This can happen if there is something wrong when executing java with the
    # compiled classes. Remove them and ask the user to try again.
    rm -rf "$classes_dir"
    echo "Configure failed to launch. Please try again." 1>&2
  fi
  exit $result_code
}
tempdir=$(mktemp -d -t jdk-configure.XXXXXX)
debug "Using temporary directory: $tempdir"

# We pass the command line as a file, one argument per line, to avoid
# more shell quoting issues
commandline_file="$tempdir/commandline.txt"
echo "# Command line: $*" > "$commandline_file"
for option; do
  echo "$option" >> "$commandline_file"
done

# Now actually call the tool
debug "Running '"$java" $java_opts -cp "$classes_dir" build.tools.configure.Configure "$TOPDIR" "$commandline_file"'"

"$java" $java_opts -cp "$classes_dir" build.tools.configure.Configure "$TOPDIR" "$commandline_file"

result_code=$?
debug "Configure returned $result_code"
exit $result_code
