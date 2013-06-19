package com.neuronrobotics.industrial;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

public class AlarmAccount extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3719573762649982776L;

	public AlarmAccount(){
		setName("Alarm Accounts");
		setLayout(new MigLayout("", "[][]", "[][]"));
		
		add(new JLabel("Admin Account"), 		"cell 0 0,growx");
		add(new JTextField("logger.tanury"), 	"cell 1 0,growx");
		add(new JLabel("@gmail.com"), 			"cell 2 0,growx");
		add(new JPasswordField("Secret"), 		"cell 3 0,growx");
		add(new JButton("Connect"), 			"cell 4 0,growx");
		
		for(int i=1;i<11;i++){
			add(new JLabel("To Notify"), 		"cell 0 "+i+",growx");
			add(new JTextField(10), 			"cell 1 "+i+",growx");
			add(new JLabel("@gmail.com"), 		"cell 2 "+i+",growx");
			add(new JPasswordField("Secret"), 	"cell 3 "+i+",growx");
			add(new JButton("Add"), 			"cell 4 "+i+",growx");
		}
		
	}

}
