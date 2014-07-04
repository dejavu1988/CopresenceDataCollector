package org.sesy.coco.datacollector;

import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.Vibrator;
import android.widget.RemoteViews;

public class AlarmService extends Service
{
	public static boolean alarmStatus = false;
	public static Ringtone r = null;
	public static Vibrator vib = null;

	private static int nCounter = 0;
	private static int mCounter = 0;
	private static int aCounter = 0;
	private static int eCounter = 0;
	private static boolean freqPass = true;
	
	NotificationManager mNM;
	AppWidgetManager appWidgetManager;
	RemoteViews remoteViews;
	PrefManager pM;
	PluginManager plM;
	StatusManager sM;
	WakeLock wakeLock;
	KeyguardLock keyguardLock;
	Thread thr, thrs;
	Logger log;
	    @Override
	    public void onCreate() {
	    	
	    	log = Logger.getLogger(AlarmService.class);  
	        ConfigureLog4J.configure(this);  
	        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
	        log.info("onCreate");
	        
	    	PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
            //wakeLock.acquire();
            KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE); 
            keyguardLock = keyguardManager.newKeyguardLock("TAG");
            //keyguardLock.disableKeyguard();
            
	        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	        pM = new PrefManager(getApplicationContext());
	        plM = new PluginManager(getApplicationContext());
	        sM = new StatusManager(getApplicationContext());
	        
	        // show the icon in the status bar
	        showNotification();
	        
	        remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.appwidget);
    		appWidgetManager = AppWidgetManager.getInstance(this);
    		
	        // Start up the thread running the service.  Note that we create a
	        // separate thread because the service normally runs in the process's
	        // main thread, which we don't want to block.
	            	
		      	
	        thr = new Thread(null, mTask, "AlarmService");
	        thr.start();
	        
	        	        
	    }

	    public synchronized void stopThread(){
	    	if(thr != null){
	    		Thread moribund = thr;
	    		thr = null;
	    		moribund.interrupt();
	    	}
	    }
	    @Override
	    public void onDestroy() {
	        // Cancel the notification -- we use the same ID that we had used to start it
	        mNM.cancel(R.string.alarm_service_started);
	        stopThread();
	        //wakeLock.release();
	        //keyguardLock.reenableKeyguard();
	        AlarmService.this.stopSelf();
	        
	        log.info("onDestroy");
	        
	        
	        // Tell the user we stopped.
	        //Toast.makeText(this, R.string.alarm_service_finished, Toast.LENGTH_SHORT).show();
	    }

	    /**
	     * The function that runs in our worker thread
	     */
	    Runnable mTask = new Runnable() {
	        public void run() {
	            // Normally we would do some work here...  for our sample, we will
	            // just sleep for 30 seconds.
	        	log.info("AlarmService task started");
	        	long endTime = System.currentTimeMillis() + 5*1000;
	            while (System.currentTimeMillis() < endTime) {
	                synchronized (mBinder) {
	                    try {
	                        mBinder.wait(endTime - System.currentTimeMillis());
	                    } catch (Exception e) {
	                    }
	                }
	            }
	            log.info("waited 5 sec");
	            sM.getStatus();
	            sM.updateWidgetStatus();
        		log.info("sM widget+task+sensor updated");
        		
        		int day = getDay();
        		int hour = getHour();
        		if(pM.getDay() != day){
        			pM.updateDay(day);
        			mCounter = 0;
        			nCounter = 0;
        			aCounter = 0;
        			eCounter = 0;
        		}
        		
        		if(hour>=0 && hour<6){
        			if(pM.getNightMod() == 0) freqPass =false;
        			else	freqPass = ((nCounter++) % pM.getNightMod() == 0);
        		}else if(hour>=6 && hour<12){
        			if(pM.getMorningMod() == 0) freqPass =false;
        			else	freqPass = ((mCounter++) % pM.getMorningMod() == 0);
        		}else if(hour>=12 && hour<18){
        			if(pM.getAfternoonMod() == 0) freqPass =false;
        			else	freqPass = ((aCounter++) % pM.getAfternoonMod() == 0);
        		}else if(hour>=18 && hour<=23){
        			if(pM.getEveningMod() == 0) freqPass =false;
        			else	freqPass = ((eCounter++) % pM.getEveningMod() == 0);
        		}
        		log.info("freqPass: "+freqPass);
        		
	        	if(sM.getStatus() == Constants.STATUS_READY && !pM.getPromptBlockState() && !pM.getAutoScannerState() && freqPass){ //if allowed status
	        		wakeLock.acquire();
	        		keyguardLock.disableKeyguard();
	        		alarmStatus = true;
	        		log.info("AlarmService alarmStatus turn true");
	        	
	        		sM.getStatus();
	        		sM.updateWidgetStatus();
	        		log.info("sM widget+task updated");
	        		if(pM.getAlternativeIndicationAllow()){
	        			DataMonitor.sendData(getApplicationContext(), DataMonitor.class, DataMonitor.DEFAULT_ID, DataMonitor.APP_MAIN_CODE, null, null, DataMonitor.DISREGARD_ID);
	        		}else{
	        			DataMonitor.sendData(getApplicationContext(), DataMonitor.class, DataMonitor.DEFAULT_ID, DataMonitor.APP_ALARM_CODE, null, null, DataMonitor.DISREGARD_ID);
	        		}
	        		
	        		if(pM.getRingtoneAllow()){
	        			try {
		        	        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		        	        r = RingtoneManager.getRingtone(getApplicationContext(), notification);
		        	        r.play();
		        	    } catch (Exception e) {}
	        		}
	        		vib = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	        	    vib.vibrate(1000);
	        	    
	        	    
	        	    long endTime2 = System.currentTimeMillis() + 15*1000;
		            while (System.currentTimeMillis() < endTime2) {
		                synchronized (mBinder) {
		                    try {
		                        mBinder.wait(endTime2 - System.currentTimeMillis());
		                    } catch (Exception e) {
		                    }
		                }
		            }
		            log.info("waited for 15 sec");
		            
		            alarmStatus = false;
		            if(vib != null){
		    	  		vib.cancel();
		    	  	}
		    	  	if(r != null){
		    	  		r.stop();
		    	  	} 
		            wakeLock.release();
			        keyguardLock.reenableKeyguard();
		            log.info("AlarmService alarmStatus turn false");
		            sM.getStatus();
	        		sM.updateWidgetStatus();
	        		log.info("sM widget+task updated");
		            /*UpdateWidgetService.widgetStatus = 1;
	        		remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
	        	    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
	        	    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);
	        	    
	        	    appWidgetManager.updateAppWidget(new ComponentName(getPackageName(), MyWidgetProvider.class.getName()), remoteViews);
	        	    */
	        		
				//}   
	        		
	        	}
		            
	        	
	            // Done with our work...  stop the service!
	        	
	        	
	        	//stopThread();
	            AlarmService.this.stopSelf();
	        }
	    };

	    
	    
	    @Override
	    public IBinder onBind(Intent intent) {
	        return mBinder;
	    }

	    /**
	     * Show a notification while this service is running.
	     */
	    @SuppressWarnings("deprecation")
		private void showNotification() {
	        // In this sample, we'll use the same text for the ticker and the expanded notification
	        CharSequence text = getText(R.string.alarm_service_started);

	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.stat_sample, text,
	                System.currentTimeMillis());

	        // The PendingIntent to launch our activity if the user selects this notification
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                new Intent(this, AlarmService.class), 0);

	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, getText(R.string.alarm_service_label),
	                       text, contentIntent);

	        // Send the notification.
	        // We use a layout id because it is a unique number.  We use it later to cancel.
	        mNM.notify(R.string.alarm_service_started, notification);
	    }

	    /**
	     * This is the object that receives interactions from clients.  See RemoteService
	     * for a more complete example.
	     */
	    private final IBinder mBinder = new Binder() {
	        @Override
			protected boolean onTransact(int code, Parcel data, Parcel reply,
			        int flags) throws RemoteException {
	            return super.onTransact(code, data, reply, flags);
	        }
	    };
	    

		public int getHour(){
			Calendar cal = Calendar.getInstance();
			TimeZone tz = cal.getTimeZone();
			cal.setTimeZone(tz);
			cal.setTimeInMillis(System.currentTimeMillis());
			return cal.get(Calendar.HOUR_OF_DAY);
		}
		
		public int getDay(){
			Calendar cal = Calendar.getInstance();
			TimeZone tz = cal.getTimeZone();
			cal.setTimeZone(tz);
			cal.setTimeInMillis(System.currentTimeMillis());
			return cal.get(Calendar.DAY_OF_WEEK);
		}
}
