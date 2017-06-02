package com.pi.manager.config;

import java.util.ArrayList;

public class Config
{
	private static Config config;
	public static Config getConfigInstance()
	{
		if(config == null)
		{
			ConfigPersist.loadConfig();
		}
		return config;
	}
	public static void setConfig(Config mConfig)
	{
		config = mConfig;
	}
	
	/**
	 * 服务启停及状态读取命令
	 * @author kaers
	 * @date 2017年5月21日
	 * @time 下午3:07:45
	 */
	public class ServiceCMD
	{
		public String serviceStart = "";
		public String serviceStop = "";
		public String serviceStatus = "";
		public String serviceStatusCheck = "";
	}
	
	/**
	 * 磁盘唯一标识符
	 * @author kaers
	 * @date 2017年5月21日
	 * @time 下午3:07:20
	 */
	public class DiskInfo{
		public String UUID = "";
		public String PARTUUID = "";
		public String PTUUID = "";
		
		public String mountPoint = "";
	}

	
	private int pinButtonDisk = 0;
	private int pinButtonFan = 2;

	private int pinLedDisk = 3;
	private int pinLedFan = 4;
	
	private int pinRelayEN1 = 6;
	private int pinRelayEN2 = 5;

	private int pinPwmFan = 1;
	
	private float sysMintemp = 30f;
	private float sysMaxtemp = 50f;
	private float adjustInterval = 1.5f;
	
	private boolean pauseWhenDiskStart = true;
	private int pauseTime = 15000;
	
	private ArrayList<ServiceCMD> serviceList;
	private ArrayList<DiskInfo> diskInfoList;
	
	
	public int getPinButtonDisk() {
		return pinButtonDisk;
	}
	public void setPinButtonDisk(int pinButtonDisk) {
		this.pinButtonDisk = pinButtonDisk;
	}
	public int getPinButtonFan() {
		return pinButtonFan;
	}
	public void setPinButtonFan(int pinButtonFan) {
		this.pinButtonFan = pinButtonFan;
	}
	public int getPinLedDisk() {
		return pinLedDisk;
	}
	public void setPinLedDisk(int pinLedDisk) {
		this.pinLedDisk = pinLedDisk;
	}
	public int getPinLedFan() {
		return pinLedFan;
	}
	public void setPinLedFan(int pinLedFan) {
		this.pinLedFan = pinLedFan;
	}
	public int getPinRelayEN1() {
		return pinRelayEN1;
	}
	public void setPinRelayEN1(int pinRelayEN1) {
		this.pinRelayEN1 = pinRelayEN1;
	}
	public int getPinRelayEN2() {
		return pinRelayEN2;
	}
	public void setPinRelayEN2(int pinRelayEN2) {
		this.pinRelayEN2 = pinRelayEN2;
	}
	public int getPinPwmFan() {
		return pinPwmFan;
	}
	public void setPinPwmFan(int pinPwmFan) {
		this.pinPwmFan = pinPwmFan;
	}
	
	public float getSysMintemp() {
		return sysMintemp;
	}
	public void setSysMintemp(float sysMintemp) {
		this.sysMintemp = sysMintemp;
	}
	public float getSysMaxtemp() {
		return sysMaxtemp;
	}
	public void setSysMaxtemp(float sysMaxtemp) {
		this.sysMaxtemp = sysMaxtemp;
	}
	public float getAdjustInterval() {
		return adjustInterval;
	}
	public void setAdjustInterval(float adjustInterval) {
		this.adjustInterval = adjustInterval;
	}
	public boolean isPauseWhenDiskStart() {
		return pauseWhenDiskStart;
	}
	public void setPauseWhenDiskStart(boolean pauseWhenDiskStart) {
		this.pauseWhenDiskStart = pauseWhenDiskStart;
	}
	public int getPauseTime() {
		return pauseTime;
	}
	public void setPauseTime(int pauseTime) {
		this.pauseTime = pauseTime;
	}
	public ArrayList<ServiceCMD> getServiceList() {
		return serviceList;
	}
	public void setServiceList(ArrayList<ServiceCMD> serviceList) {
		this.serviceList = serviceList;
	}
	public ArrayList<DiskInfo> getDiskInfoList() {
		return diskInfoList;
	}
	public void setDiskInfoList(ArrayList<DiskInfo> diskInfoList) {
		this.diskInfoList = diskInfoList;
	}
	
	
	
	
}
