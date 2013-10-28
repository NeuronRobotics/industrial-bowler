package com.neuronrobotics.industrial;

import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.jfree.util.Log;

import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.sdk.network.BowlerTCPClient;


public class BathMoniterFactory {
	//static BowlerUDPClient clnt;
	
	public static ArrayList<BathMoniter> getBathMoniterList(){
		
		ArrayList<BathMoniter> list = new ArrayList<BathMoniter>();
		
		//if(clnt == null)
		//	clnt=new BowlerUDPClient(1865);
		ArrayList<InetAddress>  addrs = BowlerTCPClient.getAvailableSockets();
		int j=0;
		for (InetAddress i:addrs) {
			System.out.println((j++) +" Adding "+i);
		}
		if(addrs.size()>0){
			for (InetAddress i:addrs) {
				//System.out.println("Adding "+i);
				BathMoniterDevice d=null;
				try {
					BowlerTCPClient tcp = new BowlerTCPClient(i, 1866);
					System.out.println("TCP socket connected");
					d = new BathMoniterDevice(tcp);
					d.connect();
					list.add(new BathMoniter(d,i.getHostAddress()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					d=null;
					Log.error("Soccet in use, trying "+(1866) );
				}
			}
		}else{
			JOptionPane.showMessageDialog(null, "No bath detected, check internet connection", "No Baths Found", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		return list;
		
	}

}
