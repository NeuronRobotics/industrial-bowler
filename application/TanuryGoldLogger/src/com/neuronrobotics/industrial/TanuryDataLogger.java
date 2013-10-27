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
import java.util.ArrayList;
import java.util.Date;

import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;

public class TanuryDataLogger {
	private String dataHeader = "Date,Timestamp,Total Troy Oz,Bath Name,Bath Total,Raw Current,TYPE,<Alarm Threshhold>\n";

	private String subDir = "";
	
	private ArrayList<String> filesAccessed = new ArrayList<String>(); 
	public TanuryDataLogger(String string) {
		subDir=string;
		File folder = new File(getRoot());
		File[] listOfFiles = folder.listFiles(); 
		if(listOfFiles!=null)
			if(listOfFiles.length>0)
				for( File f:listOfFiles ){
					filesAccessed.add(f.getAbsolutePath());
				}
	}
	public void onNameChange(String newName) {
		// TODO Auto-generated method stub
		
	}
	public void onValueChange(BathMoniterEvent event, double total) {
		String data = new Date(event.getTimestamp())+","+event.getTimestamp()+","+total+","+event.getBathName()+","+event.getScaledTotalUsedToday()+","+event.getCurrentOzHrRate()+",LOG";
		writeLine(data, event.getBathName(),event.getTimestamp());
	}

	public void onAlarmEvenFire(BathAlarmEvent event) {
		String data = new Date(event.getTimestamp())+","+event.getTimestamp()+","+0+","+event.getBathName()+","+0+","+event.getCurrentOzHrRate()+",ALARM,"+event.getAlarmThreshhold()+"";
		writeLine(data, event.getBathName(),event.getTimestamp());
	}

	
	public static String getDate(long ts){
		Timestamp t = new Timestamp(ts);
		return t.toString().split("\\ ")[0];
	}
	
	private String getRoot(){
		return System.getProperty("user.home")+"/Tanury/"+subDir+"/";
	}
	
	public String getFileName(String bath, long timestamp){
		String tmp = getRoot()+"/Tanury-Logs-"+getDate(timestamp)+"-"+bath+".csv";
		for (String s: filesAccessed)
			if(tmp.contains(s))
				return s;
		filesAccessed.add(tmp);
		return tmp;
	}
	
	private void writeLine(String data, String bathName, long timestamp){
		
		File file = new File(getFileName(bathName,timestamp));
		boolean header = false;
		if(!file.exists()){
			File tmp = new File(file.getParent());
			if(!tmp.exists())
				tmp.mkdirs();
			try {
				file.createNewFile();
				header = true;
				
			} catch (IOException e) {
				System.err.println(getFileName(bathName,timestamp));
				e.printStackTrace();
			}
			
		}
		String filenameLocal = getFileName(bathName,timestamp);
		if(!logContainsTimestamp(timestamp,filenameLocal)){
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filenameLocal, true)));
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
	
	private boolean logContainsTimestamp(long timestamp,String filename) {
		// TODO Auto-generated method stub
		BufferedReader br = null;
		BathMoniterEvent ev= new BathMoniterEvent();
		try {
			String sCurrentLine;
			br = new BufferedReader(new FileReader(filename));

			br.readLine();// read the formatting data at the top
			while ((sCurrentLine = br.readLine()) != null) {
				ev.setData(sCurrentLine);
				if(ev.getTimestamp() == timestamp)
					return true;
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
		return false;
	}
	public int getNumberOfFiles(){
		return filesAccessed.size();
	}
	
	public int getNumberOfLogLines(String bathName, int fileIndex){
		int i=0;
		BufferedReader br = null;
		 
		try {
 
			@SuppressWarnings("unused")
			String sCurrentLine;
 
			String filename;
			if(fileIndex<0)
				filename=getFileName(bathName,System.currentTimeMillis());
			else
				filename = filesAccessed.get(fileIndex);
			br = new BufferedReader(new FileReader(filename));

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
	
	public BathMoniterEvent getLogLine(int lineNumber,String bathName,int fileIndex){
		int i=0;
		BufferedReader br = null;
		try {
			 
			String sCurrentLine;
			String filename;
			if(fileIndex<0)
				filename=getFileName(bathName,System.currentTimeMillis());
			else
				filename = filesAccessed.get(fileIndex);
			br = new BufferedReader(new FileReader(filename));
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
		File dirToClear = new File(getFileName(bathName,System.currentTimeMillis()));
		dirToClear.delete();
	}
	
	public void clearData(String bathName) {
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
