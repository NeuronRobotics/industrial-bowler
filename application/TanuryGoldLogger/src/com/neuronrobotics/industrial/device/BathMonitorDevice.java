package com.neuronrobotics.industrial.device;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.network.AbstractUdpDeviceServer;
import com.neuronrobotics.sdk.serial.SerialConnection;

public class BathMonitorDevice extends AbstractUdpDeviceServer{
	


	public BathMonitorDevice(BowlerAbstractConnection device) {
		super(device);
		
	}
	@Override
	public BowlerDatagram process(BowlerDatagram data) {
		// TODO Auto-generated method stub
		return null;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length != 1){
			System.err.println("Invalid port spesified");
			System.exit(1);
		}else{
			System.out.println("Using port: "+args[0]);
		}
		DyIO.disableFWCheck();
		DyIO dyio = new DyIO(new SerialConnection(args[0]));
		dyio.connect();
		new BathMonitorDevice(dyio.getConnection());
	}
}
