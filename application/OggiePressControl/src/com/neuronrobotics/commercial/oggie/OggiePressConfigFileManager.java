package com.neuronrobotics.commercial.oggie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.neuronrobotics.sdk.addons.kinematics.xml.XmlFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class OggiePressConfigFileManager implements IPressHardwareListener{
	
	private SinglePressControl press1;
	private SinglePressControl press2;
	private DualPressControl dual;
	private String configFileLocation = System.getProperty("user.home")+"/OggiePressSystem/System.xml";
	private String logFileLocation = System.getProperty("user.home")+"/OggiePressSystem/Log/log";
	
	private File config =null;
	private File log =null;
	private boolean loading = false;

	public OggiePressConfigFileManager(SinglePressControl press1,SinglePressControl press2, DualPressControl dual) throws IOException{
		this.press1 = press1;
		this.press2 = press2;
		this.dual = dual;
		loading = true;
		press1.getTable().setConfigFileManager(this);
		press2.getTable().setConfigFileManager(this);
		dual.getTable().setConfigFileManager(this);
		
		File logDir = new File(System.getProperty("user.home")+"/OggiePressSystem/Log/");
		if(!logDir.exists()){
			logDir.mkdirs();
		}
		
		config = new File(configFileLocation);
		log    = new File(logFileLocation+new Date()+".csv");
		if(!config.exists()){
			config.createNewFile();
			save();
		}else{
			try{
				System.out.println("Loading file: "+config.getAbsolutePath());
				Document doc =XmlFactory.getAllNodesDocument(new FileInputStream(config));
				NodeList p1 = doc.getElementsByTagName("press1");
				NodeList p2 = doc.getElementsByTagName("press2");
				NodeList pd = doc.getElementsByTagName("dual");
				
				press1.getTable().setConfigNode(p1);
				press2.getTable().setConfigNode(p2);
				dual.getTable().setConfigNode(pd);
				
			}catch(Exception e){
				config.createNewFile();
				save();
			}
			
		}
		
		if(! log.exists()){
			log.createNewFile();
		}
		writeLine("Open Press Application");
		loading = false;
		
	}
	
	public void save(){
		if(loading)
			return;
//		System.out.println("Saving values: #1 = \n\n"+press1.getTable().getXml());
//		System.out.println("Saving values: #2 = \n\n"+press2.getTable().getXml());
//		System.out.println("Saving values: #dual = \n\n"+dual.getTable().getXml());
		
		
		try{
			  // Create file 
			  FileWriter fstream = new FileWriter(config.getAbsolutePath());
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(getFileContents());
			  out.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
		  
	}
	
	private String getFileContents(){
		String s="";
		s+="<root>\n";
		s+="<press1>\n";
		s+=press1.getTable().getXml();
		s+="</press1>\n";
		
		s+="<press2>\n";
		s+=press2.getTable().getXml();
		s+="</press2>\n";
		
		s+="<dual>\n";
		s+=dual.getTable().getXml();
		s+="</dual>\n";
		s+="</root>\n";
		return s;
	}

	@Override
	public void onCycleStart(int i, CycleConfig config) {
		log    = new File(logFileLocation+new Date()+".csv");
		writeLine("onCycleStart,"+i+","+config.getPressure());
		ThreadUtil.wait(200);
		writeLine("onCycleStart temp,"+i+","+config.getTempString());
		ThreadUtil.wait(200);
		writeLine("onCycleStart times,"+i+","+config.getTimeString());
	}

	@Override
	public void onAbortCycle(int i) {
		String s= "onAbortCycle,"+i;
		writeLine(s);
	}

	@Override
	public void onPressureChange(int i, double pressureTons) {
		String s= "onPressureChange,"+i+","+pressureTons;
		writeLine(s);
	}

	@Override
	public void onTempretureChange(int i, double degreesFarenhight) {
		// TODO Auto-generated method stub
		String s= "onTempretureChange,"+i+","+degreesFarenhight;
		writeLine(s);
	}

	@Override
	public void onCycleIndexUpdate(int currentTableIndex, double currentTableTime, double timeRemaining, int press, double newTargetTemp) {
		String s= "onCycleIndexUpdate,"+press+","+currentTableIndex+","+currentTableTime+","+timeRemaining+","+newTargetTemp;
		writeLine(s);
	}
	
	private void writeLine(final String s){
		new Thread(){
			public void run(){
				synchronized(log){
					String line = System.currentTimeMillis()+","+new Date(System.currentTimeMillis())+","+s+"\r\n";
					try{
						  // Create file in append mode
						  FileWriter fstream = new FileWriter(log.getAbsolutePath(),true);
						  BufferedWriter out = new BufferedWriter(fstream);
						  out.write(line);
						  out.close();
					}catch (Exception e){//Catch exception if any
						  System.err.println("Error: " + e.getMessage());
					}
				}
			}
		}.start();

	}

}
