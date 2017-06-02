package com.pi.manager.control;

import com.pi.manager.config.Config;
import com.pi.manager.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;

public class LedBlink {

	private Config mConfig = Config.getConfigInstance();
	GpioPinDigitalOutput ledDisk ;
	GpioPinDigitalOutput ledFan ;

	public LedBlink() {
		GpioController gpio = GpioFactory.getInstance();
		ledDisk = gpio.provisionDigitalOutputPin(Utils.convertToPin(mConfig.getPinLedDisk()));
		ledFan = gpio.provisionDigitalOutputPin(Utils.convertToPin(mConfig.getPinLedFan()));
	}

	/**
	 * led blink
	 * @param delay 每delay秒闪一次
	 * @param count 一共闪多少次.如果为 0 则一直闪烁
	 * @param pin 指定是哪个pin执行blink
	 */
	public void blinkNum(long delay, int count, Pin pin)
	{
		long duration = 0;
		if(count != 0)
		{
			duration = (2*count -1) * delay;
		}
		
		if(pin == null)
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledFan.blink(0);

			ledDisk.blink(delay, duration);
			ledFan.blink(delay, duration);
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedDisk()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledDisk.blink(delay, duration);
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedFan()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledFan.blink(0);
			ledFan.blink(delay, duration == 0 ? delay : duration);
		}
	}

	/**
	 * 设置pin为high,即LED长亮
	 * @param pin 长亮引脚 如果pin==null disk&fan 都是长亮
	 */
	public void high(Pin pin)
	{
		if(pin == null)
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledFan.blink(0);

			ledDisk.high();
			ledFan.high();
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedDisk()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledDisk.high();
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedFan()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledFan.blink(0);
			ledFan.high();
		}
	}

	public void low(Pin pin)
	{
		if(pin == null)
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledFan.blink(0);

			ledDisk.low();
			ledFan.low();
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedDisk()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledDisk.blink(0);
			ledDisk.low();
		}
		else if(pin.getAddress() == Utils.convertToPin(mConfig.getPinLedFan()).getAddress())
		{
			//清除掉该引脚所有的blink任务
			ledFan.blink(0);
			ledFan.low();
		}
	}
}
