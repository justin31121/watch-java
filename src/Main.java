package watch;

import static js.Io.*;

import java.util.Scanner;
import java.util.Date;

import java.text.SimpleDateFormat;

import io.github.alexarchambault.windowsansi.*;

class Main {

	private static Scanner in = new Scanner(System.in);
	private static SimpleDateFormat formatter = new SimpleDateFormat("MMM EE dd HH:mm:ss");

	public static void main(String[] args) {
		//PARSE INPUT
		if(args.length<2) {
			println("ERROR: Please provide enough arguments");
			println("USAGE: <filename> <process>");
			System.exit(1);
		}
		String dir = args.length<3 ? "." : args[2];
		if(!existFile(dir+"\\"+args[0])) {
			println("ERROR: The file: \""+args[0]+"\" does not exist");
			System.exit(1);
		}
		String[] arguments = args[1].split(" ");

		//INIT COLOR IN CONSOLE
		try{
			boolean init = WindowsAnsi.setup();
			if(!init) throw new Exception();
		}
		catch(Exception e) {
			println("WARNING: Could not init WindowsAnsi");
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
					print("ERROR: Provided command: \""+cmd+"\"crashed");
					System.exit(1);
				}

				//EXIT CODE
				if(exitCode==0) {
					print("\nCompilation \u001B[32mfinished\u001B[0m");
				}
				else {
					print("\nCompilation \u001B[31mexited abnormally\u001B[0m with code \u001B[31m"+exitCode+"\u001B[0m");
				}
				String date = formatter.format(new Date());
				println(" at "+date);
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
				println("Dir: "+dir+", File: "+args[0]+", Command "+args[1]);
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