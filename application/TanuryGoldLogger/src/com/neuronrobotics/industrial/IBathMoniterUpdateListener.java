package com.neuronrobotics.industrial;

public interface IBathMoniterUpdateListener {

	/**
	 * When the name of the bath changes, this is called
	 * @param newName
	 */
	public void onNameChange(String newName);
	
	/**
	 * This fires when there is a new data point available
	 * @param bathName
	 * @param timestamp
	 * @param currentOzHrRate
	 */
	public void onValueChange(String bathName, long timestamp, double currentOzHrRate);
	
	/**
	 * THis fires when a value change crosses an alarm threshhold
	 * @param bathName
	 * @param timestamp
	 * @param currentOzHrRate
	 * @param alarmThreshhold
	 */
	public void onAlarmEvenFire(String bathName, long timestamp, double currentOzHrRate, double alarmThreshhold);
	
}
