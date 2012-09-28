package com.neuronrobotics.commercial.oggie;

import Jama.Matrix;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PressHardware {
	private DyIO dyio;
	
	private double [] pressure = new double[]{0,0};
	private double [] targetPressure = new double[]{0,0};
	private Matrix [] targetcycle = new Matrix[2];
	
	private boolean [] abort = new boolean[]{false,false};
	
	public PressHardware (DyIO d){
		dyio=d;
		
	}
	public double getPressure(int pressIndex) {
		return pressure[pressIndex];
	}
	public void abortCycle(int i) {
		abort[i]=true;
		pressure[i]=0;
	}
	public void onCycleStart(int i, Matrix m, double pressure) {
		
		if(dyio==null){
			new VirtualPress(i).start();
		}
		abort[i]=false;
		targetcycle[i]=m;
		targetPressure[i]=pressure;
	}
	
	private class VirtualPress extends Thread{
		private final int index;
		public VirtualPress(int index){
			this.index = index;
		}
		public void run(){
			System.out.println("Press hardware starting..");
			ThreadUtil.wait(2000);
			if(!abort[index]){
				pressure[index]=targetPressure[index];
				System.out.println("Press "+index+" is ready");
			}
		}
	}
}
