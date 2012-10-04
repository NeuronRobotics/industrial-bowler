package com.neuronrobotics.commercial.oggie;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;

import net.miginfocom.swing.MigLayout;

public class PressConfigurationController extends JPanel implements IPressHardwareListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4955802212820308687L;
	private final PressHardware hw;
	private PressGraph [] graphs = new PressGraph[2];
	
	public PressConfigurationController(PressHardware hw){
		this.hw = hw;
		
		setBorder(BorderFactory.createLoweredBevelBorder());
		JPanel graphPanel = new JPanel(new MigLayout());
		graphs[0]=new PressGraph("Press #0 data");
		graphs[1]=new PressGraph("Press #1 data");
		
		graphPanel.add(new JLabel("General Press Controls"),"wrap");
		graphPanel.add(graphs[0],"wrap");
		graphPanel.add(graphs[1],"wrap");
		
		JPanel controlPanel = new JPanel(new MigLayout());
		
		
		
		add(graphPanel);
		add(controlPanel);
		
		hw.addPressHardwareListener(this);
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		graphs[i].onCycleStart(i, config);
	}

	@Override
	public void onAbortCycle(int i) {
		graphs[i].onAbortCycle(i);
	}

	@Override
	public void onPressureChange(int i, double pressureTons) {
		graphs[i].onPressureChange(i, pressureTons);
	}

	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		graphs[i].onTempretureChange(i, degreesFarenhight);
	}

	@Override
	public void onCycleIndexUpdate(int currentTableIndex, double currentTableTime, double timeRemaining, int press, double newTargetTemp) {
		graphs[press].onCycleIndexUpdate(currentTableIndex, currentTableTime, timeRemaining, press, newTargetTemp);
	}

}
