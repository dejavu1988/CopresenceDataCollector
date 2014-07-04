package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class MyWidgetProvider extends AppWidgetProvider {
	private int wrole;
	Logger log;
	public void onReceive(Context context, Intent intent){
		
		log = Logger.getLogger(MyWidgetProvider.class);  
        ConfigureLog4J.configure(context);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
		if(intent.getAction() == AppWidgetManager.ACTION_APPWIDGET_UPDATE)
		wrole = intent.getIntExtra("wrole", 0); //get widget role
		super.onReceive(context,intent);
		log.info("Update request received: wrole: "+ String.valueOf(wrole));
	}
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		    log.info("onUpdate method called");
		    // Get all ids
		    ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		    
		    //Log.i("wrole", String.valueOf(wrole));
		    // Build the intent to call the service
		    Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
		    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		    intent.putExtra("wrole", wrole);
		    
		    
		    // Update the widgets via the service
		    context.startService(intent);
	}

}
