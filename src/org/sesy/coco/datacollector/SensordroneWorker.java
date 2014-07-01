package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.sensorcon.sensordrone.DroneEventListener;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.DroneStatusListener;
import com.sensorcon.sensordrone.android.tools.DroneQSStreamer;
import com.sensorcon.sensordrone.android.tools.DroneStreamer;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

public class SensordroneWorker extends Service {

	private DroneEventListener deListener;
	private DroneStatusListener dsListener;
	// A ConnectionBLinker from the SDHelper Library
	private DroneStreamer myBlinker;
	
	// Sampling rate: default 1Hz
	private int streamingRate = 1000;
	
	// An int[] that will hold the QS_TYPEs for our sensors of interest
	private int[] qsSensors;

	// Text to display
	private static final String[] SENSOR_NAMES = { "Temperature (Ambient)",
			"Humidity", "Pressure", "Object Temperature (IR)",
			"RGBC Properties", "Precision Gas", "Reducing Gas", "Oxidizing Gas",
			"Proximity Capacitance", "Altitude" };

	// Figure out how many sensors we have based on the length of our labels
	private int numberOfSensors = SENSOR_NAMES.length;
	
	// Another object from the SDHelper library. It helps us set up our
	// pseudo streaming
	private DroneQSStreamer[] streamerArray = new DroneQSStreamer[numberOfSensors];
	
	// Toggle our LED
    private boolean ledToggle = true;
    	
	private CountDownTimer timer;
	
	//private int gt, ob;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(SensordroneWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		        
                
        qsSensors = new int[] { DaemonService.myDrone.QS_TYPE_TEMPERATURE,
        		DaemonService.myDrone.QS_TYPE_HUMIDITY,
        		DaemonService.myDrone.QS_TYPE_PRESSURE,
        		DaemonService.myDrone.QS_TYPE_IR_TEMPERATURE,
        		DaemonService.myDrone.QS_TYPE_RGBC,
        		DaemonService.myDrone.QS_TYPE_PRECISION_GAS,
        		DaemonService.myDrone.QS_TYPE_REDUCING_GAS,
        		DaemonService.myDrone.QS_TYPE_OXIDIZING_GAS,
        		DaemonService.myDrone.QS_TYPE_CAPACITANCE,
				DaemonService.myDrone.QS_TYPE_ALTITUDE };
        
        // This will Blink our Drone, once a second, Blue
        myBlinker = new DroneStreamer(DaemonService.myDrone, 1000) {
            @Override
            public void repeatableTask() {
                if (ledToggle) {
                	DaemonService.myDrone.setLEDs(0, 0, 126);
                } else {
                	DaemonService.myDrone.setLEDs(0,0,0);
                }
                ledToggle = !ledToggle;
            }
        };
        
        for (int i = 0; i < numberOfSensors; i++) {
        	final int counter = i;
        	streamerArray[i] = new DroneQSStreamer(DaemonService.myDrone, qsSensors[i]);
        	// Enable our steamer
			streamerArray[counter].enable();
			// Enable the sensor
			DaemonService.myDrone.quickEnable(qsSensors[counter]);
        }
        
        /*
		 * Set up status listener
		 * 
		 */
		dsListener = new DroneStatusListener() {

			@Override
			public void adcStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void altitudeStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.altitudeStatus) {
					streamerArray[9].run();
				}
			}

			@Override
			public void batteryVoltageStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void capacitanceStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.capacitanceStatus) {
					streamerArray[8].run();
				}
			}

			@Override
			public void chargingStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void customStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void humidityStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.humidityStatus) {
					streamerArray[1].run();
				}
			}

			@Override
			public void irStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.irTemperatureStatus) {
					streamerArray[3].run();
				}
			}

			@Override
			public void lowBatteryStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void oxidizingGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.oxidizingGasStatus) {
					streamerArray[7].run();
				}
			}

			@Override
			public void precisionGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.precisionGasStatus) {
					streamerArray[5].run();
				}
			}

			@Override
			public void pressureStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.pressureStatus) {
					streamerArray[2].run();
				}
			}

			@Override
			public void reducingGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.reducingGasStatus) {
					streamerArray[6].run();
				}
			}

			@Override
			public void rgbcStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.rgbcStatus) {
					streamerArray[4].run();
				}
			}

			@Override
			public void temperatureStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				if (DaemonService.myDrone.temperatureStatus) {
					streamerArray[0].run();
				}
			}

			@Override
			public void unknownStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		/*
		 * Set up Drone Event Listener.
		 * 
		 */
		deListener = new DroneEventListener() {					
			
			@Override
			public void adcMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void altitudeMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_ALTITUDE);
				eobj.setFP(String.valueOf(DaemonService.myDrone.altitude_Meters));
				WorkerService.sdList.add(eobj);
				streamerArray[9].streamHandler.postDelayed(streamerArray[9], streamingRate);
			}

			@Override
			public void capacitanceMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_CAPACITANCE);
				eobj.setFP(String.valueOf(DaemonService.myDrone.capacitance_femtoFarad));
				WorkerService.sdList.add(eobj);
				streamerArray[8].streamHandler.postDelayed(streamerArray[8], streamingRate);
			}

			@Override
			public void connectEvent(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectionLostEvent(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void customEvent(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void disconnectEvent(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void humidityMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_HUMIDITY);
				eobj.setFP(String.valueOf(DaemonService.myDrone.humidity_Percent));
				WorkerService.sdList.add(eobj);
				streamerArray[1].streamHandler.postDelayed(streamerArray[1], streamingRate);
			}

			@Override
			public void i2cRead(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void irTemperatureMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_IR_TEMPERATURE);
				eobj.setFP(String.valueOf(DaemonService.myDrone.irTemperature_Celsius));
				WorkerService.sdList.add(eobj);
				streamerArray[3].streamHandler.postDelayed(streamerArray[3], streamingRate);
			}

			@Override
			public void oxidizingGasMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_OXIDIZING_GAS);
				eobj.setFP(String.valueOf(DaemonService.myDrone.oxidizingGas_Ohm));
				WorkerService.sdList.add(eobj);
				streamerArray[7].streamHandler.postDelayed(streamerArray[7], streamingRate);
			}

			@Override
			public void precisionGasMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_PRECISION_GAS);
				eobj.setFP(String.valueOf(DaemonService.myDrone.precisionGas_ppmCarbonMonoxide));
				WorkerService.sdList.add(eobj);
				streamerArray[5].streamHandler.postDelayed(streamerArray[5], streamingRate);
			}

			@Override
			public void pressureMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_PRESSURE);
				eobj.setFP(String.valueOf(DaemonService.myDrone.pressure_Pascals));
				WorkerService.sdList.add(eobj);
				streamerArray[2].streamHandler.postDelayed(streamerArray[2], streamingRate);
			}

			@Override
			public void reducingGasMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_REDUCING_GAS);
				eobj.setFP(String.valueOf(DaemonService.myDrone.reducingGas_Ohm));
				WorkerService.sdList.add(eobj);
				streamerArray[6].streamHandler.postDelayed(streamerArray[6], streamingRate);
			}

			@Override
			public void rgbcMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_RGBC);
				float lux = (DaemonService.myDrone.rgbcLux > 0) ? DaemonService.myDrone.rgbcLux: 0.0f;
				eobj.setFP(String.valueOf(lux) + "#" + String.valueOf(DaemonService.myDrone.rgbcColorTemperature) + "#" + String.valueOf(DaemonService.myDrone.rgbcRedChannel) + "#" + String.valueOf(DaemonService.myDrone.rgbcGreenChannel) + "#" + String.valueOf(DaemonService.myDrone.rgbcBlueChannel) + "#" + String.valueOf(DaemonService.myDrone.rgbcClearChannel));
				WorkerService.sdList.add(eobj);
				streamerArray[4].streamHandler.postDelayed(streamerArray[4], streamingRate);
			}

			@Override
			public void temperatureMeasured(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;	
				Entry eobj = new Entry(ts);
				eobj.setMT(Constants.SD_SENSOR_TEMPERATURE);
				eobj.setFP(String.valueOf(DaemonService.myDrone.temperature_Celsius));
				WorkerService.sdList.add(eobj);
				streamerArray[0].streamHandler.postDelayed(streamerArray[0], streamingRate);
			}

			@Override
			public void uartRead(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void unknown(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void usbUartRead(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WorkerService.sensordroneTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		
		WorkerService.sdList.clear();
		
		log.info("Subtask SD");
		
		timer = new CountDownTimer(32*1000, 1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				// Turn off the blinker
				myBlinker.stop();
				DaemonService.myDrone.setLEDs(0, 0, 0);
				
				DaemonService.myDrone.unregisterDroneListener(deListener);
				DaemonService.myDrone.unregisterDroneListener(dsListener);
				log.info("SD scan finished");
				WorkerService.sensordroneTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
			}
			
		};
		
		DaemonService.myDrone.registerDroneListener(deListener);
		DaemonService.myDrone.registerDroneListener(dsListener);
		timer.start();
		// Turn on our blinker
		myBlinker.start();
		log.info("Subtask SD scan started.");
		
		
		return START_NOT_STICKY;		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
