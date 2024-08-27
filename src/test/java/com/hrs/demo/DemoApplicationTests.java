package com.hrs.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}
	
	/*
	@Test
	@DisplayName("Distance between 2 points")
	public void distance() 
	{
		double result = DemoApplication.distanceBetween2Points(52.944197, -0.818512, 52.948197, -0.818512);
		Assertions.assertEquals(0.4447797065784902, result);
	}
	
	@Test
	@DisplayName("Normal")
	public void createMessage_Normal() 
	{
		String result = DemoApplication.createMessage(new DeviceInfo(6600, 52.253197, -0.818512));
		Assertions.assertEquals("Normal", result);
	}
	
	@Test
	@DisplayName("Close to office")
	public void createMessage_Office() 
	{
		String result = DemoApplication.createMessage(new DeviceInfo(6600, 52.944197, -0.818512));
		Assertions.assertEquals("Nearly Home", result);
	}
	
	@Test
	@DisplayName("Low battery warning")
	public void createMessage_Battery_Warning() 
	{
		String result = DemoApplication.createMessage(new DeviceInfo(5500, 52.25319, -0.858512));
		Assertions.assertEquals("Warning low battery", result);
	}
	
	@Test
	@DisplayName("Low battery alarm, close to office")
	public void createMessage_Battery_Alarm_At_Office() 
	{
		String result = DemoApplication.createMessage(new DeviceInfo(4600, 52.944197, -0.818512));
		Assertions.assertEquals("ALARM - low battery, turning off", result);
	}
	
	@Test
	@DisplayName("Low battery alarm, not at office")
	public void createMessage_Battery_Alarm_Not_At_Office() 
	{
		String result = DemoApplication.createMessage(new DeviceInfo(4600, 52.253197, -0.818512));
		Assertions.assertEquals("ALARM - low battery, turning off", result);
	}
	
	@Test
	@DisplayName("Create message null test")
	public void createMessage_Null_Data() 
	{
		String result = DemoApplication.createMessage(null);
		Assertions.assertEquals("Device error", result);
	}
	
	
	@Test
	@DisplayName("JSON normal")
	public void JSON_Normal() 
	{
		double result = DemoApplication.readJSON("{\"Action\":\"location\",\"BatteryLevel\":\"6600\",\"latitude\":\"52.253197\",\"IMEI\":\"003858092270329\",\"longitude\":\"-0.858512\"}");
		Assertions.assertEquals(3.858092270329E12, result);
	}
	
	
	@Test
	@DisplayName("JSON null test")
	public void JSON_Null_Data() 
	{
		double result = DemoApplication.readJSON(null);
		Assertions.assertEquals(0, result);
	}
	*/

}
