package com.neuronrobotics.crustcrawler;

import com.neuronrobotics.sdk.common.BowlerAbstractDeviceServer;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.dyio.DyIO;

public class CrustCrawlerUnderwaterROV extends BowlerAbstractDeviceServer {
	
	private final DyIO dyio;

	public CrustCrawlerUnderwaterROV(DyIO dyio, String videoFile) {
		this.dyio = dyio;
		//TODO Kick off the video server using the string of what file to use
	}


	@Override
	public void onSynchronusRecive(BowlerDatagram data) {
		// TODO Auto-generated method stub

	}

}
