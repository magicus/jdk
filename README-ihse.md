This PR proposes to introduce the following new directories below "make":
core/ -- contains the most important, system-wide or framework files needed to keep the build system functioning
test/ -- contains makefiles related to tests
targets/ -- contain makefiles that each build an independent, non-module target
utils/ -- contains files that provides utilities, not normally used in the build but triggered separately by the user

These will have further subdirectories as such:
core/api/ -- Here all SetupFoo macros reside
core/base/ -- This is the home of MakeBase.gmk, Utils.gmk and friends
core/buildtools/ -- Makefiles needed to compile and access our Java buildtools
core/interim/ -- Makefiles related to creation and usage of the interim JDK
core/modules/ -- Support for the module/phase processing
core/modules/common -- The old "make/common/modules"
core/support/ -- Gory implementation details that are not free-standing but included by some other file

test/build -- Everything related to building tests
test/run -- Everything related to running tests

utils/devkits -- The old "make/devkit"

As before, we have:
autoconf/ -- contains the code for the "configure" script
conf/ -- contain configuration files for the build
data/ -- contain data files that are not tied to a single specific module
hotspot/ -- contain makefiles related to hotspot (this is somewhat of an anomaly)
ide/ -- contains  files supporting different IDEs
modules/ -- contains per-module and per-phase specific makefiles
scripts/ -- contain shell scripts used in the build (some should probably move to utils)
jdk/, langools/ and src/ -- contain buildtools (not properly organized yet)

Here is a list of gmk files that have moved with their new destination:

./core/api/ProcessMarkdown.gmk
./core/api/TextFileProcessing.gmk
./core/api/native/DebugSymbols.gmk
./core/api/native/CompileFile.gmk
./core/api/native/Link.gmk
./core/api/native/Paths.gmk
./core/api/native/Flags.gmk
./core/api/native/LinkMicrosoft.gmk
./core/api/Execute.gmk
./core/api/JdkNativeCompilation.gmk
./core/api/TestFilesCompilation.gmk
./core/api/ZipArchive.gmk
./core/api/CopyFiles.gmk
./core/api/JavaCompilation.gmk
./core/api/JarArchive.gmk
./core/api/NativeCompilation.gmk

./core/base/MakeIO.gmk
./core/base/MakeBase.gmk
./core/base/Utils.gmk
./core/base/FileUtils.gmk

./core/buildtools/CompileModuleTools.gmk
./core/buildtools/ModuleTools.gmk
./core/buildtools/ToolsLangtools.gmk
./core/buildtools/ToolsHotspot.gmk
./core/buildtools/ToolsJdk.gmk
./core/buildtools/CompileToolsHotspot.gmk
./core/buildtools/CompileToolsJdk.gmk

./core/interim/CompileInterimLangtools.gmk
./core/interim/CopyInterimTZDB.gmk
./core/interim/InterimImage.gmk

./core/modules/CompileJavaModules.gmk
./core/modules/ModuleWrapper.gmk
./core/modules/common/GensrcModuleInfo.gmk
./core/modules/common/LibCommon.gmk
./core/modules/common/GensrcCommon.gmk
./core/modules/common/GendataCommon.gmk
./core/modules/common/GensrcProperties.gmk
./core/modules/common/LauncherCommon.gmk
./core/modules/common/CopyCommon.gmk
./core/modules/CreateJmods.gmk
./core/modules/Modules.gmk

./core/support/MainSupport.gmk
./core/support/InitSupport.gmk
./core/support/FindTests.gmk

./core/GenerateLinkOptData.gmk
./core/Bundles.gmk
./core/Doctor.gmk
./core/ExplodedImageOptimize.gmk
./core/Main.gmk
./core/CompileCommands.gmk
./core/Init.gmk
./core/Global.gmk
./core/Images.gmk
./core/CopyImportModules.gmk
./core/SourceRevision.gmk

./test/build/BuildTestLib.gmk
./test/build/BuildMicrobenchmark.gmk
./test/build/JtregNativeJdk.gmk
./test/build/BuildFailureHandler.gmk
./test/build/JtregNativeHotspot.gmk
./test/build/BuildJtregTestThreadFactory.gmk
./test/build/JtregNativeLibTest.gmk
./test/build/BuildTestLibNative.gmk
./test/run/RunTests.gmk
./test/run/RunTestsPrebuiltSpec.gmk
./test/run/RunTestsPrebuilt.gmk

./targets/BuildStatic.gmk
./targets/GenerateModuleSummary.gmk
./targets/Hsdis.gmk
./targets/ZipSecurity.gmk
./targets/JrtfsJar.gmk
./targets/ZipSource.gmk
./targets/TestImage.gmk
./targets/StaticLibsImage.gmk
./targets/ReleaseFile.gmk
./targets/Coverage.gmk
./targets/CompileDemos.gmk
./targets/Docs.gmk
./targets/MacBundles.gmk
./targets/GraalBuilderImage.gmk

./utils/UpdateBuildDocs.gmk
./utils/UpdateX11Wrappers.gmk
