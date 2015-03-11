package com.neuronrobotics.industrial.tool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.neuronrobotics.sdk.network.BowlerUDPClient;

public class UdpNetworkTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String myIp=null;
		Socket s;
		try {
			s = new Socket("google.com", 80);
			myIp=(s.getLocalAddress().getHostAddress());
			//System.out.println(s.getLocalAddress().getHostAddress());
			s.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BowlerUDPClient clnt=new BowlerUDPClient();
		ArrayList<InetAddress>  addrs = clnt.getAllAddresses();
		for (InetAddress a:addrs){
			if (myIp.contains(a.getHostAddress())){
				System.out.println("Device alive");
				System.exit(0);
			}
		}
		System.out.println("No device found");
		System.exit(-1);
	}

}
