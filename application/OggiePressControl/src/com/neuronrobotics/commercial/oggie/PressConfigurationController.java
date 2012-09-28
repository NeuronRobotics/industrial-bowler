package com.neuronrobotics.commercial.oggie;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class PressConfigurationController extends JPanel implements IPressHardwareListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4955802212820308687L;
	
	public PressConfigurationController(PressHardware hw){
		setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel controlsPanel = new JPanel(new MigLayout());
		
		controlsPanel.add(new JLabel("General Press Controls"));
		
		add(controlsPanel);
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAbortCycle(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPressureChange(int i, double pressureTons) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		// TODO Auto-generated method stub
		
	}

}
