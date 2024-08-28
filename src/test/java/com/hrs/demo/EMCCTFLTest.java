package com.hrs.demo;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class EMCCTFLTest {
	
    private static final String PSA_CODE = "test";
    private static final String EMCC_PSA_CODE = "EMCCPSA";
    private static final String EMCC_TFL_CODE = "EMCCTFL";
	
	@Test
	void contextLoads() {
	}
	
	@Test
	@DisplayName("check if a WZ is EMCCReady, true")
	public void checkEMCCReady_True() 
	{
		//make WZ
        double [][] icelandPolygon = {
      		  {67.2421663499099, -26.5442138314509},
      		  {62.75958290911809, -25.9948974252009},
      		  {62.78973979810564, -9.6692138314509},
      		  {67.42843690437506, -10.1745849252009},
      		};
		WorkZone test= new WorkZone("test", icelandPolygon);
		
		//make device and device map
		Map<String, DeviceInfo> devices = new HashMap<>();
		DeviceInfo device1 =  new DeviceInfo(0, 0, null, null, null, EMCC_PSA_CODE, 0, null);
		DeviceInfo device2 =  new DeviceInfo(0, 0, null, null, null, EMCC_TFL_CODE, 0, null);
		devices.put("1",device1);
		devices.put("2",device2);
		
		test.addDevice("1");
		test.addDevice("2");
		
		
		boolean result = EMCCTFL.checkEMCCReady(test, devices);
		Assertions.assertEquals(true, result);
	}
	
	
	@Test
	@DisplayName("check if a WZ is EMCCReady, false")
	public void checkEMCCReady_False() 
	{
		//make WZ
        double [][] icelandPolygon = {
      		  {67.2421663499099, -26.5442138314509},
      		  {62.75958290911809, -25.9948974252009},
      		  {62.78973979810564, -9.6692138314509},
      		  {67.42843690437506, -10.1745849252009},
      		};
		WorkZone test= new WorkZone("test", icelandPolygon);
		
		//make device and device map
		Map<String, DeviceInfo> devices = new HashMap<>();
		DeviceInfo device1 =  new DeviceInfo(0, 0, null, null, null, EMCC_PSA_CODE, 0, null);
		DeviceInfo device2 =  new DeviceInfo(0, 0, null, null, null, PSA_CODE, 0, null);
		devices.put("1",device1);
		devices.put("2",device2);
		
		//add devices to WZ
		test.addDevice("1");
		test.addDevice("2");
		
		
		boolean result = EMCCTFL.checkEMCCReady(test, devices);
		Assertions.assertEquals(false, result);
	}
	
	@Test
	@DisplayName("Distance between 2 points")
	public void distance() 
	{
		double result = EMCCTFL.distanceBetween2Points(52.944197, -0.818512, 52.948197, -0.818512);
		Assertions.assertEquals(0.4447797065784902, result);
	}

}
