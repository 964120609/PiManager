package com.pi.manager.control;

public class LedBlinkFactory {
	public static LedBlink ledBlink;

	public static LedBlink getLedBlinkInstance()
	{
		if(ledBlink == null)
			ledBlink = new LedBlink();
		
		return ledBlink;
	}
}
