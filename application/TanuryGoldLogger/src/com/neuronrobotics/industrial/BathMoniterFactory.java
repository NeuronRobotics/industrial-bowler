package com.neuronrobotics.industrial;

import java.util.ArrayList;

public class BathMoniterFactory {

	public static ArrayList<BathMoniter> getBathMoniterList(){
			
		ArrayList<BathMoniter> list = new ArrayList<BathMoniter>();
		
		if(list.size()==0){
			for(int i=0;i<5;i++){
				list.add(new BathMoniter());
			}
		}
		
		return list;
		
	}

}
