package org.sesy.coco.datacollector;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.DaemonService.DaemonBinder;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.google.gson.Gson;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.util.Log;

public class TriggerService extends Service{

	private PrefManager pM;
	private StatusManager sM;
	private Gson gson = new Gson();
    private int gt, mt;
    public static boolean triggerStatus = false;
    private String uuid;
    Logger log;
    @Override
    
    public void onCreate(){
    	super.onCreate();
    	log = Logger.getLogger(TriggerService.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");
        
        
        pM = new PrefManager(getApplicationContext());
        sM = new StatusManager(getApplicationContext());
        uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean mIsBound = false;


    private DaemonService mBoundService;
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  Because we have bound to a explicit
            // service that we know is running in our own process, we can
            // cast its IBinder to a concrete class and directly access it.
        	DaemonBinder binder = (DaemonService.DaemonBinder) service;
            mBoundService = binder.getService();
            //mIsBound = true;
            // Tell the user about this for our demo.
            //Toast.makeText(this, R.string.local_service_connected,
                    //Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            // Because it is running in our same process, we should never
            // see this happen.
        	mBoundService = null;
            //Toast.makeText(this, R.string.local_service_disconnected,
                    //Toast.LENGTH_SHORT).show();
        }
    };
    
    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, DaemonService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        log.info("DaemonService bound");
    }
    
    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            log.info("DaemonService unbound");
        }
    }
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//pM = new PrefManager(getApplicationContext());
		//plM = new PluginManager(getApplicationContext());
		//sM = new StatusManager(getApplicationContext());
		triggerStatus = true;
		doBindService();
  	  	
  	  	
		//DataMonitor.On_Demand = false;
  	  	//triggerStatus = true;
  	  	
  	  	/*AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	}*/
		log.info("sM widget+task updated");
		
		//register ground truth
		gt = intent.getIntExtra("gt", 0);		
		pM.updateGT(gt);
		mt = sM.getSensorStatus();
		
		//String role = intent.getStringExtra("role");
		new Thread(new Runnable(){
        	@Override
        	public void run(){
        		
        		
        		/*if(mIsBound){
        			Log.i("trigger", "is bound");
        			socket = mBoundService.getSocket();
        			in = mBoundService.getIn();
        			out = mBoundService.getOut();
        		}*/
        		sM.getStatus();
        		sM.updateWidgetStatus();
        		
        		HashMap<String,String> msgObj = new HashMap<String,String>();
        		String msg = ""; 
        		msgObj.clear();                  	
        		
          		msgObj.put("id", Constants.REQ_TASK);
          		msgObj.put("uuid", uuid);
          		msgObj.put("gt", String.valueOf(gt));
          		msgObj.put("mt", String.valueOf(mt));
            	msg = gson.toJson(msgObj);
            	log.info("REQ_TASK msg built: "+msg);
        		if (DaemonService.socket.isConnected()) {  
                    if (!DaemonService.socket.isOutputShutdown()) {  
                    	DaemonService.out.println(msg);
                    	DaemonService.out.flush();
                    	DaemonService.timeoutSet = true;
                    	DaemonService.timeoutTimer = SystemClock.elapsedRealtime();
                    	DaemonService.timeoutCounter = 0;
                    	log.info("REQ_TASK msg sent");
                    }  
                }  
        		DaemonService.taskTimeout = SystemClock.elapsedRealtime();
        		//mBoundService.requestTask(gt);
        		TriggerService.this.stopSelf();
        	}
        }).start();
		
		
		Log.i("triggerservice", "trigger service started");
		
		return START_NOT_STICKY;		
	}
	
	@Override
	public void onDestroy() {
		doUnbindService();
        super.onDestroy();
        
    }
}
