package com.pi.manager.control;

import java.util.ArrayList;

import com.pi.manager.config.Config;
import com.pi.manager.config.Config.ServiceCMD;
import com.pi.manager.listener.ServiceListener;
import com.pi.manager.utils.LoggerFactory;
import com.pi.manager.utils.ShellUtils;
import com.pi.manager.utils.Utils;

/**
 * 读取配置文件，管理服务
 * @author kaers
 * @date 2017年5月20日
 * @time 下午5:01:23
 */
public class SoftwareService implements ServiceListener{
	
	private Config mConfig = Config.getConfigInstance();
	
	public boolean start() {

		if(mConfig.getServiceList() == null || mConfig.getServiceList().size() == 0) return true;

		ArrayList<ServiceCMD> serviceList = mConfig.getServiceList();

		for(ServiceCMD cmd : serviceList)
		{
			ShellUtils.CommandResult result = ShellUtils.execCommand(cmd.serviceStart, true, true);
			if(result.result != 0)
			{
				LoggerFactory.getInfoLogger().info("start service failure. " + result.errorMsg);
				return false;
			}
		}

		return true;
	}

	public boolean stop() {
		if(mConfig.getServiceList() == null || mConfig.getServiceList().size() == 0) return true;

		ArrayList<ServiceCMD> serviceList = mConfig.getServiceList();

		for(ServiceCMD cmd : serviceList)
		{
			ShellUtils.CommandResult result = ShellUtils.execCommand(cmd.serviceStop, true, true);
			if(result.result != 0)
			{
				//某些服务在停止状态再去停止会返回1 但是msg是空的，这时认为执行成功了 如pkill deluged命令
				if(Utils.isEmpty(result.errorMsg) && Utils.isEmpty(result.successMsg)) return true;
				
				LoggerFactory.getInfoLogger().info("stop service failure. " + result.result + result.errorMsg);
				return false;
			}
		}

		return true;
	}

	public boolean restart() {
		if(!stop()) return false;
		if(!start()) return false;

		return true;
	}

	public boolean status() {

		if(mConfig.getServiceList() == null || mConfig.getServiceList().size() == 0) return true;

		ArrayList<ServiceCMD> serviceList = mConfig.getServiceList();

		for(ServiceCMD cmd : serviceList)
		{
			ShellUtils.CommandResult result = ShellUtils.execCommand(cmd.serviceStatus, true, true);
			if(result.successMsg.indexOf(cmd.serviceStatusCheck) == -1)
			{
				LoggerFactory.getInfoLogger().info("check service status failure. " + result.errorMsg);
				return false;
			}
		}

		return true;
	}

}
