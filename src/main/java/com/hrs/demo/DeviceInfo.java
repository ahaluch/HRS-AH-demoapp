package com.hrs.demo;

public class DeviceInfo {
	private int batteryLevel;
	private double latitude;
	private double longitude;
	private double batteryVoltage;
	private String network;
	private String PCBVersion;
	private String productionFirmware;
	private String productName;
	private int signalStrength;
	private String iccid; 
	private WorkZone workZone;
	private int sessionID;
	private double speed;


	public DeviceInfo(int batteryLevel, double d,String network, String pCBVersion, String string, String productName, int signalStrength,String iccid) {
		this.batteryLevel = batteryLevel;
		this.batteryVoltage = d;
		this.network = network;
		this.PCBVersion = pCBVersion;
		this.productionFirmware = string;
		this.productName = productName;
		this.signalStrength = signalStrength;
		this.iccid = iccid;
	}


	public int getBatteryLevel() 
	{
		return batteryLevel;
	}

	public double getBatteryVoltage() {
		return batteryVoltage;
	}
	
	public int getSessionID() {
		return sessionID;
	}


	public void setSessionID(int sessionID) {
		this.sessionID = sessionID;
	}


	public void setBatteryVoltage(double d) {
		this.batteryVoltage = d;
	}


	public String getNetwork() {
		return network;
	}


	public void setNetwork(String network) {
		this.network = network;
	}


	public String getProductName() {
		return productName;
	}


	public void setProductName(String productName) {
		this.productName = productName;
	}


	public int getSignalStrength() {
		return signalStrength;
	}


	public void setSignalStrength(int signalStrength) {
		this.signalStrength = signalStrength;
	}


	public String getIccid() {
		return iccid;
	}


	public void setIccid(String iccid) {
		this.iccid = iccid;
	}


	public double getLongitude() 
	{
		return longitude;
	}
	
	public double getLatitude() 
	{
		return latitude;
	}
	
	public String getPCBVersion() {
		return PCBVersion;
	}


	public void setPCBVersion(String pCBVersion) {
		PCBVersion = pCBVersion;
	}


	public String getProductionFirmware() {
		return productionFirmware;
	}


	public void setProductionFirmware(String productionFirmware) {
		this.productionFirmware = productionFirmware;
	}


	public void setBatteryLevel(int batteryLevel) 
	{
		this.batteryLevel = batteryLevel;
	}
	
	public void setLatitude(double lat) 
	{
		this.latitude = lat;
	}

	public void setLongitude(double lon) 
	{
		this.longitude = lon;
	}
	
	public WorkZone getWorkZone() {
		return workZone;
	}
	
	public void setWorkZone(WorkZone workZone) {
		this.workZone = workZone;
	}
	
	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}
