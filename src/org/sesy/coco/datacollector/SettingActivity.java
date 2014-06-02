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

public class SettingActivity extends PreferenceActivity {
	
	
	private PrefManager pM;
	private StatusManager sM;
	//private CheckBoxPreference _cBoxPref1, _cBoxPref2;
	private ListPreference _listPref1,_listPref2,_listPref3,_listPref4,_listPref5;
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
        
        log = Logger.getLogger(SettingActivity.class);  
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
        addPreferencesFromResource(R.xml.preferences);
        
        _listPref1 = (ListPreference) findPreference(Constants.KEY_PREF_MOR);
        _listPref1.setSummary(_listPref1.getEntry());
        _listPref1.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        	
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set the value as the new value
            	_listPref1.setValue(newValue.toString());
                // Get the entry which corresponds to the current value and set as summary
                preference.setSummary(_listPref1.getEntry());
                return true;
            }
        });
        _listPref2 = (ListPreference) findPreference(Constants.KEY_PREF_AFT);
        _listPref2.setSummary(_listPref2.getEntry());
        _listPref2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set the value as the new value
            	_listPref2.setValue(newValue.toString());
                // Get the entry which corresponds to the current value and set as summary
                preference.setSummary(_listPref2.getEntry());
                return true;
            }
        });
        _listPref3 = (ListPreference) findPreference(Constants.KEY_PREF_EVE);
        _listPref3.setSummary(_listPref3.getEntry());
        _listPref3.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set the value as the new value
            	_listPref3.setValue(newValue.toString());
                // Get the entry which correspon ds to the current value and set as summary
                preference.setSummary(_listPref3.getEntry());
                return true;
            }
        });
        _listPref4 = (ListPreference) findPreference(Constants.KEY_PREF_NIG);
        _listPref4.setSummary(_listPref4.getEntry());
        _listPref4.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set the value as the new value
            	_listPref4.setValue(newValue.toString());
                // Get the entry which corresponds to the current value and set as summary
                preference.setSummary(_listPref4.getEntry());
                return true;
            }
        });
        
        _listPref5 = (ListPreference) findPreference(Constants.KEY_PREF_BHR);
        _listPref5.setValue("0");
        if(pM.getTimeBlockState()){
        	_listPref5.setSummary("Time Block terminates in "+ pM.getBTLeft() + ".");
        }else{
        	_listPref5.setSummary("Time Block not set.");
        }       
        _listPref5.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Set the value as the new value
            	_listPref5.setValue(newValue.toString());
            	tmpbhr = Integer.parseInt(newValue.toString());
                // Get the entry which corresponds to the current value and set as summary
            	if(tmpbhr == -1){
            		pM.updateTimeBlock(tmpbhr);
            		preference.setSummary("Time Block not set.");
            	}else if(tmpbhr > 0){
            		preference.setSummary("Time Block terminates in "+ String.valueOf(tmpbhr) + " hours.");
            	}            		
                return true;
            }
        });
        
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
		if(tmpbhr > 0){
			pM.updateTimeBlock(tmpbhr);
		}
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

	/*@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		if (key.equals(KEY_PREF_MOR) || key.equals(KEY_PREF_AFT) || key.equals(KEY_PREF_EVE) ||
				key.equals(KEY_PREF_NIG) || key.equals(KEY_PREF_BHR)) {
            ListPreference currentListPref = (ListPreference) findPreference(key);
            // Set summary to be the user-description for the selected value
            currentListPref.setSummary(sharedPreferences.getString(key, ""));
        }

	}*/
	
	
}
