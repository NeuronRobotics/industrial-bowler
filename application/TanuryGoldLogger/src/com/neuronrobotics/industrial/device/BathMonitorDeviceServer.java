package com.neuronrobotics.industrial.device;

import java.util.List;

import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractServer;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.network.BowlerUDPServer;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMonitorDeviceServer extends BowlerAbstractServer implements IAnalogInputListener{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalVoltage;
	private double reference;
	private double signal;
	
	private int pollingRate = 1000*60*2;
	
	private DyIO dyio;
	private String name = null;
	static{
		DyIO.disableFWCheck();
	}

	public BathMonitorDeviceServer(DyIO device) {
		super(device.getAddress());
		setServer(new BowlerUDPServer(1865));
		//Log.enableDebugPrint(true);
		dyio=device;
		
		referenceVoltage = 	new AnalogInputChannel(dyio,15);
		signalVoltage = 	new AnalogInputChannel(dyio, 13);
		reference = referenceVoltage.getValue();
		//signalVoltage.configAdvancedAsyncAutoSample(5000);
		referenceVoltage.addAnalogInputListener(this);
		signalVoltage.addAnalogInputListener(this);
		new Thread(){
			public void run(){
				while(dyio.isAvailable()){
					onAnalogValueChange(signalVoltage, signalVoltage.getValue());
					ThreadUtil.wait(100);
				}
			}
		}.start();
		new Thread(){
			public void run(){
				while(dyio.isAvailable()){
					BathMoniterEvent be = new BathMoniterEvent(getName(), System.currentTimeMillis(), getCurrent());
					pushAsyncPacket(be.getPacket(dyio.getAddress()));
					ThreadUtil.wait(getPollingRate());
				}
			}
		}.start();
		
		addBowlerDeviceServerNamespace(new TanuryBathNamespaceImp(this,getMacAddress()));
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
		}
		
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SerialConnection con=null;
		List<String> ports = SerialConnection.getAvailableSerialPorts();
		for(String s:ports){
			System.err.println(s);
		}
		if(args.length != 1){
			System.err.println("No port specified, choosing first");
			
			if(ports.size() >0){
				con = new SerialConnection(ports.get(0));
			}else{
				System.err.println("No port availible");
				System.exit(1);
			}
		}else{
			System.out.println("Using port: "+args[0]);
			con  = new SerialConnection(args[0]);
		}
		DyIO.disableFWCheck();
		DyIO dyio = new DyIO(con);
		dyio.connect();
		new BathMonitorDeviceServer(dyio);
	}

	public String getName() {
		if(name == null)
			name=dyio.getInfo();
		return name;
	}
	public void setName(String name) {
		this.name = name;
		dyio.setInfo(name);
	}

	public int getPollingRate() {
		return pollingRate;
	}
	/**
	 * Sets the internal variable for the polling rate
	 * @param pollingRate The polling rate in seconds
	 */
	public void setPollingRate(int pollingRate) {
		this.pollingRate = 1000*60*pollingRate;
	}
}
