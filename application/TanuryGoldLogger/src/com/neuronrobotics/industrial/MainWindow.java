package com.neuronrobotics.industrial;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.neuronrobotics.industrial.device.BathMoniterEvent;

import java.awt.Component;
import java.security.AllPermission;
import java.util.ArrayList;

import net.miginfocom.swing.MigLayout;
import javax.swing.BoxLayout;

public class MainWindow implements IBathMoniterUpdateListener{

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
					window.frame.setLocationRelativeTo(null);
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

		dashBoard = new DashBoard(list);
		alarm = new AlarmAccount();
		tabbedPane.removeAll();
		tabbedPane.addTab("Dash Board",dashBoard);
		for(BathMoniter b:list){
			tabbedPane.addTab(b.getName(), null, b, null);
			b.setMainWindow(this);
		}
		tabbedPane.addTab("Allarm Notifications",alarm );
		dashBoard.updateTableData();
		updateTabData();
	}
	
	public void updateTabData(){
		int totalTabs = tabbedPane.getTabCount();
		for(int i = 1; i < totalTabs-1; i++){
		   tabbedPane.setTitleAt(i, list.get(i-1).getName());
		}
		dashBoard.updateTableData();
	}

	@Override
	public void onNameChange(String newName) {
		updateTabData();
	}

	@Override
	public void onValueChange(BathMoniterEvent event) {
		dashBoard.onValueChange(event);
	}

	@Override
	public void onAlarmEvenFire(String bathName, long timestamp,
			double currentOzHrRate, double alarmThreshhold) {
		dashBoard.onAlarmEvenFire(bathName, timestamp, currentOzHrRate, alarmThreshhold);
	}

}
