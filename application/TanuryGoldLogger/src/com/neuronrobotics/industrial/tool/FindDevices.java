package com.neuronrobotics.industrial.tool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import com.neuronrobotics.sdk.network.BowlerUDPClient;

public class FindDevices {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		BowlerUDPClient clnt=new BowlerUDPClient();
		ArrayList<InetAddress>  addrs = clnt.getAllAddresses();
		for (InetAddress a:addrs){
			System.out.println(a.getHostAddress());
		}
		System.exit(0);
	}

}
