package com.neuronrobotics.industrial;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import org.jfree.util.Log;

import com.neuronrobotics.industrial.device.BathMoniterDevice;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.ui.ConnectionDialog;


public class BathMoniterFactory {
	//static BowlerUDPClient clnt;
	
	public static ArrayList<BathMoniter> getBathMoniterList(){
		
		ArrayList<BathMoniter> list = new ArrayList<BathMoniter>();
		
		//if(clnt == null)
		//	clnt=new BowlerUDPClient(1865);
		ArrayList<InetAddress>  addrs = BowlerTCPClient.getAvailableSockets();
		if(addrs.size()>0){
			for (InetAddress i:addrs) {
				System.out.println("Adding "+i);
				BathMoniterDevice d;
				try {
					d = new BathMoniterDevice(new BowlerTCPClient(i, 1866));
					d.connect();
					list.add(new BathMoniter(d));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			BathMoniterDevice d =new BathMoniterDevice();
			d.setConnection(ConnectionDialog.promptConnection());
			d.connect();
			for(int i=0;i<5;i++){
				list.add(new BathMoniter(d));
			}
		}
		
		return list;
		
	}

}
