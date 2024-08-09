#!/bin/sh

echo "running script_before.sh"

readonly JDK_BUILD_PATH="../../build/mac*";
readonly JRE_LOCATE_PATTERN="images/jdk/bin/javac";
readonly HOTSPOT_TOUCH_FILE="../../src/hotspot/os/posix/jvm_posix.cpp";

echo ">>>>>>> Making a copy of JDK ...";

javac_file_array=( $(find ${JDK_BUILD_PATH} | grep ${JRE_LOCATE_PATTERN}) );
#echo ">>>>>>>      javac_file_array: "${javac_file_array};
#javac_file_array: ../../build/macosx-x64/jdk/bin/javac

javac_file=${javac_file_array[0]};
if [ -z ${javac_file} ] ; then
{
  echo ">>>>>>>   ERROR: could not locate bin/javac (did you remember to do \"make images\"?)";
  exit 1;
}
fi
#echo ">>>>>>>      javac_file: "${javac_file};
#javac_file: ../../build/macosx-x64/jdk/bin/javac

jdk_build_path=$(dirname $(dirname ${javac_file}));
#echo ">>>>>>>      jdk_build_path: "${jdk_build_path};
#jdk_build_path=../../build/macosx/images/jdk

if [ ! -f "build/${JRE_LOCATE_PATTERN}" ] ; then
{
  echo ">>>>>>>   Copying jdk over...";
  rsync -a "${jdk_build_path}" "build/";
}
fi

# the following files will be supplied by the Xcode build
rm -rf "build/jdk/lib/server/libjvm.dylib";
rm -rf "build/jdk/lib/server/libjvm.dylib.dSYM";
rm -rf "build/jdk/lib/server/libjvm.diz";

echo ">>>>>>> DONE";

echo ">>>>>>> Touching ${HOTSPOT_TOUCH_FILE} to force HotspotVM rebuilt";
if [ ! -f ${HOTSPOT_TOUCH_FILE} ] ; then
{
    echo ">>>>>>>   Cannot find ${HOTSPOT_TOUCH_FILE}";
    exit 1;
}
fi
touch ${HOTSPOT_TOUCH_FILE};

echo ">>>>>>> DONE";

echo ">>>>>>> Xcode should be building the HotspotVM now...";
