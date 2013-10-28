package com.neuronrobotics.industrial;

import java.awt.EventQueue;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.network.BowlerTCPClient;

import java.awt.Component;
import java.net.InetAddress;
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
    JOptionPane pane = new JOptionPane(null, JOptionPane.ERROR_MESSAGE);
    JDialog dialog = pane.createDialog(frame, "BATH Alarm!!");
    

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					Log.enableDebugPrint(true);
					Log.setMinimumPrintLevel(Log.WARNING);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
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
		frame.setBounds(100, 100, 820, 480);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		ArrayList<InetAddress>  addrs = BowlerTCPClient.getAvailableSockets();
		new Thread(){
			public void run(){
				loadTabs();
				frame.getContentPane().removeAll();
				frame.getContentPane().add(tabbedPane);
				frame.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane}));
				frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{tabbedPane, frame.getContentPane()}));
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		}.start();
		JPanel p = new JPanel(new MigLayout());
		for(InetAddress a:addrs){
			p.add(new JLabel("Found bath at: "+a.getHostAddress()), "wrap");
		}
		p.add(new JLabel("Loading..."), "wrap");
		frame.getContentPane().add(p);
		
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
		tabbedPane.addTab("Alarm Notifications",alarm );
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
	public void onClearData() {
		dashBoard.onClearData();
	}

	@Override
	public void onAlarmEvenFire(BathAlarmEvent ev) {
		dashBoard.onAlarmEvenFire(ev);
		if(dialog.isShowing()){
			System.out.println("Dialog already open");
			return;
		}
		alarm.onAlarmEvenFire(ev);
		pane.setMessage(new String(ev.toString()));
		dialog = pane.createDialog(frame, "BATH Alarm!!");
		dialog.setVisible(true);	
	}

}
