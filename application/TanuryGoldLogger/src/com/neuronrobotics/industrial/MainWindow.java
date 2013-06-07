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
	private ArrayList<BathMoniter> list;
	private DashBoard dashBoard;
	private AlarmAccount alarm;
	private JTabbedPane tabbedPane;

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
		frame.setBounds(100, 100, 640, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		loadTabs();
		frame.getContentPane().add(tabbedPane);
		
		

		frame.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane}));
		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane, frame.getContentPane()}));
	}
	
	private void loadTabs(){
		
		
		list = BathMoniterFactory.getBathMoniterList();
		
		if(list.size()==0){
			list.add(new BathMoniter());
			list.add(new BathMoniter());
		}
		dashBoard = new DashBoard(list);
		alarm = new AlarmAccount();

		updateTabData();
	}
	
	public void updateTabData(){
		System.out.println("Setting the tab data");
		tabbedPane.removeAll();
		tabbedPane.addTab("Dash Board",dashBoard);
		for(BathMoniter b:list){
			tabbedPane.addTab(b.getName(), null, b, null);
			b.setMainWindow(this);
		}
		tabbedPane.addTab("Allarm Notifications",alarm );
		dashBoard.updateTableData();
	}

}
