package com.neuronrobotics.crustcrawler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDeviceServer;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;

public class CrustCrawlerUnderwaterROV extends BowlerAbstractDeviceServer {
	private String webcamNms = "neuronrobotics.bowlercam.*;0.3;;";
	private final DyIO dyio;
	private String address="localhost";
	public CrustCrawlerUnderwaterROV(BowlerAbstractConnection connection,DyIO dyio, String videoFile) throws IOException {
		this.dyio = dyio;
		//TODO Kick off the video server using the string of what file to use
		init(connection);
	}

	private void init(BowlerAbstractConnection connection ) throws IOException {
		Log.info("Starting BowlerCam Server");
		try{
	        Enumeration e = NetworkInterface.getNetworkInterfaces();
	        while(e.hasMoreElements()){
	        	NetworkInterface ni =(NetworkInterface)  e.nextElement();
		        Enumeration e2 = ni.getInetAddresses();
		        while(e2.hasMoreElements()){
		        	InetAddress ip = (InetAddress) e2.nextElement();
		        	if(!(ip.isAnyLocalAddress() || ip.isLinkLocalAddress()||ip.isLoopbackAddress()||ip.isMulticastAddress()))
		        		address=(ip.getHostAddress()); 
		        }
	        }
		}catch(Exception e){}
		addNamespace(webcamNms);
		addNamespace("neuronrobotics.crustcrawler.urov.*;0.3;;");
		setConnection(connection);
		connect();
	}

	@Override
	public void onSynchronusRecive(BowlerDatagram data) {
		// TODO Auto-generated method stub

	}

}
