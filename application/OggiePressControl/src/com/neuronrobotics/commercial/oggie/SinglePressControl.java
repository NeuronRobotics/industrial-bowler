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

	private final int pressIndex;
	public SinglePressControl(PressHardware hw,int pressIndex, DualPressControl both){
		this.pressIndex = pressIndex;
		setLayout(new MigLayout());
		add(enabledDisplay,"wrap");
		table = new TableDisplay("Press #"+pressIndex);
		add(table,"wrap");
		setPressControlEnabled(true);
	}
	
	public void setPressControlEnabled(boolean b){
		if(b){
			enabledDisplay.setText("Press #"+pressIndex+ " Enabled");
		}else{
			enabledDisplay.setText("Press #"+pressIndex+ " Disabled");
		}
	}
}
