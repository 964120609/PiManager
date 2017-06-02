package com.pi.manager.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.pi.manager.control.FanSpeedControl;
import com.pi.manager.control.FanSpeedFactory;
import com.pi.manager.utils.LoggerFactory;
import com.pi.manager.utils.ShellUtils;

/**
 * 系统温度监控线程，监控CPU,GPU，硬盘等部件温度
 * 
 * @author kaers
 * @date 2017年5月21日
 * @time 下午2:37:32
 */
public class SystemTempMonitor implements Runnable {

	private boolean isPause = false;
	private boolean isStop = false;

	private boolean readDiskTemp = false;

	private Object objectLock;

	private int checkInterval = 60000;
	private float cpuTemp = 0;
	private float diskTemp = 0;

	private ArrayList<String> diskList;
	private FanSpeedControl speedControl;

	public SystemTempMonitor(Object objectLock) {
		this.objectLock = objectLock;

		diskList = new ArrayList<String>();
		speedControl = FanSpeedFactory.getFanSpeedInstance();
	}


	public void start() {
		isStop = false;

		Thread thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		isStop = true;

		speedControl.pwmStop();
	}

	/**
	 * 暂停系统温度监控线程
	 */
	public void pause() {
		isPause = true;
	}

	/**
	 * 
	 */
	public void unPause() {
		isPause = false;
	}

	public boolean isReadDiskTemp() {
		return readDiskTemp;
	}

	public void setReadDiskTemp(boolean readDiskTemp, HashMap<String, Boolean> diskHashMap) {
		this.readDiskTemp = readDiskTemp;

		// 需要监控硬盘温度时取需要监控的硬盘device列表
		if (readDiskTemp) {
			for (Entry<String, Boolean> entry : diskHashMap.entrySet()) {
				if (entry.getValue()) {
					diskList.add(entry.getKey());
				}
			}
		} else {
			diskTemp = 0;
			diskList.clear();
		}
	}

	//private long currentTime;
	public void run() {
		try {
			while (!isStop) {

				if (isPause) {
					synchronized (objectLock) {
						// System.out.println("SystemTempMonitor Thread wait..."
						// +new Date().toLocaleString());
						// 超时一个检测温度间隔的时间,就算是一直没有从wait中醒来,在一个checkInterval时间后也会出来继续往下走,检测温度,控制风扇,防止系统温度过高
						// 超时后风扇将会启动,如果还是Pause状态会再次等一个checkInterval时间
						objectLock.wait(checkInterval);
					}
				}

				readCPUTemp();

				// 读取硬盘温度
				if (isReadDiskTemp()) {
					readDiskTemp();
				}

				float maxTemp = 0;
				if (cpuTemp != 0 || diskTemp != 0) {
					maxTemp = cpuTemp > diskTemp ? cpuTemp : diskTemp;

					speedControl.fanSpeeder(maxTemp);
				}

				//这块代码太耗费cpu了。不在检查isReadDiskTemp了，最坏的情况就是睡60s + 读取硬盘信息60s(或wait超时) + 启动各种服务若干秒 硬盘正常工作后wait退出，正常控制
				//中间最多一两分钟（根据当前配置文件是105s）风扇是不转的,影响不大，但是能节约很多cpu
				//如果已经硬盘已经启动了，就改为睡眠的方式节约cpu，否则使用判断时间的方式，防止错过wait
				/*if(isReadDiskTemp())
				{
					Thread.sleep(checkInterval);
				}
				else
				{
					if(System.currentTimeMillis() - currentTime < checkInterval) continue;
					currentTime = System.currentTimeMillis();
				}*/
				
				Thread.sleep(checkInterval);
				// pwmFrequency in Hz = 19.2e6 Hz / pwmClock / pwmRange.
			}

		} catch (Exception e) {
			LoggerFactory.getInfoLogger().info("The fan control module has an exception." + e.getMessage());
		}
	}

	private void readCPUTemp()
	{
		// 读取cpu温度
		ShellUtils.CommandResult result = ShellUtils.execCommand("cat /sys/class/thermal/thermal_zone0/temp", true, true);
		if (result.result == 0) {
			cpuTemp = Float.parseFloat(result.successMsg) / 1000;
		}
		else
		{
			LoggerFactory.getInfoLogger().info("read cpu temperature failure. " + result.errorMsg);
		}
	}

	private void readDiskTemp()
	{
		if (diskList.size() > 0) {

			for (String deviceName : diskList) {

				ShellUtils.CommandResult result = ShellUtils.execCommand(" smartctl -A " + deviceName, true, true);

				if (result.result == 0) {

					String[] msgs = result.successMsg.split("\n");

					for (String mMsg : msgs) {

						// 194 Temperature_Celsius 0x0002 193 193 000 Old_age Always - 31 (Min/Max 12/53)
						if (mMsg.indexOf("194 Temperature_Celsius") > -1) {

							int index = mMsg.indexOf("(Min/Max");

							if (index > 4) {
								try {
									float temp = Float.parseFloat(mMsg.substring(index - 4, index).trim());
									diskTemp = temp > diskTemp ? temp : diskTemp;
									break;
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
				else
				{
					LoggerFactory.getInfoLogger().info("read cpu temperature failure. " + result.errorMsg);
				}
			}
		}
	}
}
