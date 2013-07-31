package com.neuronrobotics.industrial.device;

import com.neuronrobotics.sdk.common.BowlerDataType;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.common.RpcEncapsulation;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractDeviceServerNamespace;

public class TanuryBathNamespaceImp extends BowlerAbstractDeviceServerNamespace {
	
	private BathMonitorDeviceServer bathMonitorDeviceServer;


	public TanuryBathNamespaceImp(BathMonitorDeviceServer bathMonitorDeviceServer, MACAddress mac){
		super(mac,"tanury.bath.*;;");
		this.bathMonitorDeviceServer = bathMonitorDeviceServer;
		rpc.add(new RpcEncapsulation(getNamespaceIndex(), 
				getNamespace() , 
				"name", 
				BowlerMethod.GET, 
				new BowlerDataType[]{}, 
				BowlerMethod.POST, 
				new BowlerDataType[]{BowlerDataType.ASCII}));//Name
		
		rpc.add(new RpcEncapsulation(getNamespaceIndex(), 
				getNamespace()  , 
				"name", 
				BowlerMethod.POST, 
				new BowlerDataType[]{BowlerDataType.ASCII},//name
				BowlerMethod.POST, 
				new BowlerDataType[]{}));
		
		rpc.add(new RpcEncapsulation(getNamespaceIndex(), 
				getNamespace()  , 
				"rate", 
				BowlerMethod.POST, 
				new BowlerDataType[]{BowlerDataType.I32},//name
				BowlerMethod.POST, 
				new BowlerDataType[]{}));
	}


	public Object [] process(Object [] data, String rpc, BowlerMethod method){
		if(rpc.contains("name") && method == BowlerMethod.GET){
			Object[] back = new Object[]{bathMonitorDeviceServer.getName()};
			return back;
		}if(rpc.contains("name") && method == BowlerMethod.POST){
			Object[] back = new Object[0];
			bathMonitorDeviceServer.setName((String) data[0]);
			return back;
		}if(rpc.contains("rate") && method == BowlerMethod.POST){
			Object[] back = new Object[0];
			bathMonitorDeviceServer.setPollingRate((Integer)data[0]);
			return back;
		}
		
		return data;
	}
	

}
