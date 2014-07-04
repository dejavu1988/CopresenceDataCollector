package org.sesy.coco.datacollector;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.DaemonService.DaemonBinder;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.google.gson.Gson;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BindActivity extends Activity {
	
	private TextView tv_msg, tv_msg11, tv_msg12, tv_msg13, tv_msg21 ,tv_msg31= null;  
    private EditText ed_msg01, ed_msg21 = null;  
    private Button btn_send01, btn_send02, btn_send21, btn_send31 = null; 
    private LinearLayout lay0, lay1, lay2, lay3 = null;
    
    public static boolean bindingStatus = false; 
    private static int wid = 0;
    private static String uuid = "";
    private PrefManager pM;
    private Gson gson = new Gson();
    private StatusManager sM;
    Thread thrb1, thrb2, thrb;
    private A1Task mTask1;
    private A2Task mTask2;
    private A3Task mTask3;
    private A4Task mTask4;
    
    Logger log;
    private boolean isFirstDevice, isThisActive;
    ProgressDialog dialog1, dialog2, dialog3, dialog4 = null;
    private boolean flag1, flag2;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        isThisActive = false;
        flag1 = true;
        flag2 = true;
        
        log = Logger.getLogger(BindActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
        
        setContentView(R.layout.activity_binding);
        lay0 = (LinearLayout) findViewById(R.id.lay0);
        lay1 = (LinearLayout) findViewById(R.id.lay1);
        lay2 = (LinearLayout) findViewById(R.id.lay2);
        lay3 = (LinearLayout) findViewById(R.id.lay3);
        tv_msg = (TextView) findViewById(R.id.debug);
        ed_msg01 = (EditText) findViewById(R.id.EditText01);
        btn_send01 = (Button) findViewById(R.id.Button01);
        btn_send02 = (Button) findViewById(R.id.Button02);
        tv_msg11 = (TextView) findViewById(R.id.TextView13);
        //btn_send11 = (Button) findViewById(R.id.Button11);
        tv_msg12 = (TextView) findViewById(R.id.TextView14);
        tv_msg13 = (TextView) findViewById(R.id.TextView15);
        tv_msg21 = (TextView) findViewById(R.id.TextView23);
        ed_msg21 = (EditText) findViewById(R.id.EditText21);
        btn_send21 = (Button) findViewById(R.id.Button21);
        btn_send31 = (Button) findViewById(R.id.Button31);
        tv_msg31 = (TextView) findViewById(R.id.TextView31);
        
        log.info("Bind views");
        
        Bundle extras = getIntent().getExtras();        
		if(extras != null){
        	wid = extras.getInt("wid");
        	log.info("Bundle got: "+wid);
        }
		
		
		doBindService();
        
        uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
        log.info("UUID set: "+uuid);
        
        pM = new PrefManager(getApplicationContext());
        sM = new StatusManager(getApplicationContext());
        
        
        
        mTask1 = new A1Task();
        mTask2 = new A2Task();
        mTask3 = new A3Task();
        mTask4 = new A4Task();
        thrb1 = new Thread(null, b1Task, "GetAckValq");
        thrb2 = new Thread(null, b2Task, "GetAckUnbind");
        
        btn_send01.setOnClickListener(new Button.OnClickListener() {  
        	// Button to First Device
            @Override  
            public void onClick(View v) {  
                // TODO Auto-generated method stub  
            	log.info("First device button clicked");
            	String tmp = ed_msg01.getText().toString();
            	if(tmp == "") tmp = Build.MODEL;
            	pM.updateName(tmp);
            	tv_msg11.setText(tmp);
            	tv_msg11.setTextColor(Color.RED);
                lay0.setVisibility(View.GONE);
                lay1.setVisibility(View.VISIBLE);
                lay2.setVisibility(View.GONE);
                lay0.setVisibility(View.GONE);
                dialog2 = ProgressDialog.show(BindActivity.this, "", "Loading Queue Number...", true);
                new Thread(new Runnable(){
                	@Override
                	public void run(){
                		
                		HashMap<String,String> msgObj = new HashMap<String,String>();
                		String msg = ""; 
                		msgObj.clear();                  		
                  		msgObj.put("id", Constants.REQ_GETQ);
                  		msgObj.put("uuid", uuid);
                  		msgObj.put("name", pM.getName());
                    	msg = gson.toJson(msgObj);
                    	log.info("BIND MSG built: "+msg);
                		if (DaemonService.socket.isConnected()) {  
                            if (!DaemonService.socket.isOutputShutdown()) {  
                            	DaemonService.out.println(msg);  
                            	DaemonService.out.flush();
                                log.info("BIND MSG sent");
                            }  
                        } 
                		isFirstDevice = true;
                		mTask2.execute("");
                		thrb1.start();
                	}
                }).start();
                
            }  
        });  
        btn_send02.setOnClickListener(new Button.OnClickListener() {  
        	// Button to Second Device
            @Override  
            public void onClick(View v) {  
                // TODO Auto-generated method stub  
            	log.info("Second device button clicked");
            	String tmp = ed_msg01.getText().toString();
            	if(tmp == "") tmp = Build.MODEL;
            	pM.updateName(tmp);
            	tv_msg21.setText(tmp);
            	tv_msg21.setTextColor(Color.RED);
            	lay0.setVisibility(View.GONE);
                lay1.setVisibility(View.GONE);
                lay2.setVisibility(View.VISIBLE);
                lay0.setVisibility(View.GONE);
                isFirstDevice = false;
                log.info("Second device view updated");
            }  
        });  
        
        
        btn_send21.setOnClickListener(new Button.OnClickListener() {  
        	// Button to Submit Qnum
            @Override  
            public void onClick(View v) {  
                // TODO Auto-generated method stub  
            	log.info("Submit Qnum button clicked");
            	dialog3 = ProgressDialog.show(BindActivity.this, "", "Validating Queue Number...", true);
                new Thread(new Runnable(){
                	@Override
                	public void run(){
                		
                		HashMap<String,String> msgObj = new HashMap<String,String>();
                		String msg = ""; 
                		msgObj.clear();                  	
                		
                  		msgObj.put("id", Constants.REQ_VALQ);
                  		msgObj.put("uuid", uuid);
                  		msgObj.put("name", pM.getName());
                  		msgObj.put("qnum", ed_msg21.getText().toString());
                    	msg = gson.toJson(msgObj);
                    	log.info("BIND MSG built: "+msg);
                		if (DaemonService.socket.isConnected()) {  
                            if (!DaemonService.socket.isOutputShutdown()) {  
                            	DaemonService.out.println(msg);  
                            	DaemonService.out.flush();
                                log.info("BIND MSG sent");
                            }  
                        }  
                		if(!isFirstDevice){
                			mTask3.execute("");
                		}
                		
                	}
                }).start();
                
            }  
        });  
        btn_send31.setOnClickListener(new Button.OnClickListener() {  
        	// Button to Unbind
            @Override  
            public void onClick(View v) {  
                // TODO Auto-generated method stub  
            	log.info("unbind button clicked");
            	dialog4 = ProgressDialog.show(BindActivity.this, "", "Unbinding...", true);
                new Thread(new Runnable(){
                	@Override
                	public void run(){
                		HashMap<String,String> msgObj = new HashMap<String,String>();
                		String msg = ""; 
                		msgObj.clear();                  	
                		
                  		msgObj.put("id", Constants.REQ_UNBIND);
                  		msgObj.put("uuid", uuid);
                    	msg = gson.toJson(msgObj);
                    	log.info("BIND MSG built: "+msg);
                		if (DaemonService.socket.isConnected()) {  
                            if (!DaemonService.socket.isOutputShutdown()) {  
                            	DaemonService.out.println(msg); 
                            	DaemonService.out.flush();
                                log.info("BIND MSG sent");
                            }  
                        }  
                		isThisActive = true;
                		mTask4.execute("");
                	}
                }).start();
                
            }  
        });  
        
	}
	
	Runnable bTask = new Runnable() {
        public void run() {
        	log.info("Request for bind started");
        	
        	
			HashMap<String,String> msgObj = new HashMap<String,String>();
    		String msg = ""; 
    		msgObj.clear();                  		
      		msgObj.put("id", Constants.REQ_ALIVE);
      		msgObj.put("uuid", uuid);
        	msg = gson.toJson(msgObj);
        	log.info("BIND MSG built: "+msg);
        	
        	if (DaemonService.socket.isConnected()) {  
                if (!DaemonService.socket.isOutputShutdown()) {  
                	DaemonService.out.println(msg);  
                	DaemonService.out.flush();
                	DaemonService.timeoutSet = true;
                	DaemonService.timeoutTimer = SystemClock.elapsedRealtime();
                	DaemonService.timeoutCounter = 0;
                    log.info("BIND MSG sent");
                }  
            }
        	mTask1.execute("");
        }
    };
    
    Runnable b1Task = new Runnable() {
        public void run() {
        	
        	while(flag1){
        		if(isFirstDevice && mTask2.getStatus() == AsyncTask.Status.FINISHED){
        			mTask3.execute("");
        			flag1 = false;
        		}
        		try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}        	
        }
    };
    
    Runnable b2Task = new Runnable() {
        public void run() {
        	
        	while(flag2){
        		boolean test1 = ( mTask1.getStatus() == AsyncTask.Status.FINISHED && mTask2.getStatus() == AsyncTask.Status.PENDING && mTask3.getStatus() == AsyncTask.Status.PENDING );
        		boolean test2 = ( mTask1.getStatus() == AsyncTask.Status.FINISHED && mTask3.getStatus() == AsyncTask.Status.FINISHED );
        		if(!isThisActive && DaemonService.bindStatus && (test1 || test2 )){
        			mTask4.execute("");
        			flag2 = false;
        		}
        		try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
    };
	 /** 
     * AlertDialog popups upon exception of connection！ 
     */  
    /*public void ShowDialog(String msg) {  
        new AlertDialog.Builder(this).setTitle("Notification").setMessage(msg)  
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {  
  
                    @Override  
                    public void onClick(DialogInterface dialog, int which) {  
  
                    }  
                }).show();  
    } */ 
    
	
	@Override
	public void onResume(){
				
	    super.onResume();
	    bindingStatus = true;
	    AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} 
	  	new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		
	    dialog1 = ProgressDialog.show(BindActivity.this, "", "Loading Bind Status...", true);
	    thrb = new Thread(null, bTask, "GetBindStatus");
        thrb.start();  
        //thrb2.start();
        
	}
	
	
	public synchronized void stopThread(){
    	if(thrb != null){
    		Thread moribund = thrb;
    		thrb = null;
    		moribund.interrupt();
    	}
    }
	
	public synchronized void stop1Thread(){
    	if(thrb1 != null){
    		Thread moribund = thrb1;
    		thrb1 = null;
    		moribund.interrupt();
    	}
    }
	
	public synchronized void stop2Thread(){
    	if(thrb2 != null){
    		Thread moribund = thrb2;
    		thrb2 = null;
    		moribund.interrupt();
    	}
    }
	
	@Override
	public void onPause(){
		log.info("onPause");
		
		bindingStatus = false;
		DaemonService.bindToken = -1;
		DaemonService.qnum = "";
		mTask1.cancel(true);
		mTask2.cancel(true);
		mTask3.cancel(true);
		mTask4.cancel(true);
		flag1 = false;
		flag2 = false;
		stopThread();
		stop1Thread();
		//stop2Thread();
		
		new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		Thread thr = new Thread(null, sTask, "BindUpdate");
        thr.start();
        doUnbindService();
        
	    super.onPause();	    
	}
	
	

	Runnable sTask = new Runnable() {
        public void run() {
        	log.info("Send bind finished to standoutwindow");
        	DataMonitor.sendData(getApplicationContext(), DataMonitor.class, wid, DataMonitor.APP_BIND_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		
        }
	};
	
	private class A1Task extends AsyncTask<String, Void, Void> {
	    @Override
	    protected Void doInBackground(String... furi) {
	    	
	    	try {
	    		boolean flag = true;
	    		while(flag){
	    			if(DaemonService.bindToken == 1){
	    				flag = false;
	    			}
	    			Thread.sleep(500);
	    		}
	    		log.info("Bind Status fetched");
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	dialog1.dismiss(); 
	    	if(DaemonService.bindStatus){
	    		tv_msg31.setText("Your device is bound to " + DaemonService.bindName);
        		lay0.setVisibility(View.GONE);
        		lay1.setVisibility(View.GONE);
        		lay2.setVisibility(View.GONE);
        		lay3.setVisibility(View.VISIBLE);
        		//mTask4.execute("");
	    	}else{
	    		lay0.setVisibility(View.VISIBLE);
        		ed_msg01.setText(Build.MODEL);
        		lay1.setVisibility(View.GONE);
        		lay2.setVisibility(View.GONE);
        		lay3.setVisibility(View.GONE);
        		
        		
	    	}
	    }
	}
	
	private class A2Task extends AsyncTask<String, Void, Void> {
	    @Override
	    protected Void doInBackground(String... furi) {
	    	
	    	try {
	    		boolean flag = true;
	    		while(flag){
	    			if(DaemonService.bindToken == 2){
	    				flag = false;
	    			}
	    			Thread.sleep(500);
	    		}
	    		log.info("Qnum acquired");
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	dialog2.dismiss(); 
	    	//btn_send11.setVisibility(View.GONE);
        	tv_msg12.setText(DaemonService.qnum);
        	tv_msg12.setTextColor(Color.RED);
    		//tv_msg12.setVisibility(View.VISIBLE);
    		tv_msg13.setVisibility(View.VISIBLE);
    		
	    }
	}
	
	private class A3Task extends AsyncTask<String, Void, Void> {
	    @Override
	    protected Void doInBackground(String... furi) {
	    	
	    	try {
	    		boolean flag = true;
	    		while(flag){
	    			if(DaemonService.bindToken == 3){
	    				flag = false;
	    			}
	    			Thread.sleep(500);
	    		}
	    		log.info("ACK VALQ received");
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	if(!isFirstDevice){
	    		dialog3.dismiss(); 
	    	}
	    	tv_msg31.setText("Your device is successfully bound to " + DaemonService.bindName);
        	lay0.setVisibility(View.GONE);
        	lay1.setVisibility(View.GONE);
    		lay2.setVisibility(View.GONE);
    		lay3.setVisibility(View.VISIBLE);
	    	
	    }
	}
	
	private class A4Task extends AsyncTask<String, Void, Void> {
	    @Override
	    protected Void doInBackground(String... furi) {
	    	
	    	try {
	    		boolean flag = true;
	    		while(flag){
	    			if(DaemonService.bindToken == 4){
	    				flag = false;
	    			}
	    			Thread.sleep(500);
	    		}
	    		log.info("ACK Unbind received");
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	if(isThisActive){
	    		dialog4.dismiss(); 
	    		isThisActive = false;
	    	}
	    	lay0.setVisibility(View.VISIBLE);
        	ed_msg01.setText(Build.MODEL);
        	lay1.setVisibility(View.GONE);
    		lay2.setVisibility(View.GONE);
    		lay3.setVisibility(View.GONE);
	    	
	    }
	}
	
	Runnable wTask = new Runnable() {
        public void run() {
        	sM.getStatus();    		
      	  	sM.updateWidgetStatus(); 
        }
	};
	
	
}
