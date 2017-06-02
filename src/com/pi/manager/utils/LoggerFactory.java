/**
 * 
 */
package com.pi.manager.utils;

import org.apache.log4j.Logger;

/**
 * @author chao
 * @date 2016年8月15日下午2:25:16
 */
public class LoggerFactory {
	private static Logger logger_info ;
	
	public LoggerFactory() {
		logger_info = Logger.getLogger("logger_info");
	}

	public static Logger getInfoLogger()
	{
		if(logger_info == null){
			logger_info = Logger.getLogger("logger_info");
		}
		
		return logger_info;
	}
}
