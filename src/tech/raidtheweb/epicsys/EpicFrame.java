package tech.raidtheweb.epicsys;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import tech.raidtheweb.epicsys.cpu.CPU;
import tech.raidtheweb.epicsys.hexdump.JHexView;
import tech.raidtheweb.epicsys.hexdump.JHexView.DefinitionStatus;
import tech.raidtheweb.epicsys.hexdump.SimpleDataProvider;
import tech.raidtheweb.epicsys.registerdump.RegisterDump;

public class EpicFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private EpicPanel panel;
	private CPU c;
	public JHexView hexView;
	
	public EpicFrame(CPU c) {
		URL url = ClassLoader.getSystemResource("tech/raidtheweb/epicsys/resources/icon.png");
		
		hexView = new JHexView();
		
		setPreferredSize(new Dimension(640, 320));
		pack();
		setPreferredSize(new Dimension(640 + getInsets().left + getInsets().right, 320 + getInsets().top + getInsets().bottom));
		this.c = c;
		panel = new EpicPanel(c);
		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBackground(Color.BLACK);
		
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(url);
		this.setIconImage(img);
		
		this.setTitle("Epic8 System Emulator");
		this.setResizable(false);
		
		JMenuBar menuBar = new JMenuBar();
		
		/**
		 * File
		 */
		JMenu jmFile = new JMenu("File");
		JMenuItem jmiOpen = new JMenuItem("Open");
		jmFile.add(jmiOpen);
		
		JMenuItem jmiExit = new JMenuItem("Exit");
		jmFile.add(jmiExit);
		menuBar.add(jmFile);
		
		/**
		 * Emulator
		 */
		JMenu jmEmulator = new JMenu("Emulator");
		JMenuItem jmiReset = new JMenuItem("Reset");
		jmEmulator.add(jmiReset);
		
		JMenuItem jmiPause = new JMenuItem("Pause");
		jmEmulator.add(jmiPause);
		
		JMenuItem jmiResume = new JMenuItem("Resume");
		jmEmulator.add(jmiResume);
		
		JMenuItem jmiSaveState = new JMenuItem("Save State");
		jmEmulator.add(jmiSaveState);
		
		JMenuItem jmiLoadState = new JMenuItem("Load State");
		jmEmulator.add(jmiLoadState);
		
		/**
		 * Emulator > Debug
		 */
		JMenu jmiDebug = new JMenu("Debug");
		JMenuItem jmiMemory = new JMenuItem("Dump Memory");
		jmiDebug.add(jmiMemory);
		JMenuItem jmiRegister = new JMenuItem("Dump Registers");
		jmiDebug.add(jmiRegister);
		
		jmEmulator.add(jmiDebug);
		
		/**
		 * Emulator > Execution Speed
		 */
		JMenu jmiSpeed = new JMenu("Execution Speed");
		JMenuItem jmi1x = new JMenuItem("1x Speed");
		jmiSpeed.add(jmi1x);
		JMenuItem jmi05x = new JMenuItem("0.5x Speed");
		jmiSpeed.add(jmi05x);
		JMenuItem jmi01x = new JMenuItem("0.1x Speed");
		jmiSpeed.add(jmi01x);
		
		jmEmulator.add(jmiSpeed);
		
		menuBar.add(jmEmulator);
		
		/* FILE */
		jmiOpen.addActionListener(this);
		jmiExit.addActionListener(this);
		
		/* EMULATOR */
		jmiReset.addActionListener(this);
		jmiPause.addActionListener(this);
		jmiResume.addActionListener(this);
		jmiSaveState.addActionListener(this);
		jmiLoadState.addActionListener(this);
		
		/* EMULATOR > DEBUG */
		jmiMemory.addActionListener(this);
		jmiRegister.addActionListener(this);
		
		/* EMULATOR > SPEED */
		jmi1x.addActionListener(this);
		jmi05x.addActionListener(this);
		jmi01x.addActionListener(this);
		
		this.setJMenuBar(menuBar);
		
		
		setVisible(true);
	}
	
	public void restartApplicationWithROM(String ROM) {
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		File currentJar = null;
		try {
			currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	
		if(!currentJar.getName().endsWith(".jar")) {
			JOptionPane.showMessageDialog(this, "Cannot preform an open as the emulator is not running as a .jar file!");
			return;
		}
	
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());
		command.add(ROM);
	
		final ProcessBuilder builder = new ProcessBuilder(command);
		try {
			builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	  	System.exit(0);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		String comStr = ae.getActionCommand();
		
		switch(comStr) {
			/* FILE */
			case "Open": {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("ROM FILES", "bin", "rom", "e8");
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					System.out.println(selectedFile.getAbsolutePath());
					restartApplicationWithROM(selectedFile.getAbsolutePath());
				}
				
				break;
			}
			
			case "Exit": {
				System.exit(0);
				break;
			}

			/* EMULATOR */
			case "Reset": {
				c.reset();
				break;
			}
			
			case "Pause": {
				Main.paused = true;
				break;
			}
			
			case "Resume": {
				Main.paused = false;
				break;
			}
			
			case "Save State": {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setDialogTitle("Specify a file to save emulator state");
				int userSelection = fileChooser.showSaveDialog(this);
				 
				if (userSelection == JFileChooser.APPROVE_OPTION) {
				    File fileToSave = fileChooser.getSelectedFile();
				    c.saveState(fileToSave.getAbsolutePath());
				}
				break;
			}
			
			case "Load State": {
				JFileChooser fileChooser = new JFileChooser();
				int result = fileChooser.showOpenDialog(this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					c.restoreState(selectedFile.getAbsolutePath());
				}
				
				break;
			}
			
			/* EMULATOR > DEBUG */
			case "Dump Memory": {
				JFrame frame = new JFrame();
				frame.setLayout(new BorderLayout());
				frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				frame.setSize(600, 600);
				frame.setTitle("Memory Dump - Hex Viewer");
				
				URL url = ClassLoader.getSystemResource("tech/raidtheweb/epicsys/resources/icon.png");
				
				Toolkit kit = Toolkit.getDefaultToolkit();
				Image img = kit.createImage(url);
				frame.setIconImage(img);
				
				frame.add(hexView);
				
				byte[] data = new String(c.getMemory()).getBytes();
				
				hexView.setData(new SimpleDataProvider(data, data.length - 0x400));
				
				hexView.setDefinitionStatus(DefinitionStatus.DEFINED);
				hexView.setEnabled(true);
				
				frame.setVisible(true);
				break;
			}
			
			case "Dump Registers": {
				RegisterDump frame = new RegisterDump(c);
				
				c.setRegisterDumper(frame);
				
				frame.setVisible(true);
				break;
			}
			
			/* EMULATOR > SPEED */
			case "1x Speed": {
				Main.speed = 1;
				break;
			}
			
			case "0.5x Speed": {
				Main.speed = 8;
				break;
			}
			
			case "0.1x Speed": {
				Main.speed = 16;
				break;
			}
			
		}
	}
}
