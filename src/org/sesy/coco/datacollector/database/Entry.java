package org.sesy.coco.datacollector.database;

public class Entry {
	//Entry unit class
	//public static String uuid = "";	//local UUID
	//private int ob;	//observation number
	private long ts;	//local timestamp
	//private int gt;	// ground truth number
	private int mt;	//modality type name
	private String fp;	//fingerprint json
	
	//Constructor
	public Entry() {
		//this.ob = 0;
		this.ts = 0;
		//this.gt = 0;
		this.mt = 0;
		this.fp = "";
	}
	
	public Entry(long ts) {
		this.ts = ts;
		this.mt = 0;
		this.fp = "";
	}
	
	public Entry(long ts, int mt) {
		//this.ob = ob;
		this.ts = ts;
		//this.gt = gt;
		this.mt = mt;
		this.fp = "";
	}
	
	public Entry(long ts, int mt, String fp) {
		//this.ob = ob;
		this.ts = ts;
		//this.gt = gt;
		this.mt = mt;
		this.fp = fp;
	}
	
	public void setTS(long ts){
		this.ts = ts;
	}
	
	/*public void setOB(int ob){
		this.ob = ob;
	}
	
	public void setGT(int gt){
		this.gt = gt;
	}*/
	
	public void setMT(int mt){
		this.mt = mt;
	}
	
	public void setFP(String fp){
		this.fp = fp;
	}
	
	
	public long getTS() {
		return this.ts;
	}
	
	/*public int getOB() {
		return this.ob;
	}
	
	public int getGT() {
		return this.gt;
	}*/
	
	public int getMT() {
		return this.mt;
	}
	
	public String getFP() {
		return this.fp;
	}
	
}
