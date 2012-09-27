package com.neuronrobotics.commercial.oggie;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class SinglePressControl extends JPanel implements IPressControler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3805068601470133493L;
	
	private JLabel enabledDisplay = new JLabel("Single Press Enabled");
	private TableDisplay table;
	public SinglePressControl(PressHardware hw,int pressIndex, DualPressControl both){
		setLayout(new MigLayout());
		add(enabledDisplay,"wrap");
		table = new TableDisplay("Press #"+pressIndex);
		add(table,"wrap");
	}
	
	public void setPressControlEnabled(boolean b){
		if(b){
			enabledDisplay.setText("Single Press Enabled");
		}else{
			enabledDisplay.setText("Single Press Disabled");
		}
	}
}
