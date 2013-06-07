package com.neuronrobotics.industrial;

import javax.swing.JPanel;
import javax.swing.BoxLayout;

public class BathMoniter extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7734077989616188631L;
	private static int index=1;
	public BathMoniter(){
		setName("Bath Moniter "+ index++);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
	}

}
