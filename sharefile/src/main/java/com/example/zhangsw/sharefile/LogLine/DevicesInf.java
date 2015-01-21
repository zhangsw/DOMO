package com.example.zhangsw.sharefile.LogLine;

public class DevicesInf {

	private String deviceName;
	private int connectStyle;
	private String deviceID;
	
	public DevicesInf(String name, String ID,int style){
		deviceName = name;
		connectStyle = style;
		deviceID = ID;
	}
	
	public String getName(){
		return deviceName;
	}
	
	public int getStyle(){
		return connectStyle;
	}
	
	public String getID(){
		return deviceID;
	}

}
