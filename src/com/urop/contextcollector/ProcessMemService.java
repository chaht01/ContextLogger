package com.urop.contextcollector;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class ProcessMemService extends Service {
	private static final boolean D = true;
	private static final String TAG = "ProcessMemService";
	private static final int REBOOT_DELAY_TIMER = 10 * 1000;
    private ActivityManager mActivityManager;

    //for app memory usage
    private List<ActivityManager.RunningAppProcessInfo> appList;
    private int[] pid;
    private String[] pname;
    private android.os.Debug.MemoryInfo[] pmem;
	
	
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
	
						appList = mActivityManager.getRunningAppProcesses();
						pid = new int[appList.size()];
						pname = new String[appList.size()];
						for(int position=0;position<appList.size();position++){
							ActivityManager.RunningAppProcessInfo App = appList.get(position);
							pid[position] = App.pid;
							pname[position] = App.processName;
						}
						pmem = mActivityManager.getProcessMemoryInfo(pid);
						Log.e("aadsf","pname size:"+pname.length+"and pmem size:"+pmem.length);
						Log.e("aadsf","pname is:"+pname[30]+" and pmem is:"+pmem[30].getTotalPss());				    
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
		Intent intent = new Intent(ProcessMemService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_PROCESSMEMSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(ProcessMemService.this, 0, intent, 0);
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += REBOOT_DELAY_TIMER; 
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
	}

	void unregisterRestartAlarm(){
		if(D) Log.d(TAG, "unregisterRestartAlarm()");
		Intent intent = new Intent(ProcessMemService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_PROCESSMEMSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(
		ProcessMemService.this, 0, intent, 0);
		
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
}
