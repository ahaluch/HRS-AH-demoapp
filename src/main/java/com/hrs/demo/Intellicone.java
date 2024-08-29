package com.hrs.demo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

public class Intellicone{
	
	private static final String RED_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[-1,0,0,3,78,0]}";
	private static final String RED_STATUS = "Alarm red";
	
	private static final String GREEN_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[0,-1,0,3,78,0]}";
	private static final String GREEN_STATUS = "Alarm green";
	
	private String deviceAddress;
	private double latitude;
	private double longitude;
	private static Database database;
	
	private static Map<String, Intellicone> cones = new HashMap<>();
	private static Map<String, Integer> processedIDs = new HashMap<>(); 
	
	
	public Intellicone(String deviceAddress, double latitude, double longitude) {
		this.deviceAddress = deviceAddress;
		this.latitude = latitude;
		this.longitude = longitude;

		cones.put(deviceAddress, this);
	}
	
	public static JsonObject ctsupdate(JsonObject json, String deviceAddress, Database db ) 
	{
		if (!checkProcessed(deviceAddress, json)) {
			System.out.println("process intellicone msg");
			database = db;
			//if device needs powered off
			if (getPowerState(json) == 0)
			{
				//TURN OFF DEVICE
			} else
			{
				//checks if cone already exists
				if (cones.containsKey(deviceAddress))
				{
					updateInfo(json, deviceAddress);
					database.powerOnDevice(deviceAddress, json.get("latitude").getAsDouble(), json.get("longitude").getAsDouble(), LocalDateTime.now());
				} else {
					new Intellicone(deviceAddress,json.get("latitude").getAsDouble() , json.get("longitude").getAsDouble());
				}
				
				int alarmState =  getAlarmState(json);
				//check if device is alarming
				if(alarmState == 1 || alarmState == 2)
				{
					alarmWZ(deviceAddress, json, RED_ALARM_MESSAGE, RED_STATUS);
				} else {
					alarmWZ(deviceAddress, json, GREEN_ALARM_MESSAGE, GREEN_STATUS);
				}
			}
		}
		
		return null;
	}
	
	private synchronized static boolean checkProcessed(String deviceAddress, JsonObject json)
	{
		try {
			int a= processedIDs.get(deviceAddress);
			int b =json.get("seqId").getAsInt();
			if (processedIDs.get(deviceAddress) < json.get("seqId").getAsInt())
			{
				processedIDs.put(deviceAddress, json.get("seqId").getAsInt());
				return false;
			} else {
				return true;
			}
		} catch (Exception e) {
			processedIDs.put(deviceAddress, 0);
			return false;
		}

	}
	
	private static void updateInfo(JsonObject json, String deviceAddress) {
		Intellicone cone = cones.get(deviceAddress);
		cone.setLatitude(json.get("latitude").getAsDouble());
		cone.setLongitude(json.get("longitude").getAsDouble());
		
	}
	
	private static void alarmWZ(String deviceAddress, JsonObject json, String alarm, String status)
	{
		WorkZone WZ = DemoApplication.getWorkZone(json.get("latitude").getAsDouble(), json.get("longitude").getAsDouble());
		WZ.setAlarmStatus(alarm);
		
		DemoApplication.sendMessages(WZ, alarm, status);
		
	}
	
	private static int getAlarmState(JsonObject json)
	{
		try {
			return json.get("alarmState").getAsInt();
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * gets the powerstate of device, 1 means powering on, 0 means powering off
	 * @param json of received message
	 * @return 1/0/null
	 */
	private static int getPowerState(JsonObject json)
	{
		try {
			return json.get("powerState").getAsInt();
		} catch (Exception e) {
			return -1;
		}
		
	}
	
	
	
	public String getDeviceAddress() {
		return deviceAddress;
	}
	public void setDeviceAddress(String deviceAddress) {
		this.deviceAddress = deviceAddress;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
}