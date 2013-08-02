package com.neuronrobotics.industrial.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class DeviceConfiguration {
	
	
	private String filename = System.getProperty("user.home")+"/Tanury/TanuryData.xml";
	
	private dataStorage data = new dataStorage();
	
	public DeviceConfiguration(){
		XStream xstream = new XStream();
		xstream.alias("configuration", dataStorage.class);
		//System.out.println(getXML());
		
		File file = new File(filename);
		if(!file.exists()){
			System.out.println("File does not exist");
			File tmp = new File(file.getParent());
			if(!tmp.exists())
				tmp.mkdirs();
			try {
				file.createNewFile();
				
				writeFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}else{
			file = new File(filename);
			data = (dataStorage)xstream.fromXML(fileContents(file));
		}
	}
	
	private String fileContents(File file){
		BufferedReader br = null;
		String back="";
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(file));
 
			while ((sCurrentLine = br.readLine()) != null) {
				back+=sCurrentLine+"\n";
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
		//System.out.println("Read back: "+file.getAbsolutePath());
		//System.out.println(back);
		return back;
	}
	private void writeFile() {
		String xml = getXML();
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, false)));
			//System.out.println("Writing to "+filename);
			//System.out.println(xml);
			out.println(xml);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getXML(){
		XStream xstream = new XStream();
		xstream.alias("configuration", dataStorage.class);
		return xstream.toXML(data);
	}

	private class dataStorage{
		private double scaleFactor = .01;
		private double dailyTotal = 0;
		public double getScaleFactor() {
			return scaleFactor;
		}

		public void setScaleFactor(double scaleFactor) {
			this.scaleFactor = scaleFactor;
		}

		public double getDailyTotal() {
			return dailyTotal;
		}

		public void setDailyTotal(double dailyTotal) {
			this.dailyTotal = dailyTotal;
		}
		
	}
	
	public double getDailyTotal() {
		return data.getDailyTotal();
	}

	public void setDailyTotal(double dailyTotal) {
		data.setDailyTotal(dailyTotal);
		writeFile();
	}
	
	public double getScaleFactor() {
		
		return data.getScaleFactor();
	}

	public void setScaleFactor(double scaleFactor) {
		data.setScaleFactor(scaleFactor);
		writeFile();
	}
	
}
