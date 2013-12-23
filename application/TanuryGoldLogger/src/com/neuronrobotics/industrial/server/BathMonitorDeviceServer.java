package com.neuronrobotics.industrial.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
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
	private double localTotal=0;
	private double lastSampleTime=-1;
	static{
		DyIO.disableFWCheck();
	}

	public BathMonitorDeviceServer(DyIO device) {
		super(device.getAddress());
		lastPacketDay = cal.get(Calendar.DAY_OF_MONTH);
		Log.info("Starting configuration XML");
		configuration = new DeviceConfiguration();
		Log.info("Starting logger");
		logger = new TanuryDataLogger("device");
		Log.info("Adding namespaces");
		addBowlerDeviceServerNamespace(new TanuryBathNamespaceImp(this,getMacAddress()));
	

		
		
		dyio=device;
		dyio.getConnection().setSynchronusPacketTimeoutTime(1000);
		Log.info("Resetting Inputs");
		for (int i=0;i<24;i++){
			if(	i!=12 &&
				i!=13 &&
				i!=14 &&
				i!=15	){
				dyio.setMode(i, DyIOChannelMode.DIGITAL_OUT, false);
			}
		}
		
		Log.info("Starting analog");
		referenceVoltage = 	new AnalogInputChannel(dyio, 15);
		otherVoltage	 = 	new AnalogInputChannel(dyio, 14);
		tempVoltage 	 = 	new AnalogInputChannel(dyio, 13);
		signalChannel 	 = 	new AnalogInputChannel(dyio, 12);
		
		referenceVoltage.setAsync(false);
		otherVoltage.setAsync(false);
		tempVoltage.setAsync(false);
		signalChannel.setAsync(false);
		
		
		Log.info("Initializing values");
		reference = referenceVoltage.getValue();
		signal    = signalChannel.getValue();
		//integral = new RollingAverageFilter(30, getCurrent());

		Log.info("Starting poll thread");
		new Thread(){
			double ioPoll =  300;
			public void run(){
				while(dyio.isAvailable()){
					ThreadUtil.wait((int) ioPoll);
					// Software lowpass, pull 100 values average them and push this up
					double signalAvg = 0.0;
					
					for(int l=0; l<100; l++)
						signalAvg = signalAvg+signalChannel.getValue();
					signalAvg=signalAvg/100.0;
					
					Log.info("Avg Val:\t"+signalAvg);

					onAnalogValueChange(signalChannel, signalAvg);
					
				}
			}
		}.start();
		Log.info("Starting up stream thread");
		new Thread(){
			public void run(){
				ThreadUtil.wait((int) getPollingRate());
				while(dyio.isAvailable()){
					try{
						double currentVal = scaleValue(signal);
						
						if(currentVal > getAlarmThreshhold()){
							BathAlarmEvent ev = new BathAlarmEvent(	getDeviceName(),
																	System.currentTimeMillis(), 
																	getCurrent(),
																	getAlarmThreshhold());
							logger.onAlarmEvenFire(ev);
							pushAsyncPacket(ev.getPacket(dyio.getAddress()));
							
						}else{
							
							configuration.setDailyTotal(localTotal);
							BathMoniterEvent be = new BathMoniterEvent(	getDeviceName(), 
																		System.currentTimeMillis(), 
																		getCurrent(),
																		localTotal/getScale());
							logger.onValueChange(be, 0);
							Log.info("Pushing time "+new Timestamp(be.getTimestamp())+" recorded at "+TanuryDataLogger.getDate(be.getTimestamp()));
					
							BowlerDatagram bd  = be.getPacket(dyio.getAddress());
							
							pushAsyncPacket(bd);
							
							if(lastPacketDay != cal.get(Calendar.DAY_OF_MONTH)){
								lastPacketDay = cal.get(Calendar.DAY_OF_MONTH);
								//This is where the daily total is reset at midnight
								configuration.setDailyTotal(0);
								localTotal=0;
							}
							
						}
						Log.info("Voltage = "+getCurrent());
						ThreadUtil.wait((int) getPollingRate());
					}catch(Exception ex){
						ex.printStackTrace();
					}
				}
				new RuntimeException("The main opperating thread died").printStackTrace();
				System.exit(-1);
			}
		}.start();
		
		Log.info("Starting UDP");
		try {
			startNetworkServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
		
		localTotal = configuration.getDailyTotal();
		
		Log.info("System ONLINE");
		Log.enableDebugPrint();
	}
	
	public double getCurrent(){
		double val=0;
		if(integral== null)
			val = signal;
		else{
			val = integral.getValue();
			val=signal;
		}
		return scaleValue(val);
	}
	
	private double scaleValue(double in){
		// get a fresh reference value
		reference = referenceVoltage.getValue();

		// the adc can't discern the diff between the ref and the supply.
		double scale = (2.5//Reference voltage actual volts
				*1024.0)
				/512;
		double shunt=.001;//Ohms of shunt
		double gain=33.0;
		double pivotGain=gain*1.02;
		
		//double ampScale = (1.0/32.6)*0.8064*2* 0.5362349021241151;//Amp gain
		/*
		//double ampScale = 		(1.0/28)	*0.8648396501457726;//Amp gain simplified
		double ampScale = 	(1.0/gain)	*0.8648396501457726;//Amp gain simplified
		
		double calcVal = (((in/1024.0)*scale)*ampScale)/(i);
		
		//if(calcVal>20)
		//	calcVal = (((in/1024.0)*scale)*ampScaleHigh)/(i);*/
		double vCorrect= (2.5/(reference*(5.0/1024.0)));
		// a litttle bit of a pivot. gain drops off when we're below about 10mv
		

		
		double calcVal = ((in*(5.0/1024.0))/gain)*(1.0/shunt)*vCorrect;
		
		if(calcVal<15)
			calcVal = ((in*(5.0/1024.0))/pivotGain)*(1.0/shunt)*vCorrect;
		
	//System.out.println("raw:\t"+in+"\tref:"+reference+"\tcalc:\t"+calcVal+"\tgain:\t"+gain);
	

		return calcVal;
	}
	
	@Override
	public void onAnalogValueChange(AnalogInputChannel chan, double value) {
		
		if(lastSampleTime<0){
			lastSampleTime=System.currentTimeMillis();
		}
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalChannel){
			if(scaleValue(value) < getAlarmThreshhold()){
				signal = value;
				double msSample = ((double)System.currentTimeMillis())-lastSampleTime;
				localTotal += getCurrent()*( msSample/(60.0*60.0*1000.0));
				//integral.add( value);
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
		Log.enableDebugPrint();
		
		Log.setUseColoredPrints(true);
		
		dyio.connect();
		
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
		localTotal=0;
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
					try{
						for (int i=0;i<logger.getNumberOfLogLines(name,fileIndex);i++){
							pushAsyncPacket(logger.getLogLine(i, name,fileIndex).getPacket(dyio.getAddress()));
							ThreadUtil.wait(100);
						}
					}catch (Exception e){
						
					}
				}
			}
		}.start();
	}
	
}
