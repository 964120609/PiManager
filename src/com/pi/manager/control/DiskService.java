package com.pi.manager.control;

import com.pi.manager.config.Config;
import com.pi.manager.listener.ServiceListener;
import com.pi.manager.utils.LoggerFactory;
import com.pi.manager.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;

public class DiskService implements ServiceListener {

	private Config mConfig;
	private GpioPinDigitalOutput outputEN1;
	private GpioPinDigitalOutput outputEN2;

	public DiskService() {
		mConfig = Config.getConfigInstance();

		GpioController gpio = GpioFactory.getInstance();

		outputEN1 = gpio.provisionDigitalOutputPin(Utils.convertToPin(mConfig.getPinRelayEN1()));
		outputEN2 = gpio.provisionDigitalOutputPin(Utils.convertToPin(mConfig.getPinRelayEN2()));

	}

	/**
	 * 启动硬盘
	 */
	public boolean start() {

		outputEN1.low();
		outputEN2.low();
		
		LoggerFactory.getInfoLogger().info("open disk power");

		return true;
	}

	/**
	 * 停止硬盘
	 */
	public boolean stop() {

		outputEN1.high();
		outputEN2.high();
		
		LoggerFactory.getInfoLogger().info("close disk power");

		return true;
	}

	/**
	 * 关闭硬盘后重新打开 --未实现，方法不可用--
	 */
	@Deprecated
	public boolean restart() {
		return false;
	}

	public boolean status() {

		if (outputEN1.isLow() && outputEN2.isLow()) {
			
			return true;
		} else {
			
			return false;
		}
	}

}
