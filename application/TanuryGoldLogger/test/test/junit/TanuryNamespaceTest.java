package test.junit;

import static org.junit.Assert.*;

import java.net.InetAddress;
import java.util.ArrayList;

import org.jfree.util.Log;
import org.junit.Test;

import com.neuronrobotics.industrial.BathMoniter;
import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.sdk.network.BowlerTCPClient;

public class TanuryNamespaceTest {

	@Test
	public void test() {
		ArrayList<InetAddress>  addrs = BowlerTCPClient.getAvailableSockets();
		if(addrs.size() ==0)
			fail("No baths online");
		int j=0;
		for (InetAddress i:addrs) {
			System.out.println((j++) +" Adding "+i);
		}
		
		for (InetAddress i:addrs) {
			//System.out.println("Adding "+i);
			BathMoniterDevice d=null;
			try {
				BowlerTCPClient tcp = new BowlerTCPClient(i, 1866);
				System.out.println("TCP socket connected");
				d = new BathMoniterDevice(tcp);
				d.connect();
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
