package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class StatusActivity extends Activity {

	public static TextView ts,tc,tg,tb,tw,ta,tt= null;
	Logger log;
	private StatusManager sM;
	private Message message;
	private static int wid = 0;
	public static boolean viewStatus = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_status);
		
		ts = (TextView)findViewById(R.id.status5);
        tc = (TextView)findViewById(R.id.status6);
        tg = (TextView)findViewById(R.id.status1);
        tb = (TextView)findViewById(R.id.status2);
        tw = (TextView)findViewById(R.id.status3);
        ta = (TextView)findViewById(R.id.status4);
        tt = (TextView)findViewById(R.id.status7);
        
		log = Logger.getLogger(StatusActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
        Bundle extras = getIntent().getExtras();        
		if(extras != null){
        	wid = extras.getInt("wid");
        	log.info("Bundle got: "+wid);
        }
        
        sM = new StatusManager(getApplicationContext());
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		viewStatus = true;
		AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} 
	  	
	  	new Thread(null, wTask, "wUpdate").start();
	  	
	}
	
	@Override
	public void onStop(){
		
		log.info("onStop");
		
		viewStatus = false;
		
		
		
	    super.onStop();	    
	}
	
	@Override
	public void onDestroy(){
		Thread thr = new Thread(null, sTask, "StatusUpdate");
        thr.start();
		super.onDestroy();
	}

	Runnable sTask = new Runnable() {
        public void run() {
        	log.info("Send statusview finished to standoutwindow");
        	DataMonitor.sendData(StatusActivity.this, DataMonitor.class, wid, DataMonitor.APP_STATUS_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		
        }
	};
	
	Runnable wTask = new Runnable() {
        public void run() {
        	while(viewStatus){
        		
        		message = mHandler.obtainMessage(1);
        		mHandler.sendMessage(message); 
        		
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	};
	
	private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {  
            super.handleMessage(msg); 
            switch(msg.what){
            case 0:            	
                break;
            case 1:            	
            	sM.getStatus();
        		sM.updateWidgetStatus();
        		updateConnStatus(sM._connectionStatus);
          	  	updateTaskStatus(sM._status);  
          	  	updateSensorStatus(sM._sensorStatus);
            	break;
            default:
               	break;
            }
            
        }  
    };
	
    private void updateGPSStatus(int sensorsta){
		if(viewStatus && tg != null){
			
			if ((sensorsta & Constants.STATUS_SENSOR_GPS) != Constants.STATUS_SENSOR_GPS) {	
				// If GPS service is disabled on mobile phone, give an alert dialog
				tg.setText("Diasabled");
				tg.setTextColor(Color.RED);
				
			}else{
				tg.setText("Enabled");
				tg.setTextColor(Color.GREEN);
				
			}
			
		}
	}
	
	private void updateBTStatus(int sensorsta){
		if(viewStatus && tb != null){
			if ((sensorsta & Constants.STATUS_SENSOR_BT) != Constants.STATUS_SENSOR_BT) {	
				// If GPS service is disabled on mobile phone, give an alert dialog
				tb.setText("Diasabled");
				tb.setTextColor(Color.RED);
				
			}else{
				tb.setText("Enabled");
				tb.setTextColor(Color.GREEN);
				
			}
			
		}
	}
	
	private void updateWifiStatus(int sensorsta){
		if(viewStatus && tw != null){
			if ((sensorsta & Constants.STATUS_SENSOR_WIFI) != Constants.STATUS_SENSOR_WIFI) {	
				// If GPS service is disabled on mobile phone, give an alert dialog
				StatusActivity.tw.setText("Diasabled");
				StatusActivity.tw.setTextColor(Color.RED);
				
			}else{
				StatusActivity.tw.setText("Enabled");
				StatusActivity.tw.setTextColor(Color.GREEN);
				
			}
			Log.d("status", "Wifi Status View Updated.");
		}
	}
	
	private void updateCellStatus(int sensorsta){
		if(viewStatus && tt != null){
			if ((sensorsta & Constants.STATUS_SENSOR_CELL) != Constants.STATUS_SENSOR_CELL) {	
				// If GPS service is disabled on mobile phone, give an alert dialog
				StatusActivity.tt.setText("Diasabled");
				StatusActivity.tt.setTextColor(Color.RED);
				
			}else{
				StatusActivity.tt.setText("Enabled");
				StatusActivity.tt.setTextColor(Color.GREEN);
				
			}
			Log.d("status", "Cell Status View Updated.");
		}
	}
	
	private void updateAudioStatus(int sensorsta){
		if(viewStatus && ta != null){
			if ((sensorsta & Constants.STATUS_SENSOR_AUDIO) != Constants.STATUS_SENSOR_AUDIO) {	
				// If GPS service is disabled on mobile phone, give an alert dialog
				StatusActivity.ta.setText("Diasabled");
				StatusActivity.ta.setTextColor(Color.RED);
				
			}else{
				StatusActivity.ta.setText("Enabled");
				StatusActivity.ta.setTextColor(Color.GREEN);
				
			}
			Log.d("status", "Audio Status View Updated.");
		}
	}
	
	private void updateSensorStatus(int sensorsta){
		updateGPSStatus(sensorsta);
		updateWifiStatus(sensorsta);
		updateBTStatus(sensorsta);
		updateCellStatus(sensorsta);
		updateAudioStatus(sensorsta);
	}
	
	private void updateTaskStatus(int sta){
		if(viewStatus  && ts != null){
			
			switch(sta){
				case Constants.STATUS_READY:
					ts.setText("Ready");
					ts.setTextColor(Color.GREEN);
					
					break;
				case Constants.STATUS_BLOCKED:
					ts.setText("Blocked");
					ts.setTextColor(Color.RED);
					
					break;
				case Constants.STATUS_WAITGT:
					ts.setText("Waiting for GT");
					ts.setTextColor(Color.GREEN);
					
					break;
				case Constants.STATUS_SCAN:
					ts.setText("Scanning");
					ts.setTextColor(Color.RED);
					
					break;
				case Constants.STATUS_COM:
					ts.setText("Server/Peer Communication");
					ts.setTextColor(Color.RED);
					
					break;
				default:
					break;
			}
			
		}
	}
	
	public static void updateConnStatus(int connstatus){
		if(viewStatus  && tc != null){
			
			switch(connstatus){
				case Constants.STATUS_CONN_READY:
					tc.setText("Alive");
					tc.setTextColor(Color.GREEN);
					
					break;
				case Constants.STATUS_CONN_PEEROFF:
					tc.setText("PEER OFF");
					tc.setTextColor(Color.RED);
					
					break;
				case Constants.STATUS_CONN_SERVEROFF:
					tc.setText("SERVER OFF");
					tc.setTextColor(Color.RED);
					
					break;
				case Constants.STATUS_CONN_TIMEOUT:
					tc.setText("TIMEOUT");
					tc.setTextColor(Color.RED);
					
					break;
				case Constants.STATUS_CONN_NETWORKOFF:
					tc.setText("NETWORK OFF");
					tc.setTextColor(Color.RED);
					
					break;
				default:
					break;
			}
			
		}
	}
}
