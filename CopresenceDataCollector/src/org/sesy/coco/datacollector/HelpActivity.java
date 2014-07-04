/**
 * 
 */
package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.provider.Settings.Secure;
import android.widget.Toast;

public class HelpActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	public static final String KEY_VER_PREFERENCE = "ver_preference";
	
	
	Logger log;
	private StatusManager sM;
	private PrefManager pM;
	private static int wid = 0;
	public static boolean helpStatus = false;
	private int mCounter, nCounter;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.help);
        
		log = Logger.getLogger(HelpActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
        Bundle extras = getIntent().getExtras();        
		if(extras != null){
        	wid = extras.getInt("wid");
        	log.info("Bundle got: "+wid);
        }
        
        sM = new StatusManager(getApplicationContext());
        pM = new PrefManager(getApplicationContext());
        mCounter = 0; nCounter = 0;
        
        PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = pInfo.versionName;
		String uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
       
		Preference ver_pref = (Preference) findPreference(Constants.KEY_PREF_VER);
		Preference id_pref = (Preference) findPreference(Constants.KEY_PREF_ID);
		ver_pref.setSummary(version);
		id_pref.setSummary(uuid);
		ver_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				if(mCounter < 9){
					mCounter++;
				}else{
					mCounter = 0;
					//if(!pM.getAutoScannerState()){
						new AlertDialog.Builder(HelpActivity.this).setTitle("Register Auto Scanner")
						.setMessage("Automatic Scanner checks every 4min for 3hr. Do not move device for this period. And please remember to unregister automatic scanner if needed.")
						.setCancelable(true)
						.setPositiveButton("Co-presence", new DialogInterface.OnClickListener()
						{	// When choosing to turn on GPS, go to setting page
							public void onClick(DialogInterface dialog, int which)
							{
								pM.registerAutoScanner(1);
								Toast.makeText(getBaseContext(), "Automatic Scanner has been set for co-presence!", Toast.LENGTH_LONG).show();
							}
						}).setNegativeButton("Non-copresence", new DialogInterface.OnClickListener()
						{	// When choose not to open GPS, give a notice of disability
							public void onClick(DialogInterface dialog, int which)
							{
								pM.registerAutoScanner(3);
								Toast.makeText(getBaseContext(), "Automatic Scanner has been set for non-copresence!", Toast.LENGTH_LONG).show();
							}
						}).show();
					//}
				}
				
				return true;
			}
			
		});
		
		id_pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				if(nCounter < 9){
					nCounter++;
				}else{
					nCounter = 0;
					if(pM.getAutoScannerState()){
						new AlertDialog.Builder(HelpActivity.this).setTitle("Unregister Auto Scanner")
						.setMessage("Are you sure to unregister automatic scanner? Your Auto Scanner terminates in "+pM.getAutoScannerLeft()+".")
						.setCancelable(false)
						.setPositiveButton("Yes", new DialogInterface.OnClickListener()
						{	// When choosing to turn on GPS, go to setting page
							public void onClick(DialogInterface dialog, int which)
							{
								pM.unregisterAutoScanner();
								Toast.makeText(getBaseContext(), "Automatic Scanner has been unregistered!", Toast.LENGTH_LONG).show();
							}
						}).setNegativeButton("No", new DialogInterface.OnClickListener()
						{	// When choose not to open GPS, give a notice of disability
							public void onClick(DialogInterface dialog, int which)
							{
								//Toast.makeText(getBaseContext(), "Automatic Scanner has been set for non-colocation!", Toast.LENGTH_LONG).show();
							}
						}).show();
					}else{
						new AlertDialog.Builder(HelpActivity.this).setTitle("Unregister Auto Scanner")
						.setMessage("Sorry, you have not set auto scanner.")
						.setCancelable(false)
						.setPositiveButton("Got it", new DialogInterface.OnClickListener()
						{	// When choosing to turn on GPS, go to setting page
							public void onClick(DialogInterface dialog, int which)
							{
								
							}
						}).show();
					}
				}
				
				return true;
			}
			
		});
		
        
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		
		helpStatus = true;
		AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} 
	  	new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		
	}
	
	@Override
	public void onPause() {
		
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Let's do something when my counter preference value changes
        if (key.equals(KEY_VER_PREFERENCE)) {
            Toast.makeText(this, "Thanks! You increased my count to "
                    + sharedPreferences.getInt(key, 0), Toast.LENGTH_SHORT).show();
        }
    }
	
	@Override
	public void onStop(){
		//connected = false;
		//bindingStatus = false;
		log.info("onStop");
		//Thread thr = new Thread(null, sTask3, "HelpUpdate");
        //thr.start();
		helpStatus = false;
		
		new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		
	    super.onStop();	    
	}
	
	@Override
	public void onDestroy(){
		Thread thr = new Thread(null, sTask3, "HelpUpdate");
        thr.start();
		super.onDestroy();
	}

	Runnable sTask3 = new Runnable() {
        public void run() {
        	log.info("Send bind finished to standoutwindow");
        	DataMonitor.sendData(HelpActivity.this, DataMonitor.class, wid, DataMonitor.APP_HELP_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		
        }
	};
	
	Runnable wTask = new Runnable() {
        public void run() {
        	sM.getStatus();    		
      	  	sM.updateWidgetStatus();
        }
	};
}
