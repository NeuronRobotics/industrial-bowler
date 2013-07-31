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
	
	private DyIO dyio;
	static{
		DyIO.disableFWCheck();
	}

	public BathMonitorDeviceServer(DyIO device) {
		super(device.getAddress());
		setServer(new BowlerUDPServer(1865));
		Log.enableDebugPrint(true);
		dyio=device;
		
		referenceVoltage = 	new AnalogInputChannel(dyio,15);
		signalVoltage = 	new AnalogInputChannel(dyio, 13);
		reference = referenceVoltage.getValue();
		signalVoltage.configAdvancedAsyncAutoSample(500);
		referenceVoltage.addAnalogInputListener(this);
		signalVoltage.addAnalogInputListener(this);
		new Thread(){
			public void run(){
				while(dyio.isAvailable()){
					ThreadUtil.wait(100);
					onAnalogValueChange(signalVoltage, signalVoltage.getValue());
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
			BathMoniterEvent be = new BathMoniterEvent(dyio.getInfo(), System.currentTimeMillis(), getCurrent());
			pushAsyncPacket(be.getPacket(dyio.getAddress()));
			
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
		return dyio.getInfo();
	}
	public void setName(String name) {
		dyio.setInfo(name);
	}
}
