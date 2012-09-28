package com.neuronrobotics.commercial.oggie;

import Jama.Matrix;

public interface IPressControler {
	
	/**
	 * Initializes the press cycle.
	 * @param m Matrix of time/tempreture data
	 * @param pressure the static pressure setpoint
	 */
	void onCycleStart(Matrix m, double pressure);
	
	/**
	 * This method is called to abort a running cycle. 
	 */
	void abortCycle();
	
	/**
	 * This method it to retrieve the current pressure value from the press
	 * @return the pressure in tons
	 */
	double getCurrentPressure();

}
