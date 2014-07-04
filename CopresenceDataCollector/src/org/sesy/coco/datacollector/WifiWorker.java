package org.sesy.coco.datacollector;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.google.gson.Gson;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class WifiWorker extends Service {

	private WifiManager wifiManager;	
	private BroadcastReceiver wifiReceiver;
	private List<ScanResult> wifiList;
	//private HashMap<String,Integer> wifiAPs;
	private int scanCounter;
	
	//private CountDownTimer timer;
	
	//private int gt, ob;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(WifiWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		        
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WorkerService.wifiTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		
		 WorkerService.wifiList.clear();
		/*Entry ecobj = new Entry();
		ecobj.setTS(System.currentTimeMillis());
		ecobj.setGT(gt);
		ecobj.setOB(ob);
		ecobj.setMT(Constants.STATUS_SENSOR_WIFI);
		Gson gson = new Gson();
		String meta = gson.toJson(ecobj);
		WorkerService.wifiList.add(meta);*/
		
		scanCounter = 0;
		
		log.info("Subtask Wifi");
    	//wifiAPs = new HashMap<String,Integer>();
    	wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    	IntentFilter wifiFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		
    	wifiReceiver = new BroadcastReceiver(){
    		
			@Override
			public void onReceive(Context context, Intent intent) {
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
				log.info("Subtask wifi received");					
				
				
				wifiList = wifiManager.getScanResults();
				for (ScanResult s: wifiList) {
					//if(!wifiAPs.containsKey(s.BSSID)){
						Entry eobj = new Entry();
						//wifiAPs.put(s.BSSID, s.level);
						//Gson gson = new Gson();
						WifiAP w = new WifiAP(s.BSSID, s.SSID, s.capabilities, s.frequency, s.level);
						//String fp = gson.toJson(w);
						String fp = w.toString();
						Log.i("Wifi FP", "Subtask wifi_fp: " + fp);
						//eobj.setOB(ob);
						eobj.setTS(ts);
						//eobj.setGT(gt);
						eobj.setMT(Constants.STATUS_SENSOR_WIFI);
						//eobj.setACC(AccListener.acc);
						eobj.setFP(fp);
						//String wifi = gson.toJson(eobj);
				        WorkerService.wifiList.add(eobj);
					//}
				}
				
				if(scanCounter++ < 9){
					wifiManager.startScan();
					log.info("Subtask Wifi scan started "+scanCounter);
				}else{
					if(wifiReceiver != null)
						unregisterReceiver(wifiReceiver);
					log.info("Wifi scan finished");
					WorkerService.wifiTaskDone = true;
					stopSelf();
				}
			}
		};
		
		/*timer = new CountDownTimer(32*1000, 3000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if(wifiReceiver != null)
					unregisterReceiver(wifiReceiver);
				log.info("Wifi scan finished");
				WorkerService.wifiTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				scanCounter++;
				wifiManager.startScan();
				log.info("Subtask Wifi scan started "+scanCounter);
			}
			
		};*/
		
		registerReceiver(wifiReceiver, wifiFilter);
		wifiManager.startScan();
		//timer.start();
		log.info("Subtask Wifi scan started "+scanCounter);
		
		
		return START_NOT_STICKY;		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class WifiAP{
		private String BSSID;
		private String SSID;
		private String capabilities;
		private int frequency;
		private int level;
		//private long timestamp;
		
		public WifiAP(String bssid, String ssid, String cap, int freq, int level){
			this.BSSID = bssid;
			this.SSID = ssid;
			this.capabilities = cap;
			this.frequency = freq;
			this.level = level;
			//this.timestamp = ts;
		}
		
		public String toString(){
			return this.BSSID + "#" + this.SSID + "#" + this.capabilities + "#" + String.valueOf(this.frequency) +"#" + String.valueOf(this.level);
		
		}
	}

}
