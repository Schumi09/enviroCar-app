package org.envirocar.app.application.service;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.envirocar.obdig.protocol.CommandExecutor;
import org.envirocar.obdig.protocol.OBDCommandLooper;
import org.envirocar.obdig.protocol.exception.LooperStoppedException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class AndroidCommandExecutor extends HandlerThread implements CommandExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(AndroidCommandExecutor.class);
	private Handler handler;
	private Set<Runnable> allKnownRunnables = new HashSet<Runnable>();
	private OBDCommandLooper commandLooper;
	
	public AndroidCommandExecutor(OBDCommandLooper commandLooper) {
		super("enviroCar-AndroidCommandExecutor");
		this.commandLooper = commandLooper;
	}
	
	@Override
	public void run() {
		Looper.prepare();
		logger.info("Command loop started. Hash:"+this.hashCode());
		
		handler = new Handler();
		this.commandLooper.initialize(this);
		
		try {
			Looper.loop();
		} catch (LooperStoppedException e) {
			logger.info("Command loop stopped. Hash:"+this.hashCode());
		}
	}
	

	@Override
	public void postDelayed(Runnable r, long delayPeriod) {
		this.handler.postDelayed(r, delayPeriod);
		this.allKnownRunnables.add(r);
	}

	@Override
	public void removeCallbacks(Runnable r) {
		this.handler.removeCallbacks(r);
	}

	@Override
	public void post(Runnable r) {
		this.postDelayed(r, 0);
	}

	@Override
	public void shutdownExecutions() {
		for (Runnable runnable : allKnownRunnables) {
			this.handler.removeCallbacks(runnable);
		}
	}

}
