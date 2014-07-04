package org.sesy.coco.datacollector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.communication.HttpFileUploader;
import org.sesy.coco.datacollector.file.FileHelper;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.widget.EditText;
import android.widget.Toast;

public class ReportErrActivity extends Activity{

	ProgressDialog dialog = null;
	private FileHelper fh;
	String furi;
	Logger log;
	String version;
	private int responseCode;
	private StatusManager sM;
	private static int wid = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_report);
		
		log = Logger.getLogger(ReportErrActivity.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("log configured.");
        
        Bundle extras = getIntent().getExtras();        
		if(extras != null){
        	wid = extras.getInt("wid");
        	log.info("Bundle got: "+wid);
        }
		
        sM = new StatusManager(getApplicationContext());
        
        PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		version = pInfo.versionName;
		log.info("App Version: "+version);
        
		String uuid = Secure.getString(this.getContentResolver(),Secure.ANDROID_ID);
		log.info("Device UUID: "+uuid);
		fh = new FileHelper(this);
		String root = fh.getDir();
		furi = root + "/" +"dclog_"+ uuid + ".txt";
		
	}
	
	@Override
	public void onResume(){
		super.onResume();
		WorkerService.uploadStatus = true;
		AlarmService.alarmStatus = false;
  	  	if(AlarmService.vib != null){
	  		AlarmService.vib.cancel();
	  	}
	  	if(AlarmService.r != null){
	  		AlarmService.r.stop();
	  	} 
	  	new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		
		final EditText input = new EditText(this);

		new AlertDialog.Builder(this).setTitle("Report Errors on DataCollector v"+version).setMessage("Error Description:")
		.setView(input).setCancelable(false).setPositiveButton("Submit", new DialogInterface.OnClickListener()
		{	// When choosing to turn on GPS, go to setting page
			public void onClick(DialogInterface dialog, int which)
			{
				String description = input.getText().toString(); 
				log.info("Error Description: " + description);
				new UploadTask().execute(furi);
			}
		}).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{	// When choose not to open GPS, give a notice of disability
			public void onClick(DialogInterface dialog, int which)
			{
				Toast.makeText(ReportErrActivity.this, "Error Report Canceled.", Toast.LENGTH_SHORT).show();
				
				finish();
			}
		}).show(); 
	}
	
	@Override
	public void onPause() {
		WorkerService.uploadStatus = false;
		new Thread(null, wTask, "wUpdate").start();
		log.info("sM widget+task updated");
		super.onPause();
	}
	
	@Override
	public void onStop(){
		//connected = false;
		//bindingStatus = false;
		log.info("onStop");
		Thread thr = new Thread(null, sTask3, "ReportErrUpdate");
        thr.start();
        
	    super.onStop();	    
	}

	Runnable sTask3 = new Runnable() {
        public void run() {
        	log.info("Send bind finished to standoutwindow");
        	DataMonitor.sendData(ReportErrActivity.this, DataMonitor.class, wid, DataMonitor.APP_REPORT_FINISHED_CODE, null, null, DataMonitor.DISREGARD_ID);
    		
        }
	};
	
	private class UploadTask extends AsyncTask<String, Void, Void> {
		
		@Override
		public void onPreExecute(){
			dialog = ProgressDialog.show(ReportErrActivity.this, "", "Submitting Error Report...", true);
			
		}
		
	    @Override
	    protected Void doInBackground(String... furi) {
	    	try {
	    		//WorkerService.uploadStatus = true;
	    		log.info("uploadStatus started");
	    		log.info("Upload file: "+furi[0]);
    			HttpFileUploader htfu = new HttpFileUploader(getApplicationContext(), furi[0]);
    			responseCode = htfu.doUpload(true);
	        	
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(final Void unused) {
	    	dialog.dismiss(); 
	    	if(responseCode == 200){
	    		
	    		Toast.makeText(ReportErrActivity.this, "Error Report is Uploaded Successfully!", Toast.LENGTH_LONG).show();
	    	}
	    	
	    	log.info("uploadStatus ended");
	    	finish();
	    }
	}
	
	Runnable wTask = new Runnable() {
        public void run() {
        	sM.getStatus();    		
      	  	sM.updateWidgetStatus();
        }
	};
	
}
