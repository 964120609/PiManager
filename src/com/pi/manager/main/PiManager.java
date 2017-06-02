package com.pi.manager.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import com.pi.manager.config.Config;
import com.pi.manager.config.Config.DiskInfo;
import com.pi.manager.control.DiskService;
import com.pi.manager.control.DiskServiceFactory;
import com.pi.manager.control.FanSpeedControl;
import com.pi.manager.control.FanSpeedFactory;
import com.pi.manager.control.LedBlink;
import com.pi.manager.control.LedBlinkFactory;
import com.pi.manager.control.SoftwareService;
import com.pi.manager.utils.LoggerFactory;
import com.pi.manager.utils.ShellUtils;
import com.pi.manager.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;

public class PiManager implements Observer,WrapperListener {
	private GpioStatusMonitor gpioStatusMonitor;
	private SystemTempMonitor systemTempMonitor;
	private FanSpeedControl speedControl;
	private LedBlink blink;

	private boolean isDiskStatusFlag = false;

	private boolean allowDiskTask = true;

	private Config mConfig = Config.getConfigInstance();

	private HashMap<String, Boolean> diskHashMap = new HashMap<String, Boolean>();
	private Object objectLock = new Object();
	private PiManager piManager;
	
	public static void main(String[] args) {
		
		  WrapperManager.start(new PiManager(), args); 
	}
	

	public void controlEvent(int arg0) {
		
	}

	public Integer start(String[] arg0) {
		//报告进度。防止jvm认为程序死了，强制重启程序
		WrapperManager.signalStarting(30000); 
		
		piManager = new PiManager();
		
		piManager.initLog();
		piManager.init();
		//添加钩子监听程序关闭信号
		//Runtime.getRuntime().addShutdownHook(piManager);
		//启动好了.led闪三次表示就绪
		piManager.blink.blinkNum(1000, 3, null);
		
		return null;
	}

	public int stop(int arg0) {
		
		try
		{
			WrapperManager.signalStarting(30000); 
			
			piManager.blink.high(null);

			piManager.gpioStatusMonitor.deleteObservers();
			piManager.systemTempMonitor.stop();

			if (isDiskStatusFlag) 
				diskAndSoftOff();

			piManager.blink.low(null);
			piManager.speedControl.speedMin();

			GpioController gpio = GpioFactory.getInstance();
			gpio.shutdown();
			
			LoggerFactory.getInfoLogger().info("program stoped");
			
			WrapperManager.signalStarting(5000); 
		}
		catch (Exception e) 
		{
			LoggerFactory.getInfoLogger().info("The stop module has an exception." + e.getMessage());
		}
		
		return 0;
	}

	private void init() {
		LoggerFactory.getInfoLogger().info("program started");
		
		gpioStatusMonitor = new GpioStatusMonitor();
		gpioStatusMonitor.addObserver(this);

		systemTempMonitor = new SystemTempMonitor(objectLock);
		systemTempMonitor.start();

		speedControl = FanSpeedFactory.getFanSpeedInstance();
		blink = LedBlinkFactory.getLedBlinkInstance();
	}
	
	/**
	 * 初始化log4j,替换log4j中设置的'log4jdir'
	 */
	private void initLog()
	{
		System.getProperties().remove("log4jdir");
		System.setProperty("log4jdir", System.getProperty("user.dir") + "/logs");
	}

	public void update(Observable o, Object arg) {
		if (arg == null)
			return;

		try{
			if (arg instanceof GpioPin) {
				GpioPin gpioPin = (GpioPin) arg;

				// 硬盘按钮
				if (gpioPin.getPin().getAddress() == Utils.convertToPin(mConfig.getPinButtonDisk()).getAddress()) {
					try {
						// 正在执行相关任务，暂时不响应按键事件
						if (!allowDiskTask)
							return;
						
						LoggerFactory.getInfoLogger().info("recived disk button press event");

						allowDiskTask = false;
						blink.high(Utils.convertToPin(mConfig.getPinLedDisk()));

						// 如果状态是Ture,关闭硬盘和服务
						if (isDiskStatusFlag) {
							
							LoggerFactory.getInfoLogger().info("close disk&service");

							isDiskStatusFlag = !diskAndSoftOff();

							//如果硬盘没关掉.就blink led
							if(isDiskStatusFlag)
							{
								blink.blinkNum(100, 0, Utils.convertToPin(mConfig.getPinLedDisk()));
								LoggerFactory.getInfoLogger().info("close disk&service failure");
								return;
							}
							systemTempMonitor.setReadDiskTemp(false,null);
							
							LoggerFactory.getInfoLogger().info("close disk&service success");
						}
						// 否则开启软件和服务
						else {
							
							//启动前需要暂停风扇
							if (mConfig.isPauseWhenDiskStart()) {
								LoggerFactory.getInfoLogger().info("fan max speed work " + mConfig.getPauseTime()/1000 + "s");
								
								// 风扇全速工作10秒后关闭风扇
								systemTempMonitor.pause();

								speedControl.speedMax();
								// 风扇全速工作时间
								Thread.sleep(mConfig.getPauseTime());

								speedControl.speedMin();
							} 
							
							LoggerFactory.getInfoLogger().info("open disk&service");
							// 风扇已经关闭,启动硬盘,加载服务
							isDiskStatusFlag = diskAndSoftOn();

							// 硬盘启动完成,唤醒温度控制线程继续做风扇调速的工作
							synchronized (objectLock) {
								objectLock.notifyAll();
								systemTempMonitor.unPause();

								// 设置是否需要读硬盘温度
								systemTempMonitor.setReadDiskTemp(isDiskStatusFlag, diskHashMap);
								
								LoggerFactory.getInfoLogger().info("notify fan control thread do work");
							}
							
							//如果硬盘启动过程中有问题.就blink led
							if(!isDiskStatusFlag)
							{
								blink.blinkNum(300, 0, Utils.convertToPin(mConfig.getPinLedDisk()));
								LoggerFactory.getInfoLogger().info("open disk&service failure");
								return;
							}
							
							LoggerFactory.getInfoLogger().info("open disk&service success");
						}
						
						blink.low(Utils.convertToPin(mConfig.getPinLedDisk()));
						
					} catch (Exception e) {
						LoggerFactory.getInfoLogger().info("The disk&service module has an exception." + e.getMessage());
					} finally {
						// 任务都做完了,允许响应后面的按键事件
						allowDiskTask = true;
					}
				}
				// 风扇按钮
				else if (gpioPin.getPin().getAddress() == Utils.convertToPin(mConfig.getPinButtonFan()).getAddress()) {

					synchronized (objectLock) {

						speedControl.switchPosition();
						int stall = speedControl.stall();
						
						LoggerFactory.getInfoLogger().info("switch fan control max position to " + stall);
						
						if(stall == 0)
							blink.blinkNum(5000, 1, Utils.convertToPin(mConfig.getPinLedFan()));
						else
							blink.blinkNum(500, stall, Utils.convertToPin(mConfig.getPinLedFan()));
					}
				}
			}
		}
		catch(Exception e)
		{
			LoggerFactory.getInfoLogger().info("The listener button event module has an exception." + e.getMessage());
		}
	}

	/**
	 * 启动硬盘
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean diskAndSoftOn() throws Exception {
		boolean isStarted = false;

		//启动硬盘供电
		DiskService diskService = DiskServiceFactory.getDiskServiceFactory();
		diskService.start();

		ArrayList<DiskInfo> diskInfos = mConfig.getDiskInfoList();

		long currTime = System.currentTimeMillis();
		while (!isStarted) {
			// 挂载磁盘超时，停止尝试
			if ((System.currentTimeMillis() - currTime) > 60000) {
				isStarted = true;

				if (diskHashMap.size() == 0)
					return false;
				
				LoggerFactory.getInfoLogger().info("read disk info timeout,skip");
				break;
			}

			Thread.sleep(5000);

			ShellUtils.CommandResult result = ShellUtils.execCommand("blkid", true, true);
			if (result.result == 0) {
				String resultStr = result.successMsg;

				String[] msg = resultStr.split("\n");
				for (String mStr : msg) {
					/// dev/mmcblk0p9: LABEL="root0"
					/// UUID="0de34730-c0a1-408d-aad7-6d6f243b5863" TYPE="ext4"
					/// PARTUUID="0009c43b-09"
					String deviceName = mStr.split(":")[0].trim();
					String[] deviceInfo = mStr.split(":")[1].trim().replace("\"", "").split(" ");

					for (String tempInfo : deviceInfo) {
						// String key = tempInfo.split("=")[0];
						String value = tempInfo.split("=")[1].trim();

						for (DiskInfo info : diskInfos) {
							// 是需要挂载的磁盘的UUID

							if(diskHashMap.containsKey(deviceName)) break;
							if (info.UUID.equals(value) || info.PTUUID.equals(value) || info.PARTUUID.equals(value)) {
								
								LoggerFactory.getInfoLogger().info("find new disk info, umount disk first");
								umountDisk(deviceName);
								
								boolean res = mountDisk(deviceName, info.mountPoint);
								LoggerFactory.getInfoLogger().info("mount disk result " + res);
								
								diskHashMap.put(deviceName, res);
							}
						}
					}
				}
				
				if(diskHashMap.size() == diskInfos.size())
					break;
			}
			else
			{
				LoggerFactory.getInfoLogger().info("execute blkid cmd failure. " + result.errorMsg);
			}
		}
		

		SoftwareService softwareService = new SoftwareService();
		boolean status = softwareService.restart();

		return status;
	}

	private boolean diskAndSoftOff() {

		SoftwareService softwareService = new SoftwareService();
		if (!softwareService.stop())
			return false;

		boolean status = false;
		for (Entry<String, Boolean> entry : diskHashMap.entrySet()) {

			if (entry.getValue())
				status = umountDisk(entry.getKey());
		}

		if (status) 
		{
			DiskService diskService = DiskServiceFactory.getDiskServiceFactory();
			status = diskService.stop();
		}
		else
		{
			LoggerFactory.getInfoLogger().info("skip close disk operation");
		}

		return status;
	}

	private boolean mountDisk(String deviceName, String mountPoint) {
		// sudo mount /dev/sdb1 /media/pi/wd/
		ShellUtils.CommandResult result = ShellUtils.execCommand("mount " + deviceName + " " + mountPoint, true, true);
		if (result.result == 0) {
			return true;
		}
		
		LoggerFactory.getInfoLogger().info("mount disk failure. " + result.errorMsg);
		return false;

	}

	private boolean umountDisk(String deviceName) {
		ShellUtils.CommandResult result = ShellUtils.execCommand("umount " + deviceName, true, true);
		if (result.result == 0) {
			return true;
		}
		
		LoggerFactory.getInfoLogger().info("umount disk failure. " + result.errorMsg);
		return false;
	}
}
