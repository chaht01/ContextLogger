package com.urop.contextcollector;

import java.util.LinkedList;
import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class AppCounterService extends Service {
	private static final boolean D = true;
	private static final String TAG = "AppCounterService";
	private static final int REBOOT_DELAY_TIMER = 10 * 1000;
	private static final int LOCATION_UPDATE_DELAY = 5 * 1000; 
		
	private final String LOG_NAME = "AppCCC";
	
    private static Context mContext = null;
    private String recentComponentName;
    private ActivityManager mActivityManager;
    
    //for app counter
    private LinkedList<String> AppName = new LinkedList();
    private LinkedList<Integer> AppCnt = new LinkedList();

    
	
	@Override
	public IBinder onBind(Intent intent){
			if(D) Log.d(TAG, "onBind()");
	    return null;
	 }
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		if(D) Log.d(TAG, "onCreate()");
		unregisterRestartAlarm(); 
		new Thread(new Runnable(){
				public void run(){
					while(true){
						mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	
						
				        List<ActivityManager.RecentTaskInfo> info = mActivityManager.getRecentTasks(1, Intent.FLAG_ACTIVITY_NEW_TASK);
				        if (info != null) {
				            ActivityManager.RecentTaskInfo recent = info.get(0);
				            Intent mIntent = recent.baseIntent;
				            ComponentName name = mIntent.getComponent();
				            
				            final PackageManager pm = getApplicationContext().getPackageManager();
			                ApplicationInfo ai;
			                try {
								ai = pm.getApplicationInfo(name.getPackageName(), 0);
							} catch (NameNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								ai=null;
							}
			                final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
				            
				            if (name.getPackageName().equals(recentComponentName)) {
				            } else {
				                Log.e(LOG_NAME, "onStartCommand() - name.getPackageName(): " + name.getPackageName());
				                recentComponentName = name.getPackageName();
				                Log.e(LOG_NAME, "Sqlite Insert - packageName: " + applicationName);
				                if(AppName.contains(applicationName)){
				                	int appindex = AppName.indexOf(applicationName);
				                	int appcnt = AppCnt.get(appindex);
				                	appcnt++;
				                	AppCnt.set(appindex, appcnt);
				                	
				                }else{
				                	AppName.add(applicationName);
				                	AppCnt.add(1);
				                }
				            }
				        }
				        
				        
										    
					}
				}
		}).start();
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(D) Log.d(TAG, "onDestroy()");
		registerRestartAlarm(); 
	}
	
	void registerRestartAlarm(){
		if(D) Log.d(TAG, "registerRestartAlarm()");
		Intent intent = new Intent(AppCounterService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_APPCOUNTERSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(AppCounterService.this, 0, intent, 0);
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += REBOOT_DELAY_TIMER; 
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
	}

	void unregisterRestartAlarm(){
		if(D) Log.d(TAG, "unregisterRestartAlarm()");
		Intent intent = new Intent(AppCounterService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_APPCOUNTERSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(
		AppCounterService.this, 0, intent, 0);
		
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
}
