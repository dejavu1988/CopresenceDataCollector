package org.sesy.coco.datacollector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import wei.mark.standout.StandOutWindow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.musicg.wave.Wave;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

public class DaemonService extends Service{
	//static final String ACTION_FOREGROUND = "com.example.android.apis.FOREGROUND";
    //static final String ACTION_BACKGROUND = "com.example.android.apis.BACKGROUND";
    private PrefManager pM;
	private StatusManager sM;
	private AlarmManager am;
	private PendingIntent mAlarmSender;
	public static Socket socket = null;  
	public static BufferedReader in = null;  
	public static PrintWriter out = null; 
	public static String content = "";  
	private String ver;
	public static boolean bindStatus = false;
	public static boolean taskStatus = false;
	public static boolean aliveStatus = false;
	public static long avgRTT = 0;
	private static int countRTT = 0;
	private static long lastR = 0;
	public static int bindToken = -1;
	public static String qnum = "";
	public static String bindName = "";
	private Message message;
	private String curver;
	private Gson gson = new Gson();
    private Type mapType;
    private boolean connected, checked, runHeartbeat, runTimeoutCheck, autochecked;
    private static String uuid = "";
    public static long timeoutTimer = 0; 
    public static long timeoutCounter = 0; 
    public static boolean timeoutSet = false; 
    public static long taskTimeout = 0;
    //public static boolean isTrigger = false;
    //public static long triggerTS = 0L;
    private Thread thrm, thrs, thra, thrk, thrauto;
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new DaemonBinder();
    
    private static final Class[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private NotificationManager mNM;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    private Logger log;
    
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke startForeground", e);
            }
            return;
        }
        
        
    }
    
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("ApiDemos", "Unable to invoke stopForeground", e);
            }
            return;
        }
        
        
    }
    
    public class DaemonBinder extends Binder {
        DaemonService getService() {
            return DaemonService.this;
        }
    }
    
    @Override
    public void onCreate() {
    	super.onCreate();
    	// Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.
        /*mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, AlarmService.class), 0);
        // We want the alarm to go off 30 seconds from now.
        long firstTime = SystemClock.elapsedRealtime();

        // Schedule the alarm every 15min!
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        firstTime + 3000, 15*60*1000, mAlarmSender);*/
    	
        log = Logger.getLogger(DaemonService.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
        if(!DataMonitor.existsWindow){
        	
        	StandOutWindow.closeAll(this, DataMonitor.class);
    		DataMonitor.showStatus(this);
    		log.info("StandOutWindow showed");
        }
        
        PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ver = pInfo.versionName;
		log.info("App Version: "+ver);
		curver = ver;
        
        uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
        log.info("UUID set: "+uuid);
        
        // Create an IntentSender that will launch our service, to be scheduled
        // with the alarm manager.
        mAlarmSender = PendingIntent.getService(this, 0, new Intent(this, AlarmService.class), 0);
        // We want the alarm to go off 30 seconds from now.
        long firstTime = SystemClock.elapsedRealtime() + 15000;

        
        // Schedule the alarm every 15min!
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,firstTime, 15*60*1000, mAlarmSender);
        log.info("AlarmManager set");
        
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        
		pM = new PrefManager(getApplicationContext());
		sM = new StatusManager(getApplicationContext());
		
		sM.getStatus();
		sM.updateWidgetStatus();
        log.info("sM widget+task+sensors updated");
  		
        connected = true;
        checked = true;
        runHeartbeat = true;
        runTimeoutCheck = true;
        autochecked = true;
		mapType = new TypeToken<HashMap<String,String>>(){}.getType();
		
		thrm = new Thread(new Daemon(this));
		thrm.start();
    }

    public synchronized void stopThread(){
    	if(thrm != null){
    		Thread moribund = thrm;
    		thrm = null;
    		moribund.interrupt();
    	}
    }
    
    @Override
    public void onDestroy() {
        // Make sure our notification is gone.
        stopForegroundCompat(R.string.foreground_service_started);
        connected = false;
        checked = false;
        autochecked = false;
        runHeartbeat = false;
        runTimeoutCheck = false;
        stopsThread();
        stopsThread();
        stopkThread();
        stopautoThread();
        stopThread();
        if(am != null){
        	am.cancel(mAlarmSender);
        }else{
        	am = (AlarmManager)getSystemService(ALARM_SERVICE);
        	am.cancel(mAlarmSender);
        }
        
        
        log.info("onDestroy");
    }
    
    /*@Override
    public void onStart(Intent intent, int startId) {
        handleCommand(intent);
        new Thread(new Daemon(this)).start(); 
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	log.info("onStartCommand");
        handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //new Thread(new Daemon(this)).start(); 
        thrs = new Thread(null, sTask, "UpdateStatus");
        thra = new Thread(null, aTask, "HeartBeat");
        thrk = new Thread(null, kTask, "TimeoutCheck");
        thrauto = new Thread(null, autoTask, "AutoScannerTask");
        thrs.start();
        thra.start();
        thrk.start();
        thrauto.start();
        
        return START_NOT_STICKY;
    }

    public synchronized void stopaThread(){
    	if(thra != null){
    		Thread moribund = thra;
    		thra = null;
    		moribund.interrupt();
    	}
    }
    
    public synchronized void stopsThread(){
    	if(thrs != null){
    		Thread moribund = thrs;
    		thrs = null;
    		moribund.interrupt();
    	}
    }
    
    public synchronized void stopkThread(){
    	if(thrk != null){
    		Thread moribund = thrk;
    		thrk = null;
    		moribund.interrupt();
    	}
    }
    
    public synchronized void stopautoThread(){
    	if(thrauto != null){
    		Thread moribund = thrauto;
    		thrauto = null;
    		moribund.interrupt();
    	}
    }
    
    
    Runnable autoTask = new Runnable() {
        public void run() {
        	log.info("Auto Scanner Update started");
        	while(autochecked){
        		        		
                if(pM.getAutoScannerState() && (sM.getStatus() == Constants.STATUS_READY || sM.getStatus() == Constants.STATUS_WAITGT)){
                	DataMonitor.On_Demand = false;
    				AlarmService.alarmStatus = false;
    		  	  	if(AlarmService.vib != null){
    			  		AlarmService.vib.cancel();
    			  	}
    			  	if(AlarmService.r != null){
    			  		AlarmService.r.stop();
    			  	}  
    			  	Intent autoIntent = new Intent(DaemonService.this, TriggerService.class);
    			  	autoIntent.putExtra("gt", pM.getAutoScannerGT());
          	      	startService(autoIntent);
          	      	
          	      	sM.getStatus();
          	      	sM.updateWidgetStatus();
          	      	
                }
        	    long endTime1 = System.currentTimeMillis() + 4*60*1000;
	            while (System.currentTimeMillis() < endTime1) {
	                synchronized (mBinder) {
	                    try {
	                        mBinder.wait(endTime1 - System.currentTimeMillis());
	                    } catch (Exception e) {
	                    }
	                }
	            }
	            log.info("Thread auto waited for 3 min");
        	}
        }
    };
    
    Runnable sTask = new Runnable() {
        public void run() {
        	log.info("Status Frequent Update started");
        	while(checked){
        		        		
        		sM.getStatus();
        		sM.updateWidgetStatus();
                log.info("sTask: sM widget+task+sensors updated");
        	    			
                if(!DataMonitor.existsWindow){
                	StandOutWindow.closeAll(getApplicationContext(), DataMonitor.class);
            		DataMonitor.showStatus(getApplicationContext());
            		log.info("StandOutWindow reshowed");
                }
                
                if(TriggerService.triggerStatus && (SystemClock.elapsedRealtime() - taskTimeout > 6000)){
                	TriggerService.triggerStatus = false;
                	taskTimeout = 0;
                	
                }
                
                
        	    long endTime1 = System.currentTimeMillis() + 5*1000;
	            while (System.currentTimeMillis() < endTime1) {
	                synchronized (mBinder) {
	                    try {
	                        mBinder.wait(endTime1 - System.currentTimeMillis());
	                    } catch (Exception e) {
	                    }
	                }
	            }
	            log.info("Thread s waited for 5 sec");
        	}
        }
    };
    
   
    
    Runnable aTask = new Runnable() {
        public void run() {
        	log.info("Heartbeat Frequent Update started");
        	long endTime1 = System.currentTimeMillis() + 30*1000;
            while (System.currentTimeMillis() < endTime1) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime1 - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            log.info("Thread a waited for 60 sec");
        	while(runHeartbeat){
        	
        		if(sM.isNetworkOn() && socket != null && in != null && out != null && (sM.getStatus() == Constants.STATUS_READY || sM.getStatus() == Constants.STATUS_BLOCKED || sM.getStatus() == Constants.STATUS_SCAN)){
        			aliveStatus = true;
        			log.info("Heartbeat when not tasking");
        			
        			//int tmp_sta = sM.getStatus();
        			sM.getStatus();
            		sM.updateWidgetStatus();
            		log.info("sM widget+task updated");
        			
            		/*if((sM.getUserBlockStatus() != Constants.STATUS_USER_TIMEBLOCK) && ((sM.getSensorStatus() & Constants.STATUS_SENSOR_GWB) == Constants.STATUS_SENSOR_GWB) && (sM.getConnStatus() == Constants.STATUS_CONN_PEEROFF)){
            			tmp_sta = Constants.STATUS_READY;
            		}*/
            		
        			HashMap<String,String> msgObj = new HashMap<String,String>();
            		String msg = ""; 
            		msgObj.clear();                  		
              		msgObj.put("id", Constants.REQ_ALIVE);
              		msgObj.put("uuid", uuid);
              		//msgObj.put("sta", String.valueOf(tmp_sta));
                	msg = gson.toJson(msgObj);
                	log.info("HEARTBEAT MSG built: "+msg);
                	
                	
                	if (socket.isConnected()) {  
                        if (!socket.isOutputShutdown()) {  
                            out.println(msg);  
                            out.flush();
                            lastR = SystemClock.elapsedRealtime();
                            timeoutSet = true;
                            timeoutTimer = SystemClock.elapsedRealtime();
                            timeoutCounter = 0;
                            log.info("HEARTBEAT MSG sent");
                        }  
                    }
        		
        		}
        	    long endTime2 = System.currentTimeMillis() + 60*1000;
	            while (System.currentTimeMillis() < endTime2) {
	                synchronized (mBinder) {
	                    try {
	                        mBinder.wait(endTime2 - System.currentTimeMillis());
	                    } catch (Exception e) {
	                    }
	                }
	            }
	            log.info("Heartbeat Thread a waited for 180 sec");
        	}
        }
    };
    
    Runnable kTask = new Runnable() {
        public void run() {
        	log.info("TimeOut Check thread started");
        	long endTime1 = System.currentTimeMillis() + 60*1000;
            while (System.currentTimeMillis() < endTime1) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(endTime1 - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            //log.info("TimeOut Thread a waited for 15 sec");
        	while(runTimeoutCheck){
        		log.info("Timeout check: toSet: "+timeoutSet+", toCounter: "+timeoutCounter);
        		if(timeoutSet && sM.isNetworkOn() && ((SystemClock.elapsedRealtime()-timeoutTimer) > 60000)){
        			//aliveStatus = true;
        			//timeoutSet = false;
        			timeoutCounter++;
        			if(timeoutCounter >= 2){
        				pM.updateConnStatus(Constants.STATUS_CONN_SERVEROFF);
        				
        			}else{
        				pM.updateConnStatus(Constants.STATUS_CONN_TIMEOUT);
        			}
        			
        			sM.getStatus();
            		sM.updateWidgetStatus();
            		log.info("sM widget+task updated");
        			
        			log.info("Timeout!");
        			
        			try {  
            			if(!socket.isClosed()){
            				log.info("Old socket not closed.");
	            			  socket.close();
	            			  if(in != null) in.close();
	            			  if(out != null) out.close();
	            		  	}
        	           
        	        } catch (IOException ex) {  
        	            ex.printStackTrace();  
        	            log.info("Timeout socket io exception: "+ex.getMessage());  
        	        } 
        			connected = false;
        	        stopThread();
        	        timeoutTimer = SystemClock.elapsedRealtime();
        	        thrm = new Thread(new Daemon(DaemonService.this));
        			thrm.start();
        			
        		}
        		
        		long endTime2 = System.currentTimeMillis() + 15*1000;
                while (System.currentTimeMillis() < endTime2) {
                    synchronized (mBinder) {
                        try {
                            mBinder.wait(endTime2 - System.currentTimeMillis());
                        } catch (Exception e) {
                        }
                    }
                }
        	}
        }
    };
    
    
    void handleCommand(Intent intent) {
        //if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.foreground_service_started);

            // Set the icon, scrolling text and timestamp
            Notification notification = new Notification(R.drawable.stat_sample, text,
                    System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this notification
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, SettingActivity.class), 0);

            // Set the info for the views that show in the notification panel.
            notification.setLatestEventInfo(this, getText(R.string.foreground_service_label),
                           text, contentIntent);
            
            startForegroundCompat(R.string.foreground_service_started, notification);
            
        //} else if (ACTION_BACKGROUND.equals(intent.getAction())) {
        //    stopForegroundCompat(R.string.foreground_service_started);
        //}
    }
    
    
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {  
            super.handleMessage(msg); 
            switch(msg.what){
            case 0:
            	
                break;
            case 1:
            	Toast.makeText(getApplicationContext(), "Please update to the new stable version v"+curver, Toast.LENGTH_LONG).show();
      			
                break;
            case 2:
            	sM.getStatus();
        		sM.updateWidgetStatus();
        		log.info("sM widget+task updated");
            	break;
            default:
               	break;
                //btn_send11.setVisibility(View.GONE);
                //tv_msg12.setText((String)msg.obj);
            }
            
        }  
    };
	
	
	public class Daemon implements Runnable{
		//private boolean connected;
		private HashMap<String,String> msgObj;
		private String msgAck; 
	    
		public Daemon(Context context){
	    	
	    	connected = true;
	    	this.msgObj = new HashMap<String,String>();
	    	this.msgAck = "";
	    	
	    	log.info("Daemon thread initialed");
	    	
	    }

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			aliveStatus = true;
      	  	sM.getStatus();
    		sM.updateWidgetStatus();
	        try {  
	            //socket = new Socket(Constants.SERVER_INET, Constants.SERVER_PORT);
	        	socket = new Socket();
	        	socket.connect(new InetSocketAddress(Constants.SERVER_INET, Constants.SERVER_PORT), 0);
	        	log.info("New Socket conn: "+socket);
	            in = new BufferedReader(new InputStreamReader(socket  
	                    .getInputStream()));  
	            log.info("BufferReader set: "+in);
	            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(  
	                    socket.getOutputStream())), true); 
	            log.info("PrintWriter set: "+out);
	            
	        } catch (IOException ex) {  
	            ex.printStackTrace();  
	            log.info("socket io exception: "+ ex.getMessage());   
	        } 
	        
	        if(!timeoutSet){
	        	timeoutSet = true;
	            timeoutTimer = SystemClock.elapsedRealtime();
	            timeoutCounter = 0;
	        }
	            avgRTT = 0;
	            countRTT = 0;
	            lastR = 0;
	        
	        
			try {  
	            while(connected){
	            	//log.info("in daemon thread loop");	            	
	              if (!socket.isClosed()) {  
	                  if (socket.isConnected()) {  
	                      if (!socket.isInputShutdown()  && in != null) {  
	                          if ((content = in.readLine()) != null) {  
	                              content += "\n";	
	                              log.info("MSG received: "+content);
	                            
	                            long ts = SystemClock.elapsedRealtime();
	                          	msgObj = gson.fromJson(content, mapType);
	                          	String token = msgObj.get("id");
	                          	log.info("MSG token: "+token);
	                          	
	                          	if(token.contains(Constants.REQ_UUID)){
	                          		
	                          		log.info("MSG ack for REQ_UUID");
	                          		timeoutSet = false;
	                          		if(pM.getConnStatus() == Constants.STATUS_CONN_SERVEROFF){
	                          			pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
	                          		}
	                          		
	                          		curver = msgObj.get("ver");
	                          		if(!curver.contains(ver)){
	                          			message = mHandler.obtainMessage(1);
	                          		}else{
	                          			message = mHandler.obtainMessage(0);
	                          		}
	                          		
	                          		/*int tmp_sta = sM.getStatus();
	                          		if(tmp_sta == Constants.STATUS_BLOCKED && sM.getConnStatus() == Constants.STATUS_CONN_PEEROFF){
	                        			tmp_sta = Constants.STATUS_CONN_READY;
	                        		}*/
	                          		
	                          		msgObj.clear();
	                          		msgObj.put("id", Constants.ACK_UUID);
	                          		msgObj.put("uuid", uuid);
	                          		msgObj.put("ver", ver);
	                          		//msgObj.put("sta", String.valueOf(tmp_sta));
	                            	msgAck = gson.toJson(msgObj);
	                            	log.info("MSG ACK_UUID built: "+msgAck);
	                            	if (socket.isConnected()) {  
	                                    if (!socket.isOutputShutdown()) {  
	                                        out.println(msgAck); 
	                                        out.flush();
	                                        timeoutSet = true;
	                                        timeoutTimer = SystemClock.elapsedRealtime();
	                                        timeoutCounter = 0;
	                                        log.info("MSG ACK_UUID sent");
	                                    }  
	                                } 
	                            	
	                          	}else if(token.contains(Constants.UP_BIND)){
	                          		log.info("MSG ack for UP_BIND");
	                          		timeoutSet = false;
	                          		String aUuid = "";
	                          		String aName = "";
	                          		boolean peerOn = false;
	                          		
	                          		bindStatus = Boolean.parseBoolean(msgObj.get("bind"));
	                          		log.info("MSG bind: "+bindStatus);
	                          		                          		
	                          		if(bindStatus){
	                          			aUuid = msgObj.get("bindId");
	                            		aName = msgObj.get("bindName");
	                            		peerOn = Boolean.parseBoolean(msgObj.get("peerOn"));
	                            		pM.updateBind(aUuid, aName, peerOn);
	                            		if(peerOn){
	                            			pM.updateConnStatus(Constants.STATUS_CONN_READY);
	                            			log.info("MSG when bind true, update pM: Bind-"+aUuid+" and update conn status to 1");
		                            		
	                            		}else{
	                            			pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
	                            			log.info("MSG when bind true, update pM: Bind-"+aUuid+" and update conn status to 2");
	                            		}
	                          		}else{
	                          			pM.updateBind("", "", false);
		                          		pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
		                          		log.info("MSG when bind false, update pM: Bind-'' and update conn status to 2");
	                          		}
	                          		//HashMap<String, String> bindMap = new HashMap<String, String>();
	                          		//bindMap.put("BindUUID", aUuid);
	                          		//bindMap.put("BindName", aName);
	                          		aliveStatus = false;
	                          		if(TriggerService.triggerStatus){
	                          			Intent intent = new Intent(getApplicationContext(), TriggerService.class);
	                          			intent.putExtra("gt", pM.getGT());
	                          			startService(intent);
	                          		}
	                          		message = mHandler.obtainMessage(2);
	                          		/*sM.getStatus();
	                        		sM.updateWidgetStatus();
	                        		sM.updateConnStatus();
	                        		sM.updateTaskStatus();
	                        		log.info("sM widget+task updated");*/
	                          	}else if(token.contains(Constants.ACK_ALIVE)){
	                          		timeoutSet = false;
	                          		log.info("MSG ack for ACK_ALIVE");
	                          		String aUuid = "";
	                          		String aName = "";
	                          		boolean peerOn = false;
	                          		
	                          		bindStatus = Boolean.parseBoolean(msgObj.get("bind"));
	                          		log.info("MSG bind: "+bindStatus);
	                          		                          		
	                          		if(bindStatus){
	                          			aUuid = msgObj.get("bindId");
	                            		aName = msgObj.get("bindName");
	                            		int RTTFlag = Integer.parseInt(msgObj.get("flag"));
	                            		if(RTTFlag == 1){
	                            			long curRTT = ts - lastR;		                            		
		                            		avgRTT = (avgRTT * countRTT + curRTT) / (countRTT + 1);
		                            		countRTT++;
		                            		Log.i("RTT","CurRTT=" + String.valueOf(curRTT) + " AvgRTT=" + String.valueOf(avgRTT));
		                            		log.info("CurRTT=" + String.valueOf(curRTT) + " AvgRTT=" + String.valueOf(avgRTT));
	                            		}
	                            		
	                            		peerOn = Boolean.parseBoolean(msgObj.get("peerOn"));
	                            		pM.updateBind(aUuid, aName, peerOn);
	                            		if(peerOn){
	                            			pM.updateConnStatus(Constants.STATUS_CONN_READY);
	                            			log.info("MSG when bind true and peerOn "+peerOn+", update pM: Bind-"+aUuid+" and update conn status to 1");
		                            		
	                            		}else{
	                            			pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
	                            			log.info("MSG when bind true and peerOn "+peerOn+", update pM: Bind-"+aUuid+" and update conn status to 2");
	                            		}
	                          		}else{
	                          			pM.updateBind("", "", false);
		                          		pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
		                          		log.info("MSG when bind false, update pM: Bind-'' and update conn status to 2");
	                          		}
	                          		//HashMap<String, String> bindMap = new HashMap<String, String>();
	                          		//bindMap.put("BindUUID", aUuid);
	                          		//bindMap.put("BindName", aName);
	                          		bindName = aName;
	                          		if(BindActivity.bindingStatus){
	                          			bindToken = 1;
	                          		}
	                          		aliveStatus = false;
	                          		message = mHandler.obtainMessage(2);
	                          		/*sM.getStatus();
	                        		sM.updateWidgetStatus();
	                        		sM.updateConnStatus();
	                        		sM.updateTaskStatus();
	                        		log.info("sM widget+task updated");*/
	                        		
	                        		
	                          	}else if(token.contains(Constants.ACK_GETQ)){
	                          		log.info("BIND MSG ack for ACK_GETQ");
	                          		
	                          		qnum = msgObj.get("qnum");
	                          		log.info("BIND MSG got qnum: "+qnum);
	                          		
	                          		bindToken = 2;
	                          		message = mHandler.obtainMessage(0);
	                          	}else if(token.contains(Constants.ACK_VALQ)){
	                          		log.info("BIND MSG ack for ACK_VALQ");
	                          		
	                          		
	                          		String aUuid = msgObj.get("uuid");
	                          		String aName = msgObj.get("name");
	                          		//log.info("BIND MSG got bind info: Bind-"+aUuid);
	                          		
	                          		//bindStatus = true;
	                          		//HashMap<String, String> bindMap = new HashMap<String, String>();
	                          		//bindMap.put("BindUUID", aUuid);
	                          		//bindMap.put("BindName", aName);
	                          		pM.updateBind(aUuid, aName, true);
	                          		pM.updateConnStatus(Constants.STATUS_CONN_READY);
	                          		log.info("BIND MSG ack for ACK_VALQ, update pM: Bind-"+aUuid+" and update conn status to 1");
	                          		bindName = aName;
	                          		bindToken = 3;
	                          		message = mHandler.obtainMessage(0);
	                          	}else if(token.contains(Constants.ACK_UNBIND)){
	                          		log.info("BIND MSG ack for ACK_UNBIND");
	                          		
	                          		bindStatus = false;
	                          		//HashMap<String, String> bindMap = new HashMap<String, String>();
	                          		//bindMap.put("BindUUID", "");
	                          		//bindMap.put("BindName", "");
	                          		pM.updateBind("","", false);
	                          		pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
	                          		log.info("BIND MSG ack for ACK_UNBIND, update pM: Bind-'' and update conn status to 2");
	                          		bindToken = 4;
	                          		message = mHandler.obtainMessage(0);
	                          	}else if(token.contains(Constants.REQ_AGREE)){
	                          		log.info("MSG ack for REQ_AGREE");
	                          		timeoutSet = false;
	                          		TriggerService.triggerStatus = true;
	                          		String gt = msgObj.get("gt");
	                          		
	                          		log.info("triggerStatus ended, taskStatus started");
	                          		pM.updateConnStatus(Constants.STATUS_CONN_READY);
	                          		int mt = sM.getSensorStatus();
	                          		mt = mt & pM.getSensorPrefState();
	                          	  	mt = mt & Integer.parseInt(msgObj.get("mt"));	//intersection
	                        		
	                          	  	msgObj.clear();
	                          		msgObj.put("id", Constants.ACK_AGREE);
	                          		msgObj.put("uuid", uuid);
	                          		msgObj.put("gt", gt);
	                          		msgObj.put("mt", String.valueOf(mt));
	                            	msgAck = gson.toJson(msgObj);
	                            	log.info("MSG ACK_AGREE built: "+msgAck);
	                            	if (socket.isConnected()) {   
	                                    if (!socket.isOutputShutdown()) {  
	                                        out.println(msgAck); 
	                                        out.flush();
	                                        timeoutSet = true;
	                                        timeoutTimer = SystemClock.elapsedRealtime();
	                                        timeoutCounter = 0;
	                                        log.info("MSG ACK_AGREE sent");
	                                    }  
	                                } 
	                            	TriggerService.triggerStatus = false;
	                          		message = mHandler.obtainMessage(2);
	                          	}else if(token.contains(Constants.ACK_TASK)){
	                          		log.info("MSG ack for ACK_TASK");
	                          		timeoutSet = false;
	                          		TriggerService.triggerStatus = false;
	                          		taskTimeout = 0;
	                          		int gt = Integer.parseInt(msgObj.get("gt"));
	                          		if(gt == 0){
	                          			log.info("ACK task failed");
	                          			pM.updateConnStatus(Constants.STATUS_CONN_PEEROFF);
	                          			/*sM.getStatus();
		                        		sM.updateWidgetStatus();
		                        		sM.updateConnStatus();
		                        		sM.updateTaskStatus();
		                        		log.info("sM widget+task updated");*/
	                          		}else{
	                          			taskStatus = true;
		                          		log.info("triggerStatus ended, taskStatus started");
		                          		pM.updateConnStatus(Constants.STATUS_CONN_READY);
		                          	  	/*sM.getStatus();
		                        		sM.updateWidgetStatus();
		                        		sM.updateConnStatus();
		                        		sM.updateTaskStatus();
		                        		log.info("sM widget+task updated");*/
		                        		
		                          		
		                          		log.info("MSG task gt: "+gt);
		                          		int ob = Integer.parseInt(msgObj.get("ob"));
		                          		log.info("MSG task observation no.: "+ob);
		                          		int mt = Integer.parseInt(msgObj.get("mt"));
		                          		boolean ar = Boolean.parseBoolean(msgObj.get("ar"));
		                          		Intent taskIntent = new Intent(getApplicationContext(), WorkerService.class);
			                      	    taskIntent.putExtra("gt", gt);
			                      	    taskIntent.putExtra("ob", ob);
			                      	    taskIntent.putExtra("mt", mt);
			                      	    taskIntent.putExtra("ar", ar);
		                          		startService(taskIntent);  
		                          		log.info("MSG ack: intent to WorkerService");
		                          		//connected = false;
	                          		}
	                          		message = mHandler.obtainMessage(2);
	                          	}else if(token.contains(Constants.SEND)){
	                          		log.info("MSG ack for SEND");
	                          		timeoutSet = false;
	                          		taskTimeout = 0;
	                          		//String waveHeaderJson = msgObj.get("header");
	                          		//String waveData = msgObj.get("data");
	                          		String waveFP = msgObj.get("wavefp");
	                          		
	                          		Intent taskIntent = new Intent(getApplicationContext(), AudioProc.class);
		                      	    taskIntent.putExtra("wavefp", waveFP);
	                          		startService(taskIntent);  
	                          		log.info("MSG ack: intent to AudioProc");
	                          		
	                          		message = mHandler.obtainMessage(0);
	                          	}
	                             
	                          	mHandler.sendMessage(message); 

	                          }  
	                      }  
	                  }  
	              }
	           }
			} catch (Exception e) {  
	          e.printStackTrace();  
			}  
		}
	}

}
