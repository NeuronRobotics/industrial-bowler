package com.neuronrobotics.industrial.server;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import com.neuronrobotics.industrial.TanuryDataLogger;
import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;
import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractServer;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIOChannelMode;
import com.neuronrobotics.sdk.dyio.peripherals.AnalogInputChannel;
import com.neuronrobotics.sdk.dyio.peripherals.IAnalogInputListener;
import com.neuronrobotics.sdk.network.BowlerTCPServer;
import com.neuronrobotics.sdk.network.BowlerUDPServer;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.RollingAverageFilter;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class BathMonitorDeviceServer extends BowlerAbstractServer implements IAnalogInputListener{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalChannel;
	private AnalogInputChannel tempVoltage;
	private AnalogInputChannel otherVoltage;
	private double reference;
	private double signal;
	private RollingAverageFilter integral; 
	private int lastPacketDay=0;
	private Calendar cal = Calendar.getInstance();
	
	private DyIO dyio;
	private String name = null;
	private DeviceConfiguration configuration = null;
	private TanuryDataLogger logger = null;
	private double currentSensorValue;
	static{
		DyIO.disableFWCheck();
	}

	public BathMonitorDeviceServer(DyIO device) {
		super(device.getAddress());
		lastPacketDay = cal.get(Calendar.DAY_OF_MONTH);
		Log.warning("Starting configuration XML");
		configuration = new DeviceConfiguration();
		Log.warning("Starting logger");
		logger = new TanuryDataLogger("device");
		Log.warning("Adding namespaces");
		addBowlerDeviceServerNamespace(new TanuryBathNamespaceImp(this,getMacAddress()));
	

		
		Log.enableDebugPrint(true);
		Log.setMinimumPrintLevel(Log.WARNING);
		dyio=device;
		Log.warning("Resetting Inputs");
//		for (int i=0;i<24;i++){
//			if(	i!=12 &&
//				i!=13 &&
//				i!=14 &&
//				i!=15	){
//				dyio.setMode(i, DyIOChannelMode.DIGITAL_IN, false);
//			}
//		}
		
		Log.warning("Starting analog");
		referenceVoltage = 	new AnalogInputChannel(dyio, 15);
		otherVoltage	 = 	new AnalogInputChannel(dyio, 14);
		tempVoltage 	 = 	new AnalogInputChannel(dyio, 13);
		signalChannel 	 = 	new AnalogInputChannel(dyio, 12);
		
		referenceVoltage.setAsync(false);
		otherVoltage.setAsync(false);
		tempVoltage.setAsync(false);
		signalChannel.setAsync(false);
		
		
		Log.warning("Initializing values");
		reference = referenceVoltage.getValue();
		signal    = signalChannel.getValue();
		integral = new RollingAverageFilter(10, getCurrent());

		Log.warning("Starting poll thread");
		new Thread(){
			double ioPoll = 300.0;
			public void run(){
				while(dyio.isAvailable()){
					ThreadUtil.wait((int) ioPoll);
					Log.setMinimumPrintLevel(Log.WARNING);
					onAnalogValueChange(signalChannel, signalChannel.getValue());
					Log.setMinimumPrintLevel(Log.DEBUG);
				}
			}
		}.start();
		Log.warning("Starting up stream thread");
		new Thread(){
			public void run(){
				ThreadUtil.wait((int) getPollingRate());
				while(dyio.isAvailable()){
					
					double currentVal = scaleValue(currentSensorValue);
					
					if(currentVal > getAlarmThreshhold()){
						BathAlarmEvent ev = new BathAlarmEvent(	getDeviceName(),
																System.currentTimeMillis(), 
																getCurrent(),
																getAlarmThreshhold());
						logger.onAlarmEvenFire(ev);
						pushAsyncPacket(ev.getPacket(dyio.getAddress()));
						
					}else{
						configuration.setDailyTotal(configuration.getDailyTotal() + getCurrent()*(getPollingRate()/(60*60*1000.0)));
						BathMoniterEvent be = new BathMoniterEvent(	getDeviceName(), 
																	System.currentTimeMillis(), 
																	getCurrent(),
																	configuration.getDailyTotal()/getScale());
						logger.onValueChange(be, 0);
						System.out.println("Pushing time "+System.currentTimeMillis()+" recorded at "+TanuryDataLogger.getDate(be.getTimestamp()));
						pushAsyncPacket(be.getPacket(dyio.getAddress()));
						
						if(lastPacketDay != cal.get(Calendar.DAY_OF_MONTH)){
							lastPacketDay = cal.get(Calendar.DAY_OF_MONTH);
							//This is where the daily total is reset at midnight
							configuration.setDailyTotal(0);
						}
						
					}
					Log.warning("Voltage = "+getCurrent());
					ThreadUtil.wait((int) getPollingRate());
				}
			}
		}.start();
		
		Log.warning("Starting UDP");
		try {
			startNetworkServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}

		System.err.println("System ONLINE");
		
	}
	
	public double getCurrent(){
		double val=0;
		if(integral== null)
			val = signal;
		else{
			//val = integral.getValue();
			val=currentSensorValue;
		}
		return scaleValue(val);
	}
	
	private double scaleValue(double in){
		double scale = (2.5//Reference voltage actual volts
				*1024.0)
				/reference;
		double i=.001;//Ohms of shunt
		
		double ampScale = (1/32.5)*0.8064*2;//Amp gain
		
		
		return (((in/1024.0)*scale)*ampScale)/(i);
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		this.currentSensorValue = value;
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalChannel){
			if(scaleValue(value) < getAlarmThreshhold()){
				signal = value;
				integral.add( value);
			}else{
				//System.out.println("Curren val = "+scaleValue(value)+ " treshhold= "+getAlarmThreshhold());
			}
			
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
			System.err.println("Using port: "+args[0]);
			con  = new SerialConnection(args[0]);
		}
		DyIO.disableFWCheck();
		DyIO dyio = new DyIO(con);
		System.err.println("Connecting DyIO");
		Log.setMinimumPrintLevel(Log.INFO);
		dyio.connect();
		Log.setMinimumPrintLevel(Log.WARNING);
		System.err.println("DyIO Connected");
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
		return configuration.getSecondsPolling();
	}
	/**
	 * Sets the internal variable for the polling rate
	 * @param pollingRate The polling rate in seconds
	 */
	public void setPollingRate(int pollingRate) {
		configuration.setSecondsPolling(pollingRate*1000);
	}

	public void setScale(double integer) {
		configuration.setScaleFactor(integer);
	}

	public double getScale() {
		return configuration.getScaleFactor();
	}

	public void clearData() {
		configuration.setDailyTotal(0);
		logger.clearData(name);
	}
	
	public double getAlarmThreshhold() {
		return configuration.getAlarmThreshhold();
	}

	public void setAlarmThreshhold(double alarmThreshhold) {
		configuration.setAlarmThreshhold(alarmThreshhold);
	}

	public void dumpLogs(final int num) {
		new Thread(){
			public void run(){
				ThreadUtil.wait(1000);
				int fileIndex=-1;
				for(int j=0;j<logger.getNumberOfFiles();j++){
					if(num==0){
						fileIndex=j;	
					}else{
						//cause the loop to exit after just the one
						j=logger.getNumberOfFiles();
					}
					for (int i=0;i<logger.getNumberOfLogLines(name,fileIndex);i++){
						pushAsyncPacket(logger.getLogLine(i, name,fileIndex).getPacket(dyio.getAddress()));
						ThreadUtil.wait(100);
					}
				}
			}
		}.start();
	}
	
}
