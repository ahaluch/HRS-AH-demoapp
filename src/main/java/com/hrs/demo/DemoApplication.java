package com.hrs.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;

import com.google.gson.*;

import io.github.cdimascio.dotenv.Dotenv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DemoApplication {
    private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);
    
    private static final String GREEN_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[0,-1, 0,3,78,0]}";
    private static final String RED_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[-1,0,0,3,78,0]}";
    private static final String BLUE_ALARM_MESSAGE = "{\"Action\":\"AlarmAction\",\"Data\":[0,0,-1,3,78,0]}";
    
    private static final String GREEN_STATUS = "Active";
    private static final String RED_STATUS = "Alarm red";
    private static final String BLUE_STATUS = "Alarm blue";
    private static final String OFF_STATUS = "Inactive";
    
    private static final String PSA_CODE = "test";
    private static final String EMCC_PSA_CODE = "EMCCPSA";
    private static final String EMCC_TFL_CODE = "EMCCTFL";

    private static Map<String, DeviceInfo> devices = new HashMap<>();
    private static Map<String, WorkZone> workZones = new HashMap<>(); 
    private static Map<String, Socket> deviceWriters = new HashMap<>();
    
    private static Database database;
    
    
    public static void main(String args[]) throws IOException {
        
        database = new Database();
        
        //making workzones
        /*
        double [][] icelandPolygon = {
        		  {67.2421663499099, -26.5442138314509},
        		  {62.75958290911809, -25.9948974252009},
        		  {62.78973979810564, -9.6692138314509},
        		  {67.42843690437506, -10.1745849252009},
        		};
        
        double[][] UKPolygon = {
        	  {59.403582176583114, -11.311147978462063},
        	  {48.92665193728061, -10.739858915962063},
        	  {48.98436485748158, 4.245492646537956},
        	  {59.71528456462309, 4.465219209037956},
        	};

        // Test workzones
        
        database.newWorkZone("UK", UKPolygon);
        database.newWorkZone("Iceland", icelandPolygon);
        workZones.put("UK", new WorkZone("UK", UKPolygon));
        workZones.put("Iceland", new WorkZone("Iceland", icelandPolygon));
         */
        
        //loads initial WZs from database
        loadWorkZones();

        //gets the server socket from environment variables if exists, if not default to 1234
        int socket;
        try {
        	Dotenv dotenv = Dotenv.load();
        	socket = Integer.valueOf(dotenv.get("serverSocket"));
        } catch(Exception e) {
        	e.printStackTrace();
        	socket = 1234;
        }
       
        // Start the server socket to receive device messages
        ServerSocket serverSocket = new ServerSocket(socket);
        System.out.println("Server is running and waiting for client connection on socket " + socket);
        
        //starts thread that updates WZ from changes in the database
        WorkZoneUpdater WZUpdate = new WorkZoneUpdater();
        Thread threadWZ = new Thread();
        threadWZ.start();

        //checks for new clients connecting and starts them a thread
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected!");
            
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }

    /**
     * fully processes a device message, updates everything required, sends any messages to the device
     * @param sb string received from device
     * @param out devices specific printWriter to send extra messages to device
     * @throws URISyntaxException
     */
    public static synchronized void processMessage(StringBuilder sb, Socket clientSocket) throws URISyntaxException {
        String data = sb.toString();
        System.out.println(data + "\n");

        // Parse the JSON object
        Gson gson = new Gson();
        JsonObject json = gson.fromJson(data, JsonObject.class);

        //read data that is present in every device message
        String[] commonData = readJSON(json); // 0: IMEI/deviceAddress, 1: action
        String IMEI = commonData[0];
        String action = commonData[1];
        
        //updates database with last message received time
        database.lastMessage(IMEI, LocalDateTime.now());
        database.logEvent( IMEI, data, action);

        
        JsonObject reply = null; //reply to device

        // select correct action, make correct reply and update anything required
        switch (action) {
            case "Position":
                reply = position(IMEI, json);
                break;
            case "powerup":
                reply = powerOn(IMEI, json, clientSocket);
                break;
            case "poweroff":
                reply = powerOff(IMEI);
                break;
            case "iored": //PSA alarm action
                reply = ioRed(IMEI);
                break;
            case "IoRed": //EMCC alarm action
            	reply = EMCCPSA.incursionButton(IMEI, devices, EMCC_TFL_CODE, RED_ALARM_MESSAGE);
            	break;
            case "ioblue":
                reply = ioBlue(IMEI);
                break;
            case "reset":
                reply = reset(IMEI);
                break;
            case "prism":
            	reply = EMCCTFL.prism(IMEI, json, devices);
            	break;
            case "incursionButton":
            	reply = EMCCTFL.incursionButton(IMEI);
            	break;
            case "ctsupdate":
            	reply = Intellicone.ctsupdate(json, IMEI, database);
            	
            	
        }
        
        
        if (reply != null)
        {
        	sendMessage(IMEI, reply.toString());
        }
        
    }
    
    /**
     * completes all processing to power on a device
     * @param IMEI of device turned on
     * @param json that was received from device, need everything from JSON
     * @param out PrintWriter of device received to save
     * @return message to send back to device (currently null)
     */
    public static JsonObject powerOn(String IMEI, JsonObject json, Socket clientSocket)
    {
    	//add device writer to map so can send messages to device in the future
    	deviceWriters.put(IMEI,  clientSocket);
    	
    	//gets type of device turned on from database
    	String product = database.getProduct(IMEI);
    	
    	//makes a different POJO depending on type of device turned on
    	if (product != null && product.equals(EMCC_TFL_CODE)) 
    	{
    		devices.put(IMEI, new EMCCTFL(json.get("batteryLevel").getAsInt(), json.get("batteryVoltage").getAsDouble(),json.get("network").getAsString(), json.get("PCBVersion").getAsString(), json.get("productionFirmware").getAsString(), product,json.get("signalStrength").getAsInt(),json.get("iccid").getAsString()));
    	} 
    	else if(product != null && product.equals(EMCC_PSA_CODE)) 
    	{
    		devices.put(IMEI, new EMCCPSA(json.get("batteryLevel").getAsInt(), json.get("batteryVoltage").getAsDouble(),json.get("network").getAsString(), json.get("PCBVersion").getAsString(), json.get("productionFirmware").getAsString(), product,json.get("signalStrength").getAsInt(),json.get("iccid").getAsString()));
    	}
    	else
    	{
    		devices.put(IMEI, new DeviceInfo(json.get("batteryLevel").getAsInt(), json.get("batteryVoltage").getAsDouble(),json.get("network").getAsString(), json.get("PCBVersion").getAsString(), json.get("productionFirmware").getAsString(), "test",json.get("signalStrength").getAsInt(),json.get("iccid").getAsString()));
    	}
    	
    	
		//creates session for device for logs and updates database for powerOn
		devices.get(IMEI).setSessionID(database.powerOnDevice(IMEI, json.get("batteryLevel").getAsInt(), json.get("batteryVoltage").getAsDouble(),json.get("network").getAsString(), json.get("PCBVersion").getAsString(), json.get("productionFirmware").getAsString(), product,json.get("signalStrength").getAsInt(),json.get("iccid").getAsString(), GREEN_STATUS, LocalDateTime.now()));
  
		return null;
    }

    
    /**
     * processes position action from device
     * @param IMEI of device received from
     * @param json JSONObject needs latitude, longitude, speed
     * @return reply to postion action to be sent to device
     */
    public static JsonObject position(String IMEI, JsonObject json) {
        try {
        	DeviceInfo info = devices.get(IMEI);
            
        	//updates devices lat and lon in POJO, needed for rest of processing
            double latitude = json.get("latitude").getAsDouble();
            double longitude = json.get("longitude").getAsDouble();
            info.setLatitude(latitude);
            info.setLongitude(longitude);
            
            //updates WZ based on new location
            String WZName = setDeviceWorkzone(IMEI);
            
            //If EMCC TFL is transmitting its location(Prism on) calculates distances between PSAs
            if (info.getProductName() != null && info.getProductName().equals(EMCC_TFL_CODE) && ((EMCCTFL) info).getPrism() == 0)
            {
            	EMCCTFL.updateDistance((EMCCTFL)info, IMEI, devices);
            }
            
            //sets devices speed if sent in the JSON 
            double speed = -1;
            if (json.has("speed")) 
            {
            	speed = json.get("speed").getAsDouble();
            	info.setSpeed(speed);
            }
            
            database.updatePosition(latitude, longitude, WZName, IMEI, speed);
         
            //message sent back to PSA
            return positionReply(WZName,json.get("uuid").getAsString());
        }
        catch(Exception e) {
        	e.printStackTrace();
            return null;
         }
    }
    
    /**
     * sends a red alarm to every PSA in the workzone
     * @param IMEI of any device in the workZone
     * @return reply to ioRed action (null currently)
     */
    public static JsonObject ioRed(String IMEI) 
    {
    	DeviceInfo info = devices.get(IMEI);
    	
    	//gets the WZ of particular IMEI to set alarm on other WZ devices
    	info.getWorkZone().setAlarmStatus(RED_ALARM_MESSAGE);
  
    	//sends alarm to every device in WZ
    	sendWZMessage(IMEI, RED_ALARM_MESSAGE, RED_STATUS, info);
    		
        return null;
    }
    
    /**
     * sends a blue alarm to every PSA in the workzone
     * @param IMEI of any device in the workZone
     * @return reply to ioBlue action (null currently)
     */
    public static JsonObject ioBlue(String IMEI)
    {
    	//gets the WZ of particular IMEI to set alarm on other WZ devices
    	DeviceInfo info = devices.get(IMEI);
    	info.getWorkZone().setAlarmStatus(BLUE_ALARM_MESSAGE);
    	
    	//sends alarm to every device in WZ
    	sendWZMessage(IMEI, BLUE_ALARM_MESSAGE, BLUE_STATUS, info);
    	
        return null;
    }
    
    /**
     * resets all PSAs in a WZ back to green when reset action is received
     * @param IMEI of any device in WZ of WZ to be reset
     * @return reply to reset action (null currently)
     */
    public static JsonObject reset(String IMEI)
    {
    	//gets the WZ of particular IMEI to set alarm on other WZ devices
    	DeviceInfo info = devices.get(IMEI);
    	info.getWorkZone().setAlarmStatus(GREEN_ALARM_MESSAGE);
    	
    	//sends reset command to every device in WZ
    	sendWZMessage(IMEI, GREEN_ALARM_MESSAGE, GREEN_STATUS, info);	
    	return null;
    }
    
    /**
     * powers off a given device, removes from maps and updates database to avoid future processing and incorrect alarming
     * @param IMEI of device wanting to turn off
     * @return reply to powerOff action (null currently)
     */
    public static JsonObject powerOff(String IMEI)
    {
    	//update device status in the database and removes its writer so it can't be sent another message
        deviceWriters.remove(IMEI);
        database.updateDeviceStatus(IMEI, OFF_STATUS);
        database.powerOffDevice(IMEI, devices.get(IMEI).getSessionID(), LocalDateTime.now());
        
        //remove device from workZone and devices map to free up memory
        DeviceInfo info = devices.get(IMEI);
        WorkZone WZ = info.getWorkZone();
        devices.remove(IMEI);
        if (info.getWorkZone() != null) info.getWorkZone().removeDevice(IMEI);
        
        //check if WZ can still run EMCC after the device is removed
        if (info.getProductName() != null && !info.getProductName().equals(PSA_CODE)) WZ.setEMCCCReady(EMCCTFL.checkEMCCReady(WZ, devices));
        
        return null;
    }
    
    
    /**
     * creates reply JSON to a position action
     * @param WZName name of workZone currently in
     * @param uuid of position message
     * @return
     */
    public static JsonObject positionReply(String WZName, String uuid)
    {
    	JsonObject reply = buildPositionDetail(WZName, uuid);
    	
    	System.out.println(reply);
    	
    	return reply;
    }

	private static JsonObject buildPositionDetail(String WZName, String uuid) {
		JsonObject reply = new JsonObject();
    	reply.addProperty("Action", "WorkzoneStatus");
    	reply.addProperty("Workzone", WZName);
    	reply.addProperty("uuid", uuid);
		return reply;
	}
	
	/**
	 * gets the workZone a specific point is in
	 * @param lat of point
	 * @param lon of point
	 * @return name of WZ. null if non found
	 */
	public static WorkZone getWorkZone(double lat, double lon)
	{
		for (String workZoneName : workZones.keySet())
		{
    		double [][] polygon = workZones.get(workZoneName).getPolygon();
    		
    		//checks if devices current location is inside the current WZ being checked polygon
    		if (isPointInPolygon(lat, lon,polygon))
    		{
    			return workZones.get(workZoneName);
    		}
		}
		return null;
		
	}
    
    /**
     * Sends a message to every device in a workZone and changes status of those devices
     * @param IMEI of a device in WZ you want to send message to 
     * @param reply String that will be sent to each device
     * @param status that will be put on each device in WZ
     */
    private static void sendWZMessage(String IMEI, String reply, String status, DeviceInfo info) 
    {
    	//loops through every device in WZ to send message to
    	
    	if(info.getWorkZone() != null) sendMessages( info.getWorkZone(), reply, status);
    	
    }

	public static void sendMessages(WorkZone wz, String reply, String status) {
		for(String s: wz.getDeviceIMEIs()) 
    	{
    		try {
    			// sends message to the device and updates the database with the new status
    			sendMessage(s, reply);
    			database.updateDeviceStatus(s, status);
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    		
    	}
	}
    
    /**
     * reads the JSON data that is present in every message received. (IMEI, action)
     * @param json JsonObject that holds the json, need action and IMEI
     * @return an ARRAY holding [IMEI, action], returns null if error
     */
    public static String[] readJSON(JsonObject json) 
    {
    	//gets the required data from the json object and returns it in an array
        try {
        	//gets either IMEI or device address depending on if intellicone or other device
        	String IMEI;
        	try {
        		IMEI = json.get("deviceAddress").getAsString();
        	} catch(Exception e1) {
        		IMEI = json.get("IMEI").getAsString();
        	}
            
            String action = json.get("Action").getAsString();
            return new String[]{IMEI, action};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * finds if the given device is in a WZ polygon. If it is does all the processing to put the device in the correct WZ
     * @param IMEI of device wanting to find WZ of
     * @return WZ name, nothing needs to be done with return just in case it was needed. returns null if no WZ found
     */
    public static String setDeviceWorkzone(String IMEI)
    {
    	DeviceInfo info = devices.get(IMEI);
    	
    	//loops through every workZone and retrieves polygon
    	for (String workZoneName : workZones.keySet())
    	{
    		double [][] polygon = workZones.get(workZoneName).getPolygon();
    		
    		//checks if devices current location is inside the current WZ being checked polygon
    		if (isPointInPolygon(info.getLatitude(), info.getLongitude(), polygon))
    		{	
    			//if WZ has been changed need to remove it from its previous WZ map to stop it receiving wrong WZ messages
    			if (info.getWorkZone() != null && workZoneName != info.getWorkZone().getName()) 
    			{
    	            info.getWorkZone().removeDevice(IMEI);
    			}
    			
    			//Sets all variables needed to add it to the current WZ
                info.setWorkZone(workZones.get(workZoneName));
                workZones.get(workZoneName).addDevice(IMEI);
                
                //sets current device status to the WZ status so it will alarm if WZ is alarming
                sendMessage(IMEI, info.getWorkZone().getAlarmStatus());
                
    			//check if WZ is EMCC ready after adding the new device
    			if (info.getProductName() != PSA_CODE && info.getWorkZone().isEMCCCReady()) info.getWorkZone().setEMCCCReady(EMCCTFL.checkEMCCReady(devices.get(IMEI).getWorkZone(), devices));
                
    			return workZoneName;
    		}
    		
    	}

    	//if device has been moved out of WZ and not into new WZ remove from previous WZ and set device WZ. Means device wont be sent old WZ messages
    	if (info.getWorkZone() != null)
    	{
            info.getWorkZone().removeDevice(IMEI);
            info.setWorkZone(null);
            
            //resets device alarm back to green
            sendMessage(IMEI, GREEN_ALARM_MESSAGE);
    	}
    	
    	//if no WZ found
    	return "-";
    }

    /**
     * checks if a given point is inside a given polygon. Uses ray casting to work out
     * @param lat latitude of point to be checked
     * @param lon longitude of point to be checked
     * @param polygon that the point is being checked against
     * @return
     */
	public static boolean isPointInPolygon(double lat, double lon, double[][] polygon)
	{
		int numPoints = polygon.length;
		boolean inside = false;
    	
		for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
			double lat1 = polygon[i][0];
			double lon1 = polygon[i][1];
			double lat2 = polygon[j][0];
			double lon2 = polygon[j][1];
			
			if ((lon1 > lon) != (lon2 > lon) && (lat < (lat2 - lat1) * (lon - lon1) / (lon2 - lon1) + lat1)){
				inside = !inside;
			}
			
		}
		
		return inside;
	}
	
	/**
	 * sends a message to a given device
	 * @param IMEI of device the message should be sent to
	 * @param message of what you want to sent
	 */
	public static void sendMessage(String IMEI, String message)
	{
		Socket socket = deviceWriters.get(IMEI);
		if (socket != null && socket.isConnected())
		{
			try {
				socket.getOutputStream().write(message.getBytes());
				socket.getOutputStream().flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * updates workZone map by checking for an updates in the database
	 */
	public static void loadWorkZones()
	{
		ResultSet result = database.getWorkZones();
		
		//uses a gson to read what is returned from the database 
		Gson gson = new Gson();
		
		try {
			//loops through every workZone in the database to add to the WZ map if needed
			while (result.next())
			{
				String name = result.getString("name");
				
				//checks if the current WZ being checked already exists in the map, if not then adds it to the map
				if (!workZones.containsKey(name)) 
				{
					//reads polygon String from database and turns it into a double[][] array
					String polygonJSON = result.getString("polygon");
					double[][] polygon = gson.fromJson(polygonJSON, double[][].class);
					
					//adds WZ to map to allow devices to be added to it
					workZones.put(name, new WorkZone(name, polygon));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}