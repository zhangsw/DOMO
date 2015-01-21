package com.example.zhangsw.sharefile.FileMonitor;

public interface IEventTranslate {

	public static final int FILEMODIFIED = 1;
	
	public static final int FILERENAMEORMOVE = 2;
	
	public static final int FILEMOVEFROM = 3;
	
	public static final int FILEMOVETO = 4;
	
	public static final int FILEDELETE = 5;
	
	public static final int DIRCREATE = 6;
	
	public static final int DIRRENAMEORMOVE = 7;
	
	public static final int DIRMOVEFROM = 8;
	
	public static final int DIRMOVETO = 9;
	
	public static final int DIRDELETE = 10;
	
	public static final int COVERFILE = 11;
	
	public static final int ISFILE = 100;
	
	public static final int ISDIR = 200;
	
	public int translate(String path, int type);
	
	public String getOldPath();
	
	public String getNewPath();
}
