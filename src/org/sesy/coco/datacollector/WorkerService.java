package org.sesy.coco.datacollector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.communication.HttpFileUploader;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.file.FileHelper;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import com.google.gson.Gson;

import android.app.IntentService;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WorkerService extends Service{	

	private static final String GPS_TASK_ACTION =  "org.sesy.coco.datacollector.GPS_TASK_ACTION";
	private static final String WIFI_TASK_ACTION =  "org.sesy.coco.datacollector.WIFI_TASK_ACTION";
	private static final String BT_TASK_ACTION =  "org.sesy.coco.datacollector.BT_TASK_ACTION";
	private static final String CELL_TASK_ACTION =  "org.sesy.coco.datacollector.CELL_TASK_ACTION";
	private static final String ARP_TASK_ACTION =  "org.sesy.coco.datacollector.ARP_TASK_ACTION";
	private static final String AUDIO_TASK_ACTION =  "org.sesy.coco.datacollector.AUDIO_TASK_ACTION";
	public static final String MY_SENSOR_ACTION = "org.sesy.coco.datacollector.MY_SENSOR_ACTION";
	//public static boolean taskStatus = false; 
	public static boolean audioRole = false;
	public static boolean uploadStatus = false;
	public static boolean gpsTask, btTask, wifiTask, audioTask, cellTask, arpTask, sensorTask = false;
	public static boolean gpsTaskDone, btTaskDone, wifiTaskDone, audioTaskDone, cellTaskDone, arpTaskDone, sensorTaskDone = false;
	//public static boolean laccTask, magTask, ligTask, gyroTask, gravTask, rotTask, oriTask, tempTask, humTask, baroTask, proxTask = false;
	//public static boolean laccTaskDone, magTaskDone, ligTaskDone, gyroTaskDone, gravTaskDone, rotTaskDone, oriTaskDone, tempTaskDone, humTaskDone, baroTaskDone, proxTaskDone = false;
	public static List<Entry> gpsSatList, gpsLocList, wifiList, btList, cellList, arpList, audList = null;
	public static List<Entry> magList, ligList, tempList, humList, baroList = null;
	public static long metaTS = 0L;
	public static String wavPath = "";
	public static String wavName = "";
	//public static String wavName = "";
	public static boolean isRecord = false;
	private static String[] furis = {"","",""};
	private PrefManager pM;
	//private PluginManager plM;
	private StatusManager sM;
	//private Context context;
	//private AppWidgetManager appWidgetManager;
	//private RemoteViews remoteViews;
	private int responseCode1, responseCode2, responseCode3;
	private int flag, mt;
	public static int ob, gt;
	private boolean ar;
	public static String uuid;
	Thread thr;
	Intent taskIntent1, taskIntent2, taskIntent3, taskIntent4, taskIntent4_1, sensorIntent, taskIntent5, arpIntent;
	Logger log;
	
	
	public void onCreate(){
		super.onCreate();
		log = Logger.getLogger(WorkerService.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        log.info("onCreate");
        
        //metaList = new ArrayList<Entry>();
        gpsSatList = new ArrayList<Entry>();
        gpsLocList = new ArrayList<Entry>();
        wifiList = new ArrayList<Entry>();
        btList = new ArrayList<Entry>();
        audList = new ArrayList<Entry>();
        //accList = new ArrayList<Entry>();
        
        cellList = new ArrayList<Entry>();
        arpList = new ArrayList<Entry>();
        //laccList = new ArrayList<Entry>();
        magList = new ArrayList<Entry>();
        ligList = new ArrayList<Entry>();
        //gyroList = new ArrayList<Entry>();
        //gravList = new ArrayList<Entry>();
        //rotList = new ArrayList<Entry>();
        //oriList = new ArrayList<Entry>();
        tempList = new ArrayList<Entry>();
        humList = new ArrayList<Entry>();
        baroList = new ArrayList<Entry>();
        //proxList = new ArrayList<Entry>();
        
        uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
        
        flag = 0; gt = 0; ob = 0; mt = Constants.STATUS_SENSOR_GWBAC;
        gpsTask = false;
        wifiTask = false;
        btTask = false;
        audioTask = false;
        gpsTaskDone = false;
        wifiTaskDone = false;
        btTaskDone = false;
        audioTaskDone = false;
        cellTask = false;
        cellTaskDone = false;
        arpTask = false;
        arpTaskDone = false;
        sensorTask = false;
        sensorTaskDone = false;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)  {
		// TODO Auto-generated method stub
	
		pM = new PrefManager(getApplicationContext());
		//plM = new PluginManager(getApplicationContext());
		sM = new StatusManager(getApplicationContext());
		
  	  	
		//register ground truth
		gt = intent.getIntExtra("gt", 0);		
		pM.updateGT(gt);
		ob = intent.getIntExtra("ob", 0);
		mt = intent.getIntExtra("mt", Constants.STATUS_SENSOR_GWBAC);
		mt = mt & pM.getSensorPrefState();
		//pM.updateAudioState((mt & Constants.STATUS_SENSOR_AUDIO) == Constants.STATUS_SENSOR_AUDIO);
		ar = intent.getBooleanExtra("ar", false);
		log.info("Worker Service started with gt: "+gt + " mt:"+mt + " ar:" + ar);
		//taskStatus = true;
		
		thr = new Thread(null, wTask, "wUpdate");
        thr.start();
		
        
        //Entry ecobj = new Entry();
		//ecobj.setTS(System.currentTimeMillis()- DaemonService.avgRTT/2);
		//ecobj.setGT(gt);
		//ecobj.setOB(ob);
		//Gson gson = new Gson();
		//String meta = gson.toJson(ecobj);
        //metaList.clear();
        //metaList.add(ecobj);
        
        sensorIntent = new Intent(MY_SENSOR_ACTION);
        //accIntent.putExtra("ob", ob);
        //accIntent.putExtra("gt", gt);
        taskIntent4 = new Intent(AUDIO_TASK_ACTION);	
		//taskIntent4.putExtra("ob", ob);
		//taskIntent4.putExtra("gt", gt);
		taskIntent4.putExtra("ar", ar);
		taskIntent1 = new Intent(GPS_TASK_ACTION); 
		//taskIntent1.putExtra("ob", ob);
		//taskIntent1.putExtra("gt", gt);
		taskIntent2 = new Intent(WIFI_TASK_ACTION);
		//taskIntent2.putExtra("ob", ob);
		//taskIntent2.putExtra("gt", gt);
		taskIntent3 = new Intent(BT_TASK_ACTION);
		//taskIntent3.putExtra("ob", ob);
		//taskIntent3.putExtra("gt", gt);
		taskIntent5 = new Intent(CELL_TASK_ACTION);
		//taskIntent5.putExtra("ob", ob);
		//taskIntent5.putExtra("gt", gt);
		arpIntent = new Intent(ARP_TASK_ACTION);
		//arpIntent.putExtra("ob", ob);
		//arpIntent.putExtra("gt", gt);
		
        flag = sM.getSensorStatus();
        flag = flag & mt;
        if((flag & Constants.STATUS_SENSOR_GWBAC) != 0){	//allowed only when at least one sensor enabled
        	//detach subtasks for modalities
        	metaTS = SystemClock.elapsedRealtime() - DaemonService.avgRTT/2;
        	sensorTask = true;
        	startService(sensorIntent);
        	arpTask = true;
        	startService(arpIntent);
        	
            if((flag & Constants.STATUS_SENSOR_AUDIO) == Constants.STATUS_SENSOR_AUDIO){
    			log.info("Audio task scheduled");
    			audioTask = true;    			
    			isRecord = true;	// upload raw data
    			startService(taskIntent4);
    		}else{
    			log.info("Audio task scheduled");
    			audioTask = true;    
    			isRecord = false;
    			startService(taskIntent4);
    		}
            
            if((flag & Constants.STATUS_SENSOR_GPS) == Constants.STATUS_SENSOR_GPS){
            	log.info("GPS task scheduled");
            	gpsTask = true;    			
    			startService(taskIntent1);
    		}
    		if((flag & Constants.STATUS_SENSOR_WIFI) == Constants.STATUS_SENSOR_WIFI){
    			log.info("Wifi task scheduled");
    			wifiTask = true;
    			startService(taskIntent2);
    		}
    		if((flag & Constants.STATUS_SENSOR_BT) == Constants.STATUS_SENSOR_BT){
    			log.info("Bluetooth task scheduled");
    			btTask = true;    			
    			startService(taskIntent3);
    		}
    		if((flag & Constants.STATUS_SENSOR_CELL) == Constants.STATUS_SENSOR_CELL){
    			log.info("Cell task scheduled");
    			cellTask = true;    			
    			startService(taskIntent5);
    		}
    		
    		log.info("WorkerService completed plugins schedule");
    		new Thread(null, OnTask, "MonitorTask").start();
    		
        }else{
        	DaemonService.taskStatus = false;
    		log.info("taskStatus ended");
    		stopSelf();
        }
		
		return START_NOT_STICKY;
		
	}
	
	@Override
	public void onDestroy() {
		
		new Thread(null, wTask, "wUpdate").start();	
		
		super.onDestroy();
	}
	
	Runnable OnTask = new Runnable() {
		private Handler mHandler = new Handler(Looper.getMainLooper());
		
		
        public void run() {
        	boolean flag1 = true;
        	int counter = 0;
        	while(flag1){
        		
        		try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        		if(!(gpsTask ^ gpsTaskDone) && !(wifiTask ^ wifiTaskDone) && !(btTask ^ btTaskDone) && !(audioTask ^ audioTaskDone) && !(cellTask ^ cellTaskDone) && !(arpTask ^ arpTaskDone) && !(sensorTask ^ sensorTaskDone)){
        			log.info("All tasks done.");
        			flag1 = false;
        		}        
        		counter = counter + 1;
        		if(counter > 150) 
        			flag1 = false;
        	}
        	//stopService(new Intent(WorkerService.MY_ACC_ACTION));
    		
    		DaemonService.taskStatus = false;
    		log.info("taskStatus ended");
    		
    		sM.getStatus();    		
      	  	sM.updateWidgetStatus(); 
      	  	
      	  
      	  try {
      		  furis = exportToCSV(WorkerService.this,ob);
      	  } catch (IOException e) {
      		  // TODO Auto-generated catch block
      		  e.printStackTrace();
      	  }
  		
  		
      	  if(furis[0] != null && furis[1] != null){
      		  //log.info("Upload file: "+furi);
      		mHandler.post(new Runnable() {
                public void run() {
                	new UploadTask().execute(furis);
                }
             });
      		  //new UploadTask().execute(furi);	//upload csv file	
      	  }else{
      		  log.info("upload file uri not found");
      		  stopSelf();
      	  }
    
        }
	};
	
	
	Runnable wTask = new Runnable() {
        public void run() {
        	sM.getStatus();    		
      	  	sM.updateWidgetStatus(); 
        }
	};
	
	private class UploadTask extends AsyncTask<String, Void, Void> {
	    @Override
	    protected Void doInBackground(String... txtPath) {
	    	try {
	    		uploadStatus = true;
	    		log.info("uploadStatus started");
	    		log.info("Upload file: "+txtPath[0]);
    			HttpFileUploader htfu1 = new HttpFileUploader(getApplicationContext(), txtPath[0]);
    			responseCode1 = htfu1.doUpload(false);
    			HttpFileUploader htfu2 = new HttpFileUploader(getApplicationContext(), wavPath);
    			responseCode2 = htfu2.doUpload(false);
    			HttpFileUploader htfu3 = new HttpFileUploader(getApplicationContext(), txtPath[1]);
    			responseCode3 = htfu3.doUpload(false);
    			
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	if(responseCode1 == 200 && responseCode2 == 200 && responseCode3 == 200){
	    		Toast.makeText(getApplicationContext(), "DataFile is Uploaded Successfully!", Toast.LENGTH_LONG).show();
	    	}
	    	uploadStatus = false;	   
	    	if(gt == 1){
	    		pM.updateColocationCounter();
	    	}else if(gt == 3){
	    		pM.updateNoncolocationCounter();
	    	}
	    	// Whether to keep data collected or not
	    	//new File(furis[0]).delete();
	    	//new File(furis[1]).delete();
	    	//new File(wavPath).delete();
	    	log.info("uploadStatus ended");
	    	stopSelf();
	    }
	}
	
	public String[] exportToCSV(Context context, int ob) throws IOException{
		String result = "";
		String furi = "";
		String furi2 = "";
		//String furi3 = "";
		FileHelper fh = new FileHelper(context);
		String uuid = Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
		
		String root = fh.getDir();
		File sd = new File(root);
		
		if (sd.canWrite()){
			
			String backupDBPath = "test".concat("_" + String.valueOf(ob) + "_" + uuid + ".txt");
	    	furi = root + "/" + backupDBPath;
	    	
	    	File file = new File(sd, backupDBPath);
	    	FileWriter filewriter = new FileWriter(file);  
	        BufferedWriter out = new BufferedWriter(filewriter);
	        
	        String meta = ob+"#"+uuid+"#"+gt+"\n";
	        out.write(meta);
	        
	        for(Entry e: gpsSatList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: gpsLocList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: wifiList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: btList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        for(Entry e: audList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: cellList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: arpList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }
	        for(Entry e: magList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        for(Entry e: ligList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        for(Entry e: tempList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        for(Entry e: humList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        for(Entry e: baroList){
	        	result = e.getTS() +";" + e.getMT() +";" + e.getFP() +"\n";
	        	out.write(result);
	        }	
	        		    
		    out.close();
		}
		String[] res = {furi, furi2};
		return res;
	}
	
	public static void sensorsClear(){
		//accList.clear();
		//laccList.clear();
		magList.clear();
		ligList.clear();
		//gyroList.clear();
		//gravList.clear();
		//rotList.clear();
		//oriList.clear();
		tempList.clear();
		humList.clear();
		baroList.clear();
		//proxList.clear();
	}
	
}
