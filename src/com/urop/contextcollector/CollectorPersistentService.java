package com.urop.contextcollector;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class CollectorPersistentService extends Service{
	
	private static final boolean D = true;
	private static final String TAG = "PersistentService";
	private static final int REBOOT_DELAY_TIMER = 10 * 1000;
	private static final int LOCATION_UPDATE_DELAY = 5 * 1000; 
		
		
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
						Calendar date = Calendar.getInstance(Locale.KOREA);
						SimpleDateFormat filenameFormator = new SimpleDateFormat("hh_mm", Locale.KOREA);
						SimpleDateFormat dataFormator = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss",Locale.KOREA);
						int hour = date.get(Calendar.HOUR_OF_DAY);
						int minute = date.get(Calendar.MINUTE);
						int second = date.get(Calendar.SECOND);
						int durationTime = (hour * 3600 + minute * 60 + second);
						
						if( (durationTime * 1000) % LOCATION_UPDATE_DELAY == 0 ){
							// load past duration time on SharedPreferences 
							SharedPreferences prefIn = getSharedPreferences("pref", MODE_PRIVATE);
							String pastDurationTime = prefIn.getString("durationTime", "-1");
							if(Integer.parseInt(pastDurationTime) == durationTime) continue;
							else{
								// save message list on SharedPreferences 
								SharedPreferences prefOut = getSharedPreferences("pref", MODE_PRIVATE);
								SharedPreferences.Editor editor = prefOut.edit();
								editor.putString("durationTime", durationTime+"");
								editor.commit();
        	    			}        	    
				  
							String fileName = filenameFormator.format(date.getTime()) + ".txt";
							String datetime = dataFormator.format(date.getTime());
							FileOutputStream fos;
							try {
								Log.e("time", hour+":"+minute+":"+second);
								fos = openFileOutput(fileName, Activity.MODE_APPEND);
								fos.write((datetime+"\n").getBytes()); // String��� byte��������� ��������� ������
								fos.close();
							} catch (FileNotFoundException e){
								e.printStackTrace();
							} catch (IOException e){
								e.printStackTrace();
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
		Intent intent = new Intent(CollectorPersistentService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_PERSISTENTSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(CollectorPersistentService.this, 0, intent, 0);
		long firstTime = SystemClock.elapsedRealtime();
		firstTime += REBOOT_DELAY_TIMER;
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, REBOOT_DELAY_TIMER, sender);
	}

	void unregisterRestartAlarm(){
		if(D) Log.d(TAG, "unregisterRestartAlarm()");
		Intent intent = new Intent(CollectorPersistentService.this, CollectorRestartService.class);
		intent.setAction(CollectorRestartService.ACTION_RESTART_PERSISTENTSERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(
		CollectorPersistentService.this, 0, intent, 0);
		
		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		am.cancel(sender);
	}
		
}
