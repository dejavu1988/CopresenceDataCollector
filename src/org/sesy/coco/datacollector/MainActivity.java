package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import wei.mark.standout.StandOutWindow;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LinearLayout li, la, lr, rl;
	private Button ba, ba1, ba2, by, bo, bn, bc, bs;
	private ImageView im;
	private TextView tb, t1, t2, t3, tr, t0;
	Logger log;
	private PrefManager pM;
	private StatusManager sM;
	private Message message;
	private static int blink = 0;
	private static int wid = 0;
	public static boolean mainStatus = false;
	Thread thr;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Intent intent = getIntent();

		log = Logger.getLogger(MainActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("Entered main activity.");
        rl = (LinearLayout) findViewById(R.id.feedback);
		li = (LinearLayout) findViewById(R.id.initlayout);
		la = (LinearLayout) findViewById(R.id.asklayout);
		lr = (LinearLayout) findViewById(R.id.remindlayout);
		ba = (Button) findViewById(R.id.ask_button);
		ba1 = (Button) findViewById(R.id.ask_button1);
		ba2 = (Button) findViewById(R.id.ask_button2);
		by = (Button) findViewById(R.id.yes_button);
		bo = (Button) findViewById(R.id.o_button);
		bn = (Button) findViewById(R.id.no_button);
		bc = (Button) findViewById(R.id.cancel_button);
		bs = (Button) findViewById(R.id.set_button);
		im = (ImageView) findViewById(R.id.indicator);
		tb = (TextView) findViewById(R.id.status);
		t0 = (TextView) findViewById(R.id.statinfo);
		tr = (TextView) findViewById(R.id.remindinfo);
		t1 = (TextView) findViewById(R.id.widgetinfo1);
		t2 = (TextView) findViewById(R.id.bindname);
		t3 = (TextView) findViewById(R.id.widgetinfo2);
		
		pM = new PrefManager(getApplicationContext());
		sM = new StatusManager(getApplicationContext());
		

        Bundle extras = getIntent().getExtras();        
		if(extras != null){
			if(extras.containsKey("wid")){
				wid = extras.getInt("wid");
				log.info("Bundle got: "+wid);
			}
        	if(extras.containsKey("blink")){
        		blink = extras.getInt("blink");
        	}
        }
		
		ba.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask button clicked");
				li.setVisibility(View.GONE);
				lr.setVisibility(View.GONE);
				la.setVisibility(View.VISIBLE);
				la.setVisibility(View.VISIBLE);
				int c = pM.getColocationCounter();
				int n = pM.getNoncolocationCounter();
				int sum = c + n;
				float cp = (sum == 0)? 0.0f:(float) ( c * 1000 / sum ) / 10;
				float np = (sum == 0)? 0.0f:(float) ( n * 1000 / sum ) / 10;
				t0.setText("This device has recorded "+c+"("+cp+"%) Colocation, "+n+"("+np+"%) Non-colocation.");
				t2.setText(pM.getBindName());
				DataMonitor.On_Demand = true;
				
				
			}
			
		});
		
		ba1.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask1 button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent intentBind = new Intent(MainActivity.this, BindActivity.class);
                		startActivity(intentBind);
                	}
                }).start();
				
			}
			
		});
		
		ba2.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Ask2 button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent intentStatus = new Intent(MainActivity.this, StatusActivity.class);
                		startActivity(intentStatus);
                	}
                }).start();
				
			}
			
		});
		
		by.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Yes button clicked");
				DataMonitor.On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentY = new Intent(MainActivity.this, TriggerService.class);
              	      	clickIntentY.putExtra("gt", 1);
              	      	startService(clickIntentY);
              	      	message = mHandler.obtainMessage(1);
              	      	mHandler.sendMessage(message);
                	}
                }).start();
				
			}
			
		});
		
		/*bo.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("O button clicked");
				DataMonitor.On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentO = new Intent(MainActivity.this, TriggerService.class);
              	      	clickIntentO.putExtra("gt", 2);
              	      	startService(clickIntentO);
                	}
                }).start();
				
			}
			
		});*/
		
		bn.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("No button clicked");
				DataMonitor.On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent clickIntentN = new Intent(MainActivity.this, TriggerService.class);
              	      	clickIntentN.putExtra("gt", 3);
              	      	startService(clickIntentN);
              	      	message = mHandler.obtainMessage(1);
              	      	mHandler.sendMessage(message);
                	}
                }).start();
				
			}
			
		});
		
		bc.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Cancel button clicked");
				li.setVisibility(View.GONE);
				lr.setVisibility(View.VISIBLE);
				la.setVisibility(View.GONE);
				DataMonitor.On_Demand = false;
				AlarmService.alarmStatus = false;
		  	  	if(AlarmService.vib != null){
			  		AlarmService.vib.cancel();
			  	}
			  	if(AlarmService.r != null){
			  		AlarmService.r.stop();
			  	}  
			  	
			}
			
		});
		
		bs.setOnClickListener(new Button.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				log.info("Setting button clicked");
				
				new Thread(new Runnable(){
                	@Override
                	public void run(){
                		Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
                		startActivity(intentSetting);
                	}
                }).start();
				
			}
			
		});
		
        
        
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.status:
    		Intent intentStatus = new Intent(MainActivity.this, StatusActivity.class);
    		startActivity(intentStatus);
    		break;
    	case R.id.setting:
    		Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
    		startActivity(intentSetting);
    		break;
    	case R.id.binding:
    		Intent intentBind = new Intent(MainActivity.this, BindActivity.class);
    		startActivity(intentBind);
    		break;
    	case R.id.error:
    		Intent intentError = new Intent(MainActivity.this, ReportErrActivity.class);
    		startActivity(intentError);
    		break;
    	case R.id.help:
    		Intent intentHelp = new Intent(MainActivity.this, HelpActivity.class);
    		startActivity(intentHelp);
    		break;
    	case R.id.quit:
    		new AlertDialog.Builder(this).setTitle("Exit").setMessage("Do you really want to quit Data Collector?")
    		.setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
    		{	// When choosing to turn on GPS, go to setting page
    			public void onClick(DialogInterface dialog, int which)
    			{
    				stopService(new Intent(getApplicationContext(), DaemonService.class));
					DataMonitor.checkWindow = false;
					StandOutWindow.closeAll(MainActivity.this, DataMonitor.class);
					mainStatus = false;
					AlarmService.alarmStatus = false;
			  	  	if(AlarmService.vib != null){
				  		AlarmService.vib.cancel();
				  	}
				  	if(AlarmService.r != null){
				  		AlarmService.r.stop();
				  	} 
    				finish();
    			}
    		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
    		{	// When choose not to open GPS, give a notice of disability
    			public void onClick(DialogInterface dialog, int which)
    			{
    				dialog.cancel();
    			}
    		}).show(); 
    		break;
    	default:
    		break;
    	}
		return true;
    	
    }
    
    Handler blinkHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	if(AlarmService.alarmStatus){
                switch (msg.what) {
                	case 0:
                		rl.setBackgroundColor(0x80FFFFFF);
                        break;
                	case 1:
                		rl.setBackgroundResource(R.drawable.bg_whiteborder);
                		break;

                	}
                super.handleMessage(msg);
        	}else{
        		rl.setBackgroundResource(R.drawable.bg_whiteborder);
        	}
        }
    };
    
	@Override
	public void onResume(){
		super.onResume();
		
		mainStatus = true;
		/*AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} */
		
	  	if(blink == 1){
	  		for (int i=0; i<16; i++)
		    {
		        Message msg = new Message();
		         if(i % 2 == 0){
		             msg.what = 0;
		         }
		        else{
		            msg.what=1;
		        }

		        blinkHandler.sendMessageDelayed(msg, i*1000);
		    }
	  	}
	  	thr = new Thread(null, wTask, "wUpdate");
	  	thr.start();
	  	
	}
	
	@Override
	public void onStop(){
		
		log.info("onStop");
		
		mainStatus = false;
		stopThread();
		AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	}
	  	rl.setBackgroundResource(R.drawable.bg_whiteborder);
	    super.onStop();	    
	}
	
	@Override
	public void onDestroy(){
		Thread thr = new Thread(null, sTask, "StatusUpdate");
        thr.start();
		super.onDestroy();
	}

	Runnable sTask = new Runnable() {
        public void run() {
        	log.info("Send mainview finished to standoutwindow");
        	DataMonitor.sendData(MainActivity.this, DataMonitor.class, wid, DataMonitor.APP_MAIN_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		//log.info("main send to datamonitor");
        }
	};
	
	public synchronized void stopThread(){
    	if(thr != null){
    		Thread moribund = thr;
    		thr = null;
    		moribund.interrupt();
    	}
    }
	
	Runnable wTask = new Runnable() {
        public void run() {
        	while(mainStatus){
        		
        		message = mHandler.obtainMessage(1);
        		mHandler.sendMessage(message); 
        		
        		try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
	};
	
	private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {  
            super.handleMessage(msg); 
            switch(msg.what){
            case 0:            	
                break;
            case 1:            	
            	sM.getStatus();
        		sM.updateWidgetStatus();
        		updateMainStatus(sM._status);
            	break;
            default:
               	break;
            }
            
        }  
    };
    
    private void updateMainStatus(int sta){
    	switch(sta){
		case Constants.STATUS_READY:
			la.setVisibility(View.GONE);
			lr.setVisibility(View.GONE);
			li.setVisibility(View.VISIBLE);
			im.setImageResource(R.drawable.green);
			im.setVisibility(View.VISIBLE);
			tb.setVisibility(View.GONE);
			ba.setVisibility(View.VISIBLE);
			ba1.setVisibility(View.GONE);
			ba2.setVisibility(View.GONE);
			break;
		case Constants.STATUS_BLOCKED:
			la.setVisibility(View.GONE);
			lr.setVisibility(View.GONE);
			li.setVisibility(View.VISIBLE);
			im.setImageResource(R.drawable.red);
			im.setVisibility(View.VISIBLE);
			tb.setVisibility(View.GONE);
			ba.setVisibility(View.GONE);
			ba1.setVisibility(View.GONE);
			ba2.setVisibility(View.VISIBLE);
			if(!pM.getBindPref()){
				tb.setText("Bind");
				tb.setTextColor(Color.RED);
				im.setVisibility(View.GONE);
				tb.setVisibility(View.VISIBLE);
				ba.setVisibility(View.GONE);
				ba1.setVisibility(View.VISIBLE);
				ba2.setVisibility(View.GONE);
			}
			break;
		case Constants.STATUS_WAITGT:
			la.setVisibility(View.VISIBLE);
			lr.setVisibility(View.GONE);
			li.setVisibility(View.GONE);
			im.setImageResource(R.drawable.green);
			t2.setText(pM.getBindName());
			ba.setVisibility(View.VISIBLE);
			ba1.setVisibility(View.GONE);
			ba2.setVisibility(View.GONE);
			break;
		case Constants.STATUS_SCAN:
			la.setVisibility(View.GONE);
			lr.setVisibility(View.GONE);
			li.setVisibility(View.VISIBLE);
			im.setImageResource(R.drawable.hourglass);
			im.setVisibility(View.VISIBLE);
			tb.setVisibility(View.GONE);
			ba.setVisibility(View.GONE);
			ba1.setVisibility(View.GONE);
			ba2.setVisibility(View.VISIBLE);
			break;
		case Constants.STATUS_COM:
			la.setVisibility(View.GONE);
			lr.setVisibility(View.GONE);
			li.setVisibility(View.VISIBLE);
			im.setImageResource(R.drawable.hourglass);
			im.setVisibility(View.VISIBLE);
			tb.setVisibility(View.GONE);
			ba.setVisibility(View.GONE);
			ba1.setVisibility(View.GONE);
			ba2.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

}
