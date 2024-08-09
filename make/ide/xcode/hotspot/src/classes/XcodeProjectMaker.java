/*
 * Copyright (c) 2017,2021 Oracle and/or its affiliates. All rights reserved.
 *
 */

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class XcodeProjectMaker
{
  static String VERSION = "2.0.0";

  public static void main(String[] args)
  {
    boolean isOpenJDK = true;
    
    System.out.println("");
    System.out.println("Version "+VERSION);
    System.out.println("");

    String path_to_jdk = null;
    if (args.length == 0)
    {
      System.out.println("No arguments - assuming this tool is being run somewhere from within jdk repo ...\n");
      path_to_jdk = FindRelativePathToJDKRootRoot();
    }
    else if (args.length == 1)
    {
      path_to_jdk = VerifyFile(args[0], true);
    }

    if (path_to_jdk == null)
    {
      System.err.println("Error: could not determine the path to \"jdk\" - either pass it via command line, or run this tool from within the \"jdk\"");
      System.exit(EXIT8);
    }

    final String LOG_FILE_NAME     = "build.log.hotspot";
    final String COMPILE_FILE_NAME = "compile_commands.json";
    final String BUILD_FOLDER_NAME = "build";

    String path_to_compile_commands = VerifyFile(FindFile(path_to_jdk, BUILD_FOLDER_NAME, COMPILE_FILE_NAME, true, true), false);
    if (path_to_compile_commands == null)
    {
      System.err.println("Error: could not find the compile commands file at "+path_to_compile_commands+" Did \"make compile-commands-hotspot\" succeed?");
      System.exit(EXIT10);
    }

    String path_to_build_log = VerifyFile(FindFile(path_to_jdk, BUILD_FOLDER_NAME, LOG_FILE_NAME, true, true), false);
    if (path_to_build_log == null)
    {
      System.err.println("Error: could not find the build logfile at "+path_to_build_log+" Did \"make images LOG=debug\" succeed?");
      System.exit(EXIT11);
    }

    String absolute_path_to_jdk = Paths.get(path_to_jdk).toAbsolutePath().normalize().toString();
    String absolute_path_to_compile_commands = Paths.get(path_to_compile_commands).toAbsolutePath().normalize().toString();
    String absolute_path_to_build_log = Paths.get(path_to_build_log).toAbsolutePath().normalize().toString();
    
    isOpenJDK = (FindFile(absolute_path_to_jdk, "", "closed", false, true) == null);
    System.out.println("                              Is OpenJDK \""+isOpenJDK+"\"");
    System.out.println("                          Path to jdk is \""+path_to_jdk+"\"");
    System.out.println("        Path to compile commands file is \""+path_to_compile_commands+"\"");
    System.out.println("               Path to build log file is \""+path_to_build_log+"\"");
    
    final String XCODE_FOLDER_NAME = "xcode";

    String path_to_xcode = absolute_path_to_jdk+"/"+BUILD_FOLDER_NAME+"/"+XCODE_FOLDER_NAME;
    String absolute_path_to_xcode = Paths.get(path_to_xcode).toAbsolutePath().normalize().toString();
    File xcode_folder = new File(absolute_path_to_xcode);
    xcode_folder.mkdirs();
    String path_from_xcode_to_jdk = FindRelativePathToJDKRootRoot(absolute_path_to_xcode);

    System.out.println("");
    System.out.println("                  Absolute path to jdk is \""+absolute_path_to_jdk+"\"");
    System.out.println("Absolute path to compile commands file is \""+absolute_path_to_compile_commands+"\"");
    System.out.println("       Absolute path to build log file is \""+absolute_path_to_build_log+"\"");
    System.out.println("          Xcode project will be placed in \""+absolute_path_to_xcode+"\"");
    System.out.println("");
    
    XcodeProjectMaker maker = new XcodeProjectMaker(isOpenJDK);
    maker.parse_hotspot_compile_commands(absolute_path_to_compile_commands);
    maker.parse_hotspot_build_log(absolute_path_to_build_log);
    maker.print_log_details();

    maker.prepare_files(absolute_path_to_jdk);
    maker.make_xcode_proj(absolute_path_to_jdk, absolute_path_to_xcode, path_from_xcode_to_jdk);
    
    String path_to_build = GetFileParent(absolute_path_to_compile_commands);
    maker.copy_files(absolute_path_to_xcode, path_to_build);
    maker.make_aliases(absolute_path_to_xcode, path_to_build);

    System.out.println("");
    System.out.println("");
    System.out.println("Xcode project was succesfully created and can be found in \""+absolute_path_to_xcode+"\"");
  }

  static String FALLBACK_MAPFILE_LINKER_FLAG        = "-Wl,-exported_symbols_list,";
  static String FALLBACK_LINKER_FLAGS[]             = { "-Wl,-install_name,@rpath/libjvm.dylib",
                                                        "-Wl,-rpath,@loader_path/.",
                                                        "-Wl,-rpath,@loader_path/..",
                                                        "-fPIC",
                                                        "-m64",
                                                        "-mno-omit-leaf-frame-pointer",
                                                        "-mstack-alignment=16"
                                                      };
                                                      
  static String LINKING_PARSE_TOKEN_1               = "bin/clang++";
  static String LINKING_PARSE_TOKEN_2               = "@rpath/libjvm.dylib";
  static String LINKING_PARSE_TOKEN_3               = "-o";
  
  static String EXCLUDE_PARSE_TOKEN_1               = "gtest";
  
  static String TEMPLATE_FRAMEWORK_SEARCH_PATHS     = "TEMPLATE_FRAMEWORK_SEARCH_PATHS";
  static String TEMPLATE_OTHER_CFLAGS               = "TEMPLATE_OTHER_CFLAGS";
  static String TEMPLATE_OTHER_LDFLAGS              = "TEMPLATE_OTHER_LDFLAGS";
  static String TEMPLATE_USER_HEADER_SEARCH_PATHS   = "TEMPLATE_USER_HEADER_SEARCH_PATHS";
  static String TEMPLATE_GROUP_GENSRC               = "TEMPLATE_GROUP_GENSRC";
  static String TEMPLATE_GROUP_CLOSED_SRC           = "TEMPLATE_GROUP_CLOSED_SRC";
  static String TEMPLATE_GROUP_CLOSED_TEST          = "TEMPLATE_GROUP_CLOSED_TEST";
  static String TEMPLATE_GROUP_OPEN_SRC             = "TEMPLATE_GROUP_OPEN_SRC";
  static String TEMPLATE_GROUP_OPEN_TEST            = "TEMPLATE_GROUP_OPEN_TEST";
  static String TEMPLATE_GROUPS                     = "TEMPLATE_GROUPS";
  static String TEMPLATE_PBXBUILDFILE               = "TEMPLATE_PBXBUILDFILE";
  static String TEMPLATE_PBXFILEREFERENCE           = "TEMPLATE_PBXFILEREFERENCE";
  static String TEMPLATE_PBXSOURCESSBUILDPHASE      = "TEMPLATE_PBXSOURCESSBUILDPHASE";
  static String TEMPLATE_JDK_PATH                   = "TEMPLATE_JDK_PATH";
  
  static String HOTSPOT_PBXPROJ                     = "hotspot.xcodeproj";
  static String PBXPROJ                             = "project.pbxproj";
  static String XCSAHAREDDATA                       = "xcshareddata";
  static String XCSCHEMES                           = "xcschemes";
  static String JVM_XCSCHEME                        = "jvm.xcscheme";
  static String J2D_XCSCHEME                        = "runJ2Demo.xcscheme";
  static String XCDEBUGGER                          = "xcdebugger";
  static String XCBKPTLIST                          = "Breakpoints_v2.xcbkptlist";
  
  static String DATA_DST_PATH                       = "data";
  static String TEMPLATE_PBXPROJ                    = "template_"+PBXPROJ+".txt";
  static String TEMPLATE_JVM_XCSCHEME               = "template_"+JVM_XCSCHEME+".txt";
  static String TEMPLATE_J2D_XCSCHEME               = "template_"+J2D_XCSCHEME+".txt";
  static String TEMPLATE_XCBKPTLIST                 = "template_"+XCBKPTLIST+".txt";
  
  static String EXCLUDE_FILES_PREFIX[]              = {"."};
  static String EXCLUDE_FILES_POSTFIX[]             = {".log", ".cmdline"};
  static String COMPILER_FLAGS_INCLUDE[]            = {"-m", "-f", "-D", "-W"};
  static String COMPILER_FLAGS_IS[]                 = {"-g", "-Os", "-0"};
  static String COMPILER_FLAGS_EXCLUDE[]            = {"-DTHIS_FILE", "-DGTEST_OS_MAC", "-mmacosx-version-min", "-Werror"}; // "-Werror" causes Xcode to stop compiling
  static String LINKER_FLAGS_PREFIXES[]             = {"-m", "-f", "-W", "-stdlib"};
  
  static int EXIT1                                  = -1;
  static int EXIT2                                  = -2;
  static int EXIT3                                  = -3;
  static int EXIT4                                  = -4;
  static int EXIT5                                  = -5;
  static int EXIT6                                  = -6;
  static int EXIT7                                  = -7;
  static int EXIT8                                  = -8;
  static int EXIT9                                  = -9;
  static int EXIT10                                 = -10;
  static int EXIT11                                 = -11;
  
  final boolean OpenJDK;
  
  HashMap<String,ArrayList<String>> compiled_files  = new HashMap<String,ArrayList<String>>();
  TreeSet<String> compiler_flags                    = new TreeSet();
  TreeSet<String> linker_flags                      = new TreeSet();
  TreeSet<String> header_paths                      = new TreeSet();
  
  String generated_hotspot_path                     = null;
  String isysroot                                   = null;
  String iframework                                 = null;
  String fframework                                 = null;
  
  DiskFile root_gensrc                              = new DiskFile("/", true);
  DiskFile root_closed_src                          = new DiskFile("/", true);
  DiskFile root_open_src                            = new DiskFile("/", true);
  DiskFile root_open_test                           = new DiskFile("/", true);
  
  public static String FindFile(String path, String where, String target, boolean recursive, boolean quiet)
  {
    String found = null;
    File dir = new File(path, where);
    File candidate = new File(dir, target);
    if (candidate.exists())
    {
      found = candidate.toString();
    }
    else
    {
      File files[] = dir.listFiles();
      for (File folder : files)
      {
        if (recursive && folder.isDirectory())
        {
          found = FindFile(folder.toString(), "", target, recursive, quiet);
          if (found != null)
          {
            break;
          }
        }
      }
    }
    if ((found == null) && (!quiet))
    {
      System.err.println("Error: \""+target+"\" not found in \""+path+where+"\"");
    }
    return found;
  }

  // find a path to what looks like jdk
  static String FindRelativePathToJDKRootRoot()
  {
    return FindRelativePathToJDKRootRoot(".");
  }
  static String FindRelativePathToJDKRootRoot(String root)
  {
    String path_to_jdk = null;
    String path = Paths.get(root).toAbsolutePath().normalize().toString();
    boolean found1 = false;
    boolean found2 = false;
    while ((path != null) && !found1 && !found2)
    {
      found1 = false;
      found2 = false;
      File folder = new File(path);
      File[] files = folder.listFiles();
      for (File file : files)
      {
        final String JDK_SCRIPT_TOKEN_1 = "configure";
        final String JDK_SCRIPT_TOKEN_2 = ".jcheck";
        
        String file_name = file.toPath().getFileName().toString();
        if (file_name.equals(JDK_SCRIPT_TOKEN_1))
        {
          found1 = true;
        }
        if (file_name.equals(JDK_SCRIPT_TOKEN_2))
        {
          found2 = true;
        }
        if (found1 && found2)
        {
          break;
        }
      }

      if (!found1 && !found2)
      {
        path = Paths.get(path).getParent().toString();
        if (path_to_jdk == null)
        {
          path_to_jdk = "..";
        }
        else
        {
          path_to_jdk += "/..";
        }
      }
    }
    return path_to_jdk;
  }

  static String VerifyFile(String path, boolean directory)
  {
    return VerifyFile(path, directory, true);
  }
  static String VerifyFile(String path, boolean directory, boolean fatal)
  {
    String verified = path;
    if (path != null)
    {
      File file = new File(path);
      if (!file.exists())
      {
        if (fatal)
        {
          System.err.println("Error: the specified path \""+path+"\" does not exist");
          System.exit(EXIT1);
        }
        else
        {
          System.err.println("Warning: the specified path \""+path+"\" does not exist");
        }
        verified = null;
      }
      if (directory && !file.isDirectory())
      {
        System.err.println("Error: the specified path \""+path+"\" does not specify folder");
        System.exit(EXIT2);
      }
      if (!directory && file.isDirectory())
      {
        System.err.println("Error: the specified path \""+path+"\" does not specify file");
        System.exit(EXIT3);
      }
    }
    else if (fatal)
    {
      System.err.println("Error: the specified path \""+path+"\" does not exist");
      System.exit(EXIT1);
    }
    return verified;
  }

  static String ReadFile(File file)
  {
    return ReadFile(file.toPath());
  }

  static String ReadFile(String path)
  {
    return ReadFile(Paths.get(path));
  }

  static String ReadFile(Path path)
  {
    String content = null;
    try
    {
      content = new String(Files.readAllBytes(path));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return content;
  }

  static void WriteFile(File file, String string)
  {
    WriteFile(file.toPath(), string);
  }

  static void WriteFile(Path path, String string)
  {
    try
    {
      Path path_written = Files.write(path, string.getBytes());
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(EXIT4);
    }
  }

  static boolean ExcludeFile(Path path)
  {
    return ExcludeFile(path.toString());
  }
  static boolean ExcludeFile(String string)
  {
    return ExcludeFile(string, null);
  }
  static boolean ExcludeFile(String string, String exclude)
  {
    if (exclude != null)
    {
      if (Contains(string, exclude))
      {
        return true;
      }
    }
    for (int i=0; i<EXCLUDE_FILES_PREFIX.length; i++)
    {
      if (string.startsWith(EXCLUDE_FILES_PREFIX[i]))
      {
        return true;
      }
    }
    for (int i=0; i<EXCLUDE_FILES_POSTFIX.length; i++)
    {
      if (string.endsWith(EXCLUDE_FILES_POSTFIX[i]))
      {
        return true;
      }
    }
    return false;
  }

  static boolean IsExcludeCompilerFlag(String string)
  {
    boolean flag = false;
    {
      for (int i=0; i<COMPILER_FLAGS_EXCLUDE.length; i++)
      {
        if (string.contains(COMPILER_FLAGS_EXCLUDE[i]))
        {
          flag = true;
          break;
        }
      }
    }
    return flag;
  }

  static boolean IsCompilerFlag(String string)
  {
    boolean flag = false;
    {
      for (int i=0; i<COMPILER_FLAGS_INCLUDE.length; i++)
      {
        if (string.startsWith(COMPILER_FLAGS_INCLUDE[i]))
        {
          flag = true;
          break;
        }
      }
      for (int i=0; i<COMPILER_FLAGS_IS.length; i++)
      {
        if (string.equals(COMPILER_FLAGS_IS[i]))
        {
          flag = true;
          break;
        }
      }
      if (IsExcludeCompilerFlag(string))
      {
        flag = false;
      }
    }
    return flag;
  }

  static boolean IsLinkerFlag(String string)
  {
    boolean flag = false;
    {
      for (int i=0; i<LINKER_FLAGS_PREFIXES.length; i++)
      {
        if (string.startsWith(LINKER_FLAGS_PREFIXES[i]))
        {
          flag = true;
          break;
        }
      }
    }
    return flag;
  }

  static String Strip(String string)
  {
    return string.substring(2, string.length()-1);
  }

  static String Strip(String string, String token)
  {
    int start = string.indexOf(token);
    int end = start+token.length();
    return Strip(string.substring(end, string.length()));
  }

  static boolean Contains(Path path, String token)
  {
    return Contains(path.toString(), token);
  }

  static boolean Contains(String string, String token)
  {
    return ((string.length() >= token.length()) && (string.indexOf(token) >= 0));
  }

  static String GetFileName(String path)
  {
    return Paths.get(path).getFileName().toString();
  }

  static String GetFileRoot(String path)
  {
    return Paths.get(path).getRoot().toString();
  }

  static String GetFileParent(String path)
  {
    return Paths.get(path).getParent().toString();
  }

  static String ExtractPath(String string, String from, String to)
  {
    String result = null;
    String[] tokens  = string.split("/");
    int i = 0;
    for (; i<tokens.length; i++)
    {
      if (tokens[i].equals(from))
      {
        result = "";
        break;
      }
    }
    for (; i<tokens.length; i++)
    {
      result += "/"+tokens[i];
      if (tokens[i].equals(to))
      {
        break;
      }
    }
    return result;
  }

  XcodeProjectMaker(boolean open)
  {
    OpenJDK = open;
  }

  void extract_common_compiler_flags()
  {
    // heuristic, find average count of number of flags used by each compiled file
    int count_files = 0;
    int count_flags = 0;
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      count_files++;
      ArrayList<String> flags = entry.getValue();
      count_flags += flags.size();
    }

    // when finding common flags, only consider files with this many flags
    int flag_cutoff = (count_flags/count_files)/2;

    // collect all flags
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      ArrayList<String> flags = entry.getValue();
      if (flags.size() > flag_cutoff)
      {
        for (String flag : flags)
        {
          this.compiler_flags.add(flag);
        }
      }
    }

    // find flags to remove
    TreeSet<String> remove_flags = new TreeSet();
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      ArrayList<String> flags = entry.getValue();
      if (flags.size() > flag_cutoff)
      {
        for (String common : this.compiler_flags)
        {
          if (!flags.contains(common))
          {
            remove_flags.add(common);
          }
        }
      }
    }

    // leave only common flags
    for (String flag : remove_flags)
    {
      this.compiler_flags.remove(flag);
    }

    // remove common flags from each compiler file, leaving only the unique ones
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      ArrayList<String> flags = entry.getValue();
      if (flags.size() > flag_cutoff)
      {
        for (String common : this.compiler_flags)
        {
          if (flags.contains(common))
          {
            flags.remove(common);
          }
        }
      }
    }
  }

  boolean verbose_compiler_tokens = false;
  boolean verbose_linker_tokens = false;
  void extract_compiler_flags(String line)
  {
    String file = null;
    ArrayList<String> flags = null;

    String[] commands  = line.split(",");
    for (int c=0; c<commands.length; c++)
    {
      String command = commands[c];
      
      final String FILE_TOKEN    = "\"file\": ";
      final String COMMAND_TOKEN = "\"command\": ";

      if (Contains(command, FILE_TOKEN))
      {
        file = Strip(command, FILE_TOKEN);
        //verbose_compiler_tokens = Contains(file, "vm_version.cpp");
      }
      else if (Contains(command, COMMAND_TOKEN))
      {
        String tokens = Strip(command, COMMAND_TOKEN);
        String[] arguments  = tokens.split(" ");
        if (arguments.length >= 3)
        {
          flags = new ArrayList();
          for (int a=2; a<arguments.length; a++)
          {
            final String COMPILER_LINE_HEADER = "-I";
            final String COMPILER_IFRAMEWORK  = "-iframework";
            final String COMPILER_FFRAMEWORK  = "-F";

            String argument = arguments[a];
            if (IsCompilerFlag(argument))
            {
              // catch argument like -DVMTYPE=\"Minimal\"
              if (Contains(argument, "\\\\\\\"") && argument.endsWith("\\\\\\\""))
              {
                // TODO: more robust fix needed here
                argument = argument.replace("\\", "");
                argument = argument.replaceFirst("\"", "~.~"); // temp token ~.~
                argument = argument.replace("\"", "\\\"'");
                argument = argument.replace("~.~", "'\\\"");
              }
              
              final String QUOTE_START_TOKEN = "'\\\"";
              final String QUOTE_END_TOKEN   = "\\\"'";

              // argument like -DHOTSPOT_VM_DISTRO='\"Java HotSpot(TM)\"'
              // gets split up, so reconstruct as single string
              if (Contains(argument, QUOTE_START_TOKEN) && !argument.endsWith(QUOTE_END_TOKEN))
              {
                String full_argument = argument;
                do
                {
                  argument = arguments[++a];
                  full_argument = full_argument + " " + argument;
                }
                while (!argument.endsWith(QUOTE_END_TOKEN));
                argument = full_argument;
              }
              flags.add(argument);
              if (verbose_compiler_tokens)
              {
                System.out.println("    FOUND COMPILER FLAG: "+argument);
              }
            }
            else if (argument.startsWith(COMPILER_LINE_HEADER))
            {
              this.header_paths.add(argument.substring(2));
            }
            else if (argument.equals(COMPILER_IFRAMEWORK))
            {
              if (iframework == null)
              {
                this.iframework = arguments[++a]; // gets the value, so skip it for the next loop
              }
            }
            else if (argument.equals(COMPILER_FFRAMEWORK))
            {
              if (fframework == null)
              {
                this.fframework = arguments[++a]; // gets the value, so skip it for the next loop
              }
            }
          }
        }
      }
    }
    //System.exit(0);
    if ((file != null) && (flags != null))
    {
      this.compiled_files.put(file, flags);
    }
    else
    {
      System.err.println(" WARNING: extract_compiler_flags returns file:"+file+", flags:"+flags);
    }

    if (verbose_compiler_tokens)
    {
      System.exit(0);
    }
  }

  void parse_hotspot_build_log(String path)
  {
    String content = ReadFile(path);
    String[] parts = content.split("\n");
    for (String line : parts)
    {
      if (Contains(line, LINKING_PARSE_TOKEN_1) && Contains(line, LINKING_PARSE_TOKEN_2) && !Contains(line, EXCLUDE_PARSE_TOKEN_1))
      {
        extract_linker_flags(line);
      }
    }

    // FALLBACK in case we didn't/couldn't process build.log file
    // hardcode the linker flags to a reasonable defaults
    if (linker_flags.size() == 0)
    {      
      final String MAPFILE_NAME = "mapfile";

      String build_path = GetFileParent(path); // ex: "/Volumes/Work/ide/jdk12/build/macosx-x86_64-server-fastdebug"
      String path_to_mapfile = VerifyFile(FindFile(build_path, "", MAPFILE_NAME, true, true), false);

      // ex: "-Wl,-exported_symbols_list,/Volumes/Work/ide/jdk12/build/macosx-x86_64-server-fastdebug/hotspot/variant-server/libjvm/mapfile
      String mapfile_flag = FALLBACK_MAPFILE_LINKER_FLAG; // "-Wl,-exported_symbols_list,"
      mapfile_flag += path_to_mapfile; // += "/Volumes/Work/ide/jdk12/build/macosx-x86_64-server-fastdebug"
      this.linker_flags.add(mapfile_flag);

      for (String flag : FALLBACK_LINKER_FLAGS)
      {
        this.linker_flags.add(flag);
      }

      System.err.println("Warning: Could not determine linker flags using the build log, using default flags, which may or may not work:");
      for (String flag : this.linker_flags)
      {
        System.err.println(" "+flag);
      }
    }
  }

  void extract_linker_flags(String line)
  {
    if (verbose_compiler_tokens)
    {
      System.err.println("LINKER LINE: "+line);
    }
    boolean found_clang = false;
    String[] tokens  = line.split(" ");
    for (int t=0; t<tokens.length; t++)
    {
      String token = tokens[t];
      if (verbose_linker_tokens)
      {
        System.err.println("LINKER TOKEN: "+token);
      }

      if (!found_clang)
      {
         found_clang = Contains(token, LINKING_PARSE_TOKEN_1);
      }

      // don't bother until we find clang command
      if (found_clang)
      {
        // don't bother after we find -o flag				
        if (token.equals(LINKING_PARSE_TOKEN_3))
        {
          break;
        }

        if (verbose_linker_tokens)
        {
          System.err.println("  PROCESSING LINKER TOKEN: "+token);
        }

        if (IsLinkerFlag(token))
        {
          if (verbose_linker_tokens)
          {
            System.err.println("    FOUND LINKER FLAG: "+token);
          }
          this.linker_flags.add(token);
        }
      }
    }
  }

  void parse_hotspot_compile_commands(String path)
  {
    String content = ReadFile(path);
    String[] parts = content.split("\\{"); // }
    int total_lines = parts.length;

    int found = 0;
    for (int l=0; l<total_lines; l++)
    {
      String line = parts[l];
      if (!Contains(line, EXCLUDE_PARSE_TOKEN_1) && !line.startsWith("["))
      {
        extract_compiler_flags(line);
        found++;
      }
    }
    System.out.println("Found total of "+found+" files that make up the libjvm.dylib");

    extract_common_compiler_flags();

    // figure out "gensrc" folder
    // from: "/Users/gerard/Desktop/jdk_test/jdk10/build/macosx-x86_64-normal-server-fastdebug/hotspot/variant-server/gensrc/adfiles/ad_x86_clone.cpp"
    // to:   "/build/macosx-x86_64-normal-server-fastdebug/hotspot/variant-server/gensrc"
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      String file = entry.getKey();
      if (file.contains("gensrc"))
      {
        this.generated_hotspot_path = ExtractPath(file, "build", "gensrc");
        //generated_hotspot_path = "/build/macosx-x64/hotspot/variant-server/gensrc";
        //generated_hotspot_path = "/build/macosx-x86_64-normal-server-fastdebug/hotspot/variant-server/gensrc";
      }
    }
  }

  // https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/essential/io/examples/Copy.java
  DiskFile get_hotspot_files(DiskFile root, String path_to_jdk, String hotspot_path)
  {
    File file = new File(path_to_jdk+"/"+hotspot_path);
    if (!file.exists())
    {
      return null;
    }

    try
    {
      final Path rootDir = Paths.get(path_to_jdk+hotspot_path);
      Files.walkFileTree(rootDir, new FileVisitor<Path>()
      {
        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes atts) throws IOException
        {
          if (ExcludeFile(path))
          {
            return FileVisitResult.SKIP_SUBTREE;
          }
          else
          {
            // consider folders based on their names
            Path file = path.getFileName();
            if (!ExcludeFile(file))
            {
              root.add_directory(path, hotspot_path);
              return FileVisitResult.CONTINUE;
            }
            else
            {
              // skip folders with names beginning with ".", etc
              return FileVisitResult.SKIP_SUBTREE;
            }
          }
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes mainAtts) throws IOException
        {
          Path file = path.getFileName();
          if (!ExcludeFile(file))
          {
            //System.err.println(path.toString());
            root.add_file(path, hotspot_path);
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException
        {
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException
        {
          if (exc instanceof FileSystemLoopException)
          {
            System.err.println("cycle detected: " + path);
          }
          else
          {
            System.err.format("Unable to process: %s: %s\n", path, exc);
          }
          return FileVisitResult.CONTINUE;
        }
      }
      );
    }
    catch (IOException ex)
    {
      System.err.println("ex: "+ex);
    }

    return root;
  }

  void prepare_files(String path_to_jdk)
  {
    final String OPEN_SRC_HOTSPOT_PATH  = "/src/hotspot";
    final String OPEN_TEST_HOTSPOT_PATH = "/test/hotspot/gtest";

    String path_to_open_src = OPEN_SRC_HOTSPOT_PATH;
    String path_to_open_test_src = OPEN_TEST_HOTSPOT_PATH;
    if (!OpenJDK)
    {
      path_to_open_src = "/open"+path_to_open_src;
      path_to_open_test_src = "/open"+path_to_open_test_src;
    }

    final String CLOSED_SRC_HOTSPOT_PATH = "/closed/src/hotspot";
    
    this.root_gensrc = get_hotspot_files(this.root_gensrc, path_to_jdk, this.generated_hotspot_path);
    this.root_closed_src = get_hotspot_files(this.root_closed_src, path_to_jdk, CLOSED_SRC_HOTSPOT_PATH);
    this.root_open_src = get_hotspot_files(this.root_open_src, path_to_jdk, path_to_open_src);
    this.root_open_test = get_hotspot_files(this.root_open_test, path_to_jdk, path_to_open_test_src);

    TreeSet<String>log_files = new TreeSet();
    log_files.addAll(this.compiled_files.keySet()); // make a copy of files from the log

    int total_marked_files = 0;
    DiskFile roots[] = { this.root_gensrc, this.root_closed_src, this.root_open_src }; // "closed" before "open"
    for (DiskFile root : roots)
    {
      if (root != null)
      {
        ArrayList<DiskFile> disk_files = root.get_files();
        for (DiskFile disk_file : disk_files)
        {
          if (!disk_file.is_directory())
          {
            String log_file_processed = null;
            String disk_file_path = disk_file.get_file_path();
            for (String log_file_path : log_files)
            {
              if (Contains(log_file_path, disk_file_path))
              {
                total_marked_files++;
                
                log_file_processed = log_file_path;
                
                // mark the file as needing compilation
                disk_file.mark_as_compiled(this.compiled_files.get(log_file_path));
                
                // break early if found
                break;
              }
            }
            if (log_file_processed != null)
            {
              // remove the file, so we don't have to search through it again
              log_files.remove(log_file_processed);
            }
          }
        }
      }
    }

    if (this.compiled_files.size() != total_marked_files)
    {
      System.err.println("\nError: was expecting to compile "+this.compiled_files.size()+" files, but marked "+total_marked_files);
      for (String file : log_files)
      {
        System.err.println("file: "+file);
      }
      System.exit(EXIT5);
    }

    if (log_files.size() > 0)
    {
      System.err.println("\nError: unprocessed files left over:");
      for (String log_file : log_files)
      {
        System.err.println("  "+log_file);
      }
      System.exit(EXIT6);
    }

    log_files = null;
    System.gc();
  }

  void print_log_details()
  {
    System.out.println("\nFound "+this.compiler_flags.size()+" common compiler flags:");
    for (String flag : this.compiler_flags)
    {
      System.out.println(" "+flag);
    }

    System.out.println("\nList of compiled files (each one uses common compiler flags plus extra ones as specified):");
    int count=1;
    for (Map.Entry<String,ArrayList<String>> entry : this.compiled_files.entrySet())
    {
      String file = entry.getKey();
      System.out.format("%4d: %s\n", (count++), file);
      ArrayList<String> flags = entry.getValue();
      for (String flag : flags)
      {
        System.out.println("        "+flag);
      }
    }

    System.out.println("\nFound "+this.linker_flags.size()+" linker flags:");
    for (String flag : this.linker_flags)
    {
      System.out.println(" "+flag);
    }

    System.out.println("\nFound "+this.header_paths.size()+" header paths:");
    for (String header : this.header_paths)
    {
      System.out.println(" "+header);
    }

    System.out.println("\nFrameworks:");
    System.out.println(" -iframework "+iframework);
    System.out.println(" -f "+fframework);
  }

  String make_project_pbxproj(String path_to_jdk, String string)
  {
    String c_flags = "";
    for (String flag : this.compiler_flags)
    {
      c_flags += "          \""+flag.replace("\"", "\\\\\"")+"\",\n";
    }
    c_flags = c_flags.substring(0, c_flags.length()-2);
    string = string.replaceFirst(TEMPLATE_OTHER_CFLAGS, c_flags);

    String ld_flags = "";
    for (String flag : this.linker_flags)
    {
      ld_flags += "          \""+flag+"\",\n";
    }
    ld_flags = ld_flags.substring(0, ld_flags.length()-2);
    string = string.replaceFirst(TEMPLATE_OTHER_LDFLAGS, ld_flags);

    String header_paths = "";
    for (String header : this.header_paths)
    {
      header_paths += "          \""+header+"\",\n";
    }
    header_paths = header_paths.substring(0, header_paths.length()-2);
    string = string.replaceFirst(TEMPLATE_USER_HEADER_SEARCH_PATHS, header_paths);

    String framework_paths = "";
    if (fframework != null)
    {
      framework_paths += "          \""+fframework+"\"\n";
    }
    string = string.replaceFirst(TEMPLATE_FRAMEWORK_SEARCH_PATHS, framework_paths);

    DiskFile gensrc_file = this.root_gensrc.get_child("gensrc");
    string = string.replaceFirst(TEMPLATE_GROUP_GENSRC, "        "+gensrc_file.get_xcode_id());

    DiskFile closed_src_file = null;
    if (this.root_closed_src != null)
    {
      closed_src_file = this.root_closed_src.get_child("src");
      string = string.replaceFirst(TEMPLATE_GROUP_CLOSED_SRC, "        "+closed_src_file.get_xcode_id());
    }
    else
    {
      // TODO
      string = string.replaceFirst(TEMPLATE_GROUP_CLOSED_SRC, "        11111111");
    }

    DiskFile open_src_file = this.root_open_src.get_child("src");
    string = string.replaceFirst(TEMPLATE_GROUP_OPEN_SRC, "        "+open_src_file.get_xcode_id());

    DiskFile open_test_file = this.root_open_test.get_child("test");
    string = string.replaceFirst(TEMPLATE_GROUP_OPEN_TEST, "        "+open_test_file.get_xcode_id());

    String gensrc_groups = gensrc_file.generate_PBXGroup();
    String closed_src_groups = "";
    if (closed_src_file != null)
    {
      closed_src_groups = closed_src_file.generate_PBXGroup();
    }
    String open_src_groups = open_src_file.generate_PBXGroup();
    String open_test_groups = open_test_file.generate_PBXGroup();
    string = string.replaceFirst(TEMPLATE_GROUPS, gensrc_groups+closed_src_groups+open_src_groups+open_test_groups);

    String gensrc_files = gensrc_file.generate_PBXFileReference(path_to_jdk);
    String closed_src_files = "";
    if (closed_src_file != null)
    {
      closed_src_files = closed_src_file.generate_PBXFileReference(path_to_jdk);
    }
    String open_src_files = open_src_file.generate_PBXFileReference(path_to_jdk);
    String open_test_files = open_test_file.generate_PBXFileReference(path_to_jdk);
    string = string.replaceFirst(TEMPLATE_PBXFILEREFERENCE, gensrc_files+closed_src_files+open_src_files+open_test_files);

    String gensrc_compiled = gensrc_file.generate_PBXBuildFile();
    String closed_compiled = "";
    if (closed_src_file != null)
    {
      closed_compiled = closed_src_file.generate_PBXBuildFile();
    }
    String open_compiled = open_src_file.generate_PBXBuildFile();
    string = string.replaceFirst(TEMPLATE_PBXBUILDFILE, gensrc_compiled+closed_compiled+open_compiled);

    String gensrc_built = gensrc_file.generate_PBXSourcesBuildPhase();
    String closed_built = "";
    if (closed_src_file != null)
    {
      closed_built = closed_src_file.generate_PBXSourcesBuildPhase();
    }
    String open_built = open_src_file.generate_PBXSourcesBuildPhase();
    string = string.replaceFirst(TEMPLATE_PBXSOURCESSBUILDPHASE, gensrc_built+closed_built+open_built);

    return string;
  }

  String make_template_xcscheme(String path_to_jdk, String string)
  {
    string = string.replaceAll(TEMPLATE_JDK_PATH, path_to_jdk);

    return string;
  }

  void make_xcode_proj(String path_to_jdk, String path_to_xcode, String path_from_xcode_to_jdk)
  {
    /*
     jvm.xcodeproj	                   <-- folder
       project.pbxproj                 <-- file
       xcshareddata                    <-- folder
         xcschemes                     <-- folder
           jvm.xcscheme                <-- file
         xcdebugger                    <-- folder
           Breakpoints_v2.xcbkptlist   <-- file
     */
    File xcode_dir                        = new File(path_to_xcode);
    File jvm_xcodeproj_dir                = new File(xcode_dir, HOTSPOT_PBXPROJ);
    File project_pbxproj_file             = new File(jvm_xcodeproj_dir, PBXPROJ);
    File xcshareddata_dir                 = new File(jvm_xcodeproj_dir, XCSAHAREDDATA);
    File xcschemes_dir                    = new File(xcshareddata_dir, XCSCHEMES);
    File jvm_xcscheme_file                = new File(xcschemes_dir, JVM_XCSCHEME);
    File J2Demo_xcscheme_file             = new File(xcschemes_dir, J2D_XCSCHEME);
    File xcdebugger_dir                   = new File(xcshareddata_dir, XCDEBUGGER);
    File jBreakpoints_v2_xcbkptlist_file  = new File(xcdebugger_dir, XCBKPTLIST);

    if (xcode_dir.exists())
    {
      xcode_dir.delete();
    }

    jvm_xcodeproj_dir.mkdirs();
    xcshareddata_dir.mkdirs();
    xcschemes_dir.mkdirs();
    xcdebugger_dir.mkdirs();

    File data_dir                                  = new File(DATA_DST_PATH);
    File template_project_pbxproj_file             = new File(data_dir, TEMPLATE_PBXPROJ);
    File template_jvm_xcscheme_file                = new File(data_dir, TEMPLATE_JVM_XCSCHEME);
    File template_J2Demo_xcscheme_file             = new File(data_dir, TEMPLATE_J2D_XCSCHEME);
    File template_jBreakpoints_v2_xcbkptlist_file  = new File(data_dir, TEMPLATE_XCBKPTLIST);

    String project_pbxproj_string = ReadFile(template_project_pbxproj_file);
    String jvm_xcscheme_string = ReadFile(template_jvm_xcscheme_file);
    String J2Demo_xcscheme_string = ReadFile(template_J2Demo_xcscheme_file);
    String jBreakpoints_v2_xcbkptlist_string = ReadFile(template_jBreakpoints_v2_xcbkptlist_file);

    WriteFile(project_pbxproj_file, make_project_pbxproj(path_from_xcode_to_jdk, project_pbxproj_string));
    WriteFile(jvm_xcscheme_file, make_template_xcscheme(path_to_jdk, jvm_xcscheme_string));
    WriteFile(J2Demo_xcscheme_file, make_template_xcscheme(path_to_jdk, J2Demo_xcscheme_string));
    WriteFile(jBreakpoints_v2_xcbkptlist_file, jBreakpoints_v2_xcbkptlist_string);
  }

  void copy_files(String path_to_xcode, String path_to_build)
  {
    final String SCRIPT_BEFORE          = "script_before.sh";
    final String SCRIPT_AFTER           = "script_after.sh";
    final String J2DEMO_JAR_SRC_SUBPATH = "/images/jdk/demo/jfc/J2Ddemo/";
    final String J2DEMO_JAR             = "J2Ddemo.jar";

    File script_before_file = new File(path_to_xcode+"/"+SCRIPT_BEFORE);
    File script_after_file = new File(path_to_xcode+"/"+SCRIPT_AFTER);
    try
    {
      if (!script_before_file.exists())
      {
        Files.copy(Paths.get(DATA_DST_PATH+"/"+SCRIPT_BEFORE), script_before_file.toPath());
      }
      if (!script_after_file.exists())
      {
        Files.copy(Paths.get(DATA_DST_PATH+"/"+SCRIPT_AFTER), script_after_file.toPath());
      }
    }
    catch (IOException ex)
    {
      System.err.println("Error: copying script files");
      System.err.println(ex);
      System.exit(EXIT7);
    }

    File J2Demo_file_src = new File(path_to_build+J2DEMO_JAR_SRC_SUBPATH+J2DEMO_JAR);
    File J2Demo_file_dst = new File(path_to_xcode+"/"+J2DEMO_JAR);
    try
    {
      if (!J2Demo_file_dst.exists() && J2Demo_file_src.exists())
      {
        Files.copy(J2Demo_file_src.toPath(), J2Demo_file_dst.toPath());
      }
    }
    catch (IOException ex)
    {
      System.err.println("Warning: copying \""+J2Demo_file_dst.toPath()+"\" file failed");
      System.err.println(ex);
    }
  }

  void make_aliases(String path_to_xcode, String path_to_build)
  {
    final String ALIAS_JAVA_OLD = "java_old.sh";
    final String ALIAS_JAVA_NEW = "java_new.sh";
    final String JDK_BIN_JAVA   = "/jdk/bin/java";

    File xcode_dir    = new File(path_to_xcode);
    File jdk_old_sh   = new File(xcode_dir, ALIAS_JAVA_OLD);
    File jdk_new_sh   = new File(xcode_dir, ALIAS_JAVA_NEW);

    WriteFile(jdk_old_sh, "#!/bin/bash\n"+path_to_build+JDK_BIN_JAVA+" $@");
    WriteFile(jdk_new_sh, "#!/bin/bash\n"+path_to_xcode+"/build"+JDK_BIN_JAVA+" $@");

    try
    {
      Set<PosixFilePermission> permissions = new HashSet<>();
      permissions.add(PosixFilePermission.OWNER_READ);
      permissions.add(PosixFilePermission.OWNER_WRITE);
      permissions.add(PosixFilePermission.OWNER_EXECUTE);
      permissions.add(PosixFilePermission.GROUP_READ);
      permissions.add(PosixFilePermission.OTHERS_READ);
      Files.setPosixFilePermissions(jdk_old_sh.toPath(), permissions);
      Files.setPosixFilePermissions(jdk_new_sh.toPath(), permissions);
    }
    catch (IOException ex)
    {
      System.err.println("Warning: unable to change file permissions");
      System.err.println(ex);
    }
  }

  static class DiskFile extends LinkedHashMap<Path,DiskFile> implements Comparable<DiskFile>
  {
    Path _path;
    boolean _directory;
    ArrayList<String> _compiler_flags;
    String _xcode_id;
    String _xcode_id_2;

    public DiskFile(String path, boolean directory)
    {
      this(StringToPath(path), directory);
    }

    public DiskFile(Path path, boolean directory)
    {
      this._path = path;
      this._directory = directory;
      this._compiler_flags = null;
      this._xcode_id = get_next_xcode_id();
      this._xcode_id_2 = get_next_xcode_id();
    }

    // xcode id ex: D50000000000000000000000
    static long xcode_id_count = 0xF0000001;
    public String get_next_xcode_id()
    {
      return "D5FFFFFF"+Long.toHexString(xcode_id_count++).toUpperCase();
    }

    public String get_path()
    {
      return this._path.toString();
    }

    public boolean is_directory()
    {
      return this._directory;
    }

    public void mark_as_compiled(ArrayList<String> compiler_flags)
    {
      this._compiler_flags = compiler_flags;
    }

    public boolean is_compiled()
    {
      return (this._compiler_flags != null);
    }

    public String get_xcode_id()
    {
      return this._xcode_id;
    }

    String generate_PBXSourcesBuildPhase()
    {
      String string = "";
      if (is_compiled())
      {
        String file_name = get_file_name();
        string += String.format("        %s /* %s in Sources */,\n", this._xcode_id_2, file_name);
      }
      else if (is_directory())
      {
        for (Map.Entry<Path,DiskFile> entry : entrySet())
        {
          DiskFile file = entry.getValue();
          string += file.generate_PBXSourcesBuildPhase();
        }
      }
      return string;
    }

    // D5FFFFFFFFFFFFFFF0006506 /* vm_version.cpp in Sources */ = {isa = PBXBuildFile; fileRef = D5FFFFFFFFFFFFFFF0006505 /* vm_version.cpp */; settings = {COMPILER_FLAGS = HEREHERE; }; };
    String generate_PBXBuildFile()
    {
      String string = "";
      if (is_compiled())
      {
        String flags_string = "";
        for (String flag : this._compiler_flags)
        {
          flags_string += flag.replace("\"", "\\\\\"")+" ";
        }
        String file_name = get_file_name();
        string += String.format("    %s /* %s in Sources */ = {isa = PBXBuildFile; fileRef = %s /* %s */; settings = {COMPILER_FLAGS = \"%s\"; }; };\n",
                                this._xcode_id_2, file_name, this._xcode_id, file_name, flags_string);
      }
      else if (is_directory())
      {
        for (Map.Entry<Path,DiskFile> entry : entrySet())
        {
          DiskFile file = entry.getValue();
          string += file.generate_PBXBuildFile();
        }
      }
      return string;
    }

    String generate_PBXFileReference(String root_path)
    {
      String string = "";
      if (!is_directory())
      {
        String file_name = get_file_name();
        String suffix = get_file_name_suffix();
        string += String.format("    %s /* %s */ = {isa = PBXFileReference; fileEncoding = 4; lastKnownFileType = %s%s; name = %s; path = \"%s%s\"; sourceTree = \"<group>\"; };\n",
                                this._xcode_id, file_name, file_name, suffix, file_name, root_path, get_path());
      }
      else if (is_directory())
      {
        for (Map.Entry<Path,DiskFile> entry : entrySet())
        {
          DiskFile file = entry.getValue();
          string += file.generate_PBXFileReference(root_path);
        }
      }
      return string;
    }

    String generate_PBXGroup()
    {
      String string = String.format("    %s /* %s */ = {\n      isa = PBXGroup;\n      children = (\n", this._xcode_id, get_file_name());
      
      TreeSet<DiskFile> sortedSet = new TreeSet();
      sortedSet.addAll(values());

      for (DiskFile file : sortedSet)
      {
        string += String.format("        %s /* %s */,\n", file._xcode_id, file.get_file_name());
      }
      string += String.format("      );\n      name = %s;\n      sourceTree = \"<group>\";\n    };\n", get_file_name());

      for (DiskFile file : sortedSet)
      {
        if (file.is_directory())
        {
          string += file.generate_PBXGroup();
        }
      }

      return string;
    }

    ArrayList<DiskFile> get_files(ArrayList<DiskFile> array)
    {
      for (Map.Entry<Path,DiskFile> entry : entrySet())
      {
        DiskFile file = entry.getValue();
        if (file._directory)
        {
          array.add(file);
          array = file.get_files(array);
        }
        else
        {
          array.add(file);
        }
      }
      return array;
    }

    public ArrayList<DiskFile> get_files()
    {
      return get_files(new ArrayList<DiskFile>());
    }

    String get_file_path()
    {
      return this._path.toString();
    }

    String get_file_name()
    {
      Path file_name = this._path.getFileName();
      if (file_name != null)
      {
        return file_name.toString();
      }
      else
      {
        return this._path.toString();
      }
    }

    String get_file_name_no_suffix()
    {
      String string = null;
      Path file_name = this._path.getFileName();
      if (file_name != null)
      {
        string = file_name.toString();
        int index = string.indexOf(".");
        if (index >= 0)
        {
          string = string.substring(0, index);
        }
      }
      else
      {
        string = this._path.toString();
      }
      return string;
    }

    String get_file_name_suffix()
    {
      String file_name = get_file_name();
      int index = file_name.indexOf(".");
      if (index >= 0)
      {
        return file_name.substring(index, file_name.length());
      }
      else
      {
        return "";
      }
    }

    private DiskFile get_child(String file_name)
    {
      DiskFile child = null;
      for (Map.Entry<Path,DiskFile> entry : entrySet())
      {
        DiskFile file = entry.getValue();
        if (file.get_file_name().equals(file_name))
        {
          child = entry.getValue();
          break;
        }
        else if (file._directory)
        {
          child = file.get_child(file_name);
          if (child != null)
          {
            break;
          }
        }
      }
      return child;
    }

    private DiskFile get_parent(Path path)
    {
      Path parent_path = path.getParent();
      Path component = path.getFileName();
      DiskFile parent = get(parent_path);
      if (parent == null)
      {
        if (this._path.equals(parent_path))
        {
          parent = this;
        }
        else
        {
          parent = get_parent(parent_path).get(parent_path);
        }
        parent.putIfAbsent(path, new DiskFile(path, true));
      }
      return parent;
    }

    public void add_file(String path, String clip)
    {
      add_file(StringToPath(path), clip);
    }

    public void add_file(Path path, String clip)
    {
      path = ClipPath(path, clip);
      DiskFile parent = get_parent(path);
      parent.put(path, new DiskFile(path, false));
    }

    public void add_directory(String path, String clip)
    {
      add_directory(StringToPath(path), clip);
    }

    public void add_directory(Path path, String clip)
    {
      path = ClipPath(path, clip);
      DiskFile parent = get_parent(path);
      parent.putIfAbsent(path, new DiskFile(path, true));
    }

    static Path StringToPath(String string)
    {
      if (string != null)
      {
        return new File(string).toPath();
      }
      else
      {
        return null;
      }
    }

    static Path ClipPath(Path path, String clip)
    {
      return ClipPath(path.toString(), clip);
    }

    static Path ClipPath(String path, String clip)
    {
      String subpath = path;
      if (path.contains(clip))
      {
        subpath = clip;
      }
      int index = path.indexOf(subpath);
      return StringToPath(path.substring(index, path.length()));
    }
    
    @Override
    public int compareTo(DiskFile file)
    {
      // ".hpp", then ".inline.hpp", then ".cpp"
      int equal = get_file_name_no_suffix().compareTo(file.get_file_name_no_suffix());
      if (equal == 0)
      {
        String suffix1 = get_file_name_suffix();
        String suffix2 = file.get_file_name_suffix();
        if (!suffix1.equals(".inline.hpp") && !suffix2.equals(".inline.hpp"))
        {
          // .hpp before .cpp
          equal = -(get_file_name_suffix().compareTo(file.get_file_name_suffix()));
        }
        else if (suffix1.equals(".inline.hpp") && suffix2.equals(".hpp"))
        {
          return 1;
        }
        else if (suffix1.equals(".inline.hpp") && suffix2.equals(".cpp"))
        {
          return -1;
        }
        else if (suffix1.equals(".hpp") && suffix2.equals(".inline.hpp"))
        {
          return -1;
        }
        else if (suffix1.equals(".cpp") && suffix2.equals(".inline.hpp"))
        {
          return 1;
        }
      }
      return equal;
    }
  }
}
