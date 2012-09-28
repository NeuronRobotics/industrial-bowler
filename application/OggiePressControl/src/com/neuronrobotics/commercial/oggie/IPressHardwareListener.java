package com.neuronrobotics.commercial.oggie;

public interface IPressHardwareListener {
	/**
	 * On the event of a cycle start
	 * @param i press index
	 * @param config the configuration table
	 */
	void onCycleStart(int i, CycleConfig config);
	/**
	 * Abort a cycle for a press
	 * @param i press index
	 */
	void onAbortCycle(int i);
	/**
	 * Pressure value for a press has changed
	 * @param i press index
	 * @param pressureTons
	 */
	void onPressureChange(int i, double pressureTons);
	/**
	 * Tempreture has changed
	 * @param i  press index
	 * @param degreesFarenhight
	 */
	void onTempretureChange(int i, double degreesFarenhight);
}
