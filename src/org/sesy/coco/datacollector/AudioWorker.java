package org.sesy.coco.datacollector;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.audio.ExtAudioRecorder;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.file.FileHelper;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.google.gson.Gson;
import com.musicg.wave.Wave;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings.Secure;

public class AudioWorker extends Service {

	Logger log;
	private CountDownTimer timer;
	//private MediaRecorder recorder;
	private ExtAudioRecorder extAudioRecorder;
	//private Date timestamp;
	//private int gt, ob;
	private boolean ar;
	private static long ts = 0L;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(AudioWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		   
        
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		WorkerService.audioTask = true;
		log.info("Subtask Audio");
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		//ar = intent.getBooleanExtra("ar", false);
		ar = true;
		WorkerService.audList.clear();
		extAudioRecorder = ExtAudioRecorder.getInstanse(false);
		
		timer = new CountDownTimer(Constants.AUDIO_PERIOD * 1000, 1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				
				log.info("Audio scan finished");
				//recorder.stop();
				//recorder.reset();
				//recorder.release();
				extAudioRecorder.stop();
				extAudioRecorder.reset();
				extAudioRecorder.release();
				
				String fp =  "####" + WorkerService.wavName;
				/*if(recordLock){
					fp = wavName + "#";
				}*/
				
				//if(ar){
					Wave wave = new Wave(WorkerService.wavPath);
					Gson gson = new Gson();
					String waveHeaderJson = gson.toJson(wave.getWaveHeader());
					String waveData = new String(Hex.encodeHex(wave.getBytes()));
					/*--------------------
					 * send json string to server with "send"
					 * -------------------
					 */
					/*HashMap<String,String> msgObj = new HashMap<String,String>();
	        		String msg = ""; 
	        		msgObj.clear();                  	
	        		
	          		msgObj.put("id", Constants.SEND);
	          		msgObj.put("uuid", WorkerService.uuid);
	          		msgObj.put("header", waveHeaderJson);
	          		msgObj.put("data", waveData);
	            	msg = gson.toJson(msgObj);
	            	//log.info("SEND msg built: "+msg);
	        		if (DaemonService.socket.isConnected()) {  
	                    if (!DaemonService.socket.isOutputShutdown()) {  
	                    	DaemonService.out.println(msg);
	                    	DaemonService.out.flush();
	                    	DaemonService.timeoutSet = true;
	                    	DaemonService.timeoutTimer = SystemClock.elapsedRealtime();
	                    	DaemonService.timeoutCounter = 0;
	                    	log.info("SEND msg sent");
	                    }  
	                }  
	        		DaemonService.taskTimeout = SystemClock.elapsedRealtime();       		
	        		
	        		//remove wav if not server storage not enabled	
					if(!WorkerService.isRecord){
						new File(WorkerService.wavPath).delete();
					}*/
					
					Entry eobj = new Entry();				
					//eobj.setOB(ob);
					eobj.setTS(ts);
					//eobj.setGT(gt);
					eobj.setMT(Constants.STATUS_SENSOR_AUDIO);
					eobj.setFP(fp);
			        WorkerService.audList.add(eobj);
					
					if(ar){	//if this is trigger side
						WorkerService.audioTaskDone = true;
					}
					
				//}		
				
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		FileHelper fh = new FileHelper(getApplicationContext());
		String uuid = Secure.getString(getApplicationContext().getContentResolver(),Secure.ANDROID_ID);
		
		String root = fh.getDir();
		
		ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
		
		WorkerService.wavName = "test_" + String.valueOf(WorkerService.ob) + "_" +uuid + "_" + ts + ".wav";
		WorkerService.wavPath = root + "/" + WorkerService.wavName;
		//recorder.setOutputFile(WorkerService.wavPath);
		extAudioRecorder.setOutputFile(WorkerService.wavPath);
		
		try {
			//recorder.prepare();
			extAudioRecorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//recorder.start();
		extAudioRecorder.start();
		timer.start();
		
		
		return START_NOT_STICKY;		
	}
}
