package org.sesy.coco.datacollector;

import com.sensorcon.sensordrone.DroneEventHandler;
import com.sensorcon.sensordrone.DroneEventObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SDSetupActivity extends Activity {
	
	private PrefManager pM;
	private static int wid = 0;
	public TextView tvMAC, tvConnStatus;
	public Button btnConnect, btnReconnect, btnDisconnect; 
	public DroneEventHandler myDroneEventHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sdsetup);
		
		tvMAC = (TextView)findViewById(R.id.textViewSDMAC);
		tvConnStatus = (TextView)findViewById(R.id.textViewConnectionStatus);
		btnConnect = (Button)findViewById(R.id.buttonConnect);
		btnReconnect = (Button)findViewById(R.id.buttonReconnect);
		btnDisconnect = (Button)findViewById(R.id.buttonDisconnect);
		
		pM = new PrefManager(getApplicationContext());
		
		if(DaemonService.myDrone.isConnected){
			tvMAC.setText(DaemonService.myDrone.lastMAC);
			tvConnStatus.setText("is connected.");
		}else{
			tvMAC.setText("");
			tvConnStatus.setText("is not connected.");
		}
		
		myDroneEventHandler = new DroneEventHandler(){

			@Override
			public void parseEvent(DroneEventObject droneEventObject) {
				// TODO Auto-generated method stub
				if(droneEventObject.matches(DroneEventObject.droneEventType.CONNECTED)){
					tvMAC.setText(DaemonService.myDrone.lastMAC);
					tvConnStatus.setText("is connected.");
				}else if(droneEventObject.matches(DroneEventObject.droneEventType.DISCONNECTED)){
					tvMAC.setText("");
					tvConnStatus.setText("is not connected.");
				}
				
			}
			
		};
		
		btnConnect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!DaemonService.myDrone.isConnected){
					DaemonService.myHelper.connectFromPairedDevices(DaemonService.myDrone, SDSetupActivity.this);
				}else{
					Toast.makeText(getApplicationContext(), "Sensordrone is already connected", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		btnReconnect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!DaemonService.myDrone.isConnected){
					if(pM.getSensordroneMAC() != ""){
						DaemonService.myDrone.btConnect(pM.getSensordroneMAC());
					}else{
						Toast.makeText(getApplicationContext(), "Last MAC not found... Please connect once", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(getApplicationContext(), "Sensordrone is already connected", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		btnDisconnect.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(DaemonService.myDrone.isConnected){
					DaemonService.myDrone.disconnect();
				}else{
					Toast.makeText(getApplicationContext(), "Sensordrone is already disconnected", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		DaemonService.myDrone.registerDroneListener(myDroneEventHandler);
	}
	
	@Override
	public void onPause(){
		super.onResume();
		DaemonService.myDrone.unregisterDroneListener(myDroneEventHandler);
	}
	
	@Override
	public void onDestroy(){
		Thread thr = new Thread(null, sTask, "StatusUpdate");
        thr.start();
		super.onDestroy();
	}

	Runnable sTask = new Runnable() {
        public void run() {
        	DataMonitor.sendData(SDSetupActivity.this, DataMonitor.class, wid, DataMonitor.APP_SDSETUP_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		//log.info("main send to datamonitor");
        }
	};
}
