package com.example.zhangsw.sharefile.PsyLine;

import java.util.ArrayList;

public class DetectConnection implements Runnable{

	private ArrayList <SocketIO> socketList;
	
	private boolean tag;
	
	public DetectConnection(ArrayList<SocketIO> list){
		socketList = list;
		tag = true;
	}
	
	public void run() {
		// TODO Auto-generated method stub
		while(tag){
			if(!socketList.isEmpty()){
				
			}
		}
	}

}
