package watch;

import static js.Io.*;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.io.IOException;
import java.lang.InterruptedException;

class Shell {
	private String[] args;
	private String dir;

	public Shell(String[] args, String dir) {
		this.args = args;
		this.dir = dir;
	}

	public String getArgumentsString() {
		String acc = "";
		for(int i=0;i<args.length;i++) {
			acc += args[i];
			if(i!=args.length-1) {
				acc += " ";
			}
		}
		return acc;
	}

	public void runCommand(String command) {
		try {
			new ProcessBuilder("cmd", "/c", command)
			.inheritIO().start().waitFor();
		}
		catch(Exception e) {

		}
	}

	public void clear(){
		runCommand("cls");
	}

	public int runCommand() throws IOException, InterruptedException{
		ProcessBuilder ps = new ProcessBuilder(args);
		ps.directory(new File(dir));

		ps.redirectErrorStream(true);
		Process pr = ps.start();

		BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		String line;
		while((line = in.readLine()) != null) {
			println(line);
		}
		int n = pr.waitFor();

		in.close();  

		return n;
	}
}