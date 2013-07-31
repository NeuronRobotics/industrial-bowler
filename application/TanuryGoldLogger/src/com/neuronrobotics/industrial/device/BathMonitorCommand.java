package com.neuronrobotics.industrial.device;

import com.neuronrobotics.sdk.common.BowlerAbstractCommand;
import com.neuronrobotics.sdk.common.BowlerMethod;

public class BathMonitorCommand extends BowlerAbstractCommand{

	 public BathMonitorCommand(BathMoniterEvent ev){
		 setOpCode("bath");
		 setMethod(BowlerMethod.ASYNCHRONOUS);
		 getCallingDataStorage().add(ev.getBathName());
		 getCallingDataStorage().addAs32((int) ev.getTimestamp());
		 getCallingDataStorage().addAs32((int)(ev.getCurrentOzHrRate()*1000));
	 }

}
