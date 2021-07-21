package tech.raidtheweb.epicsys.cpu;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JOptionPane;

import tech.raidtheweb.epicsys.EpicFrame;
import tech.raidtheweb.epicsys.Main;
import tech.raidtheweb.epicsys.devices.Device;
import tech.raidtheweb.epicsys.devices.TestDevice;
import tech.raidtheweb.epicsys.hexdump.SimpleDataProvider;
import tech.raidtheweb.epicsys.registerdump.Dump;
import tech.raidtheweb.epicsys.registerdump.NullDump;
import tech.raidtheweb.epicsys.serializable.MemoryObject;

public class CPU {
	
	/**
	 * Le BIIIG Memory
	 * 64KB
	 */
	private char[] memory;
	
	/**
	 * Stack pointer, points to next free stack entry in memory
	 */
	private int sp;
	
	/**
	 * Stack, 256 levels deep
	 */
	private short stack[];
	
	/**
	 * Pointer to current operation
	 */
	private short pc;
	
	/**
	 * Display
	 */
	private byte[] display;
	
	/**
	 * Self explanatory
	 */
	private boolean redrawRequired;
	
	/**
	 * Display register
	 */
	private char D;
	
	/**
	 * General index register
	 */
	private short I;
	
	
	private short X;
	
	
	private short Y;
	
	
	private short Z;
	
	
	private short P;
	
	
	private short F;
	
	
	private short E;
	
	
	private short C;
	
	
	private short A;
	
	/**
	 * INB Return register
	 */
	private short K;
	
	/**
	 * Return register
	 */
	private short R;
	
	/**
	 * Stack pointer register
	 */
	private short S;
	
	/**
	 * Text based display Buffer
	 */
	private char[] displayBuffer;
	
	/**
	 * Pointer to current index in display buffer
	 */
	private int displayPointer;
	
	/**
	 * ROM file path
	 */
	private String rom;
	
	/**
	 * Frame Object
	 */
	private EpicFrame frame;
	
	/**
	 * Delay timer (runs at 120hz)
	 */
	private int delay_timer;

	private int audio_latch;

	/**
	 * Register Dumper Class
	 */
	private Dump registerDumper;
	
	/**
	 * Devices query on ports
	 */
	private Device[] devices;
	
	public CPU() {
		this.memory = new char[1024 * 64];
		
		this.stack = new short[256];
		
		this.sp = 0;
		
		this.display = new byte[64 * 32];
		
		this.redrawRequired = false;
		
		this.pc = 0x400;
		
		this.D = 0x00;
		
		this.I = 0x00;
		
		this.X = 0x00;
		
		this.Y = 0x00;
		
		this.Z = 0x00;
		
		this.P = 0x00;
		
		this.F = 0x00;
		
		this.E = 0x00;
		
		this.C = 0x00;
		
		this.A = 0x00;
		
		this.K = 0x00;
		
		this.R = 0x00;
		
		this.S = 0x00;
		
		this.displayBuffer = new char[64 * 32];
		
		this.displayPointer = 0;
		
		this.delay_timer = 0;
		
		this.audio_latch = 0;
		
		this.registerDumper = new NullDump();
		
		this.devices = new Device[0xFF];
		this.devices[0x01] = new TestDevice();
	}
	
	public void run() {
		//char operation = (char)((this.memory[this.pc] << 8) | this.memory[this.pc + 1]);
		
		/*
		char a = this.memory[this.pc];
		char b = this.memory[this.pc + 1];
		char c = this.memory[this.pc + 2];
		char d = this.memory[this.pc + 3];
		*/
		
		char a = this.memory[this.pc];
		char b = this.memory[this.pc + 1];
		char c = this.memory[this.pc + 2];
		char d = this.memory[this.pc + 3];
		
		/* update hex dump data */
		byte[] data = new String(this.getMemory()).getBytes();
		frame.hexView.setData(new SimpleDataProvider(data, data.length - 0x400));
		/* update hex dump data */
		
		
		int operation = (d | (c<<8) | (b<<16) | (a << 24));
		
		System.out.println(String.format("0x%08x", operation));
		
		if(operation == 0x00000000) {
			Main.logger.log("Reached EOF (NOOP), Halting CPU Automatically");
			System.out.println("Current PC: " + this.pc);
			System.exit(0);
		}
		
		switch(operation & 0xFF000000) {
			case 0x00000000: { // 0000NNNN JMP: Jump to an address, or a label (assembler turns said label into an address anyways)
				int nnnn = operation & 0x0000FFFF;
				if(!(nnnn >= 0x00000400)) { Main.logger.log("Illegal address access occurred, Program attempted to access illegal address: 0x" + Integer.toHexString(nnnn).toUpperCase() + ", Halting CPU"); System.exit(1); }
				pc = (short)nnnn;
				break;
			}
			
			case 0x01000000: { // 0x01FFFFFF JMP: Infinite Jump to current address
				// hang CPU ;)
				pc = (short)pc;
				break;
			}
			
			case 0x02000000: { // 0x02XXXXYY MOV: Move XX into register YY
				int x = (operation & 0x00FFFF00) >> 8;
				int y = operation & 0x000000FF;
				
				System.out.println(String.format("0x%04x", x));
				
				switch(y) {
					case 0x0D: {
						this.S = (short) x;
						break;
					}
					
					case 0x0C: {
						this.K = (short) x;
						break;
					}
					
					case 0x0B: {
						this.A = (short) x;
						break;
					}
					
					case 0x0A: {
						this.C = (short) x;
						break;
					}
					
					case 0x09: {
						this.E = (short) x;
						break;
					}
					
					case 0x08: {
						this.F = (short) x;
						break;
					}
					
					case 0x07: {
						this.P = (short) x;
						break;
					}
					
					case 0x06: {
						this.Z = (short) x;
						break;
					}
					
					case 0x05: {
						this.R = (short) x;
						break;
					}
					
					case 0x04: {
						this.Y = (short) x;
						break;
					}
					
					case 0x03: {
						this.X = (short) x;
						break;
					}
				
					case 0x02: {
						this.I = (short) x;
						break;
					}
				
					case 0x01:
					default: {
						this.D = (char) x;
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x03000000: { // 0x0300NN CMD: Call interrupt NN
				int x = (operation & 0x000000FF);
				
				switch((char) x) {
				
					case 0x11: // clear screen and wipe buffer;
						pc += 4;
						this.displayBuffer = new char[64 * 32];
						this.displayPointer = 0;
						this.redrawRequired = true;
						this.D = 0x00;
						break;
				
					case 0x10: // push D to display buffer and write buffer as characters to display
						pc += 4;
						this.redrawRequired = true;
						this.displayBuffer[displayPointer++] = this.D;
						this.D = 0x00;
						break;
						
					case 0x09: // push D to display buffer and write buffer as decimal to display
						pc += 4;
						this.redrawRequired = true;
						if(this.D < 0x10)
							this.displayBuffer[displayPointer++] = (char) ((char)(0x30) + this.D);
						else
							this.displayBuffer[displayPointer++] = 0x30;
						this.D = 0x00;
						break;
						
					default:
						Main.logger.log("Unknown Interrupt: 0x" + Integer.toHexString(x));
						System.exit(1);
						break;
				}
				break;
			}
			
			case 0x04000000: { // 0x0400RR PUSH: Push data onto stack
				int rr = operation & 0x000000FF;
				
				switch(rr) {
					case 0x0D: {
						this.stack[this.sp++] = this.S;
						break;
					}
					
					case 0x0C: {
						this.stack[this.sp++] = this.K;
						break;
					}
					
					case 0x0B: {
						this.stack[this.sp++] = this.A;
						break;
					}
				
					case 0x0A: {
						this.stack[this.sp++] = this.C;
						break;
					}
					
					case 0x09: {
						this.stack[this.sp++] = this.E;
						break;
					}
					
					case 0x08: {
						this.stack[this.sp++] = this.F;
						break;
					}
					
					case 0x07: {
						this.stack[this.sp++] = this.P;
						break;
					}
					
					case 0x06: {
						this.stack[this.sp++] = this.Z;
						break;
					}
					
					case 0x05: {
						this.stack[this.sp++] = this.R;
						break;
					}
					
					case 0x04: {
						this.stack[this.sp++] = this.Y;
						break;
					}
					
					case 0x03: {
						this.stack[this.sp++] = this.X;
						break;
					}
				
					case 0x02: {
						this.stack[this.sp++] = this.I;
						break;
					}
				
					case 0x01:
					default: {
						this.stack[this.sp++] = (short) this.D;
						break;
					}
				}
				System.out.println(this.stack[0]);
				pc += 4;
				break;
			}
			
			case 0x05000000: { // 0x050000RR POP: Pop data from stack into register
				int rr = operation & 0x000000FF;
				
				switch(rr) {
					
					case 0x0D: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.S = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x0C: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.K = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x0B: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.A = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x0A: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.C = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x09: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.E = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x08: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.F = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x07: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.P = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x06: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.Z = this.removeTheElement(this.sp);
						}
						break;
					}
				
					case 0x05: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.R = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x04: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.Y = this.removeTheElement(this.sp);
						}
						break;
					}
					
					case 0x03: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.X = this.removeTheElement(this.sp);
						}
						break;
					}
				
					case 0x02: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.I = this.removeTheElement(this.sp);
						}
						break;
					}
				
					case 0x01:
					default: {
						if(this.sp > 0) {
							this.sp -= 1;
							this.D = (char)this.removeTheElement(this.sp);
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x06000000: { // 0x0600NNNN PUSH: Push hex data to stack
				short nnnn = (short)(operation & 0x0000FFFF);
				
				this.stack[this.sp] = nnnn;
				this.sp++;
				
				pc += 4;
				break;
			}
			
			case 0x07000000: { // 0x070000RR JNULL: Skip next instruction if RR equals 0x00
				int rr = operation & 0x000000FF;
				switch(rr) {
					case 0x0D: {
						if(this.S == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x0C: {
						if(this.K == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x0B: {
						if(this.A == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x0A: {
						if(this.C == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x09: {
						if(this.E == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x08: {
						if(this.F == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x07: {
						if(this.P == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x06: {
						if(this.Z == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
				
					case 0x05: {
						if(this.R == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x04: {
						if(this.Y == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
					
					case 0x03: {
						if(this.X == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
				
					case 0x02: {
						if(this.I == 0x00) {
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
				
					case 0x01: {
						if(this.D == 0x00) {
							System.out.println("D == 0x00");
							pc += 8;
						} else {
							pc += 4;
						}
						break;
					}
				}
				break;
			}
		
			case 0x08000000: { // 0x08000000 PEEK: Get data from stack into register without changing stack pointer
				int rr = operation & 0x000000FF;
				
				switch(rr) {
					case 0x0D: {
						if(this.sp > 0) {
							this.S = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x0C: {
						if(this.sp > 0) {
							this.K = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x0B: {
						if(this.sp > 0) {
							this.A = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x0A: {
						if(this.sp > 0) {
							this.C = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x09: {
						if(this.sp > 0) {
							this.E = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x08: {
						if(this.sp > 0) {
							this.F = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x07: {
						if(this.sp > 0) {
							this.P = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x06: {
						if(this.sp > 0) {
							this.Z = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x05: {
						if(this.sp > 0) {
							this.R = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x04: {
						if(this.sp > 0) {
							this.Y = this.stack[this.sp - 1];
						}
						break;
					}
					
					case 0x03: {
						if(this.sp > 0) {
							this.X = this.stack[this.sp - 1];
						}
						break;
					}
				
					case 0x02: {
						if(this.sp > 0) {
							this.I = this.stack[this.sp - 1];
						}
						break;
					}
				
					case 0x01:
					default: {
						if(this.sp > 0) {
							this.D = (char)this.stack[this.sp - 1];
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x09000000: { // 0x090000RR POPL: FIFO stack pop into register
				int rr = operation & 0x000000FF;
				
				switch(rr) {
					case 0x0D: {
						this.S = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x0C: {
						this.K = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x0B: {
						this.A = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x0A: {
						this.C = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x09: {
						this.E = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x08: {
						this.F = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x07: {
						this.P = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x06: {
						this.Z = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x05: {
						this.R = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x04: {
						this.Y = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
					
					case 0x03: {
						this.X = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
				
					case 0x02: {
						this.I = this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
				
					case 0x01:
					default: {
						this.D = (char) this.removeTheElement(0);
						if(sp > 0) this.sp--;
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x0A000000: { // 0x0A00NNNN WAIT: Wait NNNN clock cycles 
				int x = operation & 0x0000FFFF;
				System.out.println(String.format("0x%04x", x));
				if(this.delay_timer == 0)
					this.delay_timer = x;
				if(delay_timer <= 1)
					pc += 4;
				break;
			}
			
			case 0x0B000000: { // 0x0BRR00SS ADD: Set RR to RR + SS
				int rr = operation & 0x00FF0000;
				int ss = operation & 0x000000FF;
				System.out.println(String.format("0x%04x", rr));
				switch(rr) {
					case 0x0D: {
						switch(ss) {
							case 0x0D: {
								this.S += (short) S;
								break;
							}
							
							case 0x0C: {
								this.S += (short) K;
								break;
							}
							
							case 0x0B: {
								this.S += (short) A;
								break;
							}
							
							case 0x0A: {
								this.S += (short) C;
								break;
							}
							
							case 0x09: {
								this.S += (short) E;
								break;
							}
							
							case 0x08: {
								this.S += (short) F;
								break;
							}
							
							case 0x07: {
								this.S += (short) P;
								break;
							}
							
							case 0x06: {
								this.S += (short) Z;
								break;
							}
							
							case 0x05: {
								this.S += (short) R;
								break;
							}
							
							case 0x04: {
								this.S += (short) Y;
								break;
							}
							
							case 0x03: {
								this.S += (short) X;
								break;
							}
						
							case 0x02: {
								this.S += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.S += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0C: {
						switch(ss) {
							case 0x0D: {
								this.K += (short) S;
								break;
							}
							
							case 0x0C: {
								this.K += (short) K;
								break;
							}
							
							case 0x0B: {
								this.K += (short) A;
								break;
							}
							
							case 0x0A: {
								this.K += (short) C;
								break;
							}
							
							case 0x09: {
								this.K += (short) E;
								break;
							}
							
							case 0x08: {
								this.K += (short) F;
								break;
							}
							
							case 0x07: {
								this.K += (short) P;
								break;
							}
							
							case 0x06: {
								this.K += (short) Z;
								break;
							}
							
							case 0x05: {
								this.K += (short) R;
								break;
							}
							
							case 0x04: {
								this.K += (short) Y;
								break;
							}
							
							case 0x03: {
								this.K += (short) X;
								break;
							}
						
							case 0x02: {
								this.K += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.K += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0B: {
						switch(ss) {
							case 0x0D: {
								this.A += (short) S;
								break;
							}
							
							case 0x0C: {
								this.A += (short) K;
								break;
							}
							
							case 0x0B: {
								this.A += (short) A;
								break;
							}
							
							case 0x0A: {
								this.A += (short) C;
								break;
							}
							
							case 0x09: {
								this.A += (short) E;
								break;
							}
							
							case 0x08: {
								this.A += (short) F;
								break;
							}
							
							case 0x07: {
								this.A += (short) P;
								break;
							}
							
							case 0x06: {
								this.A += (short) Z;
								break;
							}
							
							case 0x05: {
								this.A += (short) R;
								break;
							}
							
							case 0x04: {
								this.A += (short) Y;
								break;
							}
							
							case 0x03: {
								this.A += (short) X;
								break;
							}
						
							case 0x02: {
								this.A += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.A += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0A: {
						switch(ss) {
							case 0x0D: {
								this.C += (short) S;
								break;
							}
							
							case 0x0C: {
								this.C += (short) K;
								break;
							}
							
							case 0x0B: {
								this.C += (short) A;
								break;
							}
							
							case 0x0A: {
								this.C += (short) C;
								break;
							}
							
							case 0x09: {
								this.C += (short) E;
								break;
							}
							
							case 0x08: {
								this.C += (short) F;
								break;
							}
							
							case 0x07: {
								this.C += (short) P;
								break;
							}
							
							case 0x06: {
								this.C += (short) Z;
								break;
							}
							
							case 0x05: {
								this.C += (short) R;
								break;
							}
							
							case 0x04: {
								this.C += (short) Y;
								break;
							}
							
							case 0x03: {
								this.C += (short) X;
								break;
							}
						
							case 0x02: {
								this.C += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.C += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x09: {
						switch(ss) {
							case 0x0D: {
								this.E += (short) S;
								break;
							}
							
							case 0x0C: {
								this.E += (short) K;
								break;
							}
							
							case 0x0B: {
								this.E += (short) A;
								break;
							}
							
							case 0x0A: {
								this.E += (short) C;
								break;
							}
							
							case 0x09: {
								this.E += (short) E;
								break;
							}
							
							case 0x08: {
								this.E += (short) F;
								break;
							}
							
							case 0x07: {
								this.E += (short) P;
								break;
							}
							
							case 0x06: {
								this.E += (short) Z;
								break;
							}
							
							case 0x05: {
								this.E += (short) R;
								break;
							}
							
							case 0x04: {
								this.E += (short) Y;
								break;
							}
							
							case 0x03: {
								this.E += (short) X;
								break;
							}
						
							case 0x02: {
								this.E += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.E += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x08: {
						switch(ss) {
							case 0x0D: {
								this.F += (short) S;
								break;
							}
							
							case 0x0C: {
								this.F += (short) K;
								break;
							}
							
							case 0x0B: {
								this.F += (short) A;
								break;
							}
							
							case 0x0A: {
								this.F += (short) C;
								break;
							}
							
							case 0x09: {
								this.F += (short) E;
								break;
							}
							
							case 0x08: {
								this.F += (short) F;
								break;
							}
							
							case 0x07: {
								this.F += (short) P;
								break;
							}
							
							case 0x06: {
								this.F += (short) Z;
								break;
							}
							
							case 0x05: {
								this.F += (short) R;
								break;
							}
							
							case 0x04: {
								this.F += (short) Y;
								break;
							}
							
							case 0x03: {
								this.F += (short) X;
								break;
							}
						
							case 0x02: {
								this.F += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.F += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x07: {
						switch(ss) {
							case 0x0D: {
								this.P += (short) S;
								break;
							}
							
							case 0x0C: {
								this.P += (short) K;
								break;
							}
							
							case 0x0B: {
								this.P += (short) A;
								break;
							}
							
							case 0x0A: {
								this.P += (short) C;
								break;
							}
							
							case 0x09: {
								this.P += (short) E;
								break;
							}
							
							case 0x08: {
								this.P += (short) F;
								break;
							}
							
							case 0x07: {
								this.P += (short) P;
								break;
							}
							
							case 0x06: {
								this.P += (short) Z;
								break;
							}
							
							case 0x05: {
								this.P += (short) R;
								break;
							}
							
							case 0x04: {
								this.P += (short) Y;
								break;
							}
							
							case 0x03: {
								this.P += (short) X;
								break;
							}
						
							case 0x02: {
								this.P += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.P += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x06: {
						switch(ss) {
							case 0x0D: {
								this.Z += (short) S;
								break;
							}
							
							case 0x0C: {
								this.Z += (short) K;
								break;
							}
							
							case 0x0B: {
								this.Z += (short) A;
								break;
							}
							
							case 0x0A: {
								this.Z += (short) C;
								break;
							}
							
							case 0x09: {
								this.Z += (short) E;
								break;
							}
							
							case 0x08: {
								this.Z += (short) F;
								break;
							}
							
							case 0x07: {
								this.Z += (short) P;
								break;
							}
							
							case 0x06: {
								this.Z += (short) Z;
								break;
							}
							
							case 0x05: {
								this.Z += (short) R;
								break;
							}
							
							case 0x04: {
								this.Z += (short) Y;
								break;
							}
							
							case 0x03: {
								this.Z += (short) X;
								break;
							}
						
							case 0x02: {
								this.Z += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Z += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x05: {
						switch(ss) {
							case 0x0D: {
								this.R += (short) S;
								break;
							}
							
							case 0x0C: {
								this.R += (short) K;
								break;
							}
							
							case 0x0B: {
								this.R += (short) A;
								break;
							}
							
							case 0x0A: {
								this.R += (short) C;
								break;
							}
							
							case 0x09: {
								this.R += (short) E;
								break;
							}
							
							case 0x08: {
								this.R += (short) F;
								break;
							}
							
							case 0x07: {
								this.R += (short) P;
								break;
							}
							
							case 0x06: {
								this.R += (short) Z;
								break;
							}
							
							case 0x05: {
								this.R += (short) R;
								break;
							}
							
							case 0x04: {
								this.R += (short) Y;
								break;
							}
							
							case 0x03: {
								this.R += (short) X;
								break;
							}
						
							case 0x02: {
								this.R += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.R += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x04: {
						switch(ss) {
							case 0x0D: {
								this.Y += (short) S;
								break;
							}
							
							case 0x0C: {
								this.Y += (short) K;
								break;
							}
							
							case 0x0B: {
								this.Y += (short) A;
								break;
							}
							
							case 0x0A: {
								this.Y += (short) C;
								break;
							}
							
							case 0x09: {
								this.Y += (short) E;
								break;
							}
							
							case 0x08: {
								this.Y += (short) F;
								break;
							}
							
							case 0x07: {
								this.Y += (short) P;
								break;
							}
							
							case 0x06: {
								this.Y += (short) Z;
								break;
							}
							
							case 0x05: {
								this.Y += (short) R;
								break;
							}
							
							case 0x04: {
								this.Y += (short) Y;
								break;
							}
							
							case 0x03: {
								this.Y += (short) X;
								break;
							}
						
							case 0x02: {
								this.Y += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Y += (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x03: {
						switch(ss) {
							case 0x0D: {
								this.X += (short) S;
								break;
							}
							
							case 0x0C: {
								this.X += (short) K;
								break;
							}
							
							case 0x0B: {
								this.X += (short) A;
								break;
							}
							
							case 0x0A: {
								this.X += (short) C;
								break;
							}
							
							case 0x09: {
								this.X += (short) E;
								break;
							}
							
							case 0x08: {
								this.X += (short) F;
								break;
							}
							
							case 0x07: {
								this.X += (short) P;
								break;
							}
							
							case 0x06: {
								this.X += (short) Z;
								break;
							}
							
							case 0x05: {
								this.X += (short) R;
								break;
							}
							
							case 0x04: {
								this.X += (short) Y;
								break;
							}
							
							case 0x03: {
								this.X += (short) X;
								break;
							}
						
							case 0x02: {
								this.X += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.X += (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x02: {
						switch(ss) {
							case 0x0D: {
								this.I += (short) S;
								break;
							}
							
							case 0x0C: {
								this.I += (short) K;
								break;
							}
							
							case 0x0B: {
								this.I += (short) A;
								break;
							}
							
							case 0x0A: {
								this.I += (short) C;
								break;
							}
							
							case 0x09: {
								this.I += (short) E;
								break;
							}
							
							case 0x08: {
								this.I += (short) F;
								break;
							}
							
							case 0x07: {
								this.I += (short) P;
								break;
							}
							
							case 0x06: {
								this.I += (short) Z;
								break;
							}
							
							case 0x05: {
								this.I += (short) R;
								break;
							}
							
							case 0x04: {
								this.I += (short) Y;
								break;
							}
							
							case 0x03: {
								this.I += (short) X;
								break;
							}
						
							case 0x02: {
								this.I += (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.I += (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x01:
					default: {
						switch(ss) {
							case 0x0D: {
								this.D += (char) S;
								break;
							}
							
							case 0x0C: {
								this.D += (char) K;
								break;
							}
							
							case 0x0B: {
								this.D += (char) A;
								break;
							}
							
							case 0x0A: {
								this.D += (char) C;
								break;
							}
							
							case 0x09: {
								this.D += (char) E;
								break;
							}
							
							case 0x08: {
								this.D += (char) F;
								break;
							}
							
							case 0x07: {
								this.D += (char) P;
								break;
							}
							
							case 0x06: {
								this.D += (char) Z;
								break;
							}
							
							case 0x05: {
								this.D += (char) R;
								break;
							}
							
							case 0x04: {
								this.D += (char) Y;
								break;
							}
							
							case 0x03: {
								this.D += (char) X;
								break;
							}
						
							case 0x02: {
								this.D += (char) I;
								break;
							}
						
							case 0x01:
							default: {
								this.D += (char) D;
								break;
							}
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x0C000000: { // 0x0CRRNNNN ADD: Set RR to RR + NNNN
				int rr = operation & 0x00FF0000;
				int nnnn = operation & 0x0000FFFF;
				
				System.out.println(String.format("NNNN: 0x%04x", nnnn));
				
				switch(rr) {
					case 0x0D: {
						this.S -= (short) nnnn;
						break;
					}
					
					case 0x0C: {
						this.K -= (short) nnnn;
						break;
					}
					
					case 0x0B: {
						this.A -= (short) nnnn;
						break;
					}
					
					case 0x0A: {
						this.C -= (short) nnnn;
						break;
					}
					
					case 0x09: {
						this.E -= (short) nnnn;
						break;
					}
					
					case 0x08: {
						this.F -= (short) nnnn;
						break;
					}
					
					case 0x07: {
						this.P -= (short) nnnn;
						break;
					}
					
					case 0x06: {
						this.Z -= (short) nnnn;
						break;
					}
					
					case 0x05: {
						this.R -= (short) nnnn;
						break;
					}
					
					case 0x04: {
						this.Y -= (short) nnnn;
						break;
					}
					
					case 0x03: {
						this.X -= (short) nnnn;
						break;
					}
				
					case 0x02: {
						this.I -= (short) nnnn;
						break;
					}
				
					case 0x01:
					default: {
						this.D -= (char) nnnn;
						break;
					}
				}
				pc -= 4;
				break;
			}
			
			case 0x0D000000: { // 0x0DRR00SS SUB: Set RR to RR - SS
				int rr = operation & 0x00FF0000;
				int ss = operation & 0x000000FF;
				System.out.println(String.format("0x%04x", rr));
				switch(rr) {
					case 0x0D: {
						switch(ss) {
							case 0x0D: {
								this.S -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.S -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.S -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.S -= (short) C;
								break;
							}
							
							case 0x09: {
								this.S -= (short) E;
								break;
							}
							
							case 0x08: {
								this.S -= (short) F;
								break;
							}
							
							case 0x07: {
								this.S -= (short) P;
								break;
							}
							
							case 0x06: {
								this.S -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.S -= (short) R;
								break;
							}
							
							case 0x04: {
								this.S -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.S -= (short) X;
								break;
							}
						
							case 0x02: {
								this.S -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.S -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0C: {
						switch(ss) {
							case 0x0D: {
								this.K -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.K -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.K -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.K -= (short) C;
								break;
							}
							
							case 0x09: {
								this.K -= (short) E;
								break;
							}
							
							case 0x08: {
								this.K -= (short) F;
								break;
							}
							
							case 0x07: {
								this.K -= (short) P;
								break;
							}
							
							case 0x06: {
								this.K -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.K -= (short) R;
								break;
							}
							
							case 0x04: {
								this.K -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.K -= (short) X;
								break;
							}
						
							case 0x02: {
								this.K -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.K -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0B: {
						switch(ss) {
							case 0x0D: {
								this.A -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.A -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.A -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.A -= (short) C;
								break;
							}
							
							case 0x09: {
								this.A -= (short) E;
								break;
							}
							
							case 0x08: {
								this.A -= (short) F;
								break;
							}
							
							case 0x07: {
								this.A -= (short) P;
								break;
							}
							
							case 0x06: {
								this.A -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.A -= (short) R;
								break;
							}
							
							case 0x04: {
								this.A -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.A -= (short) X;
								break;
							}
						
							case 0x02: {
								this.A -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.A -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0A: {
						switch(ss) {
							case 0x0D: {
								this.C -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.C -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.C -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.C -= (short) C;
								break;
							}
							
							case 0x09: {
								this.C -= (short) E;
								break;
							}
							
							case 0x08: {
								this.C -= (short) F;
								break;
							}
							
							case 0x07: {
								this.C -= (short) P;
								break;
							}
							
							case 0x06: {
								this.C -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.C -= (short) R;
								break;
							}
							
							case 0x04: {
								this.C -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.C -= (short) X;
								break;
							}
						
							case 0x02: {
								this.C -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.C -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x09: {
						switch(ss) {
							case 0x0D: {
								this.E -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.E -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.E -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.E -= (short) C;
								break;
							}
							
							case 0x09: {
								this.E -= (short) E;
								break;
							}
							
							case 0x08: {
								this.E -= (short) F;
								break;
							}
							
							case 0x07: {
								this.E -= (short) P;
								break;
							}
							
							case 0x06: {
								this.E -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.E -= (short) R;
								break;
							}
							
							case 0x04: {
								this.E -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.E -= (short) X;
								break;
							}
						
							case 0x02: {
								this.E -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.E -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x08: {
						switch(ss) {
							case 0x0D: {
								this.F -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.F -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.F -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.F -= (short) C;
								break;
							}
							
							case 0x09: {
								this.F -= (short) E;
								break;
							}
							
							case 0x08: {
								this.F -= (short) F;
								break;
							}
							
							case 0x07: {
								this.F -= (short) P;
								break;
							}
							
							case 0x06: {
								this.F -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.F -= (short) R;
								break;
							}
							
							case 0x04: {
								this.F -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.F -= (short) X;
								break;
							}
						
							case 0x02: {
								this.F -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.F -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x07: {
						switch(ss) {
							case 0x0D: {
								this.P -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.P -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.P -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.P -= (short) C;
								break;
							}
							
							case 0x09: {
								this.P -= (short) E;
								break;
							}
							
							case 0x08: {
								this.P -= (short) F;
								break;
							}
							
							case 0x07: {
								this.P -= (short) P;
								break;
							}
							
							case 0x06: {
								this.P -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.P -= (short) R;
								break;
							}
							
							case 0x04: {
								this.P -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.P -= (short) X;
								break;
							}
						
							case 0x02: {
								this.P -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.P -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x06: {
						switch(ss) {
							case 0x0D: {
								this.Z -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Z -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Z -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Z -= (short) C;
								break;
							}
							
							case 0x09: {
								this.Z -= (short) E;
								break;
							}
							
							case 0x08: {
								this.Z -= (short) F;
								break;
							}
							
							case 0x07: {
								this.Z -= (short) P;
								break;
							}
							
							case 0x06: {
								this.Z -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Z -= (short) R;
								break;
							}
							
							case 0x04: {
								this.Z -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Z -= (short) X;
								break;
							}
						
							case 0x02: {
								this.Z -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Z -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x05: {
						switch(ss) {
							case 0x0D: {
								this.R -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.R -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.R -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.R -= (short) C;
								break;
							}
							
							case 0x09: {
								this.R -= (short) E;
								break;
							}
							
							case 0x08: {
								this.R -= (short) F;
								break;
							}
							
							case 0x07: {
								this.R -= (short) P;
								break;
							}
							
							case 0x06: {
								this.R -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.R -= (short) R;
								break;
							}
							
							case 0x04: {
								this.R -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.R -= (short) X;
								break;
							}
						
							case 0x02: {
								this.R -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.R -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x04: {
						switch(ss) {
							case 0x0D: {
								this.Y -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Y -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Y -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Y -= (short) C;
								break;
							}
							
							case 0x09: {
								this.Y -= (short) E;
								break;
							}
							
							case 0x08: {
								this.Y -= (short) F;
								break;
							}
							
							case 0x07: {
								this.Y -= (short) P;
								break;
							}
							
							case 0x06: {
								this.Y -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Y -= (short) R;
								break;
							}
							
							case 0x04: {
								this.Y -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Y -= (short) X;
								break;
							}
						
							case 0x02: {
								this.Y -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Y -= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x03: {
						switch(ss) {
							case 0x0D: {
								this.X -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.X -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.X -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.X -= (short) C;
								break;
							}
							
							case 0x09: {
								this.X -= (short) E;
								break;
							}
							
							case 0x08: {
								this.X -= (short) F;
								break;
							}
							
							case 0x07: {
								this.X -= (short) P;
								break;
							}
							
							case 0x06: {
								this.X -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.X -= (short) R;
								break;
							}
							
							case 0x04: {
								this.X -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.X -= (short) X;
								break;
							}
						
							case 0x02: {
								this.X -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.X -= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x02: {
						switch(ss) {
							case 0x0D: {
								this.I -= (short) S;
								break;
							}
							
							case 0x0C: {
								this.I -= (short) K;
								break;
							}
							
							case 0x0B: {
								this.I -= (short) A;
								break;
							}
							
							case 0x0A: {
								this.I -= (short) C;
								break;
							}
							
							case 0x09: {
								this.I -= (short) E;
								break;
							}
							
							case 0x08: {
								this.I -= (short) F;
								break;
							}
							
							case 0x07: {
								this.I -= (short) P;
								break;
							}
							
							case 0x06: {
								this.I -= (short) Z;
								break;
							}
							
							case 0x05: {
								this.I -= (short) R;
								break;
							}
							
							case 0x04: {
								this.I -= (short) Y;
								break;
							}
							
							case 0x03: {
								this.I -= (short) X;
								break;
							}
						
							case 0x02: {
								this.I -= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.I -= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x01:
					default: {
						switch(ss) {
							case 0x0D: {
								this.D -= (char) S;
								break;
							}
							
							case 0x0C: {
								this.D -= (char) K;
								break;
							}
							
							case 0x0B: {
								this.D -= (char) A;
								break;
							}
							
							case 0x0A: {
								this.D -= (char) C;
								break;
							}
							
							case 0x09: {
								this.D -= (char) E;
								break;
							}
							
							case 0x08: {
								this.D -= (char) F;
								break;
							}
							
							case 0x07: {
								this.D -= (char) P;
								break;
							}
							
							case 0x06: {
								this.D -= (char) Z;
								break;
							}
							
							case 0x05: {
								this.D -= (char) R;
								break;
							}
							
							case 0x04: {
								this.D -= (char) Y;
								break;
							}
							
							case 0x03: {
								this.D -= (char) X;
								break;
							}
						
							case 0x02: {
								this.D -= (char) I;
								break;
							}
						
							case 0x01:
							default: {
								this.D -= (char) D;
								break;
							}
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x0E000000: { // 0x0ERRNNNN SUB: Set RR to RR -x NNNN
				int rr = operation & 0x00FF0000;
				int nnnn = operation & 0x0000FFFF;
				
				System.out.println(String.format("NNNN: 0x%04x", nnnn));
				
				switch(rr) {
					case 0x0D: {
						this.S -= (short) nnnn;
						break;
					}
					
					case 0x0C: {
						this.K -= (short) nnnn;
						break;
					}
					
					case 0x0B: {
						this.A -= (short) nnnn;
						break;
					}
					
					case 0x0A: {
						this.C -= (short) nnnn;
						break;
					}
					
					case 0x09: {
						this.E -= (short) nnnn;
						break;
					}
					
					case 0x08: {
						this.F -= (short) nnnn;
						break;
					}
					
					case 0x07: {
						this.P -= (short) nnnn;
						break;
					}
					
					case 0x06: {
						this.Z -= (short) nnnn;
						break;
					}
					
					case 0x05: {
						this.R -= (short) nnnn;
						break;
					}
					
					case 0x04: {
						this.Y -= (short) nnnn;
						break;
					}
					
					case 0x03: {
						this.X -= (short) nnnn;
						break;
					}
				
					case 0x02: {
						this.I -= (short) nnnn;
						break;
					}
				
					case 0x01:
					default: {
						this.D -= (char) nnnn;
						break;
					}
				}
				pc += 4;
				break;
			}
			
			
			case 0xFF000000: {
				switch(operation & 0x0000000F) {
					case 0x0000000F: { // F000000F HALT: Stop CPU
						Main.logger.log("Recieved Halt Operation 0xFF00000F");
						System.exit(0);
						break;
					}
					default:
						break;
				}
				break;
			}
			
			case 0x10000000: { // 0x10RR00SS DIV: Set RR to RR / SS
				int rr = operation & 0x00FF0000;
				int ss = operation & 0x000000FF;
				System.out.println(String.format("0x%04x", rr));
				switch(rr) {
					case 0x0D: {
						switch(ss) {
							case 0x0D: {
								this.S /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.S /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.S /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.S /= (short) C;
								break;
							}
							
							case 0x09: {
								this.S /= (short) E;
								break;
							}
							
							case 0x08: {
								this.S /= (short) F;
								break;
							}
							
							case 0x07: {
								this.S /= (short) P;
								break;
							}
							
							case 0x06: {
								this.S /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.S /= (short) R;
								break;
							}
							
							case 0x04: {
								this.S /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.S /= (short) X;
								break;
							}
						
							case 0x02: {
								this.S /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.S /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0C: {
						switch(ss) {
							case 0x0D: {
								this.K /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.K /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.K /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.K /= (short) C;
								break;
							}
							
							case 0x09: {
								this.K /= (short) E;
								break;
							}
							
							case 0x08: {
								this.K /= (short) F;
								break;
							}
							
							case 0x07: {
								this.K /= (short) P;
								break;
							}
							
							case 0x06: {
								this.K /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.K /= (short) R;
								break;
							}
							
							case 0x04: {
								this.K /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.K /= (short) X;
								break;
							}
						
							case 0x02: {
								this.K /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.K /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0B: {
						switch(ss) {
							case 0x0D: {
								this.A /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.A /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.A /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.A /= (short) C;
								break;
							}
							
							case 0x09: {
								this.A /= (short) E;
								break;
							}
							
							case 0x08: {
								this.A /= (short) F;
								break;
							}
							
							case 0x07: {
								this.A /= (short) P;
								break;
							}
							
							case 0x06: {
								this.A /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.A /= (short) R;
								break;
							}
							
							case 0x04: {
								this.A /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.A /= (short) X;
								break;
							}
						
							case 0x02: {
								this.A /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.A /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0A: {
						switch(ss) {
							case 0x0D: {
								this.C /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.C /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.C /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.C /= (short) C;
								break;
							}
							
							case 0x09: {
								this.C /= (short) E;
								break;
							}
							
							case 0x08: {
								this.C /= (short) F;
								break;
							}
							
							case 0x07: {
								this.C /= (short) P;
								break;
							}
							
							case 0x06: {
								this.C /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.C /= (short) R;
								break;
							}
							
							case 0x04: {
								this.C /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.C /= (short) X;
								break;
							}
						
							case 0x02: {
								this.C /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.C /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x09: {
						switch(ss) {
							case 0x0D: {
								this.E /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.E /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.E /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.E /= (short) C;
								break;
							}
							
							case 0x09: {
								this.E /= (short) E;
								break;
							}
							
							case 0x08: {
								this.E /= (short) F;
								break;
							}
							
							case 0x07: {
								this.E /= (short) P;
								break;
							}
							
							case 0x06: {
								this.E /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.E /= (short) R;
								break;
							}
							
							case 0x04: {
								this.E /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.E /= (short) X;
								break;
							}
						
							case 0x02: {
								this.E /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.E /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x08: {
						switch(ss) {
							case 0x0D: {
								this.F /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.F /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.F /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.F /= (short) C;
								break;
							}
							
							case 0x09: {
								this.F /= (short) E;
								break;
							}
							
							case 0x08: {
								this.F /= (short) F;
								break;
							}
							
							case 0x07: {
								this.F /= (short) P;
								break;
							}
							
							case 0x06: {
								this.F /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.F /= (short) R;
								break;
							}
							
							case 0x04: {
								this.F /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.F /= (short) X;
								break;
							}
						
							case 0x02: {
								this.F /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.F /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x07: {
						switch(ss) {
							case 0x0D: {
								this.P /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.P /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.P /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.P /= (short) C;
								break;
							}
							
							case 0x09: {
								this.P /= (short) E;
								break;
							}
							
							case 0x08: {
								this.P /= (short) F;
								break;
							}
							
							case 0x07: {
								this.P /= (short) P;
								break;
							}
							
							case 0x06: {
								this.P /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.P /= (short) R;
								break;
							}
							
							case 0x04: {
								this.P /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.P /= (short) X;
								break;
							}
						
							case 0x02: {
								this.P /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.P /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x06: {
						switch(ss) {
							case 0x0D: {
								this.Z /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Z /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Z /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Z /= (short) C;
								break;
							}
							
							case 0x09: {
								this.Z /= (short) E;
								break;
							}
							
							case 0x08: {
								this.Z /= (short) F;
								break;
							}
							
							case 0x07: {
								this.Z /= (short) P;
								break;
							}
							
							case 0x06: {
								this.Z /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Z /= (short) R;
								break;
							}
							
							case 0x04: {
								this.Z /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Z /= (short) X;
								break;
							}
						
							case 0x02: {
								this.Z /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Z /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x05: {
						switch(ss) {
							case 0x0D: {
								this.R /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.R /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.R /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.R /= (short) C;
								break;
							}
							
							case 0x09: {
								this.R /= (short) E;
								break;
							}
							
							case 0x08: {
								this.R /= (short) F;
								break;
							}
							
							case 0x07: {
								this.R /= (short) P;
								break;
							}
							
							case 0x06: {
								this.R /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.R /= (short) R;
								break;
							}
							
							case 0x04: {
								this.R /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.R /= (short) X;
								break;
							}
						
							case 0x02: {
								this.R /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.R /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x04: {
						switch(ss) {
							case 0x0D: {
								this.Y /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Y /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Y /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Y /= (short) C;
								break;
							}
							
							case 0x09: {
								this.Y /= (short) E;
								break;
							}
							
							case 0x08: {
								this.Y /= (short) F;
								break;
							}
							
							case 0x07: {
								this.Y /= (short) P;
								break;
							}
							
							case 0x06: {
								this.Y /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Y /= (short) R;
								break;
							}
							
							case 0x04: {
								this.Y /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Y /= (short) X;
								break;
							}
						
							case 0x02: {
								this.Y /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Y /= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x03: {
						switch(ss) {
							case 0x0D: {
								this.X /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.X /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.X /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.X /= (short) C;
								break;
							}
							
							case 0x09: {
								this.X /= (short) E;
								break;
							}
							
							case 0x08: {
								this.X /= (short) F;
								break;
							}
							
							case 0x07: {
								this.X /= (short) P;
								break;
							}
							
							case 0x06: {
								this.X /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.X /= (short) R;
								break;
							}
							
							case 0x04: {
								this.X /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.X /= (short) X;
								break;
							}
						
							case 0x02: {
								this.X /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.X /= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x02: {
						switch(ss) {
							case 0x0D: {
								this.I /= (short) S;
								break;
							}
							
							case 0x0C: {
								this.I /= (short) K;
								break;
							}
							
							case 0x0B: {
								this.I /= (short) A;
								break;
							}
							
							case 0x0A: {
								this.I /= (short) C;
								break;
							}
							
							case 0x09: {
								this.I /= (short) E;
								break;
							}
							
							case 0x08: {
								this.I /= (short) F;
								break;
							}
							
							case 0x07: {
								this.I /= (short) P;
								break;
							}
							
							case 0x06: {
								this.I /= (short) Z;
								break;
							}
							
							case 0x05: {
								this.I /= (short) R;
								break;
							}
							
							case 0x04: {
								this.I /= (short) Y;
								break;
							}
							
							case 0x03: {
								this.I /= (short) X;
								break;
							}
						
							case 0x02: {
								this.I /= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.I /= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x01:
					default: {
						switch(ss) {
							case 0x0D: {
								this.D /= (char) S;
								break;
							}
							
							case 0x0C: {
								this.D /= (char) K;
								break;
							}
							
							case 0x0B: {
								this.D /= (char) A;
								break;
							}
							
							case 0x0A: {
								this.D /= (char) C;
								break;
							}
							
							case 0x09: {
								this.D /= (char) E;
								break;
							}
							
							case 0x08: {
								this.D /= (char) F;
								break;
							}
							
							case 0x07: {
								this.D /= (char) P;
								break;
							}
							
							case 0x06: {
								this.D /= (char) Z;
								break;
							}
							
							case 0x05: {
								this.D /= (char) R;
								break;
							}
							
							case 0x04: {
								this.D /= (char) Y;
								break;
							}
							
							case 0x03: {
								this.D /= (char) X;
								break;
							}
						
							case 0x02: {
								this.D /= (char) I;
								break;
							}
						
							case 0x01:
							default: {
								this.D /= (char) D;
								break;
							}
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x11000000: { // 0x11RRNNNN DIV: Set RR to RR / NNNN
				int rr = operation & 0x00FF0000;
				int nnnn = operation & 0x0000FFFF;
				
				System.out.println(String.format("NNNN: 0x%04x", nnnn));
				
				switch(rr) {
					case 0x0D: {
						this.S /= (short) nnnn;
						break;
					}
					
					case 0x0C: {
						this.K /= (short) nnnn;
						break;
					}
					
					case 0x0B: {
						this.A /= (short) nnnn;
						break;
					}
					
					case 0x0A: {
						this.C /= (short) nnnn;
						break;
					}
					
					case 0x09: {
						this.E /= (short) nnnn;
						break;
					}
					
					case 0x08: {
						this.F /= (short) nnnn;
						break;
					}
					
					case 0x07: {
						this.P /= (short) nnnn;
						break;
					}
					
					case 0x06: {
						this.Z /= (short) nnnn;
						break;
					}
					
					case 0x05: {
						this.R /= (short) nnnn;
						break;
					}
					
					case 0x04: {
						this.Y /= (short) nnnn;
						break;
					}
					
					case 0x03: {
						this.X /= (short) nnnn;
						break;
					}
				
					case 0x02: {
						this.I /= (short) nnnn;
						break;
					}
				
					case 0x01:
					default: {
						this.D /= (char) nnnn;
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x12000000: { // 0x10RR00SS MUL: Set RR to RR * SS
				int rr = operation & 0x00FF0000;
				int ss = operation & 0x000000FF;
				System.out.println(String.format("0x%04x", rr));
				switch(rr) {
					case 0x0D: {
						switch(ss) {
							case 0x0D: {
								this.S *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.S *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.S *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.S *= (short) C;
								break;
							}
							
							case 0x09: {
								this.S *= (short) E;
								break;
							}
							
							case 0x08: {
								this.S *= (short) F;
								break;
							}
							
							case 0x07: {
								this.S *= (short) P;
								break;
							}
							
							case 0x06: {
								this.S *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.S *= (short) R;
								break;
							}
							
							case 0x04: {
								this.S *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.S *= (short) X;
								break;
							}
						
							case 0x02: {
								this.S *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.S *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0C: {
						switch(ss) {
							case 0x0D: {
								this.K *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.K *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.K *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.K *= (short) C;
								break;
							}
							
							case 0x09: {
								this.K *= (short) E;
								break;
							}
							
							case 0x08: {
								this.K *= (short) F;
								break;
							}
							
							case 0x07: {
								this.K *= (short) P;
								break;
							}
							
							case 0x06: {
								this.K *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.K *= (short) R;
								break;
							}
							
							case 0x04: {
								this.K *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.K *= (short) X;
								break;
							}
						
							case 0x02: {
								this.K *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.K *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0B: {
						switch(ss) {
							case 0x0D: {
								this.A *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.A *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.A *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.A *= (short) C;
								break;
							}
							
							case 0x09: {
								this.A *= (short) E;
								break;
							}
							
							case 0x08: {
								this.A *= (short) F;
								break;
							}
							
							case 0x07: {
								this.A *= (short) P;
								break;
							}
							
							case 0x06: {
								this.A *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.A *= (short) R;
								break;
							}
							
							case 0x04: {
								this.A *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.A *= (short) X;
								break;
							}
						
							case 0x02: {
								this.A *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.A *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x0A: {
						switch(ss) {
							case 0x0D: {
								this.C *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.C *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.C *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.C *= (short) C;
								break;
							}
							
							case 0x09: {
								this.C *= (short) E;
								break;
							}
							
							case 0x08: {
								this.C *= (short) F;
								break;
							}
							
							case 0x07: {
								this.C *= (short) P;
								break;
							}
							
							case 0x06: {
								this.C *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.C *= (short) R;
								break;
							}
							
							case 0x04: {
								this.C *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.C *= (short) X;
								break;
							}
						
							case 0x02: {
								this.C *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.C *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x09: {
						switch(ss) {
							case 0x0D: {
								this.E *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.E *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.E *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.E *= (short) C;
								break;
							}
							
							case 0x09: {
								this.E *= (short) E;
								break;
							}
							
							case 0x08: {
								this.E *= (short) F;
								break;
							}
							
							case 0x07: {
								this.E *= (short) P;
								break;
							}
							
							case 0x06: {
								this.E *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.E *= (short) R;
								break;
							}
							
							case 0x04: {
								this.E *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.E *= (short) X;
								break;
							}
						
							case 0x02: {
								this.E *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.E *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x08: {
						switch(ss) {
							case 0x0D: {
								this.F *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.F *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.F *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.F *= (short) C;
								break;
							}
							
							case 0x09: {
								this.F *= (short) E;
								break;
							}
							
							case 0x08: {
								this.F *= (short) F;
								break;
							}
							
							case 0x07: {
								this.F *= (short) P;
								break;
							}
							
							case 0x06: {
								this.F *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.F *= (short) R;
								break;
							}
							
							case 0x04: {
								this.F *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.F *= (short) X;
								break;
							}
						
							case 0x02: {
								this.F *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.F *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x07: {
						switch(ss) {
							case 0x0D: {
								this.P *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.P *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.P *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.P *= (short) C;
								break;
							}
							
							case 0x09: {
								this.P *= (short) E;
								break;
							}
							
							case 0x08: {
								this.P *= (short) F;
								break;
							}
							
							case 0x07: {
								this.P *= (short) P;
								break;
							}
							
							case 0x06: {
								this.P *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.P *= (short) R;
								break;
							}
							
							case 0x04: {
								this.P *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.P *= (short) X;
								break;
							}
						
							case 0x02: {
								this.P *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.P *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x06: {
						switch(ss) {
							case 0x0D: {
								this.Z *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Z *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Z *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Z *= (short) C;
								break;
							}
							
							case 0x09: {
								this.Z *= (short) E;
								break;
							}
							
							case 0x08: {
								this.Z *= (short) F;
								break;
							}
							
							case 0x07: {
								this.Z *= (short) P;
								break;
							}
							
							case 0x06: {
								this.Z *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Z *= (short) R;
								break;
							}
							
							case 0x04: {
								this.Z *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Z *= (short) X;
								break;
							}
						
							case 0x02: {
								this.Z *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Z *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x05: {
						switch(ss) {
							case 0x0D: {
								this.R *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.R *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.R *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.R *= (short) C;
								break;
							}
							
							case 0x09: {
								this.R *= (short) E;
								break;
							}
							
							case 0x08: {
								this.R *= (short) F;
								break;
							}
							
							case 0x07: {
								this.R *= (short) P;
								break;
							}
							
							case 0x06: {
								this.R *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.R *= (short) R;
								break;
							}
							
							case 0x04: {
								this.R *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.R *= (short) X;
								break;
							}
						
							case 0x02: {
								this.R *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.R *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x04: {
						switch(ss) {
							case 0x0D: {
								this.Y *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.Y *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.Y *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.Y *= (short) C;
								break;
							}
							
							case 0x09: {
								this.Y *= (short) E;
								break;
							}
							
							case 0x08: {
								this.Y *= (short) F;
								break;
							}
							
							case 0x07: {
								this.Y *= (short) P;
								break;
							}
							
							case 0x06: {
								this.Y *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.Y *= (short) R;
								break;
							}
							
							case 0x04: {
								this.Y *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.Y *= (short) X;
								break;
							}
						
							case 0x02: {
								this.Y *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.Y *= (short) D;
								break;
							}
						}
						break;
					}
					
					case 0x03: {
						switch(ss) {
							case 0x0D: {
								this.X *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.X *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.X *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.X *= (short) C;
								break;
							}
							
							case 0x09: {
								this.X *= (short) E;
								break;
							}
							
							case 0x08: {
								this.X *= (short) F;
								break;
							}
							
							case 0x07: {
								this.X *= (short) P;
								break;
							}
							
							case 0x06: {
								this.X *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.X *= (short) R;
								break;
							}
							
							case 0x04: {
								this.X *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.X *= (short) X;
								break;
							}
						
							case 0x02: {
								this.X *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.X *= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x02: {
						switch(ss) {
							case 0x0D: {
								this.I *= (short) S;
								break;
							}
							
							case 0x0C: {
								this.I *= (short) K;
								break;
							}
							
							case 0x0B: {
								this.I *= (short) A;
								break;
							}
							
							case 0x0A: {
								this.I *= (short) C;
								break;
							}
							
							case 0x09: {
								this.I *= (short) E;
								break;
							}
							
							case 0x08: {
								this.I *= (short) F;
								break;
							}
							
							case 0x07: {
								this.I *= (short) P;
								break;
							}
							
							case 0x06: {
								this.I *= (short) Z;
								break;
							}
							
							case 0x05: {
								this.I *= (short) R;
								break;
							}
							
							case 0x04: {
								this.I *= (short) Y;
								break;
							}
							
							case 0x03: {
								this.I *= (short) X;
								break;
							}
						
							case 0x02: {
								this.I *= (short) I;
								break;
							}
						
							case 0x01:
							default: {
								this.I *= (short) D;
								break;
							}
						}
						break;
					}
				
					case 0x01:
					default: {
						switch(ss) {
							case 0x0D: {
								this.D *= (char) S;
								break;
							}
							
							case 0x0C: {
								this.D *= (char) K;
								break;
							}
							
							case 0x0B: {
								this.D *= (char) A;
								break;
							}
							
							case 0x0A: {
								this.D *= (char) C;
								break;
							}
							
							case 0x09: {
								this.D *= (char) E;
								break;
							}
							
							case 0x08: {
								this.D *= (char) F;
								break;
							}
							
							case 0x07: {
								this.D *= (char) P;
								break;
							}
							
							case 0x06: {
								this.D *= (char) Z;
								break;
							}
							
							case 0x05: {
								this.D *= (char) R;
								break;
							}
							
							case 0x04: {
								this.D *= (char) Y;
								break;
							}
							
							case 0x03: {
								this.D *= (char) X;
								break;
							}
						
							case 0x02: {
								this.D *= (char) I;
								break;
							}
						
							case 0x01:
							default: {
								this.D *= (char) D;
								break;
							}
						}
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x13000000: { // 0x13RRNNNN MUL: Set RR to RR * NNNN
				int rr = operation & 0x00FF0000;
				int nnnn = operation & 0x0000FFFF;
				
				System.out.println(String.format("NNNN: 0x%04x", nnnn));
				
				switch(rr) {
					case 0x0D: {
						this.S *= (short) nnnn;
						break;
					}
					
					case 0x0C: {
						this.K *= (short) nnnn;
						break;
					}
					
					case 0x0B: {
						this.A *= (short) nnnn;
						break;
					}
					
					case 0x0A: {
						this.C *= (short) nnnn;
						break;
					}
					
					case 0x09: {
						this.E *= (short) nnnn;
						break;
					}
					
					case 0x08: {
						this.F *= (short) nnnn;
						break;
					}
					
					case 0x07: {
						this.P *= (short) nnnn;
						break;
					}
					
					case 0x06: {
						this.Z *= (short) nnnn;
						break;
					}
					
					case 0x05: {
						this.R *= (short) nnnn;
						break;
					}
					
					case 0x04: {
						this.Y *= (short) nnnn;
						break;
					}
					
					case 0x03: {
						this.X *= (short) nnnn;
						break;
					}
				
					case 0x02: {
						this.I *= (short) nnnn;
						break;
					}
				
					case 0x01:
					default: {
						this.D *= (char) nnnn;
						break;
					}
				}
				pc += 4;
				break;
			}
			
			case 0x14000000: { // 0x140000PP INB: Read 4 Bytes on port PP into register K
				int pp = operation & 0x000000FF;
				try {
					this.K = this.devices[pp].poll();
				} catch (NullPointerException e) {
					Main.logger.log(String.format("Device not found at port 0x%02x", pp));
					System.exit(1);
				}
				pc += 4;
				break;
			}
			
			case 0x15000000: { // 0x15PPNNNN OUTB: Write 4 bytes (NNNN) to port PP
				int pp = (operation & 0x00FF0000) >> 16;
				int nnnn = operation & 0x0000FFFF;
				try {
					this.devices[pp].query((short)nnnn);
				} catch (NullPointerException e) {
					Main.logger.log(String.format("Device not found at port 0x%02x", pp));
					System.exit(1);
				}
				pc += 4;
				break;
			}
			
			default:
				Main.logger.log("Illegal Operation, exiting...");
				System.exit(0);
				break;
		}
		this.S = (short)this.sp;
		
		this.registerDumper.update();
		
		if(delay_timer > 0)
			delay_timer--;
	}
	
	public short removeTheElement(int index) {
		if (this.stack == null
		|| index < 0
		|| index >= this.stack.length) {
		
			return 0x00;
		}
		
		short[] anotherArray = new short[this.stack.length];
		
		short popped = 0x00;
		
		for (int i = 0, k = 0; i < this.stack.length; i++) {
			
			if (i == index) {
				popped = this.stack[i];
				continue;
			}
			
			anotherArray[k++] = this.stack[i];
		}
		
		this.stack = anotherArray;
		return popped;
	}
	
	public void loadROM(String file, EpicFrame frame) {
		this.rom = file;
		this.frame = frame;
		DataInputStream romfile = null;
		
		if(!Files.exists(Paths.get(file))) {
			JOptionPane.showMessageDialog(frame, "ROM File does not exist!");
			Main.logger.log("ROM File does not exist!");
			System.exit(1);
		}
		
		try {
			romfile = new DataInputStream(new FileInputStream(new File(file)));
			
			int offset = 0;
			while(romfile.available() > 0) {
				this.memory[0x400 + offset++] = (char)(romfile.readByte() & 0xFF);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if(romfile != null)
				try { romfile.close(); } catch(IOException ex) {}
		}
	}
	
	public char getMemoryOffset(int offset) {
		return this.memory[offset];
	}
	
	public boolean needsRedraw() {
		return this.redrawRequired;
	}
	
	public void cancelRedraw() {
		this.redrawRequired = false;
	}

	public byte[] getDisplay() {
		return this.display;
	}
	
	public char[] getDisplayBuffer() {
		return this.displayBuffer;
	}
	
	public char getRegisterD() {
		return this.D;
	}
	
	public short getRegisterI() {
		return this.I;
	}
	
	public short getRegisterX() {
		return this.X;
	}
	
	public short getRegisterY() {
		return this.Y;
	}
	
	public short getRegisterR() {
		return this.R;
	}
	
	public short getRegisterZ() {
		return this.Z;
	}
	
	public short getRegisterP() {
		return this.P;
	}
	
	public short getRegisterF() {
		return this.F;
	}
	
	public short getRegisterE() {
		return this.E;
	}
	
	public short getRegisterC() {
		return this.C;
	}
	
	public short getRegisterA() {
		return this.A;
	}
	
	public short getRegisterK() {
		return this.K;
	}
	
	public short getRegisterS() {
		return this.S;
	}

	public void setRegisterD(char d) {
		this.D = d;
	}
	
	public short getPc() {
		return this.pc;
	}
	
	public void reset() {
		Main.paused = true;
		this.memory = new char[1024 * 64];
		
		this.stack = new short[256];
		this.sp = 0;
		this.display = new byte[64 * 32];
		this.redrawRequired = false;
		this.pc = 0x400;
		this.D = 0x00;
		this.I = 0x00;
		this.X = 0x00;
		this.Y = 0x00;
		this.Z = 0x00;
		this.P = 0x00;
		this.F = 0x00;
		this.E = 0x00;
		this.C = 0x00;
		this.A = 0x00;
		this.K = 0x00;
		this.R = 0x00;
		this.S = 0x00;
		this.displayBuffer = new char[64 * 32];
		this.displayPointer = 0;
		this.delay_timer = 0;
		this.audio_latch = 0;
		loadROM(this.rom, this.frame);
		Main.paused = false;
	}

	public char[] getMemory() {
		return this.memory;
	}
	
	public void pokeIntoMemory(int index, char data) {
		this.memory[index] = data;
	}
	
	public void clearDisplayBuffer() {
		this.displayPointer = 0;
		this.displayBuffer = new char[64 * 32];
	}
	
	public void saveState(String filepath) {
		Main.paused = true;
		MemoryObject saved = new MemoryObject(this.memory, this.D, this.I, this.X, this.Y, this.Z, this.P, this.F, this.E, this.C, this.A, this.K, this.R, this.S, this.pc, this.sp, this.stack, this.redrawRequired, this.displayBuffer, this.displayPointer);
		
		Object serObj = saved;
        FileOutputStream fileOut = null;
        ObjectOutputStream objectOut = null;
		try {
			fileOut = new FileOutputStream(filepath);
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(serObj);
			objectOut.close();
		} catch (Exception e) {}
		finally {
			Main.paused = false;
		}
	}
	
	public void restoreState(String filepath) {
		Main.paused = true;
		FileInputStream fileIn = null;
		Object obj = null;
		try {
			fileIn = new FileInputStream(filepath);
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);

	        obj = objectIn.readObject();
		} catch (IOException | ClassNotFoundException e) {}
		finally {
			if(obj != null) {
				MemoryObject state = ((MemoryObject) obj);
				this.memory = state.getMemory();
				
				this.D = state.getD();
				this.I = state.getI();
				this.X = state.getX();
				this.Y = state.getY();
				this.Z = state.getZ();
				this.P = state.getP();
				this.F = state.getF();
				this.E = state.getE();
				this.C = state.getC();
				this.A = state.getA();
				this.K = state.getK();
				this.R = state.getR();
				this.S = state.getS();
				
				this.pc = state.getPc();
				
				this.sp = state.getSp();
				this.stack = state.getStack();
				
				this.redrawRequired = true;
				this.displayBuffer = state.getDisplayBuffer();
				this.displayPointer = state.getDisplayPointer();
				Main.paused = false;
			} else {
				Main.paused = false;
				JOptionPane.showMessageDialog(frame, "Could not restore saved state.");
			}
		}
		
	}

	public void setRegisterDumper(Dump dumper) {
		this.registerDumper = dumper;
	}
}
