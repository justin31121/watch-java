package watch;

import java.util.Scanner;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.io.File;

class Main {

    private static String script = "$signature = @'\n" +
	"[DllImport(\"kernel32.dll\", SetLastError = true)]\n" +
	"public static extern IntPtr GetStdHandle(int nStdHandle);\n" +
	"[DllImport(\"kernel32.dll\", SetLastError = true)]\n" +
	"public static extern uint GetConsoleMode(\n" +
	"    IntPtr hConsoleHandle,\n" +
	"    out uint lpMode);\n" +
	"[DllImport(\"kernel32.dll\", SetLastError = true)]\n" +
	"public static extern uint SetConsoleMode(\n" +
	"    IntPtr hConsoleHandle,\n" +
	"    uint dwMode);\n" +
	"public const int STD_OUTPUT_HANDLE = -11;\n" +
	"public const int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 0x0004;\n" +
	"'@\n" +
	"\n" +
	"$WinAPI = Add-Type -MemberDefinition $signature `\n" +
	"    -Name WinAPI -Namespace ConinModeScript `\n" +
	"    -PassThru\n" +
	"\n" +
	"$handle = $WinAPI::GetStdHandle($WinAPI::STD_OUTPUT_HANDLE)\n" +
	"$mode = 0\n" +
	"$ret = $WinAPI::GetConsoleMode($handle, [ref]$mode)\n" +
	"if ($ret -eq 0) {\n" +
	"    throw \"GetConsoleMode failed (is stdin a console?)\"\n" +
	"}\n" +
	"$ret = $WinAPI::SetConsoleMode($handle, $mode -bor $WinAPI::ENABLE_VIRTUAL_TERMINAL_PROCESSING)\n" +
	"if ($ret -eq 0) {\n" +
	"    throw \"SetConsoleMode failed (is stdin a console?)\"\n" +
	"}\n";


    static boolean isWindows;

    static {
        isWindows = System.getProperty("os.name")
	    .toLowerCase(java.util.Locale.ROOT)
	    .contains("windows");
    }

    private static Scanner in = new Scanner(System.in);
    private static SimpleDateFormat formatter = new SimpleDateFormat("MMM EE dd HH:mm:ss");

    private static boolean setup() {
	try{
	    if (isWindows && System.console() != null) {
		PowershellRunner.runScript(script);
	    }
	    return true;
	}
	catch(Exception e) {
	    return false;
	}
    }

    public static void main(String[] args) {
	//PARSE INPUT
	if(args.length<2) {
	    System.out.println("ERROR: Please provide enough arguments");
	    System.out.println("USAGE: <filename> <process>");
	    System.exit(1);
	}
	String dir = args.length<3 ? "." : args[2];
	boolean exist = (new File(dir+"\\"+args[0]).exists());
	if(!exist) {
	    System.out.println("ERROR: The file: \""+args[0]+"\" does not exist");
	    System.exit(1);
	}
	String[] arguments = args[1].split(" ");

	//INIT COLOR IN CONSOLE
	try{
	    boolean init = setup();
	    if(!init) throw new Exception();
	}
	catch(Exception e) {
	    System.out.println("WARNING: Could not init WindowsAnsi");
	}

	//SHELL
	Shell shell = new Shell(arguments, dir);
	String cmd = shell.getArgumentsString();

	//WHEN FILE IS CHANGED DO 
	Listener listener = new Listener(dir+"\\"+args[0], 
					 () -> { 
					     //RUN COMMAND
					     shell.clear();
					     int exitCode = -1;
					     try{
						 exitCode = shell.runCommand();
					     }
					     catch(Exception e) {
						 e.printStackTrace();
						 System.out.print("ERROR: Provided command: \""+cmd+"\"crashed");
						 System.exit(1);
					     }

					     //EXIT CODE
					     if(exitCode==0) {
						 System.out.print("\nCompilation \u001B[32mfinished\u001B[0m");
					     }
					     else {
						 System.out.print("\nCompilation \u001B[31mexited abnormally\u001B[0m with code \u001B[31m"+exitCode+"\u001B[0m");
					     }
					     String date = formatter.format(new Date());
					     System.out.println(" at "+date);
	});

	Thread thread = new Thread(listener);
	thread.start();

	//STDIN
	while(true) {
	    String line = in.nextLine();

	    if("q".equals(line)) {
		break;
	    }
	    if("r".equals(line)) {
		listener.reload();
	    }
	    if("log".equals(line)) {
		System.out.println("Dir: "+dir+", File: "+args[0]+", Command "+args[1]);
	    }
	    else if(line.toCharArray()[0]==':') {
		String command = line.substring(1);

		shell.runCommand("cd "+dir+" && "+command);
	    }
	}

	//EXIT
	System.exit(0);
    }
}
