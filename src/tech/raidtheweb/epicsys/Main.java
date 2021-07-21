package tech.raidtheweb.epicsys;

import tech.raidtheweb.epicsys.assembler.Assembler;
import tech.raidtheweb.epicsys.cpu.CPU;
import tech.raidtheweb.epicsys.logger.Logger;

public class Main extends Thread {
	
	public static Logger logger = null;
	public static CPU epic8 = null;
	private static EpicFrame frame = null;
	public static int speed = 1;
	public static boolean paused = false;
	
	public Main(String[] args) {
		logger = new Logger();
		if(args.length > 0) {
			if(args[0].indexOf("-asm") != -1 && args.length == 3) {
				logger.log("Assembling File...");
				Assembler assembler = new Assembler();
				assembler.assemble(args[1], args[2]);
				run2(args[2]);
			} else {
				run2(args[0]);
			}
		} else if(args.length == 1) {
			run2(args[0]);
		} else {
			run2("./rom.bin");
		}
	}
	
	private static void run2(String file) {
		logger.log("Starting Epic8 CPU Emulator");
		
		epic8 = new CPU();
		frame = new EpicFrame(epic8);
		logger.log("Initiated CPU");
	
		logger.log("Loading ROM");
		epic8.loadROM(file, frame);
		logger.log("Loaded ROM Successfully!");
	
		logger.log("Testing memory offset...");
		char programStart = epic8.getMemoryOffset(0x403);
		System.out.println("0x" + Integer.toHexString(programStart).toUpperCase());
		logger.log("Running CPU...");
	}
	
	public void run() {
		while(true) {
			if(paused == false) {
				epic8.run();
				if(epic8.needsRedraw()) {
					epic8.cancelRedraw();
					frame.repaint();
				}
				try {
					Thread.sleep(speed);
				} catch(InterruptedException e) {
					
				}
			} else {
				try {
					Thread.sleep(speed);
				} catch(InterruptedException e) {
					
				}
			}
		}
	}
	
	public static void main(String[] args) {
		Main main = new Main(args);
		main.start();
	}
}
