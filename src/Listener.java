package watch;

import java.io.File;

import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.Files;

import static js.Io.*;

class Listener implements Runnable {

	private String path;
	private String[] paths;
	private FileTime last;
	private Function f;
	private boolean running = true;

	public Listener(String path, Function f) {
		last = null;
		this.f = f;
		this.path = null;

		File origin = new File(path);

		if(origin.isDirectory()) {			
			this.path = path;
		} 
		else {
			paths = new String[]{path};
		}
	}

	public void reload() {
		f.foo();
	}

	private void loadFiles() {
		File[] files = new File(path).listFiles();
		paths = new String[files.length];
		int i=0;
		for(File file : files) {
			paths[i++]=file.getAbsolutePath();
		}
	}

	private FileTime lastModified(String file_path) throws Exception {
		File file = new File(file_path);

		Path path = file.toPath();

		BasicFileAttributes fatr = Files.readAttributes(path, 
			BasicFileAttributes.class);

		return fatr.lastModifiedTime();
	}

	private boolean hasChanged(FileTime next) {
		return last.compareTo(next) < 0;
	}

	@Override
	public void run() {
		while(running) {
			try{
				Thread.sleep(500);
			}
			catch(Exception e){

			}

			if(path!=null) loadFiles();

			FileTime latest = null;
			String prop = null;

			for(String p : paths) {
				FileTime next;
				try{
					next = lastModified(p);    
				}
				catch(Exception e) {
					continue;
				}

				if(latest==null || latest.compareTo(next)<0) {
					latest = next;
					prop = p;
				}
			}

			if(last==null || hasChanged(latest)) {
				last = latest;
				try{
					reload();						
				}
				catch(Exception e) {

				}
			}
		}
	}
}
