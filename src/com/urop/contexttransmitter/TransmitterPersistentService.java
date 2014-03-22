package com.urop.contexttransmitter;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
 
public class TransmitterPersistentService extends Service implements Runnable {
	//private Socket socket;
	private static final int SERVERPORT = 9999;
	private static final String SERVER_IP = "martini.snu.ac.kr";
    private static final String TAG = "PersistentService";
    private String curr_time;
    // 서비스 종료시 재부팅 딜레이 시간, activity의 활성 시간을 벌어야 한다.
    private static final int REBOOT_DELAY_TIMER = 10 * 1000;
 
    // GPS를 받는 주기 시간
    private static final int LOCATION_UPDATE_DELAY = 5 * 1000; // 5 * 60 * 1000
 
    private Handler mHandler;
    private boolean mIsRunning;
    private int mStartId = 0;
 
    @Override
    public IBinder onBind(Intent intent) {
 
        Log.d("PersistentService", "onBind()");
        return null;
    }
 
    @Override
    public void onCreate() {
 
        // 등록된 알람은 제거
        Log.d("PersistentService", "onCreate()");
        unregisterRestartAlarm();
 
        super.onCreate();
 
        mIsRunning = false;
 
    }
 
    @Override
    public void onDestroy() {
 
        // 서비스가 죽었을때 알람 등록
        Log.d("PersistentService", "onDestroy()");
        registerRestartAlarm();
 
        super.onDestroy();
 
        mIsRunning = false;
    }
 
    /**
     * (non-Javadoc)
     * @see android.app.Service#onStart(android.content.Intent, int)
     * 
     * 서비스가 시작되었을때 run()이 실행되기까지 delay를 handler를 통해서 주고 있다.
     */
    @Override
    public void onStart(Intent intent, int startId) {
 
        Log.d("PersistentService", "onStart()");
        super.onStart(intent, startId);
 
        mStartId = startId;
 
        // 5분후에 시작
        mHandler = new Handler();
        mHandler.postDelayed(this, LOCATION_UPDATE_DELAY);
        mIsRunning = true;
 
    }
 
    /**
     * (non-Javadoc)
     * @see java.lang.Runnable#run()
     * 
     * 서비스가 돌아가고 있을때 실제로 내가 원하는 기능을 구현하는 부분
     */
    @Override
    public void run() {
 
        Log.e(TAG, "run()");
        
        if(!mIsRunning)
        {
            Log.d("PersistentService", "run(), mIsRunning is false");
            Log.d("PersistentService", "run(), alarm service end");
            return;
            
        } else {
            
            Log.d("PersistentService", "run(), mIsRunning is true");
            Log.d("PersistentService", "run(), alarm repeat after five minutes");
            
            function();
            
            mHandler.postDelayed(this, LOCATION_UPDATE_DELAY);
            mIsRunning = true;
        }
 
    }
 
    private void function() {
        
        Log.d(TAG, "========================");
        Log.d(TAG, "function()");
        Log.d(TAG, "========================");
        Log.d("prefname",getPreferences(0));
        String planTosend;
        if(getPreferences(1).equals("welcome")){
        	savePreferences(GetCurrTimeString());
        }else{
        	planTosend = getPreferences(1);
        	curr_time = GetCurrTimeString();
        	if (!planTosend.equals(curr_time)){
        		new Thread(new Runnable(){
        			public void run(){
        				//get device id
        				TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        				String deviceId = tManager.getDeviceId();
        				
        				try{
                			Log.d("log1","the man is mad1");
                			InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                			Log.d("log2","the man is mad2");
                			Socket sock = new Socket(serverAddr,SERVERPORT);
                			Log.d("log3","the man is mad3");
                			try{
                				PrintWriter out = new PrintWriter(new BufferedWriter(new
                				OutputStreamWriter(sock.getOutputStream())), true);
                				out.println(deviceId);
                				out.println(getPreferences(0)+".txt");
                				out.flush();
                				//getFilesDir().getPath()
                			  //DataInputStream dis = new DataInputStream(new FileInputStream(new File(getPreferences(0)+".txt")));
                				FileInputStream fis = openFileInput(getPreferences(0)+".txt");
                				DataOutputStream dos = new DataOutputStream(sock.getOutputStream());
                				/*byte[] buf = new byte[1024];
                				Log.e("전송준비완료"," ");
                				while(fis.read(buf)>0){
                					Log.e("전송내용", buf+"");
                					dos.write(buf);
                					//dos.flush();
                								}
                				Log.e("전송끝", " ");*/
                				byte[] buffer = new byte[fis.available()];
                				fis.read(buffer);
                				dos.write(buffer);
                				Log.e("전송내용", new String(buffer));
                				dos.flush();
                				dos.close();              								
                				fis.close();                				
                			}catch(Exception e){
                				e.printStackTrace();
                			}finally{
                				/* delete internal file */
                				File dir = getFilesDir();
                				File file = new File(dir, getPreferences(0)+".txt");
                				boolean deleted = file.delete();
                				Log.e("삭제여부", deleted+"");
                				
                				sock.close();
                				Log.d("success","전송완료!!!!");
                				Log.d("전송내역","전송파일날짜:"+getPreferences(0)+" 현재시각:"+GetCurrTimeString());
                       	savePreferences(curr_time);
                       	Log.d("modified","modified!!!!!!!");
                							}
                	}catch(Exception e){}
        					}
        		}).start();
        		
        	}else{
        		Log.d("modified","no happening");
        	}
        }
        
        
    }
  public static String GetCurrTimeString(){
    	Calendar date = Calendar.getInstance(Locale.KOREA);
    	SimpleDateFormat formator = new SimpleDateFormat("yyyy:MM:dd:hh_mm",Locale.KOREA);
    	String result = "";
    	
    	result = formator.format(date.getTime());
    	return result;	
    }
    
  private String getPreferences(int flag){
    SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
    if(flag==0){
    		return pref.getString("RecentUpdate", "welcome");
    	}
    else{
    		return pref.getString("RecentUpdate_full", "welcome");	
    	}
    }
   private void savePreferences(String string){
    	SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.putString("RecentUpdate_full",string);
    	editor.putString("RecentUpdate",string.substring(11));
    	editor.commit();
    }
   private void removePreferences(){
    	SharedPreferences pref = getSharedPreferences("pref",MODE_PRIVATE);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.remove("RecentUpdate");
    	editor.commit();
    }
    /**
     * 서비스가 시스템에 의해서 또는 강제적으로 종료되었을 때 호출되어
     * 알람을 등록해서 10초 후에 서비스가 실행되도록 한다.
     */
    private void registerRestartAlarm() {
 
        Log.d("PersistentService", "registerRestartAlarm()");
 
        Intent intent = new Intent(TransmitterPersistentService.this, TransmitterRestartService.class);
        intent.setAction(TransmitterRestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(TransmitterPersistentService.this, 0, intent, 0);
 
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += REBOOT_DELAY_TIMER; // 10초 후에 알람이벤트 발생
 
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime,REBOOT_DELAY_TIMER, sender);
    }
 
 
    /**
     * 기존 등록되어있는 알람을 해제한다.
     */
    private void unregisterRestartAlarm() {
 
        Log.d("PersistentService", "unregisterRestartAlarm()");
        Intent intent = new Intent(TransmitterPersistentService.this, TransmitterRestartService.class);
        intent.setAction(TransmitterRestartService.ACTION_RESTART_PERSISTENTSERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(TransmitterPersistentService.this, 0, intent, 0);
 
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
}