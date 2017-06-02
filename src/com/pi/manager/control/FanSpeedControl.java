package com.pi.manager.control;

import com.pi.manager.config.Config;
import com.pi.manager.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.wiringpi.Gpio;

public class FanSpeedControl {

	private Config mConfig = Config.getConfigInstance();
	private GpioPinPwmOutput pwm;

	private double minTemp = 30;
	private double maxMinTemp = 50;
	private double diffValue = 20;

	//最大档位全速 1000,每档200.共5档
	private int curPwmPosition = 1000;
	private final int perPosition = 200;

	public FanSpeedControl() {
		GpioController gpio = GpioFactory.getInstance();
		pwm = gpio.provisionPwmOutputPin(Utils.convertToPin(mConfig.getPinPwmFan()));
		
		Gpio.pwmSetMode(com.pi4j.wiringpi.Gpio.PWM_MODE_MS);
		Gpio.pwmSetRange(1000);
		Gpio.pwmSetClock(500);

		minTemp = mConfig.getSysMintemp();
		maxMinTemp = mConfig.getSysMaxtemp();
		diffValue = maxMinTemp - minTemp;
	}

	public void fanSpeeder(float temp) {

		//超出预设上线,不算了,直接开到最大,并把档位设置复位
		if(temp > mConfig.getSysMaxtemp())
		{
			setSpeedPwm(1000);
			
			curPwmPosition = 1000;
			return;
		}
		
		//如果是关闭的状态,就不算了,风扇关闭
		if(curPwmPosition == 0)
		{
			setSpeedPwm(0);
			return;
		}
		
		int mPwm = 0 ;
		
		double diff = temp-minTemp;
		if(diff < 0) return;
		mPwm = (int)(diff/diffValue*1000);
		
		if(mPwm > curPwmPosition) mPwm = curPwmPosition;
		
		//如果当前没关风扇,但是控制转速太低 也就直接关掉
		if(mPwm < 100)
		{
			setSpeedPwm(100);
		}
		else
		{
			setSpeedPwm(mPwm);
		}
	}

	/**
	 * 风扇调速
	 * @param pwmValue 0-1000 (0-关闭,1000-最大)
	 */
	public void setSpeedPwm(int pwmValue)
	{
		pwm.setPwm(pwmValue);
	}
	
	public void speedMax()
	{
		setSpeedPwm(1000);
	}
	
	public void speedMin()
	{
		setSpeedPwm(0);
	}

	public void switchPosition()
	{
		
		curPwmPosition -= perPosition;
		
		//从最大开始
		if(curPwmPosition < 0)
		{
			curPwmPosition = 1000;
		}
	}

	public void pwmStop()
	{
		//设置不输出
		pwm.setShutdownOptions(true);
	}
	
	/**
	 * 获取当前风扇档位
	 * @return
	 */
	public int stall()
	{
		return curPwmPosition/perPosition;
	}
}
