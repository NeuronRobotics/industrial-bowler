package com.neuronrobotics.industrial.device;

import java.util.List;

import com.neuronrobotics.sdk.common.device.server.BowlerAbstractServer;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.network.BowlerUDPServer;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.RollingAverageFilter;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMonitorDeviceServer extends BowlerAbstractServer implements IAnalogInputListener{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalVoltage;
	private double reference;
	private double signal;
	private RollingAverageFilter integral; 
	private double pollingRate = 1000*2;
	private double totalUsedToday =0;
	private double scaleValue=.01;
	
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
		signalVoltage = 	new AnalogInputChannel(dyio, 12);
		reference = referenceVoltage.getValue();
		signal    = signalVoltage.getValue();
		integral = new RollingAverageFilter(20, getCurrent());
		//signalVoltage.configAdvancedAsyncAutoSample(5000);
		referenceVoltage.addAnalogInputListener(this);
		signalVoltage.addAnalogInputListener(this);
		new Thread(){
			public void run(){
				while(dyio.isAvailable()){
					onAnalogValueChange(signalVoltage, signalVoltage.getValue());
					ThreadUtil.wait(200);
				}
			}
		}.start();
		new Thread(){
			public void run(){
				ThreadUtil.wait((int) getPollingRate());
				while(dyio.isAvailable()){
					totalUsedToday +=getCurrent()*(getPollingRate()/1000);
					//System.out.println("Current voltage= "+ signal+ " Scaled= "+getCurrent());
					
					BathMoniterEvent be = new BathMoniterEvent(	getDeviceName(), 
																System.currentTimeMillis(), 
																getCurrent(),
																totalUsedToday*getScale());
					pushAsyncPacket(be.getPacket(dyio.getAddress()));
					ThreadUtil.wait((int) getPollingRate());
				}
			}
		}.start();
		
		addBowlerDeviceServerNamespace(new TanuryBathNamespaceImp(this,getMacAddress()));
		System.out.println("System ONLINE");
	}
	
	public double getCurrent(){
		
		double scale = (2.5//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=150;//Ohms of shunt
		
		double ampScale = (1/32.5)*0.8064;//Amp gain
		double val = 0;
		if(integral== null)
			val = signal;
		else
			val = integral.getValue();
		return (((val/1024)*scale)*ampScale)/(i/1000);
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalVoltage){
			signal = value;
			integral.add( value);
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

	public String getDeviceName() {
		if(name == null)
			name=dyio.getInfo();
		return name;
	}
	public void setName(String name) {
		this.name = name;
		dyio.setInfo(name);
	}

	public double getPollingRate() {
		return pollingRate;
	}
	/**
	 * Sets the internal variable for the polling rate
	 * @param pollingRate The polling rate in seconds
	 */
	public void setPollingRate(int pollingRate) {
		this.pollingRate = 1000*pollingRate;
	}

	public void setScale(double integer) {
		scaleValue = integer;
	}

	public double getScale() {
		return scaleValue;
	}

	public void clearData() {
		totalUsedToday = 0;
	}
}
