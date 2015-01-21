package com.example.zhangsw.sharefile.ShareFileService;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import com.example.zhangsw.sharefile.MemoryManager.MemoryManager;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;




public class SharedMem extends Service implements ConnectionChangeCallBack{

	private MemoryManager memManager;
	private String localID;
	private List fileList;
	private String path;
	
	private MyBinder myBinder = new MyBinder();
	private ConnectionChangeReceiver mConChangeReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		
		return myBinder;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("enter service oncreate");
		try {
			localID = getLocalAddress();
			initialize();
			memManager = new MemoryManager(localID);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		path = FileConstant.DEFAULTSHAREPATH;
		mConChangeReceiver = new ConnectionChangeReceiver(this);
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		registerReceiver(mConChangeReceiver,filter);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mConChangeReceiver);
		memManager.stop();
	}
	
	/**
	 * 本地设备的wifi被关闭了
	 */
	public void wifiDisabled(){
		System.out.println("wifi is disabled");
		networkDisabled();
	}
	
	/**
	 * 本地设备连接上了wifi
	 */
	public void wifiConnected(){
		System.out.println("wifi is connected,local ip is " + getLocalAddress());
		String ip = getLocalAddress();
		if((localID == null) || !ip.equals(localID)){
			localID = ip;
			networkReconnectAll(ip);
		}
	}
	
	/**
	 * 本地设备断开了wifi
	 */
	public void wifiDisconnected(){
		System.out.println("wifi is disconnected");
		//wifi已经断开了当前连接，网络不可用
		networkDisabled();
	}
	
	
	public void readFile(String filename,String filePath){
		File file = new File(filePath);
	}
	
	public void writeFile(String filename){
		
	}

    public String getSharedPath(){
        return path;
    }
	
	
	private void initialize(){
		//初始化，创建文件夹
		FileOperateHelper.makeDir(FileConstant.DEFAULTAPPPATH);
		FileOperateHelper.makeDir(FileConstant.DEFAULTSHAREPATH);
		FileOperateHelper.makeDir(FileConstant.DEFAULTVERSIONLOGPATH);
		FileOperateHelper.makeDir(FileConstant.DEFAULTSAVEPATH);
	}
	
	/**
	 * 网络重新连接
	 */
	private void networkReconnectAll(String localIP){
		memManager.reconnectAll(localIP);
		System.out.println("reconnect to all device");
	}
	
	/**
	 * 网络被关闭
	 */
	private void networkDisabled(){
		localID = null;
		memManager.networkDisabled();	
	}
	
	private String getLocalMacAddress(){
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		return wifiInfo.getMacAddress();
	}
	
	private String getLocalAddress(){
		WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();    
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
	}

	public class MyBinder extends Binder{
		public SharedMem getService(){
			return SharedMem.this;
		}

		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
				int flags) throws RemoteException {
			// TODO Auto-generated method stub
			return super.onTransact(code, data, reply, flags);
		}
		
		public boolean connect(String ip) throws IOException{
            System.out.println("---SharedMem---connect "+ip);
			if(memManager == null) memManager = new MemoryManager(getLocalMacAddress());
            return memManager.connect(ip);
		}
		
		public boolean disconnect(String ip){
			if(memManager == null) return false;
			else{
				System.out.println("before enter memManager's disconnect device:" + ip);
				boolean tag = memManager.disconnect(ip);
				if(tag){
					System.out.println("has been disconnected with device: " + ip);
					return true;
				}
				return false;
			}
		}
		
		
	}
	
	
	
	
}

