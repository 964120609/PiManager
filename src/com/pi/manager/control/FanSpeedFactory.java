package com.pi.manager.control;

public class FanSpeedFactory {

	private static FanSpeedControl fanSpeedControl;
	
	public static FanSpeedControl getFanSpeedInstance()
	{
		if(fanSpeedControl == null)
		{
			fanSpeedControl = new FanSpeedControl();
		}
		
		return fanSpeedControl;
	}
}
