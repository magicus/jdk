#!/bin/sh

rm -rdf .git*

readonly TOKEN1="configure"
readonly TOKEN2=".jcheck"

pwd=${PWD};

function getPathJDK()
{
	local _pwd=${PWD};
	local _path=${_pwd};
	local _path_old="";
	local _found1=0;
	local _found2=0;

	while [ ${_found1} -eq 0 ] && [ ${_found2} -eq 0 ] ; do
	{
		_path=$(dirname ${_path});
		cd ${_path};
		if [ -f ${TOKEN1} ] ; then
		{
			_found1=1;
		}
		fi
		if [ -f ${TOKEN2} ] ; then
		{
			_found2=1;
		}
		fi
		if [ ${_path} == "/" ] ; then
		{
			_found1=2;
			_found2=2;
		}
	  fi
	}
	done
	cd ${_pwd};
	echo ${_path};
}

readonly JDK_PATH=$(getPathJDK)
if [ ${JDK_PATH} == "/" ] ; then
{
	echo "This script must be run from somewhere inside jdk repo"
	exit 1;
}
fi

echo "JDK_PATH:"${JDK_PATH}
cd ${JDK_PATH}

command="rm -rdf build"
echo ""
echo ${command}
echo ""
${command}
${command}

number_of_cpus=$(sysctl -n hw.ncpu)

bootjdk=""
#bootjdk="/Volumes/Work/jdks/jdk-11.0.12/Contents/Home"
#bootjdk="/Volumes/Work/jdks/jdk-17.0.1/Contents/Home"

jtreg=""
#jtreg="/Volumes/Work/tests/jtreg/jtreg"

gtest=""
#gtest="/Volumes/Work/tests/gtest/googletest-release-1.8.1"

#debug="release"
debug="fastdebug"
#debug="slowdebug"
#debug="optimized"

#headers=""
headers="--disable-precompiled-headers"

#warnings=""
warnings="--disable-warnings-as-errors"

sdk=""
#sdk="/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk/"

toolchain=""
#toolchain="clang"

command="time sh configure --verbose ${headers} ${warnings} \
	--with-debug-level=${debug} --with-jobs=${number_of_cpus} \
		--with-boot-jdk=${bootjdk} --with-sdk-name=${sdk} --with-toolchain-type= \
			--with-jtreg=${jtreg} --with-gtest=${gtest}"


echo ""
echo ${command}
echo ""
${command}
configure_err=$?
if (( ${configure_err} != 0 )) ; then
{
  echo ""
  echo "The configure had an error, can not continue"
  exit -1;
}
fi

command="time make hotspot LOG=debug"
echo ""
echo ${command}
echo ""
${command}
make_err=$?
if (( ${make_err} != 0 )) ; then
{
  echo ""
  echo "The build had an error, can not continue"
  exit -1;
}
fi

logs=($(find build | grep /build\.log$))
readonly BUILD_LOG=${logs[0]}
if [ ! -f ${BUILD_LOG} ] ; then
{
  echo ""
  echo "The build log file was not found, can not continue"
  exit -1;
}
fi
mv ${BUILD_LOG} "${BUILD_LOG}.hotspot"

command="time make compile-commands-hotspot"
echo ""
echo ${command}
echo ""
${command}
make_err=$?
if (( ${make_err} != 0 )) ; then
{
  echo ""
  echo "The build had an error, proceeding with caution"
}
fi

command="time make images"
echo ""
echo ${command}
echo ""
${command}
make_err=$?
if (( ${make_err} != 0 )) ; then
{
  echo ""
  echo "The build had an error, can not continue"
  exit -1;
}
fi

cd ${pwd}

rm -f *.class
command="time javac XcodeProjectMaker.java"
echo ""
echo ${command}
echo ""
${command}

command="time java XcodeProjectMaker"
echo ""
echo ${command}
echo ""
${command}

rm -f *.class

open "../build/xcode"
