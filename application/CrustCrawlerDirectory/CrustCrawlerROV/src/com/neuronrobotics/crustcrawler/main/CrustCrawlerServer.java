package com.neuronrobotics.crustcrawler.main;

import com.neuronrobotics.crustcrawler.CrustCrawlerUnderwaterROV;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.network.BowlerUDPServer;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class CrustCrawlerServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			if(args.length != 2 || !args[0].contains("DyIO")  || !args[1].contains("video")) {
				System.err.println("Usage Example: java -jar CrustCrawlerServer.jar /dev/DyIO0 /dev/video0");
				System.exit(1);
			}
			SerialConnection s=new SerialConnection(args[0]);
			DyIO dyio = new DyIO(s);
			dyio.connect();
			
			new CrustCrawlerUnderwaterROV(new BowlerUDPServer(),dyio, args[1]);
			
			while(s.isConnected()) {
				ThreadUtil.wait(100);
			}
			System.exit(0);
			
		}catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
