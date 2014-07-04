package org.sesy.coco.datacollector;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.RemoteViews;

public class SensorActivity extends PreferenceActivity {
	
	
	private PrefManager pM;
	private StatusManager sM;
	//private CheckBoxPreference _cBoxPref1, _cBoxPref2;
	private static int wid = 0;
	private int tmpbhr;
	private Context context;
	AppWidgetManager appWidgetManager;
	RemoteViews remoteViews;
	Logger log;
	public static boolean settingStatus = false;
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        log = Logger.getLogger(SensorActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
        
        Bundle extras = getIntent().getExtras();        
		if(extras != null){
        	wid = extras.getInt("wid");
        }
		
		pM = new PrefManager(getApplicationContext());
		sM = new StatusManager(getApplicationContext());
		//sM.getStatus();
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.sensor_preferences);        
        
        context = getApplicationContext();
        remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
		appWidgetManager = AppWidgetManager.getInstance(this);
		
        //Thread thr = new Thread(null, sTask1, "Setting");
        //thr.start();
    }
	
	Runnable wTask = new Runnable() {
        public void run() {
        	sM.getStatus();    		
      	  	sM.updateWidgetStatus();  
        }
	};
	
	Runnable sTask2 = new Runnable() {
        public void run() {
        	DataMonitor.sendData(context, DataMonitor.class, wid, DataMonitor.APP_SELECTOR_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		
        }
	};
	
	@Override
	public void onStart(){
		super.onStart();
		//_prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
	}
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    settingStatus = true;
		AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} 
	  	new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
	    /*RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.appwidget);
	    UpdateWidgetService.widgetStatus = 1;
  	  	remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
	    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
	    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
	    appWidgetManager.updateAppWidget(new ComponentName(this.getPackageName(), MyWidgetProvider.class.getName()), remoteViews);
	 */
	    //getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		log.info("onPause");
		settingStatus = false;
		new Thread(null, wTask, "wUpdate").start();
		
		log.info("sM widget+task updated");
		
	    super.onPause();
	    //getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	protected void onStop() {
		
		//DataMonitor.sendData(this, DataMonitor.class, wid, DataMonitor.APP_SELECTOR_FINISHED_CODE, null, null, wid);
		Thread thr = new Thread(null, sTask2, "SetUpdate");
        thr.start();
		//finish();
	    super.onStop();
	    	    
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();		
	}
	
	
}
