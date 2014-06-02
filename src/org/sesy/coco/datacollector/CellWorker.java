package org.sesy.coco.datacollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CellWorker extends Service{
	
	private TelephonyManager tm;
	private int scanCounter;
    private String radioType;
    private String networkType;
    private String mcc;
    private String mnc;
    private List<NeighboringCellInfo> neighboringCellInfoList;
    private List<Cell> cellInfoList;
    private int cellId, lac, psc, signalStrength;

    private CountDownTimer timer;
    //private int gt, ob;
	Logger log;
    
	public static Map<Integer,String> networkTypeStr;
    static {
        networkTypeStr = new HashMap<Integer,String>();
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA");
        networkTypeStr.put(15, "HSPAP");	//NETWORK_TYPE_HSPAP
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_0, "EVDO_0");
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_EVDO_A, "EVDO_A");
        networkTypeStr.put(12, "EVDO_B");	//NETWORK_TYPE_EVDO_B
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_1xRTT, "1xRTT");
        networkTypeStr.put(14, "EHRPD");	//NETWORK_TYPE_EHRPD
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_IDEN, "IDEN");
        networkTypeStr.put(13, "LTE");	//NETWORK_TYPE_LTE
        networkTypeStr.put(TelephonyManager.NETWORK_TYPE_UNKNOWN, "UNKNOWN");
    }
    
    @Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(CellWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        
	}
    
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WorkerService.cellTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		
		WorkerService.cellList.clear();
		cellInfoList = new ArrayList<Cell>();
		scanCounter = 0;
		
		log.info("Subtask Cell");
    	
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE); 
				
		timer = new CountDownTimer(31*1000, 3000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				log.info("Cell scan finished");
				WorkerService.cellTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub
				if(scanCounter >= 0 && scanCounter <= 9){
					getCellInfos();
					scanCounter++;				
					log.info("Subtask Cell scan started "+scanCounter);
				}
			}
			
		};
		
		timer.start();
		log.info("Subtask Cell scan started "+scanCounter);
		
		
		return START_NOT_STICKY;		
	}
    
   
	private void getCellInfos(){
		long ts = SystemClock.elapsedRealtime() - WorkerService.metaTS;
		List<NeighboringCellInfo> list = tm.getNeighboringCellInfo();  
		if(!list.isEmpty()){
			for (NeighboringCellInfo i : list) {  
				int rssi = 0;
				switch(getNetworkClass(i.getNetworkType())){
				case 1:
					rssi = -113 + 2*(i.getRssi());	//GSM(TS 27.007): dBm = 2*ASU - 113, ASU in the range of 0..31 and 99
					break;
				case 2:
					rssi = -116 + i.getRssi();	//UMTS(TS 25.125): dBm = ASU - 116, ASU in the range of -5..91
					break;
				default:
					rssi = i.getRssi();
				}
	            Cell cell = new Cell(i.getCid(), i.getLac(), i.getPsc(), rssi, networkTypeStr.get(i.getNetworkType()));  
	            Entry eobj = new Entry();
				String fp = cell.toString();
				Log.i("Cell FP", "Subtask cell_fp: " + fp);
				//eobj.setOB(ob);
				eobj.setTS(ts);
				//eobj.setGT(gt);
				eobj.setMT(Constants.STATUS_SENSOR_CELL);
				eobj.setFP(fp);
		        WorkerService.cellList.add(eobj);  
	        }  
		}
    }
    
    private int getNetworkClass(int networkType) {
        switch (networkType) {
            case -1:
                return -1;	//NONE
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return 1;	//GSM
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            	return 2;	//UMTS
            case TelephonyManager.NETWORK_TYPE_LTE:
            	return 5;	//LTE
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return 3;	//CDMA
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return 4;	//IDEN
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
            default:
                return 0;	//UNKNOWN
        }
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
    
    private class Cell {
        private int cid;	// Cell ID
        private int lac;	// Location Area Code
        private int psc;
        //private int mcc;	// Mobile Country Code
        //private int mnc;	// Mobile Network Code
        private int rssi;	// Signal Strength in dBm
        private String radioType;	// GSM or CDMA
        //private String type;	// Neighbour 'n' or Connected 'c'
        
        public Cell(int cid, int lac, int psc, int rssi, String radioType){
        	this.cid = cid;
        	this.lac = lac;
        	this.psc = psc;
        	//this.mcc = mcc;
        	//this.mnc = mnc;
        	this.rssi = rssi;
        	this.radioType = radioType;
        	//this.type= type;
        }
        @Override
        public String toString(){
        	return cid+"#"+radioType+"#"+lac+"#"+psc+"#"+rssi;
        }
    }
    
    
    /*private class GSMCellInfo extends CellInfo {
        private int cellId;
        private int lac;
        private int mcc;
        private int mnc;
        private int psc;
        private int signalStrength;
        
        @Override
        public String toString(){
        	return cellId+"#"+lac+"#"+signalStrength+"#"+mcc+"#"+mnc+"#"+psc;
        }
    }
    
    private class CDMACellInfo extends CellInfo {
        private int cellId;
        private int networkId;
        private int systemId;
        private int longitude;
        private int latitude;
        private int signalStrength;
        
        @Override
        public String toString(){
        	return cellId+"#"+networkId+"#"+signalStrength+"#"+longitude+"#"+latitude+"#"+systemId;
        }
    }
    
    private class LTECellInfo extends CellInfo {
        private int cellId;
        private int tac;
        private int mcc;
        private int mnc;
        private int pci;
        private int signalStrength;
        
        @Override
        public String toString(){
        	return cellId+"#"+tac+"#"+signalStrength+"#"+mcc+"#"+mnc+"#"+pci;
        }
    }
    
    private class UMTSCellInfo extends CellInfo {
        private int cellId;
        private int lac;
        private int mcc;
        private int mnc;
        private int psc;
        private int signalStrength;
        
        @Override
        public String toString(){
        	return cellId+"#"+lac+"#"+signalStrength+"#"+mcc+"#"+mnc+"#"+psc;
        }
    }
    
    private class IDENCellInfo extends CellInfo {
        private int cellId;
        private int lac;
        private int mcc;
        private int mnc;
        private int psc;
        private int signalStrength;
        
        @Override
        public String toString(){
        	return cellId+"#"+lac+"#"+signalStrength+"#"+mcc+"#"+mnc+"#"+psc;
        }
    }*/
    
}
