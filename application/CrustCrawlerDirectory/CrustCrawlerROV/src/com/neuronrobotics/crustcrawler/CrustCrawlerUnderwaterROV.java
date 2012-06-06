package com.neuronrobotics.crustcrawler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import com.neuronrobotics.sdk.commands.bcs.core.PingCommand;
import com.neuronrobotics.sdk.commands.bcs.core.ReadyCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.bowlercam.ImageCommand;
import com.neuronrobotics.sdk.commands.neuronrobotics.bowlercam.ImageURLCommand;
import com.neuronrobotics.sdk.common.BowlerAbstractConnection;
import com.neuronrobotics.sdk.common.BowlerAbstractDeviceServer;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;

public class CrustCrawlerUnderwaterROV extends BowlerAbstractDeviceServer {
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
	        Enumeration<?> e = NetworkInterface.getNetworkInterfaces();
	        while(e.hasMoreElements()){
	        	NetworkInterface ni =(NetworkInterface)  e.nextElement();
		        Enumeration<?> e2 = ni.getInetAddresses();
		        while(e2.hasMoreElements()){
		        	InetAddress ip = (InetAddress) e2.nextElement();
		        	if(!(ip.isAnyLocalAddress() || ip.isLinkLocalAddress()||ip.isLoopbackAddress()||ip.isMulticastAddress()))
		        		address=(ip.getHostAddress()); 
		        }
	        }
		}catch(Exception e){}
		addNamespace("neuronrobotics.crustcrawler.urov.*;0.3;;");
		setConnection(connection);
		connect();
	}
	
	private void asyncTest() {
		try {
			sendAsync(new PingCommand(),0x01);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSynchronusRecive(BowlerDatagram data) {
		String rpc = data.getRPC();
		if(rpc.contains("imsv")){
			try {
				int camera = data.getData().getByte(0);
				sendSyncResponse(new ImageURLCommand( getURLString() ));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			// default response
			try {
				sendSyncResponse(new ReadyCommand(0,0));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String getURLString() {
		//TODO add to this any data needed for accessing the webcam server URL
		return "http://"+address;
	}

}
