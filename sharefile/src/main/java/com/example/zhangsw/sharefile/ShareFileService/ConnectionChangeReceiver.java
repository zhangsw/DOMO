package com.example.zhangsw.sharefile.ShareFileService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Parcelable;

public class ConnectionChangeReceiver extends BroadcastReceiver{

    ConnectionChangeCallBack callBack;
    public ConnectionChangeReceiver(ConnectionChangeCallBack callBack){
        this.callBack = callBack;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){			//检测wifi是否打开或者关闭
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
            switch(wifiState){
                case WifiManager.WIFI_STATE_DISABLING:{

                }break;
                case WifiManager.WIFI_STATE_DISABLED:{
                    //wifi被关闭
                    callBack.wifiDisabled();
                }break;
                case WifiManager.WIFI_STATE_ENABLING:{

                }break;
                case WifiManager.WIFI_STATE_ENABLED:{

                }break;
                case WifiManager.WIFI_STATE_UNKNOWN:{

                }break;
                default:{

                }break;
            }
        }

        if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
            Parcelable parcelableExtra = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if(null != parcelableExtra){
                NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                State state = networkInfo.getState();
                if(state == State.CONNECTED){
                    callBack.wifiConnected();
                }else if(state == State.DISCONNECTED){
                    callBack.wifiDisconnected();
                }else if(state == State.CONNECTING){
                    System.out.println("wifi is connecting");
                }else if(state == State.DISCONNECTING){
                    System.out.println("wifi is disconnecting");
                }else if(state == State.SUSPENDED){
                    System.out.println("wifi is suspended");
                }else if(state == State.UNKNOWN){
                    System.out.println("wifi is unknown");
                }
            }
        }
    }

}
