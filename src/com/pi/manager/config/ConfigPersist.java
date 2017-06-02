package com.pi.manager.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.alibaba.fastjson.JSON;
import com.pi.manager.utils.LoggerFactory;

public class ConfigPersist {

	private static String path;

	static {
		path = new ConfigPersist().getClass().getResource("/").getFile().toString() + "config.json";
	}

	public static void loadConfig() {
		BufferedReader br = null;

		try {
			if (!new File(path).exists())
			{
				Config.setConfig(new Config());
				return;
			}
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
			String data = null;
			StringBuffer buffer = new StringBuffer();

			while ((data = br.readLine()) != null) {
				buffer.append(data);
			}

			Config config = JSON.parseObject(buffer.toString(), Config.class);
			Config.setConfig(config);
		} catch (Exception e) {
			LoggerFactory.getInfoLogger().info("The load config module has an exception." + e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存配置到文件
	 */
	public static void saveConfig() {
		try {

			Config config = Config.getConfigInstance();
			String jsonConfig = JSON.toJSONString(config, true);

			if (!new File(path).exists()) {
				File file = new File(path);
				file.createNewFile();
			}
			byte b[] = jsonConfig.getBytes();

			FileOutputStream in = new FileOutputStream(path);
			in.write(b, 0, b.length);
			in.close();

		} catch (Exception e) {
			LoggerFactory.getInfoLogger().info("The save config module has an exception." + e.getMessage());
		}
	}
}
