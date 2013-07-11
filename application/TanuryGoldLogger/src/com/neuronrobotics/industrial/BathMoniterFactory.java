package com.neuronrobotics.industrial;

import java.net.InetAddress;
import java.util.ArrayList;

import org.jfree.util.Log;

import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.dyio.DyIORegestry;
import com.neuronrobotics.sdk.network.BowlerUDPClient;
import com.neuronrobotics.sdk.ui.ConnectionDialog;


public class BathMoniterFactory {
	static BowlerUDPClient clnt;
	
	public static ArrayList<BathMoniter> getBathMoniterList(){
		DyIO.disableFWCheck();
		ArrayList<BathMoniter> list = new ArrayList<BathMoniter>();
		
		if(clnt == null)
			clnt=new BowlerUDPClient(1865);
		ArrayList<InetAddress>  addrs = clnt.getAllAddresses();
		if(addrs.size()>0){
			for (InetAddress i:addrs) {
				Log.info("Adding "+i);
			}
		}else{
			DyIORegestry.setConnection(ConnectionDialog.promptConnection());
		}
		if(list.size()==0){
			for(int i=0;i<5;i++){
				list.add(new BathMoniter(DyIORegestry.get()));
			}
		}
		
		return list;
		
	}

}
