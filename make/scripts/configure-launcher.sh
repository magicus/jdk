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
### Find the Boot JDK
###

# Start by checking if there is a boot jdk option, if so, we need to read it
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
  debug "Boot JDK specified on command line: $bootjdk"

  # Check if the given bootjdk is valid
  if [ ! -x "$bootjdk/bin/java" ]; then
    echo "Error: The boot jdk '$bootjdk' is missing bin/java" 1>&2
    exit 1
  fi
  if [ ! -x "$bootjdk/bin/javac" ]; then
    echo "Error: The boot jdk '$bootjdk' is missing bin/javac" 1>&2
    echo "A JDK is required, not just a JRE" 1>&2
    exit 1
  fi
else
  # Try to locate a usable JDK
  debug "Looking for a usable boot JDK"
  :
  echo not found
fi

echo Our bootjdk is: :$bootjdk:
#java="$bootjdk/bin/java"
java=java
#javac="$bootjdk/bin/javac"
javac=javac
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

  debug "Running 'javac -d $classes_dir -sourcepath $source_path \
      $main_class_dir/build/tools/configure/Configure.java'"

  "$javac" -d "$classes_dir" --source-path "$source_path" \
      "$main_class_dir/build/tools/configure/Configure.java"

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
  if [ "$tempdir" != "" ]; then
    debug "Removing temporary directory: $tempdir"
    rm -rf $tempdir
  fi
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
debug "Running 'java $java_opts -cp $classes_dir \
    build.tools.configure.Configure $commandline_file'"
"$java" $java_opts -cp "$classes_dir" build.tools.configure.Configure \
    "$TOPDIR" "$commandline_file"
result_code=$?
debug "Configure returned $result_code"
exit $result_code
