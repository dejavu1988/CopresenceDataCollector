package org.sesy.coco.datacollector;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

public class PluginManager {
	private Context _context;
	//private BluetoothAdapter mBluetoothAdapter = null;
	//private WifiManager mainWifi = null;
	//Logger log;
	
	public PluginManager(Context context){
		this._context = context;
		
		/*log = Logger.getLogger(PluginManager.class);  
        ConfigureLog4J.configure(context);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   */
        //log.info("log configured.");
	}
	
	public boolean isGPSOn(){
		String provider = Settings.Secure.getString(_context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	    
		return provider.contains("gps");
	}
	
	public boolean isBTOn(){
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		return mBluetoothAdapter.isEnabled();
	}
	
	public boolean isWifiOn(){
		WifiManager mainWifi = (WifiManager) _context.getSystemService(Context.WIFI_SERVICE);
		
		return mainWifi.isWifiEnabled();
	}
	
	public boolean isCellAvailable() {       
	    TelephonyManager tel = (TelephonyManager) _context.getSystemService(Context.TELEPHONY_SERVICE); 
	    if(tel.getNetworkOperator() == null || tel.getSimState() != TelephonyManager.SIM_STATE_READY)
	    	return false;
	    else if(tel.getNetworkOperator().equals(""))
	    	return false;
	    else return true;      
	}
	
	
	
}
