package com.neuronrobotics.industrial.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.neuronrobotics.industrial.TanuryDataLogger;
import com.neuronrobotics.industrial.device.BathAlarmEvent;
import com.neuronrobotics.industrial.device.BathMoniterEvent;
import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.MACAddress;
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

public class BathMonitorDeviceServer extends BowlerAbstractServer{
	
	private AnalogInputChannel referenceVoltage;
	private AnalogInputChannel signalChannel;
	private AnalogInputChannel tempVoltage;
	private AnalogInputChannel otherVoltage;
	private double reference;
	private double signal;
	private RollingAverageFilter integral; 
	private Calendar lastPacketDay=null;
	private Calendar cal = Calendar.getInstance();
	
	private DyIO dyio;
	private String name = null;
	private DeviceConfiguration configuration = null;
	private TanuryDataLogger logger = null;
	private double localTotal=0;
	private double lastSampleTime=-1;
	private MACAddress mymac;
	private double currentVal;
	private double ampTuneValue =1;
	static{
		DyIO.disableFWCheck();
	}
	
	

	public BathMonitorDeviceServer(DyIO device, MACAddress addr) {
		super(addr);
		if(device!= null){
			setupDevice(device);
			
		}else{
			Log.error("No device connected");
		}
		Log.info("Starting up stream thread");
		new Thread(){
			

			public void run(){
				ThreadUtil.wait((int) getPollingRate());
				while(true){
					try{
						
						if(currentVal > getAlarmThreshhold()){
							BathAlarmEvent ev = new BathAlarmEvent(	getDeviceName(),
																	System.currentTimeMillis(), 
																	currentVal,
																	getAlarmThreshhold());
							logger.onAlarmEvenFire(ev);
							pushAsyncPacket(ev.getPacket(getMymac() ));
							
						}else{
							
							configuration.setDailyTotal(localTotal);
							BathMoniterEvent be = new BathMoniterEvent(	getDeviceName(), 
																		System.currentTimeMillis(), 
																		currentVal,
																		localTotal/getScale());
							logger.onValueChange(be, 0);
							Log.info("Pushing time "+new Timestamp(be.getTimestamp())+" recorded at "+TanuryDataLogger.getDate(be.getTimestamp()));
					
							BowlerDatagram bd  = be.getPacket(getMymac() );
							
							pushAsyncPacket(bd);
							cal = Calendar.getInstance();
							if(lastPacketDay.get(Calendar.DAY_OF_MONTH) != cal.get(Calendar.DAY_OF_MONTH)){
								Log.warning("Resetting the daily total "+localTotal+" was "+lastPacketDay+" is "+cal.get(Calendar.DAY_OF_MONTH));
								lastPacketDay = cal;
								//This is where the daily total is reset at midnight
								configuration.setDailyTotal(0);
								localTotal=0;
								
							}else{
								//Log.debug("Today is the same, no reset "+localTotal+" was "+lastPacketDay+" is "+cal.get(Calendar.DAY_OF_MONTH));
							}

							
						}
						Log.info("Voltage = "+currentVal);
						ThreadUtil.wait((int) getPollingRate());
					}catch(Exception ex){
						Log.error("Exception in main upstream thread "+ex.getMessage());
						Log.error("Main loop exiting, resetting");

						System.exit(-1);
					}
				}
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
		ampTuneValue= configuration.getAmpTuneValue();
		
		Log.info("System ONLINE");

	}
	
	private void setupDevice(DyIO device){
		lastPacketDay =  Calendar.getInstance();
		Log.info("Starting configuration XML");
		configuration = new DeviceConfiguration();
		Log.info("Starting logger");
		logger = new TanuryDataLogger("device");
		Log.info("Adding namespaces");
		addBowlerDeviceServerNamespace(new TanuryBathNamespaceImp(this,getMacAddress()));
	

		
		
		dyio=device;
		dyio.getConnection().setSynchronusPacketTimeoutTime(1750);
		Log.info("Resetting Inputs");
		for (int i=0;i<24;i++){
			if(	i!=12 &&
				i!=13 &&
				i!=14 &&
				i!=15	){
				dyio.setMode(i, DyIOChannelMode.DIGITAL_OUT, false);
			}
		}
		setMymac(dyio.getAddress());
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
			double ioPoll =  50;
			public void run(){
				long ts=-1;
				double LastIntegral=0;
				while(true){
					BowlerDatagramFactory.setPacketTimeout(dyio.getConnection().getSleepTime());
					while(dyio.isAvailable()){
						try{
							ThreadUtil.wait(1);
							
							// Software lowpass, pull 100 values average them and push this up
							double signalAvg = 0.0;
							int level = Log.getMinimumPrintLevel();
							//Log.enableDebugPrint();
							//dyio.getConnection().reconnect();
							for(int l=0; l<ioPoll; l++){
								signalAvg += signalChannel.getValue();
							}
							signalAvg /= ioPoll;
							onAnalogValueChange(signalChannel, signalAvg);
							currentVal = getCurrent();
							if (ts<0){
								ts=System.currentTimeMillis();
							}
							double diffMs = (double)(System.currentTimeMillis()-ts);
							double diffHours = diffMs/(60.0*60.0*1000.0);
							double calcTotalDifference = localTotal-LastIntegral;
							double ampHrIncrease = currentVal * diffHours;
							ts=System.currentTimeMillis();
							LastIntegral=localTotal;
							
							Log.info(	"Avg Amps\t\t="+currentVal+"" +
										"\nFor\t\t\t="+diffHours+ "hr "+diffMs+"ms"+
										"\nAmp-hour difference\t="+calcTotalDifference+
										"\nExpected\t\t="+ ampHrIncrease);
							
							
							
							Log.setMinimumPrintLevel(level);
							Calendar c = Calendar.getInstance();
							if(		(c.get(Calendar.HOUR_OF_DAY) ==5 && c.get(Calendar.MINUTE) == 0) 
									//||(c.get(Calendar.HOUR_OF_DAY) ==12 && c.get(Calendar.MINUTE) == 15)
									){
								Log.error("Controlled Exiting system");
								ThreadUtil.wait(60000);
								System.exit(0);
							}
						}catch(Exception e){
							
							Log.error("Exception in main loop, reconnecting "+e.getMessage());
							Log.error("Main loop exiting, resetting");

							System.exit(-1);
						}
					}
					Log.error("Main loop exiting, resetting");

					System.exit(-1);
					
				}
			}
		}.start();
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
		double scale = (reference//Reference voltage
				*1024.0)
				/512;
		double shunt=.001;//Ohms of shunt
		double gain=32.64;
		double pivotGain=gain*0.96;
		
		//double ampScale = (1.0/32.6)*0.8064*2* 0.5362349021241151;//Amp gain
		/*
		//double ampScale = 		(1.0/28)	*0.8648396501457726;//Amp gain simplified
		double ampScale = 	(1.0/gain)	*0.8648396501457726;//Amp gain simplified
		
		double calcVal = (((in/1024.0)*scale)*ampScale)/(i);
		
		//if(calcVal>20)
		//	calcVal = (((in/1024.0)*scale)*ampScaleHigh)/(i);*/
		double vCorrect= (2.5/(reference*(5.0/1024.0)))*ampTuneValue;
		//double vCorrect= (2.5/(reference*(5.0/1024.0)))*4;// bath 1 only
		// a litttle bit of a pivot. gain drops off when we're below about 10mv
		

		
		double calcVal = ((in*(5.0/1024.0))/gain)*(1.0/shunt)*vCorrect;
		
		if(calcVal<15)
			calcVal = ((in*(5.0/1024.0))/pivotGain)*(1.0/shunt)*vCorrect;
		
	//System.out.println("raw:\t"+in+"\tref:"+reference+"\tcalc:\t"+calcVal+"\tgain:\t"+gain);
	
		if(calcVal<1.0)
			calcVal=0;
		return calcVal;
	}

	private void onAnalogValueChange(AnalogInputChannel chan, double value) {
		
		if(lastSampleTime<0){
			lastSampleTime=System.currentTimeMillis();
		}
		if(chan == referenceVoltage){
			reference =  value;
		}if(chan == signalChannel){
			double scaled = scaleValue(value);
			
			if(scaled < getAlarmThreshhold()){
				signal = value;
				double msSample = ((double)System.currentTimeMillis())-lastSampleTime;
				double current =getCurrent(); 
				double hrSample = ( msSample/(60.0*60.0*1000.0));
				double ampHrIncrease = current * hrSample;
				Log.info("Adding "+ampHrIncrease+" amp-hours "+msSample);
				localTotal += ampHrIncrease;
				lastSampleTime=System.currentTimeMillis();
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
		File l = new File("RobotLog"+".txt");
		try {
			PrintStream p =new PrintStream(l);
			Log.setOutStream(new PrintStream(p));
			Log.setErrStream(new PrintStream(p));						
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
//		
		Log.enableDebugPrint();
		
		Log.setUseColoredPrints(true);
		
		dyio.connect();
		
		System.out.println("DyIO Connected");
		new BathMonitorDeviceServer(dyio, dyio.getAddress());
		//new BathMonitorDeviceServer(null,new MACAddress());
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

	public MACAddress getMymac() {
		if(mymac==null)
			mymac=dyio.getAddress();
		return mymac;
	}

	public void setMymac(MACAddress mymac) {
		this.mymac = mymac;
	}

	public void setAmpTune(Double double1) {
		ampTuneValue=double1;
		configuration.setAmpTuneValue(double1);
	}

	public double getAmpTune() {
		// TODO Auto-generated method stub
		return ampTuneValue=configuration.getAmpTuneValue();
	}
	
}
