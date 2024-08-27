package com.hrs.demo;

import java.util.HashSet;
import java.util.Set;

public class WorkZone {
    private String name;
    private Set<String> deviceIMEIs; 
    private double[][] polygon;
    private boolean EMCCCReady = false;
    private String alarmStatus = "{\"Action\":\"AlarmAction\",\"Data\":[0,-1, 0,3,78,0]}";

	public WorkZone(String name, double[][] polygon) {
    	this.polygon= polygon;
        this.name = name;
        this.deviceIMEIs = new HashSet<>();
    }
	
	
	public String getAlarmStatus() {
		return alarmStatus;
	}

	public void setAlarmStatus(String alarmStatus) {
		this.alarmStatus = alarmStatus;
	}

    public double[][] getPolygon() {
		return polygon;
	}

	public void setPolygon(double[][] polygon) {
		this.polygon = polygon;
	}

	public boolean isEMCCCReady() {
		return EMCCCReady;
	}

	public void setEMCCCReady(boolean eMCCCReady) {
		EMCCCReady = eMCCCReady;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDeviceIMEIs(Set<String> deviceIMEIs) {
		this.deviceIMEIs = deviceIMEIs;
	}

	public String getName() {
        return name;
    }

    public Set<String> getDeviceIMEIs() {
        return deviceIMEIs;
    }

    public void addDevice(String IMEI) {
        deviceIMEIs.add(IMEI);
    }

    public void removeDevice(String IMEI) {
        deviceIMEIs.remove(IMEI);
    }
}