/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package build.tools.configure.core;

public class UtilPaths {
    public static void requireProgs(Conf conf, String variable, String toolnames) {
        String toolPath = lookupTool(toolnames);
        // FIXME: fail if not found
        conf.put(variable, toolPath);
    }

    public static void requireSpecial(Conf conf, String variable, String toolname) {
        String toolPath = lookupTool(toolname);
        // FIXME: fail if not found
        conf.put(variable, toolPath);
    }

    public static void lookupProgs(Conf conf, String variable, String toolnames) {
        String toolPath = lookupTool(toolnames);
        // FIXME: how handle missing values?
        conf.put(variable, toolPath);
    }

    //         UTIL_REQUIRE_SPECIAL(GREP, [AC_PROG_GREP])
    public static void lookupProgs(Conf conf, String variable, String toolnames, String searchPath) {
        // FIXME: propagate search path
        String toolPath = lookupTool(toolnames);
        // FIXME: how handle missing values?
        conf.put(variable, toolPath);
    }

    public static void lookupToolchainProgs(Conf conf, String variable, String toolnames) {
    }


    private static String lookupTool(String toolnames) {
        for (String toolname : toolnames.split(" ")) {
            System.out.print("checking for " + toolname + "... ");
            String toolEnvName = toolname.toUpperCase();
            String toolEnvValue = Shell.env(toolEnvName);
            if (toolEnvValue != null) {
                // "configure: error: User supplied tool CAT="/foo" does not exist or is not executable"
            }
            // FIXME: FAKE!!!
            String toolPath = "/usr/bin/" + toolname;
            System.out.println(toolPath);
            return toolPath;
        }

        // FIXME: correct?
        return null;
    }

    public static String fixupPath(String path) {
        // do stuff, especially on Windows...
        return "";
    }
    /*
    # Check if the given file is a unix-style or windows-style executable, that is,
# if it expects paths in unix-style or windows-style.
# Returns "windows" or "unix" in $RESULT.
AC_DEFUN([UTIL_CHECK_WINENV_EXEC_TYPE],
     */

    public static WinEnvType checkWinEnvExecType(String file) {
        // do stuff
    }

    /*
###############################################################################
# This will make sure the given variable points to a executable
# with a full and proper path. This means:
# 1) There will be no spaces in the path. On unix platforms,
#    spaces in the path will result in an error. On Windows,
#    the path will be rewritten using short-style to be space-free.
# 2) The path will be absolute, and it will be in unix-style (on
#     cygwin).
# Any arguments given to the executable is preserved.
# If the input variable does not have a directory specification, then
# it need to be in the PATH.
# $1: The name of the variable to fix
# $2: Where to look for the command (replaces $PATH)
# $3: set to NOFIXPATH to skip prefixing FIXPATH, even if needed on platform
AC_DEFUN([UTIL_FIXUP_EXECUTABLE],
     */

    public static String fixupExecutable(String path) {
        // do stuff
    }

}
