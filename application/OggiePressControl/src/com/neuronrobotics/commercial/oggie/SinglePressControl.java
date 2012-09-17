package com.neuronrobotics.commercial.oggie;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SinglePressControl extends JPanel implements IPressControler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3805068601470133493L;
	
	private JLabel enabledDisplay = new JLabel("Single Press Enabled");

	public SinglePressControl(PressHardware hw,int pressIndex, DualPressControl both){
		add(enabledDisplay);
	}
	
	public void setPressControlEnabled(boolean b){
		if(b){
			enabledDisplay.setText("Single Press Enabled");
		}else{
			enabledDisplay.setText("Single Press Disabled");
		}
	}
}
