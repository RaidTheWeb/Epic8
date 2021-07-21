package tech.raidtheweb.epicsys.assembler;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import tech.raidtheweb.epicsys.Main;

public class Assembler {
	
	private List<String> lines;
	private Map<String, Address> labels;
	private DataOutputStream asmout;
	private Map<String, Address> validRegisters;
	
	public Assembler() {
		this.lines = new ArrayList<String>();
		this.labels = new HashMap<>();
		this.asmout = null;
		this.validRegisters = new HashMap<>();
		this.validRegisters.put("D", new Address((char)0x01));
		this.validRegisters.put("I", new Address((char)0x02));
		this.validRegisters.put("X", new Address((char)0x03));
		this.validRegisters.put("Y", new Address((char)0x04));
		this.validRegisters.put("R", new Address((char)0x05));
		this.validRegisters.put("Z", new Address((char)0x06));
		this.validRegisters.put("P", new Address((char)0x07));
		this.validRegisters.put("F", new Address((char)0x08));
		this.validRegisters.put("E", new Address((char)0x09));
		this.validRegisters.put("C", new Address((char)0x0A));
		this.validRegisters.put("A", new Address((char)0x0B));
		this.validRegisters.put("K", new Address((char)0x0C));
		this.validRegisters.put("S", new Address((char)0x0D));
	}
	
	/**
	 * Assemble input file into output file
	 * @param file
	 */
	public void assemble(String infile, String outfile) {
		try {
			DataOutputStream asmout = new DataOutputStream(new FileOutputStream(outfile));
			
			BufferedReader asmin = new BufferedReader(new FileReader(infile));
			
			
			for(String line; (line = asmin.readLine()) != null; ) {
				this.lines.add(line);
			}
			
			asmin.close();
			this.asmout = asmout;
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		this.parseLabels();
		this.parseCode();
	}

	/**
	 * Parse general instructions
	 */
	private void parseCode() {
		try {
			for (String line : this.lines) {
				line = line.trim();
				System.out.println(line);
			
				String[] tok = line.split("[, ]+");
				
				switch(tok[0]) {
					case "JMP":
						assertArgs(tok.length == 2, "JMP");
						if (Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2,4), 16);
							int addry = Integer.parseInt(tok[1].substring(4), 16);
							
							byte[] b = new byte[4];
							b[0] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if (Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2,3), 16);
							int addry = Integer.parseInt(tok[1].substring(3), 16);
								
							byte[] b = new byte[4];
							b[0] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if (Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2), 16);
								
							byte[] b = new byte[4];
							b[0] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							this.asmout.write(b);
						} else if (Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2), 16);
								
							byte[] b = new byte[4];
							b[0] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) addrx;
							this.asmout.write(b);
						} else if (Pattern.matches("^\\$$", tok[1])){
							byte[] b = new byte[4];
							b[0] = (byte) 0x01;
							b[1] = (byte) 0xFF;
							b[2] = (byte) 0xFF;
							b[3] = (byte) 0xFF;
							this.asmout.write(b);
						} else if (Pattern.matches("^[a-zA-Z0-9_]+$", tok[1])) {
							if(!(this.labels.containsKey(tok[1]))) { Main.logger.log("No such label as " + tok[1]); System.exit(1); }
							char addr = this.labels.get(tok[1]).getValue();
							String addrString = String.format("%04X", (int)addr);
							int addrx = Integer.parseInt(addrString.substring(0, 2), 16);
							int addry = Integer.parseInt(addrString.substring(2), 16);
							
							byte[] b = new byte[4];
							b[0] = (byte) 0x00;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
							
						}
						break;
						
					case "MOV": {
						assertArgs(tok.length == 3, "MOV");
						if(Pattern.matches("^'[A-Za-z0-9_$~`!@#$%^&*()-+={}\\\\[\\\\] |\\\\\\\\:;\\\\\\\"'<,>.?/]'$", tok[1])) {
							String chara = tok[1].substring(1, tok[1].length() - 1);
							char character = chara.charAt(0);
							if(Pattern.matches("^[A-Z]$", tok[2])) {
								if(this.validRegisters.containsKey(tok[2])) {
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x02;
									b[1] = (byte) 0x00;
									b[2] = (byte) character;
									b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
									this.asmout.write(b);
								} else {
									Main.logger.log("Invalid Register " + tok[2]);
									System.exit(0);
								}
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2,4), 16);
							int addry = Integer.parseInt(tok[1].substring(4), 16);
							if(this.validRegisters.containsKey(tok[2])) {
								byte[] b = new byte[4];
									
								b[0] = (byte) 0x02;
								b[1] = (byte) addrx;
								b[2] = (byte) addry;
								b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[2]);
								System.exit(0);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2,3), 16);
							int addry = Integer.parseInt(tok[1].substring(3), 16);
							if(this.validRegisters.containsKey(tok[2])) {
								byte[] b = new byte[4];
									
								b[0] = (byte) 0x02;
								b[1] = (byte) addrx;
								b[2] = (byte) addry;
								b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[2]);
								System.exit(0);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							char data = (char) Integer.parseInt(tok[1].substring(2), 16);
							if(this.validRegisters.containsKey(tok[2])) {
								byte[] b = new byte[4];
									
								b[0] = (byte) 0x02;
								b[1] = (byte) 0x00;
								b[2] = (byte) data;
								b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[2]);
								System.exit(0);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							char data = (char) Integer.parseInt("0" + tok[1].substring(2), 16);
							if(this.validRegisters.containsKey(tok[2])) {
								byte[] b = new byte[4];
									
								b[0] = (byte) 0x02;
								b[1] = (byte) 0x00;
								b[2] = (byte) data;
								b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[2]);
								System.exit(0);
							}
						}
						
						break;
					}
					
					case "CMD": {
						assertArgs(tok.length == 2, "CMD");
						if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							char data = (char)Integer.parseInt(tok[1].substring(4), 16);
							byte[] b = new byte[4];
							b[0] = (byte) 0x03;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) data;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							char data = (char)Integer.parseInt(tok[1].substring(3), 16);
							byte[] b = new byte[4];
							b[0] = (byte) 0x03;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) data;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							char data = (char)Integer.parseInt(tok[1].substring(2), 16);
							byte[] b = new byte[4];
							b[0] = (byte) 0x03;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) data;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							char data = (char)Integer.parseInt("0" + tok[1].substring(2), 16);
							byte[] b = new byte[4];
							b[0] = (byte) 0x03;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) data;
							this.asmout.write(b);
						}
						
						break;
					}
					
					case "PUSH": {
						assertArgs(tok.length == 2, "PUSH");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x04;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2, 4), 16);
							int addry = Integer.parseInt(tok[1].substring(4), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x06;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2, 3), 16);
							int addry = Integer.parseInt(tok[1].substring(3), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x06;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x06;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) addrx;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x06;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) addrx;
							this.asmout.write(b);
						} else if(Pattern.matches("^'[A-Za-z0-9_$~`!@#$%^&*()-+={}\\\\[\\\\] |\\\\\\\\:;\\\\\\\"'<,>.?/]'$", tok[1])) {
							String chara = tok[1].substring(1, tok[1].length() - 1);
							char character = chara.charAt(0);
							byte[] b = new byte[4];
							b[0] = (byte) 0x06;
							b[1] = (byte) 0x00;
							b[2] = (byte) 0x00;
							b[3] = (byte) character;
							this.asmout.write(b);
						}
						break;
					}
					
					case "POP": {
						assertArgs(tok.length == 2, "POP");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x05;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "JNULL": {
						assertArgs(tok.length == 2, "JNULL");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x07;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "PEEK": {
						assertArgs(tok.length == 2, "PEEK");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x08;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "POPL": {
						assertArgs(tok.length == 2, "POPL");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x09;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "DB": {
						assertArgs(tok.length >= 2, "DB");
						if(tok[1].startsWith("\"")) {
							String data = String.join(" ", tok);
							System.out.println(data.substring(3));
							
							if(Pattern.matches("^\"[A-Za-z0-9_$~`!@#$%^&*()-+={}\\[\\] |\\\\:;\\\"'<,>.?/]*\"$", data.substring(3))) {
								data = data.substring(4, data.length() - 1);
								System.out.println(data);
								char[] dataChars = data.toCharArray();
								for(char character : dataChars) {
									System.out.println(character);
									byte[] b = new byte[4];
									b[0] = (byte) 0x06;
									b[1] = (byte) 0x00;
									b[2] = (byte) 0x00;
									b[3] = (byte) character;
									this.asmout.write(b);
								}
								byte[] b = new byte[4];
								b[0] = (byte) 0x06;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) 0x0A;
								this.asmout.write(b);
							}
							
							
						}
						break;
					}
					
					case "WAIT": {
						assertArgs(tok.length == 2, "WAIT");
						if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2, 4), 16);
							int addry = Integer.parseInt(tok[1].substring(4), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x0A;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2, 3), 16);
							int addry = Integer.parseInt(tok[1].substring(3), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x0A;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							int addrx = 0x00;
							int addry = Integer.parseInt(tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x0A;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							int addrx = 0x00;
							int addry = Integer.parseInt("0" + tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x0A;
							b[1] = (byte) 0x0A;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						}
						break;
					}
					
					case "ADD": {
						assertArgs(tok.length == 3, "ADD");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								if(Pattern.matches("^[A-Z]$", tok[2])) {
									if(this.validRegisters.containsKey(tok[2])) {
										byte[] b = new byte[4];
										
										b[0] = (byte) 0x0B;
										b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
										b[2] = (byte) 0x00;
										b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
									
										this.asmout.write(b);
									}
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2, 4), 16);
									int addry = Integer.parseInt(tok[2].substring(4), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0C;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2, 3), 16);
									int addry = Integer.parseInt(tok[2].substring(3), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0C;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0C;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0C;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								}
									
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "SUB": {
						assertArgs(tok.length == 3, "SUB");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								if(Pattern.matches("^[A-Z]$", tok[2])) {
									if(this.validRegisters.containsKey(tok[2])) {
										byte[] b = new byte[4];
										
										b[0] = (byte) 0x0D;
										b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
										b[2] = (byte) 0x00;
										b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
									
										this.asmout.write(b);
									}
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2, 4), 16);
									int addry = Integer.parseInt(tok[2].substring(4), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0E;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2, 3), 16);
									int addry = Integer.parseInt(tok[2].substring(3), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0E;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0E;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x0E;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								}
									
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "DIV": {
						assertArgs(tok.length == 3, "DIV");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								if(Pattern.matches("^[A-Z]$", tok[2])) {
									if(this.validRegisters.containsKey(tok[2])) {
										byte[] b = new byte[4];
										
										b[0] = (byte) 0x10;
										b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
										b[2] = (byte) 0x00;
										b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
									
										this.asmout.write(b);
									}
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2, 4), 16);
									int addry = Integer.parseInt(tok[2].substring(4), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x11;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2, 3), 16);
									int addry = Integer.parseInt(tok[2].substring(3), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x11;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x11;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x11;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								}
									
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "MUL": {
						assertArgs(tok.length == 3, "MUL");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								if(Pattern.matches("^[A-Z]$", tok[2])) {
									if(this.validRegisters.containsKey(tok[2])) {
										byte[] b = new byte[4];
										
										b[0] = (byte) 0x12;
										b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
										b[2] = (byte) 0x00;
										b[3] = (byte) this.validRegisters.get(tok[2]).getValue();
									
										this.asmout.write(b);
									}
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2, 4), 16);
									int addry = Integer.parseInt(tok[2].substring(4), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x13;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2, 3), 16);
									int addry = Integer.parseInt(tok[2].substring(3), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x13;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) addrx;
									b[3] = (byte) addry;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
									int addrx = Integer.parseInt(tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x13;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
									int addrx = Integer.parseInt("0" + tok[2].substring(2), 16);
									
									byte[] b = new byte[4];
									
									b[0] = (byte) 0x13;
									b[1] = (byte) this.validRegisters.get(tok[1]).getValue();
									b[2] = (byte) 0x00;
									b[3] = (byte) addrx;
									this.asmout.write(b);
								}
									
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						}
						break;
					}
					
					case "INB": {
						assertArgs(tok.length == 2, "INB");
						if(Pattern.matches("^[A-Z]$", tok[1])) {
							if(this.validRegisters.containsKey(tok[1])) {
								byte[] b = new byte[4];
								
								b[0] = (byte) 0x14;
								b[1] = (byte) 0x00;
								b[2] = (byte) 0x00;
								b[3] = (byte) this.validRegisters.get(tok[1]).getValue();
								this.asmout.write(b);
							} else {
								Main.logger.log("Invalid Register " + tok[1]);
								System.exit(0);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							int addrx = Integer.parseInt(tok[1].substring(2, 4), 16);
							int addry = Integer.parseInt(tok[1].substring(4), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x14;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							int addrx = Integer.parseInt("0" + tok[1].substring(2, 3), 16);
							int addry = Integer.parseInt(tok[1].substring(3), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x14;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							int addrx = 0x00;
							int addry = Integer.parseInt(tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x14;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							int addrx = 0x00;
							int addry = Integer.parseInt("0" + tok[1].substring(2), 16);
							
							byte[] b = new byte[4];
							
							b[0] = (byte) 0x14;
							b[1] = (byte) 0x00;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						}
						break;
					}
					
					case "OUTB": {
						assertArgs(tok.length == 3, "OUTB");
						int addrx = 0x00;
						int addry = 0x00;
						int addra = 0x00;
						if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[1])) {
							addra = Integer.parseInt(tok[1].substring(4), 16);
							
							
							
							if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
								addrx = Integer.parseInt(tok[2].substring(2,4), 16);
								addry = Integer.parseInt(tok[2].substring(4), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
								addrx = Integer.parseInt("0" + tok[2].substring(2,3), 16);
								addry = Integer.parseInt(tok[2].substring(3), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt(tok[2].substring(2), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt("0" + tok[2].substring(2), 16);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[1])) {
							addra = Integer.parseInt(tok[1].substring(3), 16);
							
							
							
							if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
								addrx = Integer.parseInt(tok[2].substring(2,4), 16);
								addry = Integer.parseInt(tok[2].substring(4), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
								addrx = Integer.parseInt("0" + tok[2].substring(2,3), 16);
								addry = Integer.parseInt(tok[2].substring(3), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt(tok[2].substring(2), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt("0" + tok[2].substring(2), 16);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[1])) {
							addra = Integer.parseInt(tok[1].substring(2), 16);
							
							
							
							if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
								addrx = Integer.parseInt(tok[2].substring(2,4), 16);
								addry = Integer.parseInt(tok[2].substring(4), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
								addrx = Integer.parseInt("0" + tok[2].substring(2,3), 16);
								addry = Integer.parseInt(tok[2].substring(3), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt(tok[2].substring(2), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt("0" + tok[2].substring(2), 16);
							}
						} else if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[1])) {
							addra = Integer.parseInt("0" + tok[1].substring(2), 16);
							
							
							
							if(Pattern.matches("^0x[0-9A-Fa-f]{4}$", tok[2])) {
								addrx = Integer.parseInt(tok[2].substring(2,4), 16);
								addry = Integer.parseInt(tok[2].substring(4), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{3}$", tok[2])) {
								addrx = Integer.parseInt("0" + tok[2].substring(2,3), 16);
								addry = Integer.parseInt(tok[2].substring(3), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{2}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt(tok[2].substring(2), 16);
							}
							if(Pattern.matches("^0x[0-9A-Fa-f]{1}$", tok[2])) {
								addrx = 0x00;
								addry = Integer.parseInt("0" + tok[2].substring(2), 16);
							}
						}
						
						if(addrx != 0x00 && addry != 0x00 && addra != 0x00) {
							byte[] b = new byte[4];
						
							b[0] = (byte) 0x15;
							b[1] = (byte) addra;
							b[2] = (byte) addrx;
							b[3] = (byte) addry;
							this.asmout.write(b);
						}
						break;
					}
				
					case "HALT": {
						assertArgs(tok.length == 1, "HALT");
						byte[] b = new byte[4];
						b[0] = (byte) 0xFF;
						b[1] = (byte) 0x00;
						b[2] = (byte) 0x00;
						b[3] = (byte) 0x0F;
						this.asmout.write(b);
						break;
					}
				
					default:
						break;
				}
			}
			byte[] b = new byte[4];
			b[0] = (byte) 0xFF;
			b[1] = (byte) 0x00;
			b[2] = (byte) 0x00;
			b[3] = (byte) 0x0F;
			this.asmout.write(b);
		} catch(IOException ex) { Main.logger.log("IO Exception occured during assembly process"); System.exit(1); }
	}

	/**
	 * I don't know, what does it do?
	 */
	private void assertArgs(boolean conditional, String name) {
		if(!conditional) {
			System.out.println("Invalid arguments for opcode " + name + ", try looking at the documentation.");
			System.exit(1);
		} else {
			// you did very well big man
		}
	}
	
	/**
	 * Parse labels in assembly files
	 */
	private void parseLabels() {
		int linenum = 0;
		for (String line : this.lines) {
			
			line = line.trim();
			
			if(line.equals("")) {
				continue;
			}
			
			if(line.startsWith(";")) { // comment
				// ignore comments
				continue;
			} else if (line.startsWith("DB")) {
				String[] tok = line.split("[, ]+");
				
				String data = String.join(" ", tok); 

				if(Pattern.matches("^\"[A-Za-z0-9_$~`!@#$%^&*()-+={}\\[\\] |\\\\:;\\\"'<,>.?/]*\"$", data.substring(3))) {
					data = data.substring(4, data.length() - 1);
					System.out.println(data);
					char[] dataChars = data.toCharArray();
					linenum += dataChars.length + 1;
				} else {
					linenum += 1;
				}
			} else if (line.startsWith(".") && (line.length() > 2) && line.endsWith(":")) { //label
				this.labels.put(line.substring(1, line.length() - 1), new Address((char)(0x000400 + linenum*4)));
				linenum += 1;
			} else {
				linenum += 1;
			}
		}
	}
}
