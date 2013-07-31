package com.neuronrobotics.industrial.device;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.IBathMoniterUpdateListener;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.dyio.sequencer.ISchedulerListener;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMoniterDevice extends BowlerAbstractDevice{
	
	private IBathMoniterUpdateListener bathMoniter;
	
	public BathMoniterDevice(){
		
	}

	public BathMoniterDevice(BowlerUDPClient bowlerUDPClient) {
		setConnection(bowlerUDPClient);
	}
	
	@Override
	public boolean connect(){

		return true;
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

	@Override
	public void onAsyncResponse(BowlerDatagram data) {
		if(data.getRPC().contains("bath")){
			BathMoniterEvent be = new BathMoniterEvent(data);
			
			if(getBathMoniter() !=null)
				getBathMoniter().onValueChange(be.getBathName(), be.getTimestamp(), be.getCurrentOzHrRate());
		}
	}

	public String getName() {
		Object [] args = send("tanury.bath.*", BowlerMethod.GET,
				"name",
				new Object[]{});
		return (String)args[0];
	}

	public void setName(String newName) {
		Object[] args = new Object[]{newName};
		send("tanury.bath.*",BowlerMethod.POST,
				"name",
				args);
	}

}
