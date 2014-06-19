package org.sesy.coco.datacollector;

import org.apache.log4j.Logger;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefManager {
	// Shared Preferences
    private SharedPreferences pref, _pref;
     
    // Editor for Shared preferences
    private Editor editor, _editor;
     
    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;
    
    //Shared 
    private static final String PREF_NAME = "DataCollectionPref";
    
    // local UUID
    private static final String ID = "LocalUUID";
    
    // local Name
    private static final String NAME = "LocalName";
    
    // Bound device name
    private static final String BINDID = "BindUUID";
    
    // Bound device name
    private static final String BINDNAME = "BindName";
    
    // Bound device status
    private static final String PEERON = "PeerOn";
    
    // Overall status
    private static final String STATUS = "Status";
    private static final String CONN_STATUS = "ConnectionStatus";
    private static final String TASK_STATUS = "TaskStatus";
    private static final String USER_STATUS = "UserBlockStatus";
    private static final String SENSOR_STATUS = "SensorStatus";
    
    private static final String GTCOUNTER_C = "ColocationCounter";
    private static final String GTCOUNTER_N = "NoncolocationCounter";
    
    // End of block time
    private static final String BT_END = "BlockTimeEnd";
    
    // GroundTruth for current session
    private static final String GT = "GroundTruth";
    
    private static final String DAY = "Day";
    
    private static final String ICON = "Icon";
    
    private static final String AUTO_GT = "AutoGroundTruth";
    private static final String AUTO_END = "AutoScannerEndTime";
    
    private static final String TASK_COMMENT = "TaskComment";
    
    Logger log;
    
    
    // Constructor
    public PrefManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        _pref = PreferenceManager.getDefaultSharedPreferences(_context);
        _editor = _pref.edit();
        
        /*log = Logger.getLogger(PrefManager.class);  
        ConfigureLog4J.configure(context);  
        LogManager.getRootLogger().setLevel((Level)Level.DEBUG);   */
        //log.info("log configured.");
    }
    
    
    /**
     * update local UUID
     * */
    public void updateIcon(){
        
        editor.putBoolean(ICON, true);
         
        // commit changes
        editor.commit();
    }  
    
    /**
     * update local UUID
     * */
    public void updateUUID(String uuid){
        
        editor.putString(ID, uuid);
         
        // commit changes
        editor.commit();
    }  
    
    /**
     * update local Name
     * */
    public void updateName(String name){
        
        editor.putString(NAME, name);
         
        // commit changes
        editor.commit();
    } 
    
    /**
     * update bind
     * */
    public void updateBind(String id, String name, boolean sta){
        
    	// Storing bind uuid as string
        editor.putString(BINDID, id);
         
        // Storing bind name as string
        editor.putString(BINDNAME, name);
        
        // Storing bind status as boolean
        editor.putBoolean(PEERON, sta);
        
        // commit changes
        editor.commit();
    }
    
    public void registerAutoScanner(int gt){
    	editor.putLong(AUTO_END, System.currentTimeMillis() + 3*60*60*1000);
    	editor.putInt(AUTO_GT, gt);
    	editor.commit();
    }
    
    public void unregisterAutoScanner(){
    	editor.putLong(AUTO_END, System.currentTimeMillis());
    	editor.putInt(AUTO_GT, 0);
    	editor.commit();
    }
    
    public void updateColocationCounter(){
        
        editor.putInt(GTCOUNTER_C, pref.getInt(GTCOUNTER_C, 0) + 1);
         
        // commit changes
        editor.commit();
    }
    
    public void updateNoncolocationCounter(){
        
        editor.putInt(GTCOUNTER_N, pref.getInt(GTCOUNTER_N, 0) + 1);
         
        // commit changes
        editor.commit();
    }
    
    /**
     * update Status
     * */
    public void updateStatus(int status){
        
        editor.putInt(STATUS, status);
         
        // commit changes
        editor.commit();
    } 
    
    public void updateDay(int day){
        
        editor.putInt(DAY, day);
         
        // commit changes
        editor.commit();
    } 
    
    public void updateConnStatus(int status){
        
        editor.putInt(CONN_STATUS, status);
         
        // commit changes
        editor.commit();
    } 
    
    public void updateTaskStatus(int status){
        
        editor.putInt(TASK_STATUS, status);
         
        // commit changes
        editor.commit();
    } 
    
    public void updateTaskComment(String comment){
        
        editor.putString(TASK_COMMENT, comment);
         
        // commit changes
        editor.commit();
    } 
    
    public void updateSensorStatus(int status){
        
        editor.putInt(SENSOR_STATUS, status);
         
        // commit changes
        editor.commit();
    } 
    
    /**
     * update block time
     * */
    public void updateTimeBlock(int bhr){
        // Storing bhr value as ms
        editor.putLong(BT_END, System.currentTimeMillis() + bhr*60*60*1000);
         
        // commit changes
        editor.commit();
    }   
    
    /**
     * update prompt mode
     * */
    /*public void updatePromptBlock(boolean prompt){
        // Storing bhr value as ms
        _editor.putBoolean(Constants.PBLOCK, prompt);
         
        // commit changes
        _editor.commit();
    } */  
    
    /**
     * update GT
     * */
    public void updateGT(int gt){
        // Storing GT value as boolean
        editor.putInt(GT, gt);
         
        // commit changes
        editor.commit();
    }   
    
    /*public void updateAudioState(boolean aud){
        // Storing GT value as boolean
        _editor.putBoolean(Constants.KEY_PREF_AUD, aud);
         
        // commit changes
        _editor.commit();
    }  */
    
       
    
    /**
     * update modality set
     * */
    /*public void updateModality(TreeSet<String> mod){
    	Iterator<String> it=mod.iterator();
    	String s = "";
		if(it.hasNext()) s = (String)it.next();
		while(it.hasNext()){
			s += "," + (String)it.next();
		}
    	// Storing modality set as String with ',' as delimiter
        editor.putString(MOD, s);
         
        // commit changes
        editor.commit();
    } */  
    
    public boolean getIcon(){
		return pref.getBoolean(ICON, false);     
    }  
    
    /**
     * check UUID: 
     * @return UUID as string
     * */
    public String getUUID(){
		return pref.getString(ID, "");     
    }  
    
    public String getName(){
		return pref.getString(NAME, "");     
    } 
    
    public String getTaskComment(){
		return pref.getString(TASK_COMMENT, "");     
    } 
     
    public int getColocationCounter(){
    	return pref.getInt(GTCOUNTER_C, 0);
    }
    
    public int getNoncolocationCounter(){
    	return pref.getInt(GTCOUNTER_N, 0);
    }
    
    public String getBindID(){
		return pref.getString(BINDID, "");     
    } 
    
    public String getBindName(){
		return pref.getString(BINDNAME, "");     
    } 
    
    public boolean getBindPref(){
		return (pref.getString(BINDID, "") != "");     
    } 
    
    public boolean getPeerStatus(){
		return pref.getBoolean(PEERON, false);     
    }
    
    public int getStatus(){
		return pref.getInt(STATUS, Constants.STATUS_READY);     
    }
    
    public int getConnStatus(){
		return pref.getInt(CONN_STATUS, Constants.STATUS_CONN_READY);     
    }
    
    public int getTaskStatus(){
		return pref.getInt(TASK_STATUS, Constants.STATUS_TASK_IDLE);     
    }
    
    public int getSensorStatus(){
		return pref.getInt(SENSOR_STATUS, Constants.STATUS_SENSOR_NULL);     
    }
    
    public int getUserBlockStatus(){
    	int flag = Constants.STATUS_USER_NOBLOCK;
    	if(getTimeBlockState()) flag |= Constants.STATUS_USER_TIMEBLOCK;
    	if(getPromptBlockState()) flag |= Constants.STATUS_USER_PROMPTBLOCK;
		return flag;     
    }
    /**
     * check time block end: 
     * @return long
     * */
    public long getBTEnd(){
		return pref.getLong(BT_END, 0);
    }
    
    /*public String getBTEndFormatted(){
		return getTime(pref.getLong(BT_END, 0), "hh:mm:ss dd/MM/yyyy");
    }*/
    
    public String getBTLeft(){
    	long s = (pref.getLong(BT_END, 0) - System.currentTimeMillis())/1000;
    	int m = (int) (s/60);
    	s = s - m*60; 
    	int h = m/60;
    	m = m - h*60;
		return String.valueOf(h) + ":" + String.valueOf(m) + ":" + String.valueOf(s);
    }
    
    public String getAutoScannerLeft(){
    	long s = (pref.getLong(AUTO_END, 0) - System.currentTimeMillis())/1000;
    	int m = (int) (s/60);
    	s = s - m*60; 
    	int h = m/60;
    	m = m - h*60;
		return String.valueOf(h) + ":" + String.valueOf(m) + ":" + String.valueOf(s);
    }
    /**
     * check time block state: 
     * @return True - blocked, False - unblocked
     * */
    public boolean getTimeBlockState(){
		long endTime = pref.getLong(BT_END, 0);
		if(System.currentTimeMillis() < endTime)
			return true;
		else
			return false;        
    }  
    
    public boolean getAutoScannerState(){
		long endTime = pref.getLong(AUTO_END, 0);
		if(System.currentTimeMillis() < endTime)
			return true;
		else
			return false;        
    } 
    
    /**
     * check ground truth: 
     * @return True - colocated, False - non-colocated
     * */
    public int getGT(){
		return pref.getInt(GT, 0);     
    }  
    
    public int getAutoScannerGT(){
		return pref.getInt(AUTO_GT, 0);     
    }  
    
    public int getDay(){
		return pref.getInt(DAY, 0);     
    }
    
    /**
     * check prompt block
     * */
    public boolean getPromptBlockState(){
    	return _pref.getBoolean(Constants.PBLOCK, false);
    }  
    
    public boolean getRingtoneAllow(){
    	return _pref.getBoolean(Constants.RTALLOW, false);
    }
    
    public boolean getAlternativeIndicationAllow(){
    	return _pref.getBoolean(Constants.INALLOW, false);
    }
    
    public int getMorningMod(){
    	return Integer.parseInt(_pref.getString(Constants.KEY_PREF_MOR, "1"));
    }  
    
    public int getAfternoonMod(){
    	return Integer.parseInt(_pref.getString(Constants.KEY_PREF_AFT, "1"));
    }  
    
    public int getEveningMod(){
    	return Integer.parseInt(_pref.getString(Constants.KEY_PREF_EVE, "1"));
    }  
    
    public int getNightMod(){
    	return Integer.parseInt(_pref.getString(Constants.KEY_PREF_NIG, "0"));
    }  
    /**
     * check audio recording 
     * */
    /*public boolean getAudioState(){
    	return _pref.getBoolean(Constants.KEY_PREF_AUD, false);
    }  */
    
    /*private String getTime(long ms, String timeFormat){
    	DateFormat formatter = new SimpleDateFormat(timeFormat);
    	Calendar calendar = Calendar.getInstance();
    	calendar.setTimeInMillis(ms);
    	calendar.setTimeZone(TimeZone.getDefault());
    	return formatter.format(calendar.getTime());
    }*/
    
    /**
     * Gets the sensor mask of user preference.
     * @return sensor modality mask
     */
    public int getSensorPrefState(){
    	int res = Constants.STATUS_SENSOR_NULL;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_GPS, false))
    		res = res | Constants.STATUS_SENSOR_GPS;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_WIFI, false))
    		res = res | Constants.STATUS_SENSOR_WIFI;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_BT, false))
    		res = res | Constants.STATUS_SENSOR_BT;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_AUD, false))
    		res = res | Constants.STATUS_SENSOR_AUDIO;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_CELL, false))
    		res = res | Constants.STATUS_SENSOR_CELL;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_ARP, false))
    		res = res | Constants.STATUS_SENSOR_ARP;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_MAG, false))
    		res = res | Constants.STATUS_SENSOR_MAG;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_LIG, false))
    		res = res | Constants.STATUS_SENSOR_LIG;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_TEMP, false))
    		res = res | Constants.STATUS_SENSOR_TEMP;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_HUM, false))
    		res = res | Constants.STATUS_SENSOR_HUM;
    	if (_pref.getBoolean(Constants.KEY_PREF_SENSOR_BARO, false))
    		res = res | Constants.STATUS_SENSOR_BARO;
    	
    	return res;
    }
}
