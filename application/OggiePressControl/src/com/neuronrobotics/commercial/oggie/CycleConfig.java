package com.neuronrobotics.commercial.oggie;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import Jama.Matrix;

public class CycleConfig {
	
	private Matrix timeTemp;
	private double pressure;
	
	public static final int dataSize = 17;
	
	public CycleConfig(Matrix m,double p){
		if(		m.getArray().length!=dataSize || 
				m.getArray()[0].length!=2	)
			throw new RuntimeException("Invalid matrix size, got "+m.getArray().length+"x"+m.getArray()[0].length);
		timeTemp=m;
		pressure=p;
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

	private String getTag() {
		// TODO Auto-generated method stub
		return " ";
	}

}
