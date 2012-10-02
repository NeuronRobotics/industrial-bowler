package com.neuronrobotics.commercial.oggie;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.ui.ConnectionDialog;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class OggiePress {
	
	private JFrame frame = new JFrame();
	private JTabbedPane tabs = new JTabbedPane();
	private DyIO dyio;
	private PressHardware hw;
	private SinglePressControl press1;
	private SinglePressControl press2;
	private DualPressControl   both;
	private PressConfigurationController config;
	private OggiePressConfigFileManager fm;
	public OggiePress() throws IOException{	
		DyIO.disableFWCheck();
		dyio=new DyIO();
		dyio.enableDebug();
		if (!ConnectionDialog.getBowlerDevice(dyio)){
			//System.exit(1);
			System.out.println("Running in virtual mode");
			dyio=null;
		}
		hw = new PressHardware(dyio);
		

		frame.addWindowListener(new WindowAdapter() {
			//On the close button
			public void windowClosing(WindowEvent evt) {
				Object[] options = {"Yes, exit and shut down",
	                    "No, stay active"};
				int n = JOptionPane.showOptionDialog(frame,
				    "Are you sure you wan to shut down?",
				    "Verify shutdown",
				    JOptionPane.YES_NO_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE,
				    null,
				    options,
				    options[1]);
				if(n==1){
					new Thread(){
						public void run(){
							ThreadUtil.wait(50);
							frame.setVisible(true);
						}
					}.start();
					
					return;
				}
				if(dyio !=null)
					dyio.disconnect();
				fm.save();
				Log.debug("Closing clean");
			    System.exit(0);
			}
		});
		press1 = new SinglePressControl(hw, 0);
		press2= new SinglePressControl(hw, 1);
		both= new DualPressControl(hw,press1,press2);
		fm = new OggiePressConfigFileManager(press1, press2, both);
		
		config=new PressConfigurationController(hw);
		tabs.addTab("Home", config);
		tabs.addTab("Press #1", press1);
		tabs.addTab("Press #2", press2);
		tabs.addTab("Dual Press", both);
		frame.setContentPane(tabs);
		frame.setSize(800, 600);
		frame.setVisible(true);
	}
	
	
}
