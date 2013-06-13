package com.neuronrobotics.industrial;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

public class AlarmAccount extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3719573762649982776L;

	public AlarmAccount(){
		setName("Alarm Accounts");
		setLayout(new MigLayout("", "[grow][]", "[grow][]"));
		
	}

}
