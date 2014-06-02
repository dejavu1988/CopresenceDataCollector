package org.sesy.coco.datacollector;

import java.io.File;

import org.sesy.coco.datacollector.database.Entry;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class AudioProc extends IntentService {

	//private Wave waveRemote, waveLocal;
	//final float[] trimSeconds = {10, 5, 4, 3, 2, 1};
	//private String fp;
	
	public AudioProc() {
		super("AudioProc");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("wavproc", "Audio Proc started");
		//String waveHeaderJson = intent.getStringExtra("header");
		//String waveDataString = intent.getStringExtra("data");
		String waveFP = intent.getStringExtra("wavefp");
		
		/*Gson gson = new Gson();
  		WaveHeader waveHeaderRemote = gson.fromJson(waveHeaderJson, WaveHeader.class);
  		byte[] dataRemote = null;
  		try {
  			dataRemote = Hex.decodeHex(waveDataString.toCharArray());
		} catch (DecoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  		waveRemote = new Wave(waveHeaderRemote, dataRemote);
  		waveLocal = new Wave(WorkerService.wavPath);
  		fp = WorkerService.wavName + "#";
  		if(waveRemote.length() > 0 && waveLocal.length() > 0){
  			for(int i = 0; i < 6; i++){
  				if(waveRemote.length() > trimSeconds[i]){
  					waveRemote.rightTrim(waveRemote.length() - trimSeconds[i]);
  				}
  				if(waveLocal.length() > trimSeconds[i]){
  					waveLocal.rightTrim(waveLocal.length()  - trimSeconds[i]);
  				}
  				
  				XCorrAndDistFromWav xCorrAndDistFromWav = new XCorrAndDistFromWav(waveRemote, waveLocal);
  				fp += xCorrAndDistFromWav.getMaxCorr()+"#"+xCorrAndDistFromWav.getDist()+"#";
  				
  			}
  		}*/
		
  		long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
  		
  		//remove wav if not server storage not enabled	
		if(!WorkerService.isRecord){
			new File(WorkerService.wavPath).delete();
		}
		
  		Entry eobj = new Entry();				
		//eobj.setOB(WorkerService.ob);
		eobj.setTS(ts);
		//eobj.setGT(WorkerService.gt);
		eobj.setMT(Constants.STATUS_SENSOR_AUDIO);
		eobj.setFP(waveFP+WorkerService.wavName);
        WorkerService.audList.set(0, eobj);
		
		WorkerService.audioTaskDone = true;
  		
  		
	}

}
