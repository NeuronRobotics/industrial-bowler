package com.neuronrobotics.industrial.device;

import java.sql.Timestamp;

import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.MACAddress;

public class BathAlarmEvent {
	private String bathName;
	private long timestamp;
	private double currentOzHrRate;
	private double alarmThreshhold;
	
	public BathAlarmEvent (String bathName, long timestamp, double currentOzHrRate, double alarmThreshhold){
		this.bathName = bathName;
		this.timestamp = timestamp;
		this.currentOzHrRate = currentOzHrRate;
		this.alarmThreshhold = alarmThreshhold;
		
	}
	public  BathAlarmEvent (BowlerDatagram bd) {
		ByteList data = bd.getData();
		String s = data.asString();
		data.popList(s.length()+1);
		this.setBathName(s );
		long high = new Integer(ByteList.convertToInt(data.popList(4),false));
		long low  = new Integer(ByteList.convertToInt(data.popList(4),false));
		this.setTimestamp( ((high+1)<<32) +low );
		this.setCurrentOzHrRate(new Double(ByteList.convertToInt(data.popList(4)))/1000.0);
		this.setAlarmThreshhold(new Double(ByteList.convertToInt(data.popList(4)))/1000.0);
	}
	
	public BowlerDatagram getPacket(MACAddress mac) {
		return BowlerDatagramFactory.build(mac, new BathAlarmCommand(this));
	}
	
	public String getBathName() {
		return bathName;
	}
	public void setBathName(String bathName) {
		this.bathName = bathName;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getCurrentOzHrRate() {
		return currentOzHrRate;
	}
	public void setCurrentOzHrRate(double currentOzHrRate) {
		this.currentOzHrRate = currentOzHrRate;
	}
	public double getAlarmThreshhold() {
		return alarmThreshhold;
	}
	public void setAlarmThreshhold(double alarmThreshhold) {
		this.alarmThreshhold = alarmThreshhold;
	}
	
	private String getDate(){
		Timestamp t = new Timestamp(System.currentTimeMillis());
		return t.toString().split("\\ ")[0];
	}
	
	@Override
	public String toString(){
		
		String s="";
		s+=getBathName()+"\n ";
		s+=getDate()+"\n";
		s+=currentOzHrRate+" Mili-Amps (current) \n";
		s+= alarmThreshhold+" Mili-Amps (Threshhold)";
		return s;
	}
	
}
