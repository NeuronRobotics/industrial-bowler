package com.neuronrobotics.crustcrawler.main;

import com.neuronrobotics.crustcrawler.CrustCrawlerUnderwaterROV;
import com.neuronrobotics.sdk.dyio.DyIO;
import com.neuronrobotics.sdk.serial.SerialConnection;
import com.neuronrobotics.sdk.util.ThreadUtil;

public class CrustCrawlerServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SerialConnection s=new SerialConnection("/dev/ttyDyIO0");
			DyIO dyio = new DyIO(s);
			dyio.connect();
			
			new CrustCrawlerUnderwaterROV(dyio);
			
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
