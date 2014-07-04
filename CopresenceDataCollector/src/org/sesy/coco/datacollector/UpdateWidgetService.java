package org.sesy.coco.datacollector;

import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.R;
import org.sesy.coco.datacollector.SettingActivity;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class UpdateWidgetService extends Service {
	private static final String LOG = "widget";
	//STATUS: status of recheability -> 1: server off, 2: bind off, 3: peer off, 4: peer on
	public static int STATUS = 4; 
	//private static int WIDGET_INT = 1;	//initial minimized view of widget
	//private static int WIDGET_ASK = 2;	//asking colocation choice view of widget
	//private static int WIDGET_REM = 3;	//reminder of blocking view of widget
	public static int widgetStatus = 1; // current status of widget
	public static int widgetRole = -1;
	public static boolean On_Demand = false;
	private PrefManager pM;
	//private PluginManager plM;
	private StatusManager sM;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		log = Logger.getLogger(UpdateWidgetService.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
	    
	    // Create some random data
	    pM = new PrefManager(getApplicationContext());
	    //plM = new PluginManager(getApplicationContext());
	    sM = new StatusManager(getApplicationContext());
	    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());

	    int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
	    ComponentName thisWidget = new ComponentName(getApplicationContext(), MyWidgetProvider.class);
	    int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(thisWidget);
	    Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
	    Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));
	    
	    int wrole = 0;
	    wrole = intent.getIntExtra("wrole", 0); //get widget role
	    Log.i("wrole", String.valueOf(wrole));

	    for (int widgetId : allWidgetIds) {
	      // Create some random data
	      int number = (new Random().nextInt(100));

	      RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.appwidget);
	      Log.w("WidgetExample", String.valueOf(number));
	      
	      if(wrole == 1 || wrole == 0 ){
	    	  //On_Demand = true;
	    	  sM.getStatus();
	    	  //sM.updateWidgetStatus();
	    	  log.info("sM task updated when wrole = 0 or 1");
	    	  
	      }
	      
	      // Set the text
	      //remoteViews.setTextViewText(R.id.update, "Random: " + String.valueOf(number));
	      if(wrole == 0 ) {
	    	  widgetStatus = 1;
	    	  remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
		      remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
		      remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
		      remoteViews.setImageViewResource(R.id.indicator, R.drawable.green);
		      remoteViews.setViewVisibility(R.id.indicator, View.VISIBLE);
		      remoteViews.setViewVisibility(R.id.status, View.GONE);
		      remoteViews.setViewVisibility(R.id.ask_button2, View.GONE);
		      remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
		      sM.getStatus();
		      log.info("sM task updated when wrole = 0");
	      } else if(( wrole == 1 || wrole == 0 ) && (sM.getStatus() == Constants.STATUS_READY || sM.getStatus() == Constants.STATUS_WAITGT)) {
	    	  widgetStatus = 2;
	    	  remoteViews.setViewVisibility(R.id.asklayout, View.VISIBLE);
		      remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
		      remoteViews.setViewVisibility(R.id.initlayout, View.GONE);
		      remoteViews.setTextViewText(R.id.bindname, pM.getBindName());
		      On_Demand = true;
		      sM.getStatus();
		      //sM.updateWidgetStatus();
		      log.info("sM task updated when wrole = 0 or 1");
	      }else if(wrole == 4) {
	    	  widgetStatus = 3;
	    	  remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
		      remoteViews.setViewVisibility(R.id.remindlayout, View.VISIBLE);
		      remoteViews.setViewVisibility(R.id.initlayout, View.GONE);
		      On_Demand = false;
		      AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
		      sM.getStatus();
		      log.info("sM task updated when wrole = 4");
	      }
	      
	      /*Intent mainIntent1 = new Intent(this, MainActivity.class);
    	  mainIntent1.putExtra("rangeId", 1);
    	  Intent mainIntent2 = new Intent(this, MainActivity.class);
    	  mainIntent2.putExtra("rangeId", 2);
    	  Intent mainIntent3 = new Intent(this, MainActivity.class);
    	  mainIntent3.putExtra("rangeId", 3);
    	  Intent mainIntent4 = new Intent(this, MainActivity.class);
    	  mainIntent4.putExtra("rangeId", 4);
    	  
	      PendingIntent pendingIntent1 = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent1, 0);
	      PendingIntent pendingIntent2 = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent2, 0);
	      PendingIntent pendingIntent3 = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent3, 0);
	      PendingIntent pendingIntent4 = PendingIntent.getActivity(getApplicationContext(), 0, mainIntent4, 0);
	      */
	      
	      // Register an onClickListener
	      Intent clickIntent = new Intent(this.getApplicationContext(), MyWidgetProvider.class);
	      clickIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
	      clickIntent.putExtra("wrole", 0);	//back to init view with nothing
	      
	      Intent clickIntentA = new Intent(this.getApplicationContext(), MyWidgetProvider.class);
	      clickIntentA.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntentA.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
	      clickIntentA.putExtra("wrole", 1); //to asking colocation view
	      
	      /*Intent clickIntentY = new Intent(this.getApplicationContext(), MyWidgetProvider.class);
	      clickIntentY.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntentY.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
	      clickIntentY.putExtra("wrole", 2); //to init view with colocation
	      
	      Intent clickIntentN = new Intent(this.getApplicationContext(), MyWidgetProvider.class);
	      clickIntentN.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntentN.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
	      clickIntentN.putExtra("wrole", 3); //to init view with non-colocation*/
	      
	      Intent clickIntentY = new Intent(this, TriggerService.class);
	      clickIntentY.putExtra("wrole", 2); 
	      clickIntentY.putExtra("gt", 1);//to init view with colocation
	      
	      Intent clickIntentN = new Intent(this, TriggerService.class);
	      clickIntentN.putExtra("wrole", 3);
	      clickIntentN.putExtra("gt", 3);//to init view with non-colocation
	      
	      Intent clickIntentO = new Intent(this, TriggerService.class);
	      clickIntentO.putExtra("wrole", 3);
	      clickIntentO.putExtra("gt", 2);//to init view with non-colocation
	      
	      Intent clickIntentC = new Intent(this.getApplicationContext(), MyWidgetProvider.class);
	      clickIntentC.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	      clickIntentC.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
	      clickIntentC.putExtra("wrole", 4); //to reminder view
	      
	      Intent clickIntentS = new Intent(this, SettingActivity.class);
	      //clickIntentS.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	  clickIntentS.putExtra("wrole", 5); //to setting activity
    	  
    	  Intent clickIntentB = new Intent(this, BindActivity.class);
	      //clickIntentS.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	  clickIntentS.putExtra("wrole", 5); //to binding activity
    	  
	      PendingIntent pendingIntentS = PendingIntent.getActivity(getApplicationContext(), 0, clickIntentS, 0);
	      PendingIntent pendingIntentB = PendingIntent.getActivity(getApplicationContext(), 7, clickIntentB, 0);
	      
	      PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
	      PendingIntent pendingIntentA = PendingIntent.getBroadcast(getApplicationContext(), 2, clickIntentA, PendingIntent.FLAG_UPDATE_CURRENT);
	      PendingIntent pendingIntentY = PendingIntent.getService(getApplicationContext(), 3, clickIntentY, 0);
	      PendingIntent pendingIntentN = PendingIntent.getService(getApplicationContext(), 4, clickIntentN, 0);
	      PendingIntent pendingIntentO = PendingIntent.getService(getApplicationContext(), 6, clickIntentO, 0);
	      PendingIntent pendingIntentC = PendingIntent.getBroadcast(getApplicationContext(), 5, clickIntentC, PendingIntent.FLAG_UPDATE_CURRENT);
	      
	      remoteViews.setOnClickPendingIntent(R.id.ask_button, pendingIntentA);
	      remoteViews.setOnClickPendingIntent(R.id.ask_button2, pendingIntentB);
	     
	      
	      remoteViews.setOnClickPendingIntent(R.id.yes_button, pendingIntentY);
	      remoteViews.setOnClickPendingIntent(R.id.no_button, pendingIntentN);
	      remoteViews.setOnClickPendingIntent(R.id.o_button, pendingIntentO);
	      remoteViews.setOnClickPendingIntent(R.id.cancel_button, pendingIntentC);
	      remoteViews.setOnClickPendingIntent(R.id.set_button, pendingIntentS);
	      /*remoteViews.setOnClickPendingIntent(R.id.range1_button, pendingIntent1);
	      remoteViews.setOnClickPendingIntent(R.id.range2_button, pendingIntent2);
	      remoteViews.setOnClickPendingIntent(R.id.range3_button, pendingIntent3);
	      remoteViews.setOnClickPendingIntent(R.id.range4_button, pendingIntent4);*/
	      appWidgetManager.updateAppWidget(widgetId, remoteViews);
	      
	    }
	    
	    stopSelf();

	    super.onStart(intent, startId);
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	    return null;
	  } 
}
