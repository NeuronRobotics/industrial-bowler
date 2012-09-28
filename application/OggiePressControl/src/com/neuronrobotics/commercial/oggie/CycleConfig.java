package com.neuronrobotics.commercial.oggie;

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

}
