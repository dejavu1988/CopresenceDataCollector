package org.sesy.coco.datacollector;

import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class GpsWorker extends Service implements GpsStatus.Listener, LocationListener{

	private LocationManager locMgr;
	private GpsStatus gpsStatus;
	private CountDownTimer timer;
	//private DBHelper db;
	//private int gt, ob;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(GpsWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		
        
        
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		WorkerService.gpsTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		//db = new DBHelper(this);
		//db.clear();
		 WorkerService.gpsSatList.clear();
		 WorkerService.gpsLocList.clear();
		 
		//Entry ecobj = new Entry();
		//ecobj.setTS(SystemClock.elapsedRealtime() - WorkerService.metaTS);
		//ecobj.setGT(gt);
		//ecobj.setOB(ob);
		//ecobj.setMT(Constants.STATUS_SENSOR_GPS);
		//Gson gson = new Gson();
		//String meta = gson.toJson(ecobj);		 
		//WorkerService.gpsSatList.add(ecobj);
		//db.addEntry(ecobj);
		 
		locMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		
		timer = new CountDownTimer(120*1000, 1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				locMgr.removeGpsStatusListener(GpsWorker.this);
				locMgr.removeUpdates(GpsWorker.this);
				log.info("Gps scan finished");
				WorkerService.gpsTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		locMgr.addGpsStatusListener(this);
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
		timer.start();
		log.info("Gps scan started for 180 sec");
		
		return START_NOT_STICKY;		
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub		
		Log.i("subtask", "gps location updated.");
		long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;		
		String corrdinates = String.valueOf(location.getLongitude())+"#"+String.valueOf(location.getLatitude())+"#"+String.valueOf(location.getAltitude());
		String accuracy = String.valueOf(location.getAccuracy());
		Entry eobj = new Entry();
		//eobj.setOB(ob);
        eobj.setTS(ts);
        //eobj.setGT(gt);
		eobj.setMT(Constants.STATUS_SENSOR_GPSCOORD);
		//eobj.setACC(AccListener.acc);
		eobj.setFP(corrdinates+"#"+accuracy);
		//eobj.setFPP(accuracy);
		//Gson gson = new Gson();
        //String gpsLoc = gson.toJson(eobj);
        WorkerService.gpsLocList.add(eobj);
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		Log.i("subtask", "gps prns updated.");
		long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
		gpsStatus =locMgr.getGpsStatus(null);
		if(gpsStatus != null && GpsStatus.GPS_EVENT_SATELLITE_STATUS == event){			
			//elist = new ArrayList<Entry>();
			String prns = "", snrs = "", fp = "";
			Iterable<GpsSatellite> iSatellites =gpsStatus.getSatellites();
            Iterator<GpsSatellite> it = iSatellites.iterator();
            while(it.hasNext()){
                if(!(prns.length()==0) && !(snrs.length()==0)){
                	prns += ",";
                	snrs += ",";                	
                }
                GpsSatellite oSat = (GpsSatellite) it.next();              
                prns += String.valueOf(oSat.getPrn());
                snrs += String.valueOf(oSat.getSnr());				
            }
            //if(!(prns.length()==0) && !(snrs.length()==0)){
            	fp = prns + "#" + snrs;
            	Entry eobj = new Entry();
                //eobj.setOB(ob);
                eobj.setTS(ts);
                //eobj.setGT(gt);
    			eobj.setMT(Constants.STATUS_SENSOR_GPS);
    			//eobj.setACC(AccListener.acc);
    			eobj.setFP(fp);
    			//eobj.setFPP(snrs);
                Log.i("subtask", "gps_prns: " + prns);
                Log.i("subtask", "gps_snrs: " + snrs);
                //Gson gson = new Gson();
                //String gpsSat = gson.toJson(eobj);
                WorkerService.gpsSatList.add(eobj);
                //new gpsTask().execute(eobj);
            //}
            
		}
	}
	
	/*private class gpsTask extends AsyncTask<Entry, Void, Void> {
		
		@Override
        protected Void doInBackground(Entry... eobjs) {
        	try {
            	if(db != null)
            		db.addEntry(eobjs[0]);
            	
            } catch (Exception e) {
              e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void unused) {
        	
        }
	}*/

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
