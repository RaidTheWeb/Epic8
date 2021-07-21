package tech.raidtheweb.epicsys.registerdump;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import tech.raidtheweb.epicsys.cpu.CPU;

public class RegisterDump extends JFrame implements Dump, ActionListener {

	private static final long serialVersionUID = 1L;
	private RegisterDumpPanel panel;
	
	private CPU c;
	
	public RegisterDump(CPU c) {
		this.setSize(600, 600);
		//this.setLayout(new BorderLayout());
		
		panel = new RegisterDumpPanel(c);
		
		this.c = c;
		
		//this.add(panel);
		
		this.setContentPane(panel);
		
		JMenuBar jmb = new JMenuBar();
		
		JMenuItem jmiReset = new JMenuItem("Reset");
		
		jmb.add(jmiReset);
		
		this.setJMenuBar(jmb);
		
		this.setBackground(Color.DARK_GRAY);
		
		URL url = ClassLoader.getSystemResource("tech/raidtheweb/epicsys/resources/icon.png");
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		this.setIconImage(img);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	c.setRegisterDumper(new NullDump());
		    }
		});
		
		jmiReset.addActionListener(this);
	}

	@Override
	public void update() {
		panel.repaint();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		
		if(cmd == "Reset") c.reset();
	}
}
