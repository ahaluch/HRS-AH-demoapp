package com.hrs.demo;


public class WorkZoneUpdater {

	public void run() {
		
		while(true) 
		{
			DemoApplication.loadWorkZones();
			
			//wait for 30 seconds
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

}
