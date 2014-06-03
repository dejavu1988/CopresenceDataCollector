package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

public class StatusManager {
	
	private Context _context;
	
	/* 
	 * _status:
	 *	1 - ready -> green
	 *	2 - blocked -> red
	 *	3 - wait for gt -> prompt
	 *	4 - scanning -> working
	 *	5 - communicating -> working
	 */
	public int _status;
	/*
	 * _connectionStatus: including bind on/off
	 * 	1 - ok (peer on)
	 * 	2 - peer off
	 * 	3 - server off
	 * 	4 - network off
	 */
	public int _connectionStatus;
	/*
	 * _taskStatus:
	 * 	1 - scanning
	 * 	2 - uploading
	 *  3 - binding, triggerservice, heartbeat, timeout resetting
	 * 	4 - idle
	 */
	public int _taskStatus;
	/*
	 * _userBlockStatus: bitwise
	 * 	01 - time block
	 * 	10 - prompt block
	 * 	00 - no block
	 */
	public int _userBlockStatus;
	/*
	 * _sensorStatus: bitwise
	 * 	000 - no sensor available
	 * 	001 - gps available
	 * 	010 - wifi available
	 * 	100 - bluetooth available
	 *  1000 - audio available
	 */
	public int _sensorStatus;
	Logger log;
	
	public StatusManager(Context context) {
		
		this._context = context;
		this._status = Constants.STATUS_READY;
		this._connectionStatus = Constants.STATUS_CONN_READY;
		this._taskStatus = Constants.STATUS_TASK_IDLE;
		this._userBlockStatus = Constants.STATUS_USER_NOBLOCK;
		this._sensorStatus = Constants.STATUS_SENSOR_NULL;
		
		log = Logger.getLogger(StatusManager.class);  
        ConfigureLog4J.configure(context);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
	}
	
	public int getConnStatus(){	//from preference
		
		PrefManager pM = new PrefManager(_context);
		int tmpStatus = pM.getConnStatus();
		if(!isNetworkOn()){
			_connectionStatus = Constants.STATUS_CONN_NETWORKOFF;
			
		}else if(tmpStatus == Constants.STATUS_CONN_SERVEROFF){
			_connectionStatus = Constants.STATUS_CONN_SERVEROFF;
		}else if(!pM.getBindPref() || (pM.getBindPref() && !pM.getPeerStatus())){
			_connectionStatus = Constants.STATUS_CONN_PEEROFF;
		}else if(tmpStatus == Constants.STATUS_CONN_TIMEOUT){
			_connectionStatus = Constants.STATUS_CONN_TIMEOUT;
		}else{
			_connectionStatus = Constants.STATUS_CONN_READY;
		}
		
		if(tmpStatus != _connectionStatus){
			pM.updateConnStatus(_connectionStatus);
		}
			
		return _connectionStatus;
	}
	
	public int getTaskStatus(){	//realtime
		
		PrefManager pM = new PrefManager(_context);
		if(DaemonService.taskStatus){			
			_taskStatus = Constants.STATUS_TASK_SCAN;
		}else if(WorkerService.uploadStatus || SettingActivity.settingStatus || HelpActivity.helpStatus ){	//uploading task: can access realtime status
			_taskStatus = Constants.STATUS_TASK_UPLOAD;
		}else if(BindActivity.bindingStatus || TriggerService.triggerStatus || DaemonService.aliveStatus){
			_taskStatus = Constants.STATUS_TASK_MSG;
		}else
			_taskStatus = Constants.STATUS_TASK_IDLE;
		
		if(_taskStatus != pM.getTaskStatus()){
			pM.updateTaskStatus(_taskStatus);
		}
		
		return _taskStatus;
	}
	
	public int getUserBlockStatus(){
		PrefManager pM = new PrefManager(_context);
		_userBlockStatus = pM.getUserBlockStatus();
		return _userBlockStatus;
	}
	
	public int getSensorStatus(){	//realtime
		PrefManager pM = new PrefManager(_context);
		PluginManager plM = new PluginManager(_context);
		int flag = Constants.STATUS_SENSOR_NULL;
		if(plM.isGPSOn())	flag |= Constants.STATUS_SENSOR_GPS;
		if(plM.isWifiOn())	flag |= Constants.STATUS_SENSOR_WIFI;
		if(plM.isBTOn())	flag |= Constants.STATUS_SENSOR_BT;
		if(plM.isCellAvailable())	flag |= Constants.STATUS_SENSOR_CELL;
		//if(pM.getAudioState()) 
		flag |= Constants.STATUS_SENSOR_AUDIO;
		_sensorStatus = flag;
		
		if(_sensorStatus != pM.getSensorStatus()){
			pM.updateSensorStatus(_sensorStatus);
		}
		
		return _sensorStatus;
		
	}
	// Check the realtime overall status w/o store into prefmanager
	public int getStatus(){
		
		PrefManager pM = new PrefManager(_context);
		
		getUserBlockStatus();
		getSensorStatus();
		getConnStatus();
		getTaskStatus();
		
		if(!(AlarmService.alarmStatus || DataMonitor.On_Demand)&&(_userBlockStatus != Constants.STATUS_USER_TIMEBLOCK)&&((_sensorStatus & Constants.STATUS_SENSOR_GWBAC) != 0)&&(_connectionStatus == Constants.STATUS_CONN_READY)&&(_taskStatus == Constants.STATUS_TASK_IDLE))
			// Ready: no user_time_block && all sensor enabled && connection ok && task idle
			_status = Constants.STATUS_READY;
		else if(((AlarmService.alarmStatus && _userBlockStatus == Constants.STATUS_USER_NOBLOCK )||( DataMonitor.On_Demand && _userBlockStatus != Constants.STATUS_USER_TIMEBLOCK ))&&((_sensorStatus & Constants.STATUS_SENSOR_GWBAC) != 0)&&(_connectionStatus == Constants.STATUS_CONN_READY)&&(_taskStatus == Constants.STATUS_TASK_IDLE))
			// Wait for prompt: alarm triggerd && no user_time/prompt_block && all sensor enabled && connection ok && task idle
			_status = Constants.STATUS_WAITGT;
		else if((_userBlockStatus == Constants.STATUS_USER_TIMEBLOCK)||((_sensorStatus & Constants.STATUS_SENSOR_GWBAC) == 0)||(_connectionStatus != Constants.STATUS_CONN_READY)){
			// Block: user_time_block on && not all sensor enabled && connection halt && task working
			_status = Constants.STATUS_BLOCKED;
		}else if(_taskStatus == Constants.STATUS_TASK_SCAN){
			// Task: scanning
			_status = Constants.STATUS_SCAN;
		}else if((_taskStatus == Constants.STATUS_TASK_UPLOAD)||(_taskStatus == Constants.STATUS_TASK_MSG)){
			// Task: communicating with server
			_status = Constants.STATUS_COM;
		}
		
		if(_status != pM.getStatus()){
			pM.updateStatus(_status);
		}
		/*log.info("getStatus:Status:" + String.valueOf(_status));
		log.info("getStatus:ConnStatus:" + String.valueOf(_connectionStatus));
		log.info("getStatus:SensorStatus:" + String.valueOf(_sensorStatus));
		log.info("getStatus:TaskStatus:" + String.valueOf(_taskStatus));
		log.info("getStatus:UserStatus:" + String.valueOf(_userBlockStatus));
		log.info("getStatus:On_Demand:" + String.valueOf(DataMonitor.On_Demand));*/
		log.info("Status: "+_status+", C-S-T-U-O: "+_connectionStatus+"-"+_sensorStatus+"-"+_taskStatus+"-"+_userBlockStatus+"-"+String.valueOf(DataMonitor.On_Demand));
		return _status;		
	}
	
	
	public boolean isNetworkOn(){
		ConnectivityManager connMgr = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}
	
	public void updateWidgetStatus(){
		
		if(DataMonitor.existsWindow){
			
			Bundle data = new Bundle();
			data.putInt("status", _status);
			DataMonitor.sendData(_context, DataMonitor.class, DataMonitor.DEFAULT_ID, DataMonitor.STATUS_UPDATE_CODE, data, null, DataMonitor.DISREGARD_ID);
		}
	}
	
	public void updateScreenWidgetStatus(){
		AppWidgetManager appWidgetManager;
		appWidgetManager = AppWidgetManager.getInstance(_context);
		RemoteViews remoteViews = new RemoteViews(_context.getPackageName(), R.layout.appwidget);
		PrefManager pM = new PrefManager(_context);
		
		switch(_status){
			case Constants.STATUS_READY:
				remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
				remoteViews.setImageViewResource(R.id.indicator, R.drawable.green);
				remoteViews.setViewVisibility(R.id.indicator, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.status, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button1, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
				UpdateWidgetService.widgetStatus = 1;
				break;
			case Constants.STATUS_BLOCKED:
				remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
				remoteViews.setImageViewResource(R.id.indicator, R.drawable.red);
				remoteViews.setViewVisibility(R.id.indicator, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.status, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button1, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
				UpdateWidgetService.widgetStatus = 1;
				if(!pM.getBindPref()){
					
					remoteViews.setTextViewText(R.id.status, "Bind");
					remoteViews.setTextColor(R.id.status, Color.RED);
					remoteViews.setViewVisibility(R.id.indicator, View.GONE);
					remoteViews.setViewVisibility(R.id.status, View.VISIBLE);
					remoteViews.setViewVisibility(R.id.ask_button, View.GONE);
					remoteViews.setViewVisibility(R.id.ask_button1, View.VISIBLE);
				}
				break;
			case Constants.STATUS_WAITGT:
				remoteViews.setViewVisibility(R.id.asklayout, View.VISIBLE);
			    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.initlayout, View.GONE);
			    remoteViews.setImageViewResource(R.id.indicator, R.drawable.green);
			    remoteViews.setTextViewText(R.id.bindname, pM.getBindName());
			    remoteViews.setViewVisibility(R.id.ask_button1, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
			    UpdateWidgetService.widgetStatus = 2;
				break;
			case Constants.STATUS_SCAN:
				remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
				remoteViews.setImageViewResource(R.id.indicator, R.drawable.hourglass);
				remoteViews.setViewVisibility(R.id.indicator, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.status, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button1, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
				UpdateWidgetService.widgetStatus = 1;
				break;
			case Constants.STATUS_COM:
				remoteViews.setViewVisibility(R.id.asklayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.remindlayout, View.GONE);
			    remoteViews.setViewVisibility(R.id.initlayout, View.VISIBLE);	
				remoteViews.setImageViewResource(R.id.indicator, R.drawable.hourglass);
				remoteViews.setViewVisibility(R.id.indicator, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.status, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button1, View.GONE);
				remoteViews.setViewVisibility(R.id.ask_button, View.VISIBLE);
				UpdateWidgetService.widgetStatus = 1;
				break;
			default:
				break;
		}
		
	    appWidgetManager.updateAppWidget(new ComponentName(_context.getPackageName(), MyWidgetProvider.class.getName()), remoteViews);
	    Log.d("status", "Widget Status View Updated.");
	}
	
}
