package test.junit;

import static org.junit.Assert.*;

import org.junit.Test;

import com.neuronrobotics.industrial.device.BathMoniterEvent;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.MACAddress;

public class TestBathMoniterTimestamp {

	@Test
	public void testBathMoniterEvent() {
		long time = System.currentTimeMillis();
		BathMoniterEvent be = new BathMoniterEvent(	"testName", 
													time, 
													13,
													200);
		System.out.println(be);
		
		BowlerDatagram dg = be.getPacket(new MACAddress());
		System.out.println(dg);
		
		BathMoniterEvent newEvent = new BathMoniterEvent(dg);
		
		System.out.println(newEvent);
		System.out.println(newEvent.getPacket(new MACAddress()));
		
		System.out.println("Actual time was "+time);
		if(be.getTimestamp() != time){
			System.err.println("The first event was wrong");
			fail();
		}
		
		if(newEvent.getTimestamp() != be.getTimestamp()){
			System.err.println("The two events are different");
			System.err.println("High word "+(int)(newEvent.getTimestamp()>>32));
			System.err.println("Low Word  "+(int)(newEvent.getTimestamp()));
			System.err.println("High word "+(int)(be.getTimestamp()>>32));
			System.err.println("Low Word  "+(int)(be.getTimestamp()));
			fail();
			
			
		}
		
	}

}
