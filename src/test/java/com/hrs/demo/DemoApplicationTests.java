package com.hrs.demo;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@SpringBootTest
class DemoApplicationTests {
	
	private static DemoApplication instance;
	private static Database dbInstance;
	
	@BeforeAll
	public static void setUp()
	{
		instance = new DemoApplication();
		
		
		//start deviceds map
		try {
			Field field = DemoApplication.class.getDeclaredField("devices");
			field.setAccessible(true);
			
			Map<String, DeviceInfo> devices = new HashMap<>();
			DeviceInfo device1 =  new DeviceInfo(0, 0, null, null, null, "test", 0, null);
			
			devices.put("1", device1);
			field.set(instance, devices);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//start database class
		try {
			Field field = DemoApplication.class.getDeclaredField("database");
			field.setAccessible(true);
			
			dbInstance = new Database();
			
			field.set(instance, dbInstance);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//start workzone map
		try {
			Field field = DemoApplication.class.getDeclaredField("workZones");
			field.setAccessible(true);
			
			Map<String, WorkZone> workZones = new HashMap<>(); 
			
	        double[][] UKPolygon = {
	            	  {59.403582176583114, -11.311147978462063},
	            	  {48.92665193728061, -10.739858915962063},
	            	  {48.98436485748158, 4.245492646537956},
	            	  {59.71528456462309, 4.465219209037956},
	            	};
	    	WorkZone test = new WorkZone("UK", UKPolygon);
			
			workZones.put("UK", test);
			field.set(instance, workZones);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	void contextLoads() {
	}
	
	
	@Test
	@DisplayName("setting device Work zone normal")
	public void setDeviceWorkZone_pass()
	{
		
	}
	
	@Test
	@DisplayName("setting device Work zone fail")
	public void setDeviceWorkZone_fail()
	{
		
	}
	
	@Test
	@DisplayName("if point is inside a polygon, true")
	public void isPointInPolygon_true()
	{
		
	}
	
	@Test
	@DisplayName("if point is inside a polygon, false")
	public void isPointInPolygon_false()
	{
		
	}
	
	@Test
	@DisplayName("position reply, pass")
	public void position_reply_pass()
	{
		JsonObject expected = JsonParser.parseString("{\"Action\":\"WorkzoneStatus\",\"Workzone\":\"UK\",\"uuid\":\"2\"}").getAsJsonObject();
		JsonObject result = DemoApplication.positionReply("UK", "2");
		Assertions.assertEquals(expected, result);
	}
	
	
	@Test
	@DisplayName("position action reply pass")
	public void position_pass()
	{
		JsonObject json = JsonParser.parseString("{\"IMEI\":\"4345\",\"uuid\":\"2\",\"Action\":\"Position\",\"latitude\":52.16889771553794,\"longitude\":0.5086954362703011}").getAsJsonObject();
		JsonObject expected = JsonParser.parseString("{\"Action\":\"WorkzoneStatus\",\"Workzone\":\"UK\",\"uuid\":\"2\"}").getAsJsonObject();
		JsonObject result = DemoApplication.position("1", json);
		Assertions.assertEquals(expected, result);
	}
	
	@Test
	@DisplayName("position action reply fail")
	public void position_fail()
	{
		JsonObject json = JsonParser.parseString("{\"IMEI\":\"4345\",\"uuid\":\"2\",\"Action\":\"Position\",\"latitude\":52.16889771553794,\"longitude\":0.5086954362703011}").getAsJsonObject();
		JsonObject result = DemoApplication.position("1", json);
		Assertions.assertEquals(null, result);
	}
	
	@Test
	@DisplayName("JSON normal")
	public void JSON_Normal() 
	{
		JsonObject json = JsonParser.parseString("{\"Action\":\"location\",\"BatteryLevel\":\"6600\",\"latitude\":\"52.253197\",\"IMEI\":\"003858092270329\",\"longitude\":\"-0.858512\"}").getAsJsonObject();
		String[] result = DemoApplication.readJSON(json);
		System.out.println(result[0]);
		System.out.println(result[1]);
		
		String[] expected = {"003858092270329", "location"};
		Assertions.assertArrayEquals(expected, result);
	}
	
	
	@Test
	@DisplayName("JSON null test")
	public void JSON_Null_Data() 
	{
		String[] result = DemoApplication.readJSON(null);
		Assertions.assertEquals(null, result);
	}

}
