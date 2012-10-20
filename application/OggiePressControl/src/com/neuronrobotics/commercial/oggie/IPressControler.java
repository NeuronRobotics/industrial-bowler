package com.neuronrobotics.commercial.oggie;

public interface IPressControler {
	
	/**
	 * Initializes the press cycle.
	 * @param m Matrix of time/tempreture data
	 * @param pressure the static pressure setpoint
	 */
	void onCycleStart(CycleConfig config);
	
	/**
	 * This method is called to abort a running cycle. 
	 */
	void abortCycle();
	
	/**
	 * This method it to retrieve the current pressure value from the press
	 * @return the pressure in tons
	 */
	double getCurrentPressure();

	/**
	 * Tells the press to start up to the target tempreture
	 * @param t
	 */
	void setTempreture(double t);
	
	/**
	 * Checks to see if press is up to temperature
	 * @return temp in (F)
	 */
	//boolean isAtTempreture();
	/**
	 * Returns the hardware object
	 */
	PressHardware getPressHardware();

}
