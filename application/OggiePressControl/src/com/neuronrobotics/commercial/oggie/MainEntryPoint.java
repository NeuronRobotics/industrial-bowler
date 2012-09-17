package com.neuronrobotics.commercial.oggie;

public class MainEntryPoint {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			new OggiePress();
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
	}

}
