package com.neuronrobotics.industrial.device;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.IBathMoniterUpdateListener;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.dyio.sequencer.ISchedulerListener;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMoniterDevice extends DyIO implements IAnalogInputListener{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalVoltage;
	
	
	private double reference;
	private double signal;
	private IBathMoniterUpdateListener bathMoniter;
	
	static{
		DyIO.disableFWCheck();
	}
	
	public BathMoniterDevice(){
		
	}

	public BathMoniterDevice(BowlerUDPClient bowlerUDPClient) {
		super(bowlerUDPClient);
	}
	
	@Override
	public boolean connect(){
		if(super.connect()){
			referenceVoltage = 	new AnalogInputChannel(this,15);
			signalVoltage = 	new AnalogInputChannel(this, 13);
			reference = referenceVoltage.getValue();
			signalVoltage.configAdvancedAsyncAutoSample(500);
			referenceVoltage.addAnalogInputListener(this);
			signalVoltage.addAnalogInputListener(this);
			new Thread(){
				public void run(){
					while(isAvailable()){
						ThreadUtil.wait(1000);
						onAnalogValueChange(signalVoltage, signalVoltage.getValue());
					}
				}
			}.start();
			return true;
		}
		return false;
	}
	
	public double getCurrent(){
		
		double scale = (2.5//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=150;//Ohms of shunt
		
		double ampScale = .046/1.494;
		
		return (((signal/1024)*scale)*ampScale);
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalVoltage){
			signal =  value;
			if(getBathMoniter() !=null)
				getBathMoniter().onValueChange(getInfo(), System.currentTimeMillis(), getCurrent());
		}
		
	}

	public void addBathUi(BathMoniter bathMoniter) {
		this.setBathMoniter(bathMoniter);
		
	}

	public IBathMoniterUpdateListener getBathMoniter() {
		return bathMoniter;
	}

	public void setBathMoniter(IBathMoniterUpdateListener bathMoniter) {
		this.bathMoniter = bathMoniter;
	}

}
