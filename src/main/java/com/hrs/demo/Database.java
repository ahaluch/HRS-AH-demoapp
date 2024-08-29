package com.hrs.demo;

import java.sql.*;
import java.time.LocalDateTime;

import com.google.gson.Gson;

public class Database {
	public static Connection database;
	
	public Database() {
		//opens and connects to SQLLite databse
        try {
            Class.forName("org.sqlite.JDBC");
            database = DriverManager.getConnection("jdbc:sqlite:db.db");
         } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
         }
         System.out.println("Opened database successfully");
	}
	
	// updates the last message time of a given device
	public void lastMessage(String IMEI, LocalDateTime date)
	{
		String sql = "UPDATE devices2 SET lastMessage = ? WHERE IMEI = ?;";
		String time = String.valueOf(date); 
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, time);
			statement.setString(2, IMEI);
			
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//logs every event into the db
	public void logEvent(String IMEI, String message, String action) {
		String sql = "INSERT INTO eventLogs (deviceID, action, comment) VALUES (?,?,?);";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setInt(1, getDeviceID((IMEI)));
			statement.setString(2, action);
			statement.setString(3, message);
			
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//updates db when a device is powered on, returns SessionID for deviceInfo
	public int powerOnDevice(String IMEI, int batteryLevel, double d,String network, String pCBVersion, String string, String productName, int signalStrength,String iccid, String status, LocalDateTime date) 
	{
		
		//if device exists in table then run update query, if not run an insert query 
  	  if (deviceExists(IMEI)) 
  	  {
		  updateDevice(IMEI, batteryLevel, d, network, signalStrength, status);
	  } else 
	  {
		  newDevice(IMEI, batteryLevel, d, network, pCBVersion, string, productName, signalStrength, iccid, status);
	  }
  	  
  	  return createSession(IMEI, date);
	}
	
	public int powerOnDevice(String deviceAddress, double lat, double lon, LocalDateTime date)
	{
		int sessionID = -1;
		String sql;
		if (deviceExists(deviceAddress)) {
			sql = "UPDATE devices2 SET latitude = ?, longitude = ?, lastMessage = ? WHERE IMEI = ?";
		} else {
			sql = "INSERT INTO devices2 (latitude, lomgitude) VALUES (?,?)";
		}
		
		try(PreparedStatement statement = database.prepareStatement(sql)){
			statement.setDouble(1, lat);
			statement.setDouble(2, lon);
			
			statement.execute();
			
		} catch (Exception e) {
			
			
		}
		
		
		return createSession(deviceAddress, date);
	}

	private int createSession(String IMEI, LocalDateTime date) {
		//makes a new session for the device to tell when it was turned on and off
		  String sql = "INSERT INTO sessions (deviceID, opened_session) VALUES (?,?) RETURNING id;";
		  int sessionID = -1;
			
			//executes session query and returns the columnID
			try (PreparedStatement statement = database.prepareStatement(sql)){
				statement.setInt(1, getDeviceID(IMEI));
				statement.setString(2, String.valueOf(date));
				
				try (ResultSet result = statement.executeQuery()) {
					if (result.next()) {
						sessionID = result.getInt(1);
					}
					System.out.println(sessionID);
				}
				
			} catch( Exception e) {
				e.printStackTrace();
			}
			
		  //returns id of row just inserted (Session Table) to return to save as a sessionID in DeviceInfo
		  return sessionID;
	}
	
	//powerOff action
	public void powerOffDevice(String IMEI, int sessionID, LocalDateTime date)
	{
		//update devices table to set WZ to null
		String sql = "UPDATE devices2 SET WZID = null WHERE IMEI = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, IMEI);
			statement.execute();
		} catch(Exception e) {
			
			e.printStackTrace();
		}
		
		//update seesions tbale to end the devices session
		sql = "UPDATE sessions SET closed_session = ? WHERE id = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, String.valueOf(date));
			statement.setInt(2, sessionID);
			
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	//update devices table from a psotion action
	public void updatePosition(double lat, double lon, String WZ, String imei, double speed) 
	{
		//updates devices lat and lon
		String sql = "UPDATE devices2 SET latitude = ?, longitude = ?,  WZID = ?, speed = ? WHERE IMEI = ?";
		
		int WZID = getWZID(WZ);
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setDouble(1, lat);
			statement.setDouble(2, lon);
			statement.setString(5, imei);
			
			//sets workzone if device is in a WZ
			if (WZ != null) 
			{
				statement.setInt(3, WZID);
			}
			
			//sets speed if speed is sent
			if (speed != -1) {
				statement.setDouble(4, speed);
			}
			
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	//update device status in table
	public void updateDeviceStatus(String IMEI, String status) 
	{
		String sql = "UPDATE devices2 SET status = ? WHERE IMEI = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, status);
			statement.setString(2, IMEI);
			
			statement.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	//returns all the workZones in the workZones table to update WZ map
	public ResultSet getWorkZones() {
		String sql = "SELECT * FROM workZones;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			return statement.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	//adds a new WZ into the database (Shouldn't be used as WZ wont be added from the server)
	public void newWorkZone(String name, double[][] polygon) {
		String sql = "INSERT INTO workZones (name, polygon) VALUES(?,?);";
		
		//convert polygon to JSON
		Gson gson = new Gson ();
		String jsonPolygon = gson.toJson(polygon);
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, name);
			statement.setString(2, jsonPolygon);
			
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
	}
	
	//updates an existing devices info when powered on
	public void updateDevice(String IMEI, int batteryLevel, double batteryVoltage, String network, int signalStrength, String status)
	{
		String sql = "UPDATE devices2 SET batteryLevel = ?, batteryVoltage = ?, network = ?, signalStrength = ?, status = ? WHERE IMEI = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setInt(1, batteryLevel);
			statement.setDouble(2, batteryVoltage);
			statement.setString(3, network);
			statement.setInt(4, signalStrength);
			statement.setString(5, status);
			statement.setString(6, IMEI);
			
			statement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//inserts a new device into the device table
	public void newDevice(String IMEI, int batteryLevel, double d,String network, String PCBVersion, String productionFirmware, String productName, int signalStrength,String iccid, String status) 
	{
		String sql = "INSERT INTO devices2 (IMEI, batteryLevel, batteryVoltage, network, PCBVersion, productionFirmware, productName, signalStrength, iccid, status)"
				+ " VALUES(?,?,?,?,?,?,?,?,?,?);";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, IMEI);
			statement.setInt(2, batteryLevel);
			statement.setDouble(3, d);
			statement.setString(4, network);
			statement.setString(5, PCBVersion);
			statement.setString(6, productionFirmware);
			statement.setString(7, productName);
			statement.setInt(8, signalStrength);
			statement.setString(9, iccid);
			statement.setString(10, status);
			
			statement.executeUpdate();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//returns a string of the deivce type 
	public String getProduct(String IMEI) {
		String sql = "SELECT productName FROM devices2 WHERE IMEI = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, IMEI);
			
			ResultSet result = statement.executeQuery();
			return result.getString(1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//checks if a device exists in the devices table, returns true if exists, false if not
	private boolean deviceExists(String imei)
	{
		String sql = "SELECT EXISTS(SELECT IMEI FROM devices2 WHERE IMEI = ?);";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, imei);
			var rs = statement.executeQuery();
			rs.next();
			int exists = rs.getInt(1);
			return exists == 1;
			
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//returns a workZoneID from a WZ name
	private int getWZID(String name) {
		String sql = "SELECT id FROM workZones WHERE name = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, name);
			
			ResultSet result = statement.executeQuery();
			return result.getInt("id");
			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	//gets a device id from a given IMEI
	private int getDeviceID(String IMEI) {
		String sql = "SELECT id FROM devices2 WHERE IMEI = ?;";
		
		try {
			PreparedStatement statement = database.prepareStatement(sql);
			statement.setString(1, IMEI);
			
			ResultSet result = statement.executeQuery();
			return result.getInt("id");
			
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		
	}

}
