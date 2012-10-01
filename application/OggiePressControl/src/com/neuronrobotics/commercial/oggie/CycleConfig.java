package com.neuronrobotics.commercial.oggie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;

import javax.management.RuntimeErrorException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.neuronrobotics.sdk.addons.kinematics.xml.XmlFactory;
import com.neuronrobotics.sdk.common.Log;

import Jama.Matrix;

public class CycleConfig {
	private double[][] data = {
			{0,210},
			{10,240},
			{20,270},
			{30,290},
			{40,290},
			{60,290},
			{70,290},
			{80,290},
			{90,290},
			{100,290},
			{110,270},
			{120,250},
			{130,230},
			{140,210},
			{150,200},
			{160,200},
			{170,200},
	};
	
	private Matrix timeTemp = new Matrix(data);
	private double pressure = 3.7;
	
	public static final int dataSize = 17;
	
	private File config = null;
	
	public CycleConfig(){
		//use default values
	}
	
	public CycleConfig(File currentConfig) throws FileNotFoundException{
		setConfigFile(currentConfig);
		Document doc =XmlFactory.getAllNodesDocument(new FileInputStream(getConfigFile()));
		NodeList press = doc.getElementsByTagName("OggiePress");

		for (int i = 0; i < press.getLength(); i++) {			
		    Node nNode = press.item(i);
		    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    	Element eElement = (Element)nNode;	    		    
		    	pressure = Double.parseDouble(XmlFactory.getTagValue("pressure",eElement));
		    	String times = XmlFactory.getTagValue("time",eElement);
		    	String temps = XmlFactory.getTagValue("temp",eElement);
		    	setMatrix(times,temps);
		    }else{
		    	Log.info("Not Element Node");
		    }
		}
	}
	
	public CycleConfig(Matrix m,double p){
		if(		m.getArray().length!=dataSize || 
				m.getArray()[0].length!=2	)
			throw new RuntimeException("Invalid matrix size, got "+m.getArray().length+"x"+m.getArray()[0].length);
		timeTemp=m;
		pressure=p;
	}
	
	private void setMatrix(String times, String temps) {
		double [][] data = new double[ dataSize ][2];
		
		double [] te = getDataFromString(temps);
		double [] ti = getDataFromString(times);
		
		for(int i=0;i<dataSize;i++){
			data[i][1] = te[i];
			data[i][0] = ti[i];
		}
		timeTemp = new Matrix(data);
	}

	private double[] getDataFromString(String temps) {
		double [] data = new double[dataSize];
		
		String [] contents = temps.split(",");
		
		if(data.length!=contents.length)
			throw new RuntimeException("Config file invalid!!");
		
		for(int i=0;i<dataSize;i++){
			data[i]=Double.parseDouble(contents[i]);
		}
		
		return data;
	}


	
	public Matrix getTimeTemp() {
		return timeTemp;
	}
	public void setTimeTemp(Matrix timeTemp) {
		this.timeTemp = timeTemp;
	}
	public double getPressure() {
		return pressure;
	}
	public void setPressure(double pressure) {
		this.pressure = pressure;
	}

	public double[] getTimes() {
		double [] tmp = new double[dataSize];
		double [][] array = timeTemp.getArray();
		for(int i=0;i<dataSize;i++){
			tmp[i]=array[i][0];
		}
		return tmp;
	}

	public double[] getTempretures() {
		double [] tmp = new double[dataSize];
		double [][] array = timeTemp.getArray();
		for(int i=0;i<dataSize;i++){
			tmp[i]=array[i][1];
		}
		return tmp;
	}

	public void saveToFile(File currentSave) {
		if(currentSave == null)
			return;
		setConfigFile(currentSave);
		String s = getTag();
		try{
			  // Create file 
			  FileWriter fstream = new FileWriter(currentSave.getAbsolutePath());
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(s);
			  out.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
		  
	}
	
	private String arrayToString(double [] data){
		String s="";
		for(int i=0;i<data.length;i++){
			s+=data[i];
			if(i<data.length-1)
				s+=",";
		}
		return s;
	}

	private String getTag() {
		String s="";
		s+="<OggiePress>\n";
			s+="\t<pressure>";
			s+=pressure;
			s+="</pressure>\n";
			s+="\t<time>";
			s+=arrayToString(getTimes());
			s+="</time>\n";
			s+="\t<temp>";
			s+=arrayToString(getTempretures());
			s+="</temp>\n";
		s+="</OggiePress>\n";
		return s;
	}

	public File getConfigFile() {
		return config;
	}

	public void setConfigFile(File config) {
		this.config = config;
	}

}
