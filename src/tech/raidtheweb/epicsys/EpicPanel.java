package tech.raidtheweb.epicsys;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import tech.raidtheweb.epicsys.cpu.CPU;

public class EpicPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private CPU cpu;
	private static int x;
	private static int y;
	
	public EpicPanel(CPU c) {
		this.cpu = c;
		x = 2;
		y = 12;
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		char[] buffer = cpu.getDisplayBuffer();
		for(int i = 0; i < buffer.length; i++) {
			if(buffer[i] == 0x20) {
				x += 8;
			} else if (buffer[i] == 0x0A) {
				x = 2;
				y += 12;
				if(y >= 252) {
					y = 12;
					x = 2;
					g.setColor(Color.BLACK);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.WHITE);
				}
			} else {
				g.drawString(String.valueOf(buffer[i]), x, y);
				int width = g.getFontMetrics().stringWidth(String.valueOf(buffer[i]));
				x += width;
			}
			if(x >= 600) {
				y += 12;
				x = 2;
			}
			
		}
		x = 2;
		y = 12;
	}
}
