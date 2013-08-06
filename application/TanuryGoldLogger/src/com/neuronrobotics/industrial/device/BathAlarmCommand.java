package com.neuronrobotics.industrial.device;

import com.neuronrobotics.sdk.common.BowlerAbstractCommand;
import com.neuronrobotics.sdk.common.BowlerMethod;

public class BathAlarmCommand extends BowlerAbstractCommand {
	
	public BathAlarmCommand(BathAlarmEvent ev){
		 setOpCode("alrm");
		 setMethod(BowlerMethod.ASYNCHRONOUS);
		 getCallingDataStorage().add(ev.getBathName());
		 getCallingDataStorage().add(0);
		 getCallingDataStorage().addAs32((int) ev.getTimestamp());
		 getCallingDataStorage().addAs32((int)(ev.getCurrentOzHrRate()*1000));
		 getCallingDataStorage().addAs32((int)(ev.getAlarmThreshhold()*1000));
	}

}
