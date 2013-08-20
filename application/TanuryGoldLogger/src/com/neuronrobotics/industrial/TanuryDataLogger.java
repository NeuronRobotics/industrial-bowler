package com.neuronrobotics.industrial;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;

public class TanuryDataLogger {
	private String dataHeader = "Date,Timestamp,Total Troy Oz,Bath Name,Bath Total,Raw Current,TYPE,<Alarm Threshhold>\n";


	public void onNameChange(String newName) {
		// TODO Auto-generated method stub
		
	}
	public void onValueChange(BathMoniterEvent event, double total) {
		String data = new Date()+","+event.getTimestamp()+","+total+","+event.getBathName()+","+event.getScaledTotalUsedToday()+","+event.getCurrentOzHrRate()+",LOG\n";
		writeLine(data, event.getBathName());
	}

	public void onAlarmEvenFire(BathAlarmEvent event) {
		String data = new Date()+","+event.getTimestamp()+","+0+","+event.getBathName()+","+0+","+event.getCurrentOzHrRate()+",ALARM,"+event.getAlarmThreshhold()+"\n";
		writeLine(data, event.getBathName());
	}
	public void onClearData() {
		// TODO Auto-generated method stub
		
	}
	
	private String getDate(){
		Timestamp t = new Timestamp(System.currentTimeMillis());
		return t.toString().split("\\ ")[0];
	}
	
	public String getFileName(String bath){
		return System.getProperty("user.home")+"/Tanury/"+getDate()+"/"+bath+"/Tanury-Logs-"+getDate()+"-"+bath+".csv";
	}
	
	private void writeLine(String data, String bathName){
		
		File file = new File(getFileName(bathName));
		boolean header = false;
		if(!file.exists()){
			File tmp = new File(file.getParent());
			if(!tmp.exists())
				tmp.mkdirs();
			try {
				file.createNewFile();
				header = true;
				
			} catch (IOException e) {
				System.err.println(getFileName(bathName));
				e.printStackTrace();
			}
			
		}
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(getFileName(bathName), true)));
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

}
