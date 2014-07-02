package org.sesy.coco.datacollector;

import java.util.Collections;
import java.util.Comparator;

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
	private static final int[] qsSensors = new int[] { DaemonService.myDrone.QS_TYPE_TEMPERATURE,
		DaemonService.myDrone.QS_TYPE_HUMIDITY,
		DaemonService.myDrone.QS_TYPE_PRESSURE,
		DaemonService.myDrone.QS_TYPE_IR_TEMPERATURE,
		DaemonService.myDrone.QS_TYPE_RGBC,
		DaemonService.myDrone.QS_TYPE_PRECISION_GAS,
		DaemonService.myDrone.QS_TYPE_REDUCING_GAS,
		DaemonService.myDrone.QS_TYPE_OXIDIZING_GAS,
		DaemonService.myDrone.QS_TYPE_CAPACITANCE,
		DaemonService.myDrone.QS_TYPE_ALTITUDE };

	// Sensors
	private static final String[] SENSOR_NAMES = { "Temperature (Ambient)",
			"Humidity", "Pressure", "Object Temperature (IR)",
			"RGBC Properties", "Precision Gas", "Reducing Gas", "Oxidizing Gas",
			"Proximity Capacitance", "Altitude" };
	
	private static boolean[] SENSOR_FLAGS = {false, false, false, false, false,
		false, false, false, false, false};

	// Figure out how many sensors we have based on the length of our labels
	private static int numberOfSensors = SENSOR_NAMES.length;
	
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
        
        log.info("SD sensor number:"+numberOfSensors);
        for (int counter = 0; counter < numberOfSensors; counter++) {
        	//final int counter = i;
        	streamerArray[counter] = new DroneQSStreamer(DaemonService.myDrone, qsSensors[counter]);
        	// Enable our steamer
			streamerArray[counter].enable();
			
			// Enable the sensor
			if(DaemonService.myDrone.quickEnable(qsSensors[counter])){
				log.info("SD sensor enabled:" + SENSOR_NAMES[counter]);
			}else{
				DaemonService.myDrone.quickEnable(qsSensors[counter]);
			};
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
				/*if (DaemonService.myDrone.altitudeStatus) {
					log.info("SD altitude ok");
					streamerArray[9].run();
				}*/
				runOnAltiStatus();
			}

			@Override
			public void batteryVoltageStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void capacitanceStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.capacitanceStatus) {
					log.info("SD cap ok");
					streamerArray[8].run();
				}*/
				runOnCapStatus();
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
				/*if (DaemonService.myDrone.humidityStatus) {
					log.info("SD humidity ok");
					streamerArray[1].run();
				}*/
				runOnHumidityStatus();
			}

			@Override
			public void irStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.irTemperatureStatus) {
					log.info("SD irTemp ok");
					streamerArray[3].run();
				}*/
				runOnIrTempStatus();
			}

			@Override
			public void lowBatteryStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void oxidizingGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.oxidizingGasStatus) {
					log.info("SD oGas ok");
					streamerArray[7].run();
				}*/
				runOnOgasStatus();
			}

			@Override
			public void precisionGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.precisionGasStatus) {
					log.info("SD pGas ok");
					streamerArray[5].run();
				}*/
				runOnPgasStatus();
			}

			@Override
			public void pressureStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.pressureStatus) {
					log.info("SD pressure ok");
					streamerArray[2].run();
				}*/
				runOnPressStatus();
			}

			@Override
			public void reducingGasStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.reducingGasStatus) {
					log.info("SD rGas ok");
					streamerArray[6].run();
				}*/
				runOnRgasStatus();
			}

			@Override
			public void rgbcStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.rgbcStatus) {
					log.info("SD rgbc ok");
					streamerArray[4].run();
				}*/
				runOnRgbcStatus();
			}

			@Override
			public void temperatureStatus(DroneEventObject arg0) {
				// TODO Auto-generated method stub
				/*if (DaemonService.myDrone.temperatureStatus) {
					log.info("SD temp ok");
					streamerArray[0].run();
				}*/
				runOnTempStatus();
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
		clearFlags();
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
				log.info("SD scan finished:" + WorkerService.sdList.size());
				
				Collections.sort(WorkerService.sdList, new Comparator(){

					@Override
					public int compare(Object obj1, Object obj2) {
						Entry eobj1 = (Entry) obj1;
						Entry eobj2 = (Entry) obj2;
						return (eobj1.getMT() < eobj2.getMT())?-1: (eobj1.getMT() > eobj2.getMT()?1:(eobj1.getTS() < eobj2.getTS()?-1:(eobj1.getTS() > eobj2.getTS()?1:0)));
					}
					
				});
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
		
		runOnTempStatus();
		runOnHumidityStatus();
		runOnPressStatus();
		runOnIrTempStatus();
		runOnRgbcStatus();
		runOnPgasStatus();
		runOnRgasStatus();
		runOnOgasStatus();
		runOnCapStatus();
		runOnAltiStatus();
		
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
	
	private synchronized void runOnTempStatus(){
		if (!SENSOR_FLAGS[0] && DaemonService.myDrone.temperatureStatus) {
			log.info("SD temp ok");
			SENSOR_FLAGS[0] = true;
			streamerArray[0].run();			
		}
	}
	
	private synchronized void runOnHumidityStatus(){
		if (!SENSOR_FLAGS[1] && DaemonService.myDrone.humidityStatus) {
			log.info("SD humidity ok");
			SENSOR_FLAGS[1] = true;
			streamerArray[1].run();
		}
	}
	
	private synchronized void runOnPressStatus(){
		if (!SENSOR_FLAGS[2] && DaemonService.myDrone.pressureStatus) {
			log.info("SD pressure ok");
			SENSOR_FLAGS[2] = true;
			streamerArray[2].run();
		}
	}

	private synchronized void runOnIrTempStatus(){
		if (!SENSOR_FLAGS[3] && DaemonService.myDrone.irTemperatureStatus) {
			log.info("SD irTemp ok");
			SENSOR_FLAGS[3] = true;
			streamerArray[3].run();
		}
	}
	
	private synchronized void runOnRgbcStatus(){
		if (!SENSOR_FLAGS[4] && DaemonService.myDrone.rgbcStatus) {
			log.info("SD rgbc ok");
			SENSOR_FLAGS[4] = true;
			streamerArray[4].run();
		}
	}
	
	private synchronized void runOnPgasStatus(){
		if (!SENSOR_FLAGS[5] && DaemonService.myDrone.precisionGasStatus) {
			log.info("SD pGas ok");
			SENSOR_FLAGS[5] = true;
			streamerArray[5].run();
		}
	}
	
	private synchronized void runOnRgasStatus(){
		if (!SENSOR_FLAGS[6] && DaemonService.myDrone.reducingGasStatus) {
			log.info("SD rGas ok");
			SENSOR_FLAGS[6] = true;
			streamerArray[6].run();
		}
	}
	
	private synchronized void runOnOgasStatus(){
		if (!SENSOR_FLAGS[7] && DaemonService.myDrone.oxidizingGasStatus) {
			log.info("SD oGas ok");
			SENSOR_FLAGS[7] = true;
			streamerArray[7].run();
		}
	}
	
	private synchronized void runOnCapStatus(){
		if (!SENSOR_FLAGS[8] && DaemonService.myDrone.capacitanceStatus) {
			log.info("SD cap ok");
			SENSOR_FLAGS[8] = true;
			streamerArray[8].run();
		}
	}
	
	private synchronized void runOnAltiStatus(){
		if (!SENSOR_FLAGS[9] && DaemonService.myDrone.altitudeStatus) {
			log.info("SD altitude ok");
			SENSOR_FLAGS[9] = true;
			streamerArray[9].run();
		}
	}
	
	private synchronized void clearFlags(){
		for(int i=0; i<numberOfSensors; i++){
			SENSOR_FLAGS[i] = false;
		}
	}

}
