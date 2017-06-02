package com.pi.manager.utils;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class Utils {

	/**
	 * int Pin convert to gpio Pin
	 */
	public static Pin convertToPin(int pin) {

		Pin mPin = null;

		switch (pin) {
		case 0:
			mPin = RaspiPin.GPIO_00;
			break;

		case 1:
			mPin = RaspiPin.GPIO_01;
			break;

		case 2:
			mPin = RaspiPin.GPIO_02;
			break;

		case 3:
			mPin = RaspiPin.GPIO_03;
			break;

		case 4:
			mPin = RaspiPin.GPIO_04;
			break;

		case 5:
			mPin = RaspiPin.GPIO_05;
			break;

		case 6:
			mPin = RaspiPin.GPIO_06;
			break;
		}

		return mPin;
	}

	/**
	 * gpio Pin convert to int Pin
	 */
	public static int convertToIntPin(Pin pin) {

		if (pin == RaspiPin.GPIO_00) {
			return 0;
		} else if (pin == RaspiPin.GPIO_01) {
			return 1;
		} else if (pin == RaspiPin.GPIO_02) {
			return 2;
		} else if (pin == RaspiPin.GPIO_03) {
			return 3;
		} else if (pin == RaspiPin.GPIO_04) {
			return 4;
		} else if (pin == RaspiPin.GPIO_05) {
			return 5;
		} else if (pin == RaspiPin.GPIO_06) {
			return 6;
		}

		return -1;
	}
	
	/**
	 * 判断字符串是否为空
	 * @param string 要判断的字符串
	 * @return true 不为空 ,false 为空
	 */
	public static boolean isEmpty(String string){

		if(string == null ) return true;
		if(string.length() ==0 ) return true;
		if("".equals(string)) return true;

		return false;
	}

}
