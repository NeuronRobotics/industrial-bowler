package com.neuronrobotics.industrial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;

public class TanuryDataLogger {
	private String dataHeader = "Date,Timestamp,Total Troy Oz,Bath Name,Bath Total,Raw Current,TYPE,<Alarm Threshhold>\n";

	private String subDir = "";
	public TanuryDataLogger(String string) {
		subDir=string;
	}
	public void onNameChange(String newName) {
		// TODO Auto-generated method stub
		
	}
	public void onValueChange(BathMoniterEvent event, double total) {
		String data = new Date()+","+event.getTimestamp()+","+total+","+event.getBathName()+","+event.getScaledTotalUsedToday()+","+event.getCurrentOzHrRate()+",LOG";
		writeLine(data, event.getBathName());
	}

	public void onAlarmEvenFire(BathAlarmEvent event) {
		String data = new Date()+","+event.getTimestamp()+","+0+","+event.getBathName()+","+0+","+event.getCurrentOzHrRate()+",ALARM,"+event.getAlarmThreshhold()+"";
		writeLine(data, event.getBathName());
	}
	public void onClearData() {
		// TODO Auto-generated method stub
		
	}
	
	private String getDate(){
		Timestamp t = new Timestamp(System.currentTimeMillis());
		return t.toString().split("\\ ")[0];
	}
	
	private String getRoot(){
		return System.getProperty("user.home")+"/Tanury/"+subDir+"/";
	}
	
	public String getFileName(String bath){
		return getRoot()+getDate()+"/"+bath+"/Tanury-Logs-"+getDate()+"-"+bath+".csv";
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
	
	public int getNumberOfLogLines(String bathName){
		int i=0;
		BufferedReader br = null;
		 
		try {
 
			@SuppressWarnings("unused")
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(getFileName(bathName)));

			br.readLine();// read the formatting data at the top
			while ((sCurrentLine = br.readLine()) != null) {
				i++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return i;
	}
	
	public BathMoniterEvent getLogLine(int lineNumber,String bathName){
		int i=0;
		BufferedReader br = null;
		try {
			 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(getFileName(bathName)));
			br.readLine();// read the formatting data at the top
			while ((sCurrentLine = br.readLine()) != null) {
				if(i==lineNumber){
					try {
						if (br != null)br.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
					return new BathMoniterEvent(sCurrentLine);
				}
				i++;
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return null;
	}
	public void clearTodaysData(String bathName){
		File dirToClear = new File(getFileName(bathName));
		dirToClear.delete();
	}
	public void clearData() {
		File dirToClear = new File(getRoot());
		try {
			delete(dirToClear);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void delete(File f) throws IOException {
	  if (f.isDirectory()) {
	    for (File c : f.listFiles())
	      delete(c);
	  }
	  if (!f.delete())
	    throw new FileNotFoundException("Failed to delete file: " + f);
	}

}
