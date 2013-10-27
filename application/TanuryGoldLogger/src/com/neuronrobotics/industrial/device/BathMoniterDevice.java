package com.neuronrobotics.industrial.device;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.IBathMoniterUpdateListener;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.common.DeviceConnectionException;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.dyio.sequencer.ISchedulerListener;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMoniterDevice extends BowlerAbstractDevice{
	
	private IBathMoniterUpdateListener bathMoniter;
	
	public BathMoniterDevice(){
		
	}

	public BathMoniterDevice(BowlerTCPClient bowlerTCPClient) {
		setConnection(bowlerTCPClient);
		startHeartBeat(2000);
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
			
			if(getBathMoniter() !=null){
				//System.out.println("ASYNC << "+be+"\n"+data);
				
				getBathMoniter().onValueChange(be);
			}
		}if(data.getRPC().contains("alrm")){
			
			if(getBathMoniter() !=null){
				System.out.println("ASYNC << \n"+data);
				getBathMoniter().onAlarmEvenFire(new BathAlarmEvent(data));
			}
		}
	}

	public Object [] send(String namespace,BowlerMethod method, String rpcString, Object[] arguments) throws DeviceConnectionException{
		if(!getConnection().isConnected())
			connect();
		return super.send(namespace, method, rpcString, arguments,5);
	}

	public String getName() {
		Object [] args = send(	"tanury.bath.*", 
								BowlerMethod.GET,
								"name",
								new Object[]{});
		return (String)args[0];
	}

	public void setName(String newName) {
		Object[] args = new Object[]{newName};
		send(	"tanury.bath.*",
				BowlerMethod.POST,
				"name",
				args);
	}
	/**
	 * Sets the internal variable for the polling rate
	 * @param pollingRate The polling rate in seconds
	 */
	public void setPollingRate(int seconds){
		Object[] args = new Object[]{seconds};
		send(	"tanury.bath.*",
				BowlerMethod.POST,
				"rate",
				args);
	}
	/**
	 * Gets the internal variable for the polling rate
	 * @return The polling rate in seconds
	 */
	public int getPollingRate(){
		Object[] args = send(	"tanury.bath.*",
				BowlerMethod.GET,
				"rate",
				new Object[]{});
		return (Integer)args[0];
	}
	
	public void setScale(double scale) {
		Object[] args = new Object[]{scale};
		send(	"tanury.bath.*",
				BowlerMethod.POST,
				"scal",
				args);
	}

	public double getScale() {
		Object[] args = send(	"tanury.bath.*",
				BowlerMethod.GET,
				"scal",
				new Object[]{});
		return (Double)args[0];
	}
	
	public void setAlarmLevel(double scale) {
		Object[] args = new Object[]{scale};
		send(	"tanury.bath.*",
				BowlerMethod.POST,
				"alrm",
				args);
	}

	public double getAlarmLevel() {
		Object[] args = send(	"tanury.bath.*",
				BowlerMethod.GET,
				"alrm",
				new Object[]{});
		return (Double)args[0];
	}

	public void clearData() {
		send(	"tanury.bath.*",
				BowlerMethod.POST,
				"cler",
				new Object[]{});
	}
	public void dumpLogs(Integer numberOfDays) {
		send(	"tanury.bath.*",
				BowlerMethod.GET,
				"logd",
				new Object[]{numberOfDays});
	}

}
