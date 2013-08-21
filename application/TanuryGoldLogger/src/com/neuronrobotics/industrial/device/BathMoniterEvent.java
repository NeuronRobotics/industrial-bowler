package com.neuronrobotics.industrial.device;

import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerDatagramFactory;
import com.neuronrobotics.sdk.common.ByteList;
import com.neuronrobotics.sdk.common.MACAddress;

public class BathMoniterEvent {
	
	private String bathName;
	private long timestamp;
	private double currentOzHrRate;
	private double totalUsedToday;
	
	public BathMoniterEvent(String bathName, long timestamp, double currentOzHrRate, double totalUsedToday){
		this.setTotalUsedToday(totalUsedToday);
		this.setBathName(bathName);
		this.setTimestamp(timestamp);
		this.setCurrentOzHrRate(currentOzHrRate);
		
	}

	public BathMoniterEvent(BowlerDatagram bd) {
		
		ByteList data = bd.getData();
		String s = data.asString();
		data.popList(s.length()+1);
		this.setBathName(s );
		this.setTimestamp( new Integer(ByteList.convertToInt(data.popList(4),true)));
		this.setCurrentOzHrRate(new Double(ByteList.convertToInt(data.popList(4)))/1000.0);
		this.setTotalUsedToday(new Double(ByteList.convertToInt(data.popList(4)))/1000.0);
		
	}

	public BathMoniterEvent(String sCurrentLine) {
		String [] data = sCurrentLine.toString().split(",");

		this.setTotalUsedToday(Double.parseDouble(data[4]));
		this.setBathName(data[3]);
		this.setTimestamp(Long.parseLong(data[1]));
		this.setCurrentOzHrRate(Double.parseDouble(data[5]));
	}

	public BowlerDatagram getPacket(MACAddress mac) {
		return BowlerDatagramFactory.build(mac, new BathMonitorCommand(this));
	}

	public String getBathName() {
		return bathName;
	}

	public void setBathName(String bathName) {
		this.bathName = bathName;
	}

	public long getTimestamp() {
		return timestamp;//hours
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getCurrentOzHrRate() {
		return currentOzHrRate;//MiliAmps
	}

	public void setCurrentOzHrRate(double currentOzHrRate) {
		this.currentOzHrRate = currentOzHrRate;
	}
	public double getTotalUsedToday() {
		return totalUsedToday;//amp-hour*scale;
	}
	
	public double getScaledTotalUsedToday() {
		return totalUsedToday/(1);//amp-hour*scale;
	}

	public void setTotalUsedToday(double totalUsedToday) {
		this.totalUsedToday = totalUsedToday;
	}
	
	
	@Override
	public String toString(){
		
		String s="";
		s+=getBathName()+" ";
		s+=timestamp+"ms ";
		s+=currentOzHrRate+"Oz/Hr ";
		s+= totalUsedToday+"Oz ";
		return s;
	}



}
