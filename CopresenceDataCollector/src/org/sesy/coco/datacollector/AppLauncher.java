package org.sesy.coco.datacollector;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import wei.mark.standout.StandOutWindow;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.provider.Settings.Secure;

public class AppLauncher extends Activity {
	
	
	//public static String nowTime = "";
	private PrefManager pM;
	private Logger log;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/*Time now = new Time();
		now.setToNow();
		nowTime=now.format3339(false);*/
		
		log = Logger.getLogger(AppLauncher.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
		log.info("onCreate");	
		
		pM = new PrefManager(getApplicationContext());
		if(!pM.getIcon()){
			addShortcut();
			pM.updateIcon();
		}
		
		StandOutWindow.closeAll(this, DataMonitor.class);
		DataMonitor.showStatus(this);
		log.info("StandOutWindow showed");
		
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = pInfo.versionName;
		log.info("App Version: "+version);
		
		String uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		
		log.info("UUID initialed: "+uuid);
		pM.updateUUID(uuid);
		log.info("UUID updated in pM");
		

        Intent intent = new Intent(this, DaemonService.class);
        startService(intent);
        log.info("Intent to DaemonService started");
       
        
		finish();
	}
	
	private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        Intent shortcutIntent = new Intent(getApplicationContext(), AppLauncher.class);
         
        shortcutIntent.setAction(Intent.ACTION_MAIN);
 
        Intent addIntent = new Intent();
        addIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "DataCollector");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
            Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.drawable.ic_launcher));
 
        addIntent
                .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);
    }
}
