package tech.raidtheweb.epicsys.registerdump;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import tech.raidtheweb.epicsys.cpu.CPU;

public class RegisterDumpPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private CPU c;

	public RegisterDumpPanel(CPU c) {
		this.c = c;
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		g.setColor(Color.WHITE);
		g.drawString(String.format("D: 0x%04x", (short)c.getRegisterD()), 2, 12);
		g.drawString(String.format("I: 0x%04x", (short)c.getRegisterI()), 2, 12 * 2);
		g.drawString(String.format("X: 0x%04x", (short)c.getRegisterX()), 2, 12 * 3);
		g.drawString(String.format("Y: 0x%04x", (short)c.getRegisterY()), 2, 12 * 4);
		g.drawString(String.format("R: 0x%04x", (short)c.getRegisterR()), 2, 12 * 5);
		g.drawString(String.format("Z: 0x%04x", (short)c.getRegisterZ()), 2, 12 * 6);
		g.drawString(String.format("P: 0x%04x", (short)c.getRegisterP()), 2, 12 * 7);
		g.drawString(String.format("F: 0x%04x", (short)c.getRegisterF()), 2, 12 * 8);
		g.drawString(String.format("E: 0x%04x", (short)c.getRegisterE()), 2, 12 * 9);
		g.drawString(String.format("C: 0x%04x", (short)c.getRegisterC()), 2, 12 * 10);
		g.drawString(String.format("A: 0x%04x", (short)c.getRegisterA()), 2, 12 * 11);
		g.drawString(String.format("K: 0x%04x", (short)c.getRegisterK()), 2, 12 * 12);
		g.drawString(String.format("S: 0x%04x", (short)c.getRegisterS()), 2, 12 * 13);
		g.drawString("Current PC: " + c.getPc(), 2, 12 * 14);
		
	}
}
