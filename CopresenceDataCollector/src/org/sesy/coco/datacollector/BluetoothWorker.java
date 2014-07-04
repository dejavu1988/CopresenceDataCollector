package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class BluetoothWorker extends Service {

	//private HashMap<String,Integer> btDevices;
	private int scanCounter;
	
	private BluetoothAdapter btAdapter;
	private BroadcastReceiver btReceiver;
	//private CountDownTimer timer;
	
	//private int gt, ob;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(BluetoothWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		        
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WorkerService.btTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		
		 WorkerService.btList.clear();
		/*Entry ecobj = new Entry();
		ecobj.setTS(System.currentTimeMillis());
		ecobj.setGT(gt);
		ecobj.setOB(ob);
		ecobj.setMT(Constants.STATUS_SENSOR_BT);
		Gson gson = new Gson();
		String meta = gson.toJson(ecobj);
		WorkerService.btList.add(meta);*/
		
		scanCounter = 0;
		
		log.info("Subtask Bluetoth");
    	//btDevices = new HashMap<String,Integer>();
    	IntentFilter btFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    	btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
    	btFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		
    	btReceiver = new BroadcastReceiver(){
    		
    		@Override
    		public void onReceive(Context context, Intent intent) {
    			long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
    			log.info("Subtask Bluetooth received");
    			String action = intent.getAction();
    			
    			if(BluetoothDevice.ACTION_FOUND.equals(action)) {    				
					
					//Gson gson = new Gson();
    				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    				//if(!btDevices.containsKey(device.getAddress())){
    					int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);	//RSSI in dBm
	    				//btDevices.put(device.getAddress(), rssi);
    					BTDevice dv = new BTDevice(device.getName(), device.getAddress(), rssi);
	    				//String fp = gson.toJson(dv);
    					String fp = dv.toString();
	    				Log.i("BT FP", "Subtask Bluetooth bluetooth_fp: " + fp);
	    				Entry eobj = new Entry();
	    				//eobj.setOB(ob);
						eobj.setTS(ts);
						//eobj.setGT(gt);
						eobj.setMT(Constants.STATUS_SENSOR_BT);
						//eobj.setACC(AccListener.acc);
						eobj.setFP(fp);
						//String bt = gson.toJson(eobj);
				        WorkerService.btList.add(eobj);
    				//}

    			}else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					Log.i("subtask", "bluetooth discovery started.");
					BTDevice dv = new BTDevice(btAdapter.getName(), btAdapter.getAddress(), Constants.BT_LOCAL_RSSI);
					String fp = dv.toString();
					Entry eobj = new Entry();
    				//eobj.setOB(ob);
					eobj.setTS(ts);
					//eobj.setGT(gt);
					eobj.setMT(Constants.STATUS_SENSOR_BT);
					eobj.setFP(fp);
			        WorkerService.btList.add(eobj);
				}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					Log.i("subtask", "bluetooth discovery finished.");
					if(scanCounter++ < 9){
			    		
						btAdapter.startDiscovery();
				    	log.info("Subtask Bluetooth scan started "+scanCounter);
					}else{
						if (btAdapter != null) {
							btAdapter.cancelDiscovery();
						}
						if(btReceiver != null){
							unregisterReceiver(btReceiver);
						}
						log.info("BT scan finished");
						WorkerService.btTaskDone = true;
						stopSelf();
					}
				}
    			
    		}
    	};
		
		/*timer = new CountDownTimer(65*1000, 15000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				if (btAdapter != null) {
					btAdapter.cancelDiscovery();
				}
				if(btReceiver != null){
					unregisterReceiver(btReceiver);
				}
				log.info("BT scan finished");
				WorkerService.btTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				scanCounter++;
				btAdapter.startDiscovery();
		    	log.info("Subtask Bluetooth scan started "+scanCounter);
			}
			
		};*/
		
		registerReceiver(btReceiver, btFilter);			
		// Getting the Bluetooth adapter
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
    	btAdapter.startDiscovery();
    	//timer.start();
    	log.info("Subtask Bluetooth scan started "+scanCounter);
		
		
		return START_NOT_STICKY;		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class BTDevice{
		private String Name;
		private String Addr;
		private int RSSI;
		
		public BTDevice(String name, String addr, int rssi){
			this.Name = name;
			this.Addr = addr;
			this.RSSI = rssi;
		}
		
		public String toString(){
			return this.Addr + "#" + this.Name + "#" + String.valueOf(this.RSSI);
		}
	}

}
