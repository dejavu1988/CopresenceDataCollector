package org.sesy.coco.datacollector;

public final class Constants {
	
	// Status: ready, blocked, waiting for gt, scanning, server/peer communicating
	public static final int STATUS_READY = 1;
	public static final int STATUS_BLOCKED = 2;
	public static final int STATUS_WAITGT = 3;
	public static final int STATUS_SCAN = 4;
	public static final int STATUS_COM = 5;
	
	// Connection Status: ok, peer off, server off, network off
	public static final int STATUS_CONN_READY = 1;
	public static final int STATUS_CONN_PEEROFF = 2;
	public static final int STATUS_CONN_SERVEROFF = 3;
	public static final int STATUS_CONN_NETWORKOFF = 4;
	public static final int STATUS_CONN_TIMEOUT = 5;

	// Task Status: scanning, uploading, binding/triggering/heartbeat/timeout reseting, idle
	public static final int STATUS_TASK_SCAN = 1;
	public static final int STATUS_TASK_UPLOAD = 2;
	public static final int STATUS_TASK_MSG = 3;
	public static final int STATUS_TASK_IDLE = 4;
	
	// User Block Status: no blocks, time block, prompt block
	public static final int STATUS_USER_NOBLOCK = 0;
	public static final int STATUS_USER_TIMEBLOCK = 1;
	public static final int STATUS_USER_PROMPTBLOCK = 2;
	
	// Sensor Status: null sensor, gps, wifi, bluetooth, audio...
	public static final int STATUS_SENSOR_NULL = 0;
	public static final int STATUS_SENSOR_GPS = 1;
	public static final int STATUS_SENSOR_WIFI = 2;
	public static final int STATUS_SENSOR_BT = 4;
	public static final int STATUS_SENSOR_AUDIO = 8;
	public static final int STATUS_SENSOR_GPSCOORD = 16;
	//public static final int STATUS_SENSOR_ACC = 32;
	public static final int STATUS_SENSOR_CELL = 32;
	public static final int STATUS_SENSOR_ARP= 64;
	public static final int STATUS_SENSOR_MAG= 128;
	public static final int STATUS_SENSOR_LIG= 256;
	//public static final int STATUS_SENSOR_GRAV= 1024;
	public static final int STATUS_SENSOR_TEMP= 512;
	public static final int STATUS_SENSOR_HUM= 1024;
	public static final int STATUS_SENSOR_BARO= 2048;
	//public static final int STATUS_SENSOR_ORI= 16384;
	//public static final int STATUS_SENSOR_GYRO= 32768;
	//public static final int STATUS_SENSOR_ROT= 65536;
	//public static final int STATUS_SENSOR_LACC= 131072;
	//public static final int STATUS_SENSOR_PROX= 262144;
	
	// Sensor Status: sensor combinations
	//public static final int STATUS_SENSOR_GWB = 7;
	//public static final int STATUS_SENSOR_GWBA = 15;
	public static final int STATUS_SENSOR_GWBAC = 79;
	
	// Scan params
	public static final int AUDIO_PERIOD = 10;
	public static final int BT_LOCAL_RSSI = -40;
	
	// ARP scan params
	protected final static int[] DPORTS = { 139, 445, 22, 80 };
	protected final static int TIMEOUT_SCAN = 9; // seconds
	protected final static int TIMEOUT_SHUTDOWN = 2; // seconds
	protected final static int THREADS = 10; //FIXME: Test, plz set in options again ?
	protected final static int SOCKET_TIMEOUT = 500; // socket timeout in ms
	
	// Server address
	public static final String SERVER_INET = "54.229.32.28"; 	
	public static final String UPLOAD_SERVER_INET = "54.229.32.28"; 
	public static final String UPLOAD_SERVER_DIR = "/dc/server/upload4.php";
	public static final String UPLOAD_SERVER_LOG_DIR = "/dc/server/uploadlog4.php";
	public static final int SERVER_PORT = 8897; 
    
	// Message Ids
    public static final String REQ_UUID = "REQ_UUID";
    public static final String ACK_UUID = "ACK_UUID";
    public static final String UP_BIND = "UP_BIND";
    public static final String REQ_GETQ = "REQ_GETQ";
    public static final String ACK_GETQ = "ACK_GETQ";
    public static final String REQ_VALQ = "REQ_VALQ";
    public static final String ACK_VALQ = "ACK_VALQ";
    public static final String REQ_ALIVE = "REQ_ALIVE";
    public static final String ACK_ALIVE = "ACK_ALIVE";
    public static final String REQ_UNBIND = "REQ_UNBIND";
    public static final String ACK_UNBIND = "ACK_UNBIND";
    public static final String REQ_TASK = "REQ_TASK";
    public static final String ACK_TASK = "ACK_TASK";
    public static final String REQ_AGREE = "REQ_AGREE";
    public static final String ACK_AGREE = "ACK_AGREE";
    public static final String SEND = "SEND";
    
    // Preference items
    public static final String KEY_PREF_AUD = "cbox_aud";
	public static final String KEY_PREF_MOD = "cbox_mod";
	public static final String KEY_PREF_MOR = "list_mor";
	public static final String KEY_PREF_AFT = "list_aft";
	public static final String KEY_PREF_EVE = "list_eve";
	public static final String KEY_PREF_NIG = "list_nig";
	public static final String KEY_PREF_BHR = "list_bhr";
	public static final String PBLOCK = "cbox_pbl";
	public static final String RTALLOW = "cbox_rtg";
	public static final String INALLOW = "cbox_wid";
	public static final String KEY_PREF_VER = "ver_pref";
	public static final String KEY_PREF_ID = "id_pref";
	
	// Preference items for sensors
	public static final String KEY_PREF_SENSOR_GPS = "cbox_gps";
	public static final String KEY_PREF_SENSOR_WIFI = "cbox_wifi";
	public static final String KEY_PREF_SENSOR_BT = "cbox_bt";
	public static final String KEY_PREF_SENSOR_AUD = "cbox_audio";
	public static final String KEY_PREF_SENSOR_CELL = "cbox_cell";
	public static final String KEY_PREF_SENSOR_ARP = "cbox_arp";
	public static final String KEY_PREF_SENSOR_MAG = "cbox_mag";
	public static final String KEY_PREF_SENSOR_LIG = "cbox_lig";
	public static final String KEY_PREF_SENSOR_TEMP = "cbox_temp";
	public static final String KEY_PREF_SENSOR_HUM = "cbox_hum";
	public static final String KEY_PREF_SENSOR_BARO = "cbox_baro";
}
