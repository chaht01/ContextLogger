package com.urop.contextcollector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CollectorRestartService extends BroadcastReceiver{

	public static final String ACTION_RESTART_PERSISTENTSERVICE = "ACTION.RESTART>PERSISTENTSERVICE";
	public static final String ACTION_RESTART_APPCOUNTERSERVICE = "ACTION.RESTART>APPCOUNTERSERVICE";
	public static final String ACTION_RESTART_PROCESSMEMSERVICE = "ACTION.RESTART>PROCESSMEMSERVICE";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		//TODO Auto-generated method stub
		 /* ��������� ��������� ������������ ������ ��������� ������ */
		if(intent.getAction().equals(ACTION_RESTART_PERSISTENTSERVICE)) {
			Intent i = new Intent(context, CollectorPersistentService.class);
			//Intent i = new Intent(this, PersistentService.class);
			context.startService(i);
		}
		if(intent.getAction().equals(ACTION_RESTART_APPCOUNTERSERVICE)) {
			Intent i = new Intent(context, AppCounterService.class);
			//Intent i = new Intent(this, PersistentService.class);
			context.startService(i);
		}
		if(intent.getAction().equals(ACTION_RESTART_PROCESSMEMSERVICE)) {
			Intent i = new Intent(context, ProcessMemService.class);
			//Intent i = new Intent(this, PersistentService.class);
			context.startService(i);
		}
		 /* ��� ��������������� ��������� ������ */
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	     Intent i = new Intent(context, CollectorPersistentService.class);
	     Intent j = new Intent(context, AppCounterService.class);
	     Intent k = new Intent(context, ProcessMemService.class);
	    context.startService(i);
	    context.startService(j);
	    context.startService(k);
        }
	}
	
}
