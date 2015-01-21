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

import java.io.IOException;

public class SharedMemoryService extends Service implements ConnectionChangeCallBack{

    private MemoryManager memManager;
    private String localID;
    private MyBinder myBinder = new MyBinder();
    private ConnectionChangeReceiver mConChangeReceiver;

    public boolean connect(String ip) throws IOException{
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

        //注册监听wifi
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



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
        //throw new UnsupportedOperationException("Not yet implemented");
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
        public SharedMemoryService getService(){
            return SharedMemoryService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            // TODO Auto-generated method stub
            return super.onTransact(code, data, reply, flags);
        }
    }

    public void wifiDisabled() {
        // TODO Auto-generated method stub
        System.out.println("wifi is disabled");
        networkDisabled();
    }

    public void wifiConnected() {
        // TODO Auto-generated method stub
        System.out.println("wifi is connected,local ip is " + getLocalAddress());
        String ip = getLocalAddress();
        if((localID == null) || !ip.equals(localID)){
            localID = ip;
            networkReconnectAll(ip);
        }

    }

    public void wifiDisconnected() {
        // TODO Auto-generated method stub
        System.out.println("wifi is disconnected");
        //wifi已经断开了当前连接，网络不可用
        networkDisabled();
    }


}