package com.neuronrobotics.commercial.oggie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.neuronrobotics.sdk.addons.kinematics.xml.XmlFactory;
import com.neuronrobotics.sdk.common.Log;

public class OggiePressConfigFileManager {
	
	private SinglePressControl press1;
	private SinglePressControl press2;
	private DualPressControl dual;
	private String configFileLocation = System.getProperty("user.home")+"/OggiePressSystem.xml";
	
	private File config =null;
	
	private boolean loading = false;

	public OggiePressConfigFileManager(SinglePressControl press1,SinglePressControl press2, DualPressControl dual) throws IOException{
		this.press1 = press1;
		this.press2 = press2;
		this.dual = dual;
		loading = true;
		press1.getTable().setConfigFileManager(this);
		press2.getTable().setConfigFileManager(this);
		dual.getTable().setConfigFileManager(this);
		
		config = new File(configFileLocation);
		if(!config.exists()){
			config.createNewFile();
			save();
		}else{
			//load config from file
			System.out.println("Loading file: "+config.getAbsolutePath());
			Document doc =XmlFactory.getAllNodesDocument(new FileInputStream(config));
			NodeList p1 = doc.getElementsByTagName("press1");
			NodeList p2 = doc.getElementsByTagName("press2");
			NodeList pd = doc.getElementsByTagName("dual");
			
			press1.getTable().setConfigNode(p1);
			press2.getTable().setConfigNode(p2);
			dual.getTable().setConfigNode(pd);
			
		}
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

}
