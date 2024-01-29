package build.tools.config;

public class Configure {

    /*
    What are we really trying to do?
     - selection: user selects options that will affect the resulting build
     - determine possible values for each option. For instance, target platform might have
     only one possible value.
     - determine and verify build environment.
       - this includes the build machine tools, etc
       - and toolchain
       - and libraries and other dependencies
     - adaptation: some aspect of these need configuration, e.g. additional flags to toolchain


===
1) Basic examination, prerequisites
* Examine build system: os/cpu, memory, basic tools availability. Determine build machine/os env.
  ==> Now we know that we can continue building as such
* Examine prerequisites for building the JDK: paths correctly, disk space present,
build disk is local.
  ==> Now we know that we can continue building the JDK
2) User options -- create a "wanted build" matrix
* Examine requested options by the user; provide defaults for non-specified values.
  Options can be components that are included or excluded (jvm features, docs, etc),
  or it can select between different ways of doing some things (e.g. include absolute
  path names or not), or it can just give value to things like version number or
  build date to use.

  For functionality that requires a dependency, add that to a list of "needed dependencies".
  ==> Now we know what we want to build, and what dependencies we need
3) Dependencies. Know we now what we need to have, check that we have that, verify that it is sane, and
possibly get information such as version from these dependencies.
* Check that all dependencies are present. These include:
  * bootjdk
  * toolchain
  * libraries
  * other dependencies

  For each dependency, use user overridden options first, then try to auto locate.

4) Adaptations -- create the output needed for the makefiles to work
* Make adaptations. Now that we know everything about the build, including dependencies and their
versions, we will need to make some adaptations. This includes things like compiler flags anc C defines.

     */
}
