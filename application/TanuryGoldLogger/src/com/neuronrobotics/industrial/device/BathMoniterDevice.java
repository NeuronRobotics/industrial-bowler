package com.neuronrobotics.industrial.device;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.network.BowlerUDPClient;

public class BathMoniterDevice extends DyIO implements IAnalogInputListener{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalVoltage;
	
	
	private double reference;
	private double signal;
	private BathMoniter bathMoniter;
	
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
			referenceVoltage = 	new AnalogInputChannel(this, 10);
			signalVoltage = 	new AnalogInputChannel(this, 11);
			referenceVoltage.configAdvancedAsyncAutoSample(500);
			signalVoltage.configAdvancedAsyncAutoSample(500);
			referenceVoltage.addAnalogInputListener(this);
			signalVoltage.addAnalogInputListener(this);
			return true;
		}
		return false;
	}
	
	public double getCurrent(){
		
		double scale = (4096.0//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=100.0;//Ohms of shunt
		
		return (signal*scale)/i;
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalVoltage){
			signal =  value;
		}
		if(getBathMoniter() !=null)
			getBathMoniter().getRecentCurrentRating().setText(new Double(getCurrent()).toString());
	}

	public void addBathUi(BathMoniter bathMoniter) {
		this.setBathMoniter(bathMoniter);
		
	}

	public BathMoniter getBathMoniter() {
		return bathMoniter;
	}

	public void setBathMoniter(BathMoniter bathMoniter) {
		this.bathMoniter = bathMoniter;
	}

}
