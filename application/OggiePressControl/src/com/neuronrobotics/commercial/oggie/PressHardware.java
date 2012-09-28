package com.neuronrobotics.commercial.oggie;

import Jama.Matrix;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PressHardware {
	private DyIO dyio;
	
	private double [] pressure = new double[]{0,0};
	
	private boolean [] abort = new boolean[]{false,false};
	
	public PressHardware (DyIO d){
		dyio=d;
		
	}
	public double getPressure(int pressIndex) {
		return pressure[pressIndex];
	}
	public void abortCycle(int i) {
		abort[i]=true;
		
	}
	public void onCycleStart(int i, Matrix m, double pressure) {
		if(dyio==null){
			new VirtualPress(i).start();
		}
		abort[i]=false;
	}
	
	private class VirtualPress extends Thread{
		private final int index;
		public VirtualPress(int index){
			this.index = index;
		}
		public void run(){
			ThreadUtil.wait(2000);
			if(!abort[index]){
				pressure[index]=3.7;
				System.out.println("Press "+index+" is ready");
			}
		}
	}
}
