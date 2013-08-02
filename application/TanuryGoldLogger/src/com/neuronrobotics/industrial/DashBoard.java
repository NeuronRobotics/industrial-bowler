package com.neuronrobotics.industrial;

import javax.swing.JPanel;
import javax.swing.JTable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import net.miginfocom.swing.MigLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;


import com.neuronrobotics.industrial.device.BathMoniterEvent;

public class DashBoard extends JPanel implements IBathMoniterUpdateListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3599590566590620037L;
	private JTable table;
	private JTextField textField;
	private JTextField textField_1;
	private ArrayList<BathMoniter> list;
	private String dataHeader = "Timestamp,Total Troy Oz,Bath Name,Bath Total,Raw Current\n";

	public DashBoard(ArrayList<BathMoniter> list) {
		this.list = list;
		setName("Dash Board");
		setLayout(new MigLayout("", "[grow][]", "[grow][][]"));
		
		table = new JTable(list.size(),2);
		int i=0;
		for (BathMoniter b:list){
			table.setValueAt("<value>", i++, 1);			
		}
		updateTableData();
		
		add(table, "flowy,cell 0 0,grow");
		
		JLabel lblNewLabel = new JLabel("Log File");
		add(lblNewLabel, "cell 0 1,alignx trailing");
		
		textField = new JTextField();
		textField.setText("<value>");

		textField.setText(getFileName("<bath>"));
		add(textField, "cell 0 1,growx");
		textField.setColumns(10);
		
		JLabel lblTroyOzRate = new JLabel("Troy Oz. Rate");
		add(lblTroyOzRate, "cell 0 2,alignx trailing");
		
		textField_1 = new JTextField();
		
		
		add(textField_1, "cell 0 2,growx");
		textField_1.setColumns(10);
	
	}
	
	private String getDate(){
		Timestamp t = new Timestamp(System.currentTimeMillis());
		return t.toString().split("\\ ")[0];
	}

	public void updateTableData() {
		int i=0;
		for (BathMoniter b:list){
			table.setValueAt(b.getName(), i++, 0);
		}
	}

	@Override
	public void onNameChange(String newName) {
		
	}

	@Override
	public void onValueChange(BathMoniterEvent event) {
		double total=0;
		for(int i=0;i<list.size();i++){
			if(table.getValueAt( i, 0).toString().contains(event.getBathName())){
				table.setValueAt(new Double(event.getTotalUsedToday()).toString(), i, 1);
			}
			total+=new Double(table.getValueAt( i, 1).toString());
		}
		textField_1.setText(new Double(total).toString());
		File file = new File(getFileName(event.getBathName()));
		//"Date,Timestamp,Total Troy Oz,Bath Name,Bath Total,Raw Current"
		String data = new Date()+","+event.getTimestamp()+","+total+","+event.getBathName()+","+event.getTotalUsedToday()+","+event.getCurrentOzHrRate()+"\n";
		boolean header = false;
		if(!file.exists()){
			File tmp = new File(file.getParent());
			if(!tmp.exists())
				tmp.mkdirs();
			try {
				file.createNewFile();
				header = true;
				
			} catch (IOException e) {
				System.err.println(getFileName(event.getBathName()));
				e.printStackTrace();
			}
			
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(getFileName(event.getBathName()), true)));
			if(header){
				out.println(dataHeader+data);
			}else
				out.println(data);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onAlarmEvenFire(String bathName, long timestamp,double currentOzHrRate, double alarmThreshhold) {
		// TODO Auto-generated method stub
		
	}
	
	private String getFileName(String bath){
		return System.getProperty("user.home")+"/Tanury/"+getDate()+"/"+bath+"/Tanury-Logs-"+getDate()+"-"+bath+".csv";
	}

	@Override
	public void onClearData() {
		textField.setText(getFileName("<bath>"));
	}
}
