package build.tools.configure;

public class BasicTools {
    public static void setupFundamentalTools(Conf conf) {
        // Bootstrapping: These tools were needed by UTIL_LOOKUP_PROGS
        UtilPaths.requireProgs(conf, "BASENAME", "basename");
        UtilPaths.requireProgs(conf, "DIRNAME", "dirname");
        UtilPaths.requireProgs(conf, "FILE", "file");
        UtilPaths.requireProgs(conf, "LDD", "ldd");

        // First are all the fundamental required tools.
        UtilPaths.requireProgs(conf, "BASH", "bash");
        UtilPaths.requireProgs(conf, "CAT", "cat");
        UtilPaths.requireProgs(conf, "CHMOD", "chmod");
        UtilPaths.requireProgs(conf, "CP", "cp");
        UtilPaths.requireProgs(conf, "CUT", "cut");
        UtilPaths.requireProgs(conf, "DATE", "date");
        UtilPaths.requireProgs(conf, "DIFF", "gdiff diff");
        UtilPaths.requireProgs(conf, "ECHO", "echo");
        UtilPaths.requireProgs(conf, "EXPR", "expr");
        UtilPaths.requireProgs(conf, "FIND", "find");
        UtilPaths.requireProgs(conf, "GUNZIP", "gunzip");
        UtilPaths.requireProgs(conf, "GZIP", "pigz gzip");
        UtilPaths.requireProgs(conf, "HEAD", "head");
        UtilPaths.requireProgs(conf, "LN", "ln");
        UtilPaths.requireProgs(conf, "LS", "ls");
        // gmkdir is known to be safe for concurrent invocations with -p flag.
        UtilPaths.requireProgs(conf, "MKDIR", "gmkdir mkdir");
        UtilPaths.requireProgs(conf, "MKTEMP", "mktemp");
        UtilPaths.requireProgs(conf, "MV", "mv");
        UtilPaths.requireProgs(conf, "AWK", "gawk nawk awk");
        UtilPaths.requireProgs(conf, "PRINTF", "printf");
        UtilPaths.requireProgs(conf, "RM", "rm");
        UtilPaths.requireProgs(conf, "RMDIR", "rmdir");
        UtilPaths.requireProgs(conf, "SH", "sh");
        UtilPaths.requireProgs(conf, "SORT", "sort");
        UtilPaths.requireProgs(conf, "TAIL", "tail");
        UtilPaths.requireProgs(conf, "TAR", "gtar tar");
        UtilPaths.requireProgs(conf, "TEE", "tee");
        UtilPaths.requireProgs(conf, "TOUCH", "touch");
        UtilPaths.requireProgs(conf, "TR", "tr");
        UtilPaths.requireProgs(conf, "UNAME", "uname");
        UtilPaths.requireProgs(conf, "WC", "wc");
        UtilPaths.requireProgs(conf, "XARGS", "xargs");

        // Then required tools that require some special treatment.
        UtilPaths.requireSpecial(conf, "GREP", AC.progGrep());
        UtilPaths.requireSpecial(conf, "EGREP", AC.progEGrep());
        UtilPaths.requireSpecial(conf, "FGREP", AC.progFGrep());
        UtilPaths.requireSpecial(conf, "SED", AC.progSed());

        // Optional tools, we can do without them
        UtilPaths.lookupProgs(conf, "DF", "df");
        UtilPaths.lookupProgs(conf, "GIT", "git");
        UtilPaths.lookupProgs(conf, "NICE", "nice");
        UtilPaths.lookupProgs(conf, "READLINK", "greadlink readlink");

        // These are only needed on some platforms
        UtilPaths.lookupProgs(conf, "PATHTOOL", "cygpath wslpath");
        UtilPaths.lookupProgs(conf, "LSB_RELEASE", "lsb_release");
        UtilPaths.lookupProgs(conf, "CMD", "cmd.exe", "$PATH:/cygdrive/c/windows/system32:/mnt/c/windows/system32:/c/windows/system32");

        // For compare.sh only
        UtilPaths.lookupProgs(conf, "CMP", "cmp");
        UtilPaths.lookupProgs(conf, "UNIQ", "uniq");

        // Always force rm.
        conf.put("RM", conf.get("RM") + " -f");
    }
}
