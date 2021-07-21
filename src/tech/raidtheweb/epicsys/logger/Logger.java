package tech.raidtheweb.epicsys.logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class Logger {
	
	FileOutputStream fout = null; 
	
	public Logger() {
		try {
			this.fout = new FileOutputStream("latest.log");
		} catch (FileNotFoundException e) {
			System.out.println("[" + new Date() + "] Cannot open log file");
			System.exit(0);
		}
	}
	
	public void log(String text) {
		System.out.println("[" + new Date() + "] " + text);
		String logtext = new String("[" + new Date() + "] " + text);
		for(char c : logtext.toCharArray()) {
			try {
				this.fout.write((int) c);
			} catch (IOException e) {
				System.out.println("[" + new Date() + "] Failed to write to log file.");
			}
		}
		try {
			this.fout.write('\n');
		} catch (IOException e) {
			System.out.println("[" + new Date() + "] Failed to write to log file.");
		}
	}
	
	public void destroy() {
		try {
			this.fout.close();
		} catch (IOException e) {
			// ignore
		}
	}
}
