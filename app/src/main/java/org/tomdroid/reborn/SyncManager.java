package org.tomdroid.reborn;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Handler;

public class SyncManager {
	
	private static ArrayList<SyncService> services = new ArrayList<SyncService>();
	private SyncService service;
	
	public SyncManager() {
		createServices();
		service = getCurrentService();
	}

	public ArrayList<SyncService> getServices() {
		return services;
	}
	
	public static SyncService getService(String name) {
		
		for (int i = 0; i < services.size(); i++) {
			SyncService service = services.get(i);			
			if (name.equals(service.getName()))
				return service;
		}
		
		return null;
	}
	
	public void startSynchronization(boolean push) {
		
		service = getCurrentService();
		service.setCancelled(false);
		service.startSynchronization(push);
	}
	
	public SyncService getCurrentService() {
		String serviceName = "sdcard"; //Preferences.getString(Preferences.Key.SYNC_SERVICE);
		return getService(serviceName);
	}
	
	private static SyncManager instance = null;
	private static Activity activity;
	private static Handler handler;
	
	public static SyncManager getInstance() {
		
		if (instance == null)
			instance = new SyncManager();
		
		return instance;
	}
	
	public static void setActivity(Activity a) {
		activity = a;
		getInstance().createServices();
	}
	
	public static void setHandler(Handler h) {
		handler = h;
		getInstance().createServices();
	}

	private void createServices() {
		services.clear();
		
		services.add(new WebSyncService(activity, handler));
		services.add(new SdCardSyncService(activity, handler));
	}

	// new methods to TEdit
	
	public void pullNote(String guid) {
		SyncService service = getCurrentService();
		service.pullNote(guid);		
	}

	public void cancel() {
		service.setCancelled(true);
	}
}
