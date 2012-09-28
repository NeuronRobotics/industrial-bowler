package com.neuronrobotics.commercial.oggie;

import javax.swing.JLabel;
import javax.swing.JPanel;

import Jama.Matrix;

import net.miginfocom.swing.MigLayout;

public class SinglePressControl extends JPanel implements IPressControler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3805068601470133493L;
	
	private JLabel enabledDisplay = new JLabel("Single Press Enabled");
	private TableDisplay table;

	private final int pressIndex;

	private final PressHardware hw;
	public SinglePressControl(PressHardware hw,int pressIndex, DualPressControl both){
		this.hw = hw;
		this.pressIndex = pressIndex;
		setLayout(new MigLayout());
		add(enabledDisplay,"wrap");
		table = new TableDisplay("Press #"+pressIndex, this);
		add(table,"wrap");
		setPressControlEnabled(true);
	}
	
	public void setPressControlEnabled(boolean b){
		table.setEnabled(b);
		if(b){
			enabledDisplay.setText("Press #"+pressIndex+ " Enabled");
		}else{
			enabledDisplay.setText("Press #"+pressIndex+ " Disabled");
		}
	}

	@Override
	public void onCycleStart(Matrix m, double pressure) {
		abortCycle();
		hw.onCycleStart(pressIndex, m, pressure); 
	}

	@Override
	public void abortCycle() {
		hw.abortCycle(pressIndex);
	}

	@Override
	public 	double getCurrentPressure() {
		return hw.getPressure(pressIndex);
	}
}