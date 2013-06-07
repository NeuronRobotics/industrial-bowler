package com.neuronrobotics.industrial;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import org.eclipse.wb.swing.FocusTraversalOnArray;
import java.awt.Component;
import java.security.AllPermission;
import java.util.ArrayList;

import net.miginfocom.swing.MigLayout;
import javax.swing.BoxLayout;

public class MainWindow {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 590, 418);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		loadTabs(tabbedPane);
		frame.getContentPane().add(tabbedPane);
		
		

		frame.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane}));
		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane, frame.getContentPane()}));
	}
	
	private void loadTabs(JTabbedPane tabbedPane){
		DashBoard dashBoard = new DashBoard();
		AlarmAccount alarm = new AlarmAccount();
		
		ArrayList<BathMoniter> list = BathMoniterFactory.getBathMoniterList();
		
		if(list.size()==0){
			list.add(new BathMoniter());
			list.add(new BathMoniter());
		}
		
		tabbedPane.addTab("Dash Board",dashBoard);
		for(BathMoniter b:list){
			tabbedPane.addTab(b.getName(), null, b, null);
		}
		tabbedPane.addTab("Allarm Notifications",alarm );
		dashBoard.setLayout(new BoxLayout(dashBoard, BoxLayout.X_AXIS));
		
	}

}
