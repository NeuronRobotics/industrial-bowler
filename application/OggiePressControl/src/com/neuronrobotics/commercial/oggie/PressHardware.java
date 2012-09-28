package com.neuronrobotics.commercial.oggie;

import java.util.ArrayList;

import Jama.Matrix;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.IDyIOEvent;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PressHardware {
	private DyIO dyio;
	
	private ArrayList< IPressHardwareListener> listeners = new ArrayList< IPressHardwareListener> ();
	
	private double [] pressure = new double[]{0,0};
	private CycleConfig [] targetcycle = new CycleConfig[2];
	
	private boolean [] abort = new boolean[]{false,false};
	
	public PressHardware (DyIO d){
		dyio=d;
		
	}
	
	private void setPressure(int index, double p){
		pressure[index]=p;
	}
	
	public double getPressure(int pressIndex) {
		return pressure[pressIndex];
	}
	public void abortCycle(int i) {
		abort[i]=true;
		setPressure(i, 0);
	}
	public void onCycleStart(int i, CycleConfig config) {
		
		if(dyio==null){
			new VirtualPress(i).start();
		}
		abort[i]=false;
		targetcycle[i]=config;
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
	
	/**
	 * Contact all of the listeners with the given event.
	 * 
	 * @param e
	 *            - the event to fire to all listeners
	 */
	public void fireCycleStart(int i, CycleConfig config) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			
		}
	}
	public void fireAbort(int i) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			
		}
	}
	public void firePressureChange(int i) {
		//System.out.println("DyIO Event: "+e);
		for(IPressHardwareListener l : listeners) {
			
		}
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
				setPressure(index, targetcycle[index].getPressure());
				System.out.println("Press "+index+" is ready");
			}
		}
	}
}
