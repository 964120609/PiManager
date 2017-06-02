package com.pi.manager.listener;

public interface ServiceListener {

	boolean start();
	boolean stop();
	boolean restart();
	/**
	 * true -ON<br>
	 * false ->OFF
	 * @return
	 */
	boolean status();
}
