package com.neuronrobotics.commercial.oggie;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
	public DualPressControl(PressHardware hw, SinglePressControl press1, SinglePressControl press2){
		this.press1 = press1;
		this.press2 = press2;
		setLayout(new MigLayout());
		add(useDualPress,"wrap");
		table = new TableDisplay("Dual Press");
		add(table,"wrap");
		useDualPress.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getPress1().setPressControlEnabled(!useDualPress.isSelected());
				getPress2().setPressControlEnabled(!useDualPress.isSelected());
			}
		});
	}
	public SinglePressControl getPress1() {
		return press1;
	}
	public SinglePressControl getPress2() {
		return press2;
	}
}
