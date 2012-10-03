package com.neuronrobotics.commercial.oggie;

import java.util.ArrayList;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PressHardware {
	private DyIO dyio;
	
	private ArrayList< IPressHardwareListener> listeners = new ArrayList< IPressHardwareListener> ();
	
	private double [] pressure = new double[]{0,0};
	private double [] temp = new double[]{0,0};
	private CycleConfig [] targetcycle = new CycleConfig[2];
	
	private boolean [] abort = new boolean[]{false,false};
	
	private VirtualPress [] vp = new VirtualPress[2];
	
	public PressHardware (DyIO d){
		dyio=d;
		if(d==null){
			vp[0]=new VirtualPress(0);
			vp[1]=new VirtualPress(1);
			vp[0].start();
			vp[1].start();
		}
	}
	
	public void setTargetTempreture(int index, double t){
		if(dyio==null){
			vp[index].setTargetTemp(t);
		}else{
			//do hardware
		}
		setTempreture(index, getTempreture(index));
	}
	
	private void setTempreture(int index, double t){
		temp[index]=t;
		fireTempretureChange(index, t);
	}	
	
	public double getTempreture(int pressIndex) {
		return temp[pressIndex];
	}
	
	private void setPressure(int index, double p){
		pressure[index]=p;
		firePressureChange(index, p);
	}	
	public double getPressure(int pressIndex) {
		return pressure[pressIndex];
	}
	
	
	public void abortCycle(int i) {
		if(dyio==null){
			vp[i].abort();
		}else{
			//do hardware
		}
		abort[i]=true;
		fireAbort(i);
		setPressure(i, 0);
	}
	public void onCycleStart(int i, CycleConfig config) {
		System.out.println("Starting Press Cycle from HW index = "+i);
		if(dyio==null){
			vp[i].setRunClose(true);
		}else{
			//do hardware
		}
		abort[i]=false;
		targetcycle[i]=config;
		fireCycleStart(i, config);
	}
	
	/**
	 * Add an IPressHardwareListener that will be contacted with an press hardware event on
	 * each incoming data event.
	 * 
	 * 
	 * 
	 * @param l
	 */
	public void addPressHardwareListener(IPressHardwareListener l) {
		if(listeners.contains(l)) {
			return;
		}
		
		listeners.add(l);
	}
	
	/**
	 * Removes an IPressHardwareListener from being contacted on each new
	 * 
	 * @param l
	 */
	public void removePressHardwareListener(IPressHardwareListener l) {
		if(!listeners.contains(l)) {
			return;
		}
		
		listeners.remove(l);
	}
	
	/**
	 * Clears out all current IPressHardwareListener.
	 */
	public void removeAllPressHardwareListener() {
		listeners.clear();
	}
	
	
	public void fireCycleStart(int i, CycleConfig config) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			l.onCycleStart(i, config);
		}
	}
	public void fireAbort(int i) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			l.onAbortCycle(i);
		}
	}
	public void firePressureChange(int i, double pressure) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			l.onPressureChange(i, pressure);
		}
	}
	public void fireTempretureChange(int i, double temp) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			l.onTempretureChange(i, temp);
		}
	}
	
	private class VirtualPress extends Thread{
		private final int index;
		
		private boolean runClose = false;
		private boolean runVirtual = true;
		private double targetTemp = 0.0;
		private double currentTemp = 0.0;
		
		private double closeIndex = 0;
		
		
		
		public VirtualPress(int index){
			this.index = index;
			abort();
		}
		
		public void run(){
			System.out.println("Press hardware starting..");
			double size=100;
			while(isRunVirtual()){
				ThreadUtil.wait(50);
				if(isRunClose()){
					closeIndex+=1;
					if(closeIndex<size){
						setPressure(index, targetcycle[index].getPressure()*(closeIndex/size));
					}else{
						runClose = false;
					}
				}
				if(		currentTemp > (getTargetTemp()+.5) || 
						currentTemp < (getTargetTemp()-.5)){
					if(currentTemp>getTargetTemp()){
						currentTemp-=1;
					}else{
						currentTemp+=1;
					}
					setTempreture(index, currentTemp);
				}
			}
			
		}

		public boolean isRunVirtual() {
			return runVirtual;
		}
		
		public boolean isRunClose() {
			return runClose;
		}

		public void setRunClose(boolean runClose) {
			this.runClose = runClose;
		}

		public double getTargetTemp() {
			return targetTemp;
		}

		public void setTargetTemp(double targetTemp) {
			this.targetTemp = targetTemp;
		}
		
		public void abort(){
			closeIndex = 0;
			runClose = false;
		}
	}
}
