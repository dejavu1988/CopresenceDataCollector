package org.sesy.coco.datacollector;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

public class SensorListener extends Service implements SensorEventListener{
	
	private SensorManager sensorManager; 
	private Sensor sensorMAG, sensorLIG, sensorTEMP, sensorHUM, sensorBARO; 
	private CountDownTimer timer;
	//private int gt, ob;
    Logger log;
    
	@Override
	public void onCreate()
	{
		super.onCreate();
		log = Logger.getLogger(SensorListener.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		log.info("Acclistener task started");	
		WorkerService.sensorTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		WorkerService.sensorsClear();
		
		timer = new CountDownTimer(30*1000, 1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				sensorManager.unregisterListener(SensorListener.this);
				log.info("Acc scan finished");
				WorkerService.sensorTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		init();
		/*sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
     	if(sensorManager == null) { 
     		throw new UnsupportedOperationException("Sensors are not supported");
     	} 
    	// Get default sensor of type ACCELEROMETER
    	sensorACC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
    	
    		// Register AccListener for given sensor
    	if(sensorACC != null) { 
    	   sensorManager.registerListener(this, sensorACC, SensorManager.SENSOR_DELAY_FASTEST); 
    	   timer.start();
    	   log.info("Acc scan started");
    	}*/
		return START_NOT_STICKY;
	}
	
		
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
		switch(event.sensor.getType()){
		/*case Sensor.TYPE_ACCELEROMETER:
			String fp1 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj1 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_ACC, fp1);	
			WorkerService.accList.add(eobj1);
			break;*/
		/*case Sensor.TYPE_LINEAR_ACCELERATION:
			String fp2 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj2 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_LACC, fp2);	
			WorkerService.laccList.add(eobj2);
			break;*/
		case Sensor.TYPE_MAGNETIC_FIELD:
			String fp3 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj3 = new Entry(ts, Constants.STATUS_SENSOR_MAG, fp3);
			WorkerService.magList.add(eobj3);
			break;
		case Sensor.TYPE_LIGHT:
			String fp4 = String.valueOf(event.values[0]);
			Entry eobj4 = new Entry(ts, Constants.STATUS_SENSOR_LIG, fp4);	
			WorkerService.ligList.add(eobj4);
			break;
		/*case Sensor.TYPE_GYROSCOPE:
			String fp5 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj5 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_GYRO, fp5);	
			WorkerService.gyroList.add(eobj5);
			break;*/
		/*case Sensor.TYPE_GRAVITY:
			String fp6 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj6 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_GRAV, fp6);	
			WorkerService.gravList.add(eobj6);
			break;*/
		/*case Sensor.TYPE_ROTATION_VECTOR:
			String fp7 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj7 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_ROT, fp7);	
			WorkerService.rotList.add(eobj7);
			break;*/
		/*case Sensor.TYPE_ORIENTATION:
			String fp8 = event.values[0] + "#" + event.values[1] + "#" + event.values[2];
			Entry eobj8 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_ORI, fp8);	
			WorkerService.oriList.add(eobj8);
			break;*/
		case Sensor.TYPE_RELATIVE_HUMIDITY:
			String fp9 = String.valueOf(event.values[0]);
			Entry eobj9 = new Entry(ts, Constants.STATUS_SENSOR_HUM, fp9);		
			WorkerService.humList.add(eobj9);
			break;
		case Sensor.TYPE_AMBIENT_TEMPERATURE:
			String fp10 = String.valueOf(event.values[0]);
			Entry eobj10 = new Entry(ts, Constants.STATUS_SENSOR_TEMP, fp10);	
			WorkerService.tempList.add(eobj10);
			break;
		case Sensor.TYPE_PRESSURE:
			String fp11 = String.valueOf(event.values[0]);
			Entry eobj11 = new Entry(ts, Constants.STATUS_SENSOR_BARO, fp11);	
			WorkerService.baroList.add(eobj11);
			break;
		/*case Sensor.TYPE_PROXIMITY:
			String fp12 = String.valueOf(event.values[0]);
			Entry eobj12 = new Entry(ob, ts, gt, Constants.STATUS_SENSOR_PROX, fp12);	
			WorkerService.proxList.add(eobj12);
			break;*/
		default:
			return;
		}		
		
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void init(){
		sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
     	if(sensorManager == null) { 
     		throw new UnsupportedOperationException("Sensors are not supported");
     	} 
    	// Get default sensor of type ACCELEROMETER
    	//sensorACC = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER); 
    	//sensorLACC = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION); 
    	sensorMAG = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD); 
    	sensorLIG = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); 
    	//sensorGYRO = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE); 
    	//sensorGRAV = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY); 
    	//sensorROT = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); 
    	//sensorORI = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); 
    	sensorTEMP = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE); 
    	sensorHUM = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY); 
    	sensorBARO = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE); 
    	//sensorPROX = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY); 
    	
    	/*if(sensorACC != null) { 
    	   sensorManager.registerListener(this, sensorACC, SensorManager.SENSOR_DELAY_FASTEST); 
    	   timer.start();
    	}*/
    	/*if(sensorLACC != null) { 
     	   sensorManager.registerListener(this, sensorLACC, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
    	if(sensorMAG != null) { 
     	   sensorManager.registerListener(this, sensorMAG, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}
    	if(sensorLIG != null) { 
     	   sensorManager.registerListener(this, sensorLIG, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}
    	/*if(sensorGYRO != null) { 
     	   sensorManager.registerListener(this, sensorGYRO, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
    	/*if(sensorGRAV != null) { 
     	   sensorManager.registerListener(this, sensorGRAV, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
    	/*if(sensorROT != null) { 
     	   sensorManager.registerListener(this, sensorROT, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
    	/*if(sensorORI != null) { 
     	   sensorManager.registerListener(this, sensorORI, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
    	if(sensorTEMP != null) { 
     	   sensorManager.registerListener(this, sensorTEMP, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}
    	if(sensorHUM != null) { 
     	   sensorManager.registerListener(this, sensorHUM, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}
    	if(sensorBARO != null) { 
     	   sensorManager.registerListener(this, sensorBARO, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}
    	/*if(sensorPROX != null) { 
     	   sensorManager.registerListener(this, sensorPROX, SensorManager.SENSOR_DELAY_FASTEST); 
     	   timer.start();
     	}*/
	}

}
