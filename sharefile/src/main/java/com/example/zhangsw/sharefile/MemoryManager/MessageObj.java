package com.example.zhangsw.sharefile.MemoryManager;

public class MessageObj {
	private int Type;
	private String target;
	private String fileName;
	private String MD5;
	private String filepath;
	private String relativeFilepath;
	private String newRelativeFilepath;
	
	public MessageObj(int type){
		Type = type;
	}
	
	public int getType(){
		return Type;
	}
	
	public void setTarget(String target){
		this.target = target;
	}
	
	public String getTarget(){
		return target;
	}
	
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public void setMD5(String MD5){
		this.MD5 = MD5;
	}
	
	public String getMD5(){
		return MD5;
	}
	
	public void setFilepath(String filepath){
		this.filepath = filepath;
	}
	
	public String getFilepath(){
		return filepath;
	}
	
	public void setRelativeFilepath(String relativeFilepath){
		this.relativeFilepath = relativeFilepath;
	}
	
	public String getRelativeFilepath(){
		return relativeFilepath;
	}
	
	public void setNewRelativeFilepath(String newRelativeFilepath){
		this.newRelativeFilepath = newRelativeFilepath;
	}
	
	public String getNewRelativeFilepath(){
		return newRelativeFilepath;
	}
	
}
