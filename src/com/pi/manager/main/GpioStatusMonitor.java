package com.pi.manager.main;

import java.util.Observable;

import com.pi.manager.config.Config;
import com.pi.manager.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinEdge;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Gpio口状态监控
 * @author kaers
 * @date 2017年5月6日
 * @time 下午3:36:12
 */
public class GpioStatusMonitor extends Observable implements GpioPinListenerDigital{

	public GpioStatusMonitor() {
		
		Config mConfig = Config.getConfigInstance();
		
		// create GPIO controller
		GpioController gpio = GpioFactory.getInstance();

		// provision gpio input pins with its internal pull down resistor enabled
		GpioPinDigitalInput[] pins = {
				gpio.provisionDigitalInputPin(Utils.convertToPin(mConfig.getPinButtonDisk()), PinPullResistance.PULL_DOWN),
				gpio.provisionDigitalInputPin(Utils.convertToPin(mConfig.getPinButtonFan()), PinPullResistance.PULL_DOWN),
		};
		// create and register gpio pin listener
		gpio.addListener(this, pins);
	}

	//如果当前接口回调线程被阻塞，将会从线程池中拿一个新的线程通知事件，而不会导致事件通知被阻塞所延时，所以在处理事件的时候可以做耗时操作
	public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
		
		//检测按键松开的事件
		if(event.getEdge().getValue()== PinEdge.RISING.getValue())
		{
			//检测到变化，通知Handler处理
			setChanged();
			notifyObservers(event.getPin());
		}
	}
}
