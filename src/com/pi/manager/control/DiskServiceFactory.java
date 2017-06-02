package com.pi.manager.control;

public class DiskServiceFactory {
	
	private static DiskService diskService;

	public static DiskService getDiskServiceFactory()
	{
		if(diskService == null)
			diskService = new DiskService();
		
		
		return diskService;
	}
}
