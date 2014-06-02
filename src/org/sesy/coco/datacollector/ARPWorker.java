package org.sesy.coco.datacollector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;



import org.sesy.coco.datacollector.database.Entry;
import org.sesy.coco.datacollector.log.ConfigureLog4J;
import org.sesy.coco.datacollector.net.NetInfo;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;

public class ARPWorker extends Service{

	private long start = 0;
	private long end = 0;
	private long ip = 0;
	private int size = 0;
	private int pt_move = 2; // 1=backward 2=forward
	private ExecutorService mPool;
	private Map<String,String> hostmap;
	private Map<String,Long> timemap;
	private Comparator<String> comparator;
	private int scanCounter;
	private CountDownTimer timer;	
	private Thread thr;
	//private int gt, ob;
	Logger log;
	
	@Override
	public void onCreate(){
		super.onCreate();
		
		log = Logger.getLogger(ARPWorker.class);  
        ConfigureLog4J.configure(this);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   
        //log.info("onCreate");		       
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		WorkerService.arpTask = true;
		//gt = intent.getIntExtra("gt", 0);
		//ob = intent.getIntExtra("ob", 0);
		
		WorkerService.cellList.clear();
		scanCounter = 0;
		
		log.info("Subtask ARP");
    	
		String ip_addr = NetInfo.getIPAddress();
		ip = NetInfo.getUnsignedLongFromIp(ip_addr);
		short cidr = NetInfo.getCidr();
		int shift = (32 - cidr);
		if (cidr < 31) {
            start = (ip >> shift << shift) + 1;
            end = (start | ((1 << shift) - 1)) - 1;
            size = (int) (end - start + 1);
        }
		
		comparator = new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				// TODO Auto-generated method stub
				long ip0 = NetInfo.getUnsignedLongFromIp(arg0);
				long ip1 = NetInfo.getUnsignedLongFromIp(arg1);
				if(ip0 > ip1)
					return 1;
				else if (ip0 < ip1)
					return -1;
				else
					return 0;
			}
			  
		};
		hostmap = new TreeMap<String,String>(comparator);
		timemap = new TreeMap<String,Long>(comparator);
				
		
		
		timer = new CountDownTimer(33*1000, 10*1000) {

			@Override
			public void onFinish() {
				// TODO Auto-generated method stub
				//if(scanCounter > 0 && scanCounter < 4){
					if (mPool != null) {
			            synchronized (mPool) {
			                mPool.shutdownNow();
			            }
			        }
					stopThread();
					hostmap.put(NetInfo.getIPAddress(), NetInfo.getMACAddress("wlan0"));
					if(!timemap.containsKey(NetInfo.getIPAddress()))
						timemap.put(NetInfo.getIPAddress(),SystemClock.elapsedRealtime() - WorkerService.metaTS);
					save();
				//}
				log.info("ARP scan finished");
				WorkerService.arpTaskDone = true;
				stopSelf();
			}

			@Override
			public void onTick(long millisUntilFinished) {
				// TODO Auto-generated method stub	
				if(scanCounter > 0 && scanCounter < 4){
					if (mPool != null) {
			            synchronized (mPool) {
			                mPool.shutdownNow();
			            }
			        }
					stopThread();
					hostmap.put(NetInfo.getIPAddress(), NetInfo.getMACAddress("wlan0"));
					if(!timemap.containsKey(NetInfo.getIPAddress()))
						timemap.put(NetInfo.getIPAddress(),SystemClock.elapsedRealtime() - WorkerService.metaTS);
					save();
				}
				if(scanCounter >= 0 && scanCounter <= 2){
					//startDiscovery();
					thr = new Thread(null, ARPTask, "RefreshARPTable");
					thr.start();
					scanCounter++;
					log.info("Subtask ARP scan started "+scanCounter);
				}
			}
			
		};
		
		timer.start();
		log.info("Subtask ARP scan started "+scanCounter);
		
		
		return START_NOT_STICKY;		
	}
	
	Runnable ARPTask = new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			startDiscovery();
		}
		
	};
	
	public synchronized void stopThread(){
    	if(thr != null){
    		Thread moribund = thr;
    		thr = null;
    		moribund.interrupt();
    	}
    }
	
	private void startDiscovery(){
		
		hostmap.clear();
		timemap.clear();
		getValidARPCache();
		refreshARP();
		
	}
	
	private void refreshARP(){
		pt_move = 2;
		mPool = Executors.newFixedThreadPool(Constants.THREADS);
        if (ip <= end && ip >= start) {
            log.info("ARP: Back and forth scanning");
            // gateway
            launch(start);

            // hosts
            long pt_backward = ip;
            long pt_forward = ip + 1;
            long size_hosts = size - 1;

            for (int i = 0; i < size_hosts; i++) {
                // Set pointer if of limits
                if (pt_backward <= start) {
                    pt_move = 2;
                } else if (pt_forward > end) {
                    pt_move = 1;
                }
                // Move back and forth
                if (pt_move == 1) {
                    launch(pt_backward);
                    pt_backward--;
                    pt_move = 2;
                } else if (pt_move == 2) {
                    launch(pt_forward);
                    pt_forward++;
                    pt_move = 1;
                }
            }
        } else {
            log.info("ARP: Sequential scanning");
            for (long i = start; i <= end; i++) {
                launch(i);
            }
        }
        mPool.shutdown();
        try {
            if(!mPool.awaitTermination(Constants.TIMEOUT_SCAN, TimeUnit.SECONDS)){
                mPool.shutdownNow();
                log.info("ARP: Shutting down pool");
                if(!mPool.awaitTermination(Constants.TIMEOUT_SHUTDOWN, TimeUnit.SECONDS)){
                    log.info("ARP: Pool did not terminate");
                }
            }
        } catch (InterruptedException e){
            mPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
	}
	
	private void launch(long i) {
        if(!mPool.isShutdown()) {
            mPool.execute(new CheckRunnable(NetInfo.getIpFromLongUnsigned(i), Constants.SOCKET_TIMEOUT));
        }
    }
	
	private class CheckRunnable implements Runnable {
		private String ipAddr;
	    private String macAddr;
        private int socket_timeout; //timeout in ms

        CheckRunnable(String addr, int timeout) {
        	this.ipAddr = addr;
        	this.macAddr = NetInfo.NOMAC;
            this.socket_timeout = timeout;
        }
        
        private void publish(){
        	if(!hostmap.containsKey(ipAddr)){
            	hostmap.put(ipAddr, macAddr);
            	timemap.put(ipAddr, SystemClock.elapsedRealtime() - WorkerService.metaTS);
            }else if(!hostmap.get(ipAddr).equals(macAddr)){
            	hostmap.put(ipAddr, macAddr);
            	timemap.put(ipAddr, SystemClock.elapsedRealtime() - WorkerService.metaTS);
            }
        }

        public void run() {
            //if(isCancelled()) {
              //  publish(null);
            //}
            //Log.e(TAG, "run="+addr);
            // Create host object
            try {
                InetAddress h = InetAddress.getByName(ipAddr);
                
                // Arp Check #1
                macAddr = NetInfo.getHardwareAddress(ipAddr);
                if(!NetInfo.NOMAC.equals(macAddr)){
                    publish();
                    return;
                }
                
                // Native InetAddress check
                if (h.isReachable(socket_timeout)) {
                    // Arp Check #2
	                macAddr = NetInfo.getHardwareAddress(ipAddr);
	                if(!NetInfo.NOMAC.equals(macAddr)){
	                    publish();
	                    return;
	                }
                }	                

                // TODO: Get ports from options
                Socket s = new Socket();
                for (int i = 0; i < Constants.DPORTS.length; i++) {
                    try {
                        s.bind(null);
                        s.connect(new InetSocketAddress(ipAddr, Constants.DPORTS[i]), socket_timeout);
                    } catch (IOException e) {
                    } catch (IllegalArgumentException e) {
                    } finally {
                        try {
                            s.close();
                        } catch (Exception e){
                        }
                    }
                }

                // Arp Check #3
                macAddr = NetInfo.getHardwareAddress(ipAddr);
                if(!NetInfo.NOMAC.equals(macAddr)){
                    publish();
                    return;
                }	                

            } catch (IOException e) {
            } 
        }
    }
	
	public void getValidARPCache() {
        try {
        	BufferedReader bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"), NetInfo.BUF);
            String line = "";  
            long dt = SystemClock.elapsedRealtime() - WorkerService.metaTS;
            while ((line = bufferedReader.readLine()) != null) {
            	String[] ipmac = line.split("[ ]+");
                if (!ipmac[0].matches("IP")) {
                    String ip = ipmac[0];
                    String mac = ipmac[3];
                    if (!NetInfo.NOMAC.equals(mac) && !hostmap.containsKey(ip)) {
                        hostmap.put(ip, mac);  
                        timemap.put(ip, dt); 
                    }                   
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
        }
    }
	
	private void save(){
		for(Map.Entry<String,String> entry : hostmap.entrySet()){
			Entry eobj = new Entry(timemap.get(entry.getKey()),Constants.STATUS_SENSOR_ARP, entry.getKey()+"#"+entry.getValue());
			WorkerService.arpList.add(eobj);  
        }  
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
