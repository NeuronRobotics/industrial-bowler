package com.neuronrobotics.industrial;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.swing.JOptionPane;

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
				BathMoniterDevice d=null;
				int socket=0;
				do{
					try {
						BowlerTCPClient tcp = new BowlerTCPClient(i, 1866+socket);
						System.out.println("TCP socket connected");
						d = new BathMoniterDevice(tcp);
						d.connect();
						list.add(new BathMoniter(d));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						d=null;
						socket++;
						Log.error("Soccet in use, trying "+(1866+socket) );
					}
				}while(d==null && socket<10);
			}
		}else{
			JOptionPane.showMessageDialog(null, "No bath detected, check internet connection", "No Baths Found", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		
		return list;
		
	}

}
