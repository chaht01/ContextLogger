package com.urop.contextlogger;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.urop.R;
import com.urop.contextcollector.AppCounterService;
import com.urop.contextcollector.CollectorPersistentService;
import com.urop.contextcollector.CollectorRestartService;
import com.urop.contextcollector.ProcessMemService;
import com.urop.contexttransmitter.TransmitterPersistentService;
import com.urop.contexttransmitter.TransmitterRestartService;

public class MainActivity extends Activity {

	private static final boolean D = true;
	private static final String TAG = "MainActivity";

	/* context collector */
	BroadcastReceiver collectorReceiver;
	Intent collectorIntentMyService;
	Intent appCounterService;
	Intent processMemService;
	/* context transmitter */
	BroadcastReceiver transmitterReceiver;
  Intent transmitterIntentMyService;
	
	EditText editText;
	Button button;
	TextView textView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if(D) Log.d(TAG, "service start!!!!!!");
		
		/* context collector */
    // immortal service ������
    collectorIntentMyService = new Intent(this, CollectorPersistentService.class);
	appCounterService = new Intent(this,AppCounterService.class);
	processMemService = new Intent(this,ProcessMemService.class);
        // ��������� ������
    collectorReceiver = new CollectorRestartService();
	  try 
	    {
	      // xml������ ������������ ���?
	            // ��������� ��������� ������ ��������� ������������?
	      IntentFilter mainFilter = new IntentFilter("com.urop");
	            
	            // ��������� ������
	      registerReceiver(collectorReceiver, mainFilter);
	            
	            // ��������� ������
	      startService(collectorIntentMyService);
	      startService(appCounterService);
	      startService(processMemService);
	      
	      if(D) Log.d(TAG, "collector service work!!!!!!");
	            
	  }catch (Exception e) {
	      if(D) Log.d(TAG, e.getMessage()+"");
	      e.printStackTrace();
	     }
	  
	  editText = (EditText) findViewById(R.id.file_name); // ������ ������������
	  button = (Button) findViewById(R.id.read_file_btn); // ������ ������
	  textView = (TextView) findViewById(R.id.read_file_result); // ������
	  
	  button.setOnClickListener(new OnClickListener() {
		  
          @Override
          public void onClick(View v) {
              try {
            	  	String fileName = editText.getText().toString();
            	  	//Log.e(TAG, getClass().getResourceAsStream("/data/data/com.urop/files/"+fileName)+"");
            	  	FileInputStream fis = openFileInput(fileName);
            	  	//FileInputStream fis = openFileInput("/data/data/com.example.contextcollector/files/"+fileName);
            	  	byte[] buffer = new byte[fis.available()];
                  fis.read(buffer);
                  String str = new String(buffer);
                  textView.setText(str);
                  textView.setMovementMethod(new ScrollingMovementMethod());
                  fis.close();

              } catch (Exception e) {
                  Log.e("File", "������=" + e);
                  Toast toast = Toast.makeText(getApplicationContext(), "������ ��������� ������ ��������� ������������.", Toast.LENGTH_SHORT);
                  toast.show();
             				}

          }// onClick
      });
	  
	  	/* context transmitter */
			// immortal service ������
		  transmitterIntentMyService = new Intent(this, TransmitterPersistentService.class);
		      
		     // ��������� ������
		  transmitterReceiver = new TransmitterRestartService();
		      
		  try 
		     {
		     // xml������ ������������ ���?
		          // ��������� ��������� ������ ��������� ������������?
		     IntentFilter mainFilter = new IntentFilter("com.urop");
		          
		          // ��������� ������
		     registerReceiver(transmitterReceiver, mainFilter);
		          
		          // ��������� ������
		     startService(transmitterIntentMyService);
		     
		     if(D) Log.d(TAG, "transmitter service work!!!!!!");
		     }catch (Exception e) {
			      if(D) Log.d(TAG, e.getMessage()+"");
			      e.printStackTrace();
			     }
	}
	@Override
	protected void onStart() {
		super.onStart();
		if(D) Log.d(TAG, this.getFilesDir()+ "");
		/*File mydir = this.getDir("UropContextCollector", Context.MODE_PRIVATE); //Creating an internal dir;
		try {
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(  
				      openFileInput("myfile")));
		} catch (FileNotFoundException e) {
			File fileWithinMyDir = new File(mydir, "myfile"); //Getting a file within the dir.
			try {
				FileOutputStream out = new FileOutputStream(fileWithinMyDir);
				out.close();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}*/
	}




	@Override
	protected void onDestroy(){
		super.onDestroy();
		// ��������� ��������� ������ ��������� ������
    if(D) Log.d(TAG, "Service Destroy");
    unregisterReceiver(collectorReceiver);
    unregisterReceiver(transmitterReceiver);
    
    super.onDestroy();
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
