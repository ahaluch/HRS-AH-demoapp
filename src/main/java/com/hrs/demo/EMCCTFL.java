package com.hrs.demo;

import java.io.PrintWriter;
import java.util.Map;

import com.google.gson.JsonObject;

public class EMCCTFL extends DeviceInfo{
    
    private static final double TFL_DISTANCE_CLOSE = 0.2;
    private static final double TFL_DISTANCE_FAR= 0.5;
    
    private static final String TFL_INCURSION = "{\"Action\":\"LED\",\"led3\":1,\"flash3\":1}";
    private static final String TFL_PROXIMITY = "{\"Action\":\"LED\",\"led2\":1,\"flash2\":1}";
    
    private static final String RED_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[-1,0,0,3,78,0]}";
    
	private int prism; //0 = on, 1 = off
	
	public EMCCTFL(int batteryLevel, double d,String network, String pCBVersion, String string, String productName, int signalStrength,String iccid) {
		super(batteryLevel, d,network, pCBVersion, string, productName, signalStrength,iccid);
		this.prism = 1;
	}
	
	
	 /**
	  * responds to a prism action message received, either turns on or off the prism
	  * @param IMEI of EMCC TFL vehicle
	  * @param json received from the vehicle, need prism value
	  * @param devices map of devices to allow changing device values
	  * @return response to prism action, currently null
	  */
    public static JsonObject prism(String IMEI, JsonObject json, Map<String, DeviceInfo> devices) 
    {
    	//get prism value from the json and set the EMCCTFL prism value to it. Respresents if van is working
    	int prism = json.get("deployed").getAsInt();
    	((EMCCTFL) devices.get(IMEI)).setPrism(prism);
    	
    	return null;
    }
    
	/**
	 * function for when the incursionButton action is sent
	 * @param IMEI of incursion button pressed
	 * @return the string to set off incursion LED on EMCC device
	 */
	public static JsonObject incursionButton(String IMEI, Map<String, PrintWriter> deviceWriters) 
	{
		DemoApplication.ioRed(IMEI);
		sendDeviceMessage(IMEI, TFL_INCURSION, deviceWriters);
		return null;
		
	}
    
	/**
	 * checks a specific WZ to find out if it is EMCC ready. WZ requires at least one EMCC PSA and EMCC TFL
	 * @param WZ that needs to be checked if EMCC ready
	 * @param devices map to check device names for EMCC devices
	 * @return boolean true if EMCC ready false if not EMCC ready
	 */
	public static boolean checkEMCCReady(WorkZone WZ,  Map<String, DeviceInfo> devices) 
	{
		//sets the 2 required devices to false, signifies not in WZ
		boolean PSA = false;
		boolean TFL = false;
		
		//loops through every device in WZ to check if it's a require device
		for(String IMEI : WZ.getDeviceIMEIs()) {
			String product = devices.get(IMEI).getProductName();
			
			//checks if the current device is any of the EMCC required devices and changes its boolean to true, represents in WZ
			if (product.equals("EMCCTFL"))
			{
				TFL = true;
			}
			if(product.equals("EMCCPSA")) 
			{
				PSA = true;
			}
			
			//if both required devices are present return true and don't check anymore devices
			if (PSA && TFL) {
				return true;
			}
		}
		
		//returns false if both devices are not present
		return false;
		
	}
	
	/**
	 * updates the current distance between a TFL van and and a PSA
	 * @param tfl
	 * @param tflIMEI
	 * @param devices
	 * @param deviceWriters
	 * @return
	 */
	public static String updateDistance(EMCCTFL tfl, String tflIMEI,  Map<String, DeviceInfo> devices, Map<String, PrintWriter> deviceWriters) {
		
		for(String IMEI : tfl.getWorkZone().getDeviceIMEIs()) 
		{
			DeviceInfo PSA = devices.get(IMEI);
			//check device is EMCC PSA
			if (PSA.getProductName().equals("EMCCPSA"))
			{
				//getting distances between TFL and PSAs
				double currentDistance = distanceBetween2Points(tfl.getLatitude(), tfl.getLongitude(), PSA.getLatitude(), PSA.getLongitude());
				double lastDistance = ((EMCCPSA) PSA).getDistance();
				
				//ceeck if distance passes milestones
				if (lastDistance > TFL_DISTANCE_CLOSE && currentDistance < TFL_DISTANCE_CLOSE) {
					System.out.println("less than 200m");
					TFLAlarm(IMEI, tflIMEI, deviceWriters);
				}
				else if (lastDistance > TFL_DISTANCE_FAR && currentDistance < TFL_DISTANCE_FAR)
				{
					TFLFar(IMEI, tflIMEI, deviceWriters);
					System.out.println("Less than 500m");
				}
				
				//set distance to current
				((EMCCPSA) PSA).setDistance(currentDistance);	
				
			}
		}
		return null;
	}
	
	/**
	 * sets off the EMCC incursion alarm for a device
	 * @param PSAIMEI IMEI of PSA
	 * @param tflIMEI IMEI of TFL
	 * @param deviceWriters map of devicewriters to send messages
	 */
	private static void TFLAlarm(String PSAIMEI, String tflIMEI, Map<String, PrintWriter> deviceWriters)
	{
		sendDeviceMessage(PSAIMEI, RED_ALARM_MESSAGE, deviceWriters);
		sendDeviceMessage(tflIMEI, TFL_INCURSION, deviceWriters);
	}
	
	/**
	 * sets off the EMCC proximity alarm for a device
	 * @param PSAIMEI IMEI of PSA
	 * @param tflIMEI IMEI of TFL
	 * @param deviceWriters map of devicewriters to send messages
	 */
	private static void TFLFar(String PSAIMEI, String tflIMEI, Map<String, PrintWriter> deviceWriters) 
	{
		sendDeviceMessage(PSAIMEI, RED_ALARM_MESSAGE, deviceWriters);
		sendDeviceMessage(tflIMEI, TFL_PROXIMITY, deviceWriters);
	}
	
	/**
	 * sends a message to a specific device
	 * @param IMEI of device message should be sent to
	 * @param message to be sent
	 * @param deviceWriters map of deviceWriters to send messages
	 */
	private static void sendDeviceMessage(String IMEI, String message,  Map<String, PrintWriter> deviceWriters) 
	{
		PrintWriter out = deviceWriters.get(IMEI);
		out.println(message);
	}
	
	/**
	 * finds the distance between 2 given points in km
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return double distance in km
	 */
	public static double distanceBetween2Points(double lat1, double lon1, double lat2, double lon2)
	{
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double dlon = lon2 - lon1; 
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                 + Math.cos(lat1) * Math.cos(lat2)
                 * Math.pow(Math.sin(dlon / 2),2);
             
        double c = 2 * Math.asin(Math.sqrt(a));
 
        double r = 6371; 
        
 
        return(c * r);
		
	}

	public int getPrism() {
		return prism;
	}

	public void setPrism(int prism) {
		this.prism = prism;
	}

}
