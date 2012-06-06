package com.neuronrobotics.crustcrawler;

import com.neuronrobotics.sdk.common.BowlerAbstractDeviceServer;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.dyio.DyIO;

public class CrustCrawlerUnderwaterROV extends BowlerAbstractDeviceServer {
	
	private final DyIO dyio;

	public CrustCrawlerUnderwaterROV(DyIO dyio) {
		this.dyio = dyio;
		
	}

	@Override
	public void onSynchronusRecive(BowlerDatagram data) {
		// TODO Auto-generated method stub

	}

}
