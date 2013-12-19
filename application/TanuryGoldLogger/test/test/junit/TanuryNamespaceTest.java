package test.junit;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.industrial.server.BathMonitorDeviceServer;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.serial.SerialConnection;

public class TanuryNamespaceTest {
	private void StartServer(){
		SerialConnection con=null;
		List<String> ports = SerialConnection.getAvailableSerialPorts();
		for(String s:ports){
			System.err.println(s);
		}
		
		if(ports.size() >0)
			con = new SerialConnection(ports.get(0));
		
		DyIO.disableFWCheck();
		DyIO dyio = new DyIO(con);
		System.err.println("Connecting DyIO");
		dyio.connect();
		System.err.println("DyIO Connected");
		new BathMonitorDeviceServer(dyio);
	}
	@Test
	public void test() {
		
		ArrayList<InetAddress>  addrs;
		do{
			addrs = BowlerTCPClient.getAvailableSockets();
			if(addrs.size() ==0)
				StartServer();
		}while(addrs.size() ==0);
		
		int j=0;
		for (InetAddress i:addrs) {
			System.out.println((j++) +" Adding "+i);
		}
		
		Log.enableInfoPrint();
		
		for (InetAddress i:addrs) {
			//System.out.println("Adding "+i);
			BathMoniterDevice d=null;
			try {
				BowlerTCPClient tcp = new BowlerTCPClient(i, 1866);
				System.out.println("TCP socket connected");
				d = new BathMoniterDevice(tcp);
				d.connect();
				d.stopHeartBeat();
				d.ping();
				
				ArrayList<String> namespaces = d.getNamespaces();
				for(String s: namespaces){
					System.out.println(d.getRpcList(s));
				}
				//Name test
				System.out.println("Name test");
				String name = d.getName();
				d.setName(name);
				System.out.println("Polling test");
				//Polling rate
				int rate = d.getPollingRate();
				d.setPollingRate(rate);
				System.out.println("Scale test");
				//scale
				double scale = d.getScale();
				d.setScale(scale);
				System.out.println("Alarm test");
				//alarm
				double alarm = d.getAlarmLevel();
				d.setAlarmLevel(alarm);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				d=null;
				fail("Connection failed");
			}
		}
		
		
	}

}
