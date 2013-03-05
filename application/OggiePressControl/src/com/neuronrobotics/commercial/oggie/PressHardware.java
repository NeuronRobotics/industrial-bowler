package com.neuronrobotics.commercial.oggie;

import java.util.ArrayList;
import java.util.Date;

import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.dypid.DyPIDConfiguration;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DCMotorOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.DigitalOutputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.pid.IPIDEventListener;
import com.neuronrobotics.sdk.pid.PIDChannel;
import com.neuronrobotics.sdk.pid.PIDConfiguration;
import com.neuronrobotics.sdk.pid.PIDEvent;
import com.neuronrobotics.sdk.pid.PIDLimitEvent;
import com.neuronrobotics.sdk.util.RollingAverageFilter;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class PressHardware implements IPIDEventListener, IAnalogInputListener {
	private DyIO dyio;
	
	private RollingAverageFilter filter[]= new RollingAverageFilter[2];
	private PIDChannel tempPID[] = new PIDChannel [2]; 
	private double pGain = .05;
	private PIDConfiguration pidConf1 = new PIDConfiguration(		0, true, true, true, 
			 														pGain, 0, 0, 
																	0, false, false);
	private DyPIDConfiguration dypidConf1 = new DyPIDConfiguration(	0, 
																	10, DyIOChannelMode.ANALOG_IN, 
																	2, 	DyIOChannelMode.DIGITAL_OUT);
	private PIDConfiguration pidConf2 = new PIDConfiguration(		1, true, true, true, 
			 														pGain, 0, 0, 
																	0, false, false);
	private DyPIDConfiguration dypidConf2 = new DyPIDConfiguration(	1, 
																	12, DyIOChannelMode.ANALOG_IN, 
																	13, 	DyIOChannelMode.DIGITAL_OUT);
	
	private ArrayList< IPressHardwareListener> listeners = new ArrayList< IPressHardwareListener> ();
	
	private double [] pressure = new double[]{0,0};
	private double [] temp = new double[]{0,0};
	private CycleConfig [] targetcycle = new CycleConfig[2];
	
	private boolean [] abort = new boolean[]{false,false};
	
	private VirtualPress [] vp = new VirtualPress[2];
	
	private DigitalOutputChannel enablePin;
	
	private DCMotorOutputChannel pressAnalog[] = new  DCMotorOutputChannel [2];
	private AnalogInputChannel   pressureSense[] = new AnalogInputChannel[2];
	
	public PressHardware (DyIO d){
		dyio=d;
		if(d==null){
			vp[0]=new VirtualPress(0);
			vp[1]=new VirtualPress(1);
			vp[0].start();
			vp[1].start();
		}else{
			dyio.ConfigureDynamicPIDChannels(dypidConf1);
			tempPID[0] = dyio.getPIDChannel(0);
			tempPID[0].ConfigurePIDController(pidConf1);
			tempPID[0].SetPIDSetPoint(0, 0);
			filter[0] = new RollingAverageFilter(10,tempPID[0].GetPIDPosition() );
			
			dyio.ConfigureDynamicPIDChannels(dypidConf2);
			tempPID[1] = dyio.getPIDChannel(1);
			tempPID[1].ConfigurePIDController(pidConf2);
			tempPID[1].SetPIDSetPoint(0, 0);
			filter[1] = new RollingAverageFilter(10,tempPID[1].GetPIDPosition() );
			
			tempPID[0].addPIDEventListener(this);
			tempPID[1].addPIDEventListener(this);
			
			enablePin = new DigitalOutputChannel(dyio.getChannel(1));
			pressAnalog[0] = new DCMotorOutputChannel(dyio.getChannel(5)); 
			//pressAnalog[1] = new DCMotorOutputChannel(dyio.getChannel(4)); 
			
			pressureSense[0] = new AnalogInputChannel(dyio.getChannel(8));
			pressureSense[0].addAnalogInputListener(this);
			pressureSense[1] = new AnalogInputChannel(dyio.getChannel(12));
			pressureSense[1].addAnalogInputListener(this);
			
			enablePin.setHigh(false);
			pressAnalog[0].setValue(129);
			//pressAnalog[1].setValue(129);
		}
	}
	
	public void setTargetTempreture(int index, double t){
		if(dyio==null){
			vp[index].setTargetTemp(t);
		}else{
			tempPID[index].SetPIDSetPoint(tempretureToAdc(t), 0);
		}
		//setTempreture(index, getTempreture(index));
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
			enablePin.setHigh(false);
			tempPID[i].SetPIDSetPoint(0, 0);//zero tempreture
			pressAnalog[i].setValue(128);// zero pressure
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
			enablePin.setHigh(true);
			int pressure= pressureToDcMotorValue(config.getPressure());
			System.out.println("Starting cycle with pressure "+config.getPressure()+" PWM = "+pressure);
			pressAnalog[i].setValue(pressure);
		}
		abort[i]=false;
		targetcycle[i]=config;
		fireCycleStart(i, config);
		new CycleTimer(i).start();
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
	public void fireCycleIndexUpdate(int currentTableIndex, double currentTableTime, double timeRemaining, int press, double newTargetTemp) {
		for(IPressHardwareListener l : listeners) {
			l.onCycleIndexUpdate(currentTableIndex, currentTableTime, timeRemaining, press, newTargetTemp);
		}
	}
	
	public void fireCycleStart(int i, CycleConfig config) {
		for(IPressHardwareListener l : listeners) {
			l.onCycleStart(i, config);
		}
	}
	public void fireAbort(int i) {
		for(IPressHardwareListener l : listeners) {
			l.onAbortCycle(i);
		}
	}
	public void firePressureChange(int i, double pressure) {
		for(IPressHardwareListener l : listeners) {
			l.onPressureChange(i, pressure);
		}
	}
	public void fireTempretureChange(int index, double temp) {
		synchronized (listeners){
			for(int i=0;i< listeners.size();i++) {
				listeners.get(i).onTempretureChange(index, temp);
			}
		}
	}
	
	private class CycleTimer extends Thread{
		private int pressIndex;
		private long startTime;
		double time;
		public CycleTimer(int index){
			this.pressIndex = index;
			
		}
		public void run(){
			startTime=System.currentTimeMillis();
			int cycleIndex=0;
			while(!abort[pressIndex] && cycleIndex<CycleConfig.dataSize){
				ThreadUtil.wait(1000);
				time = ((double)System.currentTimeMillis()-startTime)/1000.0/60.0;
				if(time>getTime(cycleIndex+1)){
					cycleIndex++;
				}
				fire(cycleIndex, time);
			}
			System.out.println("Cycle time bailing out!");
		}
		
		private double getTime(int i){
			if(i>=CycleConfig.dataSize)
				return targetcycle[pressIndex].getTimes()[CycleConfig.dataSize-1];
			return targetcycle[pressIndex].getTimes()[i];
		}
		
		private void fire(int i, double currentTime){
			if(i>=CycleConfig.dataSize)
				return;
			double temp = targetcycle[pressIndex].getTempretures()[i];
			setTargetTempreture(pressIndex, temp);
			double currentTableTime=targetcycle[pressIndex].getTimes()[i];
			double timeRemaining=targetcycle[pressIndex].getTimes()[CycleConfig.dataSize-1]-currentTime;
			fireCycleIndexUpdate(i, currentTime, timeRemaining, pressIndex, temp);
			//System.out.println("Fireing cycle update from hardware = "+time+" time = "+ new Date(System.currentTimeMillis()));
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
	private boolean dualMode = false;
	public boolean isDualMode() {
		// TODO Auto-generated method stub
		return dualMode;
	}

	public void setDualMode(boolean dualMode) {
		this.dualMode = dualMode;
	}

	@Override
	public void onPIDEvent(PIDEvent e) {
		if(e.getGroup() == pidConf1.getGroup()){

			setTempreture(0,filter[0].add( adcToTempreture(e.getValue())));
		}
		if(e.getGroup() == pidConf2.getGroup()){
			setTempreture(1, filter[1].add( adcToTempreture(e.getValue())));
		}
	}

	@Override
	public void onPIDLimitEvent(PIDLimitEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPIDReset(int group, int currentValue) {
		// TODO Auto-generated method stub8
		
	}
	
	double celciusToFerinheight(double celcius){
		return celcius * (9.0/5.0) + 32.0;
	}
	
	double ferinheightTocelcius(double ferinheight){
		return (ferinheight -  32.0)  *  (5.0/9.0);
	}
	
	private final double referenceVoltage = 4.6;
	
	double adcToTempreture(int adc){	
		return celciusToFerinheight(((((double)adc*(referenceVoltage/1024.0))-1.246090909)*150.56118259));
	}
	int tempretureToAdc(double tempreture){
		return (int)(((ferinheightTocelcius(tempreture)/150.56118259)+1.246090909)/(referenceVoltage/1024.0));
	}
	
	private double scaleAdcToPressure= (258/1024)*2.43;
	
	private double adcToPressure(double adc){
		return adc * scaleAdcToPressure;
	}
	
	private int pressureToDcMotorValue(double pressure){

		return (int) ((pressure/8)*128)+129;
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		if(chan == pressureSense[0]){
			setPressure(0,adcToPressure(value) );
		}
		if(chan == pressureSense[1]){
			setPressure(1,adcToPressure(value) );
		}
		
	}

}
