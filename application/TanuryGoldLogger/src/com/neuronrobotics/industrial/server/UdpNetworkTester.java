package com.neuronrobotics.industrial.server;

import java.net.InetAddress;
import java.util.ArrayList;

import com.neuronrobotics.sdk.network.BowlerUDPClient;

public class UdpNetworkTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BowlerUDPClient clnt=new BowlerUDPClient(1865);
		ArrayList<InetAddress>  addrs = clnt.getAllAddresses();
		for (InetAddress a:addrs){
			if (args[0].contains(a.getHostAddress())){
				System.out.println("Device alive");
				System.exit(0);
			}
		}
		System.out.println("No device found");
		System.exit(-1);
	}

}
