package com.neuronrobotics.industrial.device;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.IBathMoniterUpdateListener;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.network.BowlerUDPClient;

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
			signalVoltage = 	new AnalogInputChannel(this, 14);
			reference = referenceVoltage.getValue();
			signalVoltage.configAdvancedAsyncAutoSample(5000);
			referenceVoltage.addAnalogInputListener(this);
			signalVoltage.addAnalogInputListener(this);
			return true;
		}
		return false;
	}
	
	public double getCurrent(){
		
		double scale = (2.5//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=0.001;//Ohms of shunt
		
		return (signal*scale)/i;
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
