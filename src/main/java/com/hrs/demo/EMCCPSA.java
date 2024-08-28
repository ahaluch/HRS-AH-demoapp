package com.hrs.demo;

import java.io.PrintWriter;
import java.util.Map;

import com.google.gson.JsonObject;

public class EMCCPSA extends DeviceInfo{
	
    private static final String TFL_INCURSION = "{\"Action\":\"LED\",\"led3\":1,\"flash3\":1}";
	
	private double distance_metres; //distance from TFL
	private double DEFAULT_DISTANCE_METRES = 1000;
	
	public EMCCPSA(int batteryLevel, double d,String network, String pCBVersion, String string, String productName, int signalStrength,String iccid)
	{
		super(batteryLevel, d,network, pCBVersion, string, productName, signalStrength,iccid);
		this.distance_metres = DEFAULT_DISTANCE_METRES;
	}
	
	/**
	 * action for when EMCC PSA action button is pressed
	 * @param IMEI of device button was pressed on
	 * @return reply for PSA
	 */
	public static JsonObject incursionButton(String IMEI,  Map<String, DeviceInfo> devices, String TFL, String PSA)
	{
		//send red alarm to all PSAs in WZ
		DemoApplication.ioRed(IMEI);
		
		//loop through and alarm all TFL in WZ
		for(String s: devices.get(IMEI).getWorkZone().getDeviceIMEIs()) 
		{
			//if device is TFL send message
			if(devices.get(s).getProductName().equals(TFL)) {
				DemoApplication.sendMessage(IMEI, TFL_INCURSION);
			}
		}
		return null;
	}

	public double getDistance() {
		return distance_metres;
	}

	public void setDistance(double distance) {
		this.distance_metres = distance;
	}

}
