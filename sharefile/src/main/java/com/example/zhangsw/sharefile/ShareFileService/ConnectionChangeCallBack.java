package com.example.zhangsw.sharefile.ShareFileService;

public interface ConnectionChangeCallBack {

    public void wifiDisabled();

    /**
     * 本地设备连接上了wifi
     */
    public void wifiConnected();

    /**
     * 本地设备断开了wifi
     */
    public void wifiDisconnected();

}
