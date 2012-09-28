package com.neuronrobotics.commercial.oggie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import Jama.Matrix;

import net.miginfocom.swing.MigLayout;

public class DualPressControl extends JPanel implements IPressControler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5974837327355020388L;
	JCheckBox useDualPress = new JCheckBox("Use Dual Press Configuration");
	private final SinglePressControl press1;
	private final SinglePressControl press2;
	private TableDisplay table;
	private final PressHardware hw;
	public DualPressControl(PressHardware hw, SinglePressControl press1, SinglePressControl press2){
		this.hw = hw;
		this.press1 = press1;
		this.press2 = press2;
		setLayout(new MigLayout());
		add(useDualPress,"wrap");
		table = new TableDisplay("Dual Press", this);
		add(table,"wrap");
		useDualPress.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPress1().setPressControlEnabled(!useDualPress.isSelected());
				getPress2().setPressControlEnabled(!useDualPress.isSelected());
				table.setEnabled(useDualPress.isSelected());
			}
		});
		table.setEnabled(false);
	}
	public SinglePressControl getPress1() {
		return press1;
	}
	public SinglePressControl getPress2() {
		return press2;
	}
	@Override
	public void onCycleStart(CycleConfig config) {
		abortCycle();
		hw.onCycleStart(0, config); 
		hw.onCycleStart(1, config); 
	}
	@Override
	public void abortCycle() {
		hw.abortCycle(0);
		hw.abortCycle(1);
	}
	@Override
	public 	double  getCurrentPressure() {
		return (hw.getPressure(0) + hw.getPressure(1))/2;
	}
}
