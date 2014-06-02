package org.sesy.coco.datacollector.plugin;

public interface PlugInterface {
	
	public String getSensorData();
	// @return: JSON string with data entry: Timestamp|Fingerprint_structure_string
	
	public double getDistance(String json1, String json2);
	// @param: JSON strings within identical time period
	// @return: unified distance (0-1) between two data Objects,
	// data Objects can be transformed to&from JSON strings using GSON library
	
	public int checkPluginState();
	// @return: integer defined for states of sensors: 0 - not supported, 1 - supported but not enabled, 2 - enabled
	
	public String getPluginID();
	// @return: the identity of plugin (technique) such as "GPS", "WIFI", "Audio"
}
