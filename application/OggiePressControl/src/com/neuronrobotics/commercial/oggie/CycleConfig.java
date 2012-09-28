package com.neuronrobotics.commercial.oggie;

import Jama.Matrix;

public class CycleConfig {
	
	private Matrix timeTemp;
	private double pressure;
	
	public CycleConfig(Matrix m,double p){
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

}
