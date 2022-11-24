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

package build.tools.configure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Configure {
    public static boolean debugging = false;

    private final String topDir;
    private final List<String> commandLines;

    public Configure(String topDir, List<String> commandLines) {
        this.topDir = topDir;
        this.commandLines = commandLines;
    }

    public static void debug(String message) {
        if (debugging) {
            System.out.println(message);
        }
    }

    public static void debug(String message, Throwable t) {
        if (debugging) {
            System.out.println(message);
            t.printStackTrace();
        }
    }

    public static void main(String... args)  {
        String debugEnv = System.getenv("DEBUG_CONFIGURE");
        if (debugEnv != null && debugEnv.equalsIgnoreCase("true")) {
            debugging = true;
        }

        if (args.length < 2) {
            System.err.println("configure: Error: missing arguments: <top dir> <command line file>");
            System.exit(10);
        }

        String topDir = args[0];
        String commandLineFile = args[1];
        List<String> commandLines;
        try {
            commandLines = Files.readAllLines(Paths.get(commandLineFile));
        } catch (IOException e) {
            System.err.println("configure: Error: Cannot read command line file");
            debug("Command line file name: " + commandLineFile, e);
            System.exit(10);
            return; // Help compiler understand commandLines will have a value later on
        }

        Configure configure = new Configure(topDir, commandLines);
        int exitCode = configure.start();
        System.exit(exitCode);
    }

    private int start() {
        System.out.println("Hello, world!" + commandLines);
        System.out.println(Utils.foo());

        return 0;
    }
}
