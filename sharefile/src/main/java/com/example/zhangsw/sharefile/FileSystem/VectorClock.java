package com.example.zhangsw.sharefile.FileSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 保存了各个设备以及同其相对应的版本号
 * @author zhangsw
 *
 */
public class VectorClock implements Serializable{
    //用于保存设备id同其版本号
    public static final int EQUAL = 0;
    public static final int GREATER = 1;
    public static final int LESSER = 2;
    public static final int UNDEFINED = 4;

    private HashMap<String,Integer> versionMap;

    public VectorClock(){
        versionMap = new HashMap<>();
    }

    /**
     * 添加设备及其版本号，若设备已经存在，则更新其版本号
     * @param deviceId	设备号
     * @param versionNumber	版本号
     * @return	同之前该设备相对应的版本号，如果之前不存在，则为null
     */
    public synchronized Integer put(String deviceId,Integer versionNumber){
        return versionMap.put(deviceId, versionNumber);
    }

    /**
     * 删除设备及其版本号
     * @param deviceId
     */
    public synchronized void remove(String deviceId){
        versionMap.remove(deviceId);
    }

    /**
     * 获取设备的版本号
     * @param deviceId	设备号
     * @return 版本号
     */
    public Integer getVersionNumber(String deviceId){
        return versionMap.get(deviceId);
    }


    /**
     * 合并两个versionMap，合并规则利用vector clock
     * @param m
     */
    public synchronized void merge(VectorClock m){
        for (Entry<String, Integer> entry : m.versionMap.entrySet()) {
            Integer i = versionMap.get(entry.getKey());
            if (i == null || i < entry.getValue())
                versionMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 比较versionMap的大小
     * @param m 需要比较的对象
     * @return 比较结果，0表示相等，1表示大于，2表示小于，4表示不确定
     */
    public int compareTo(VectorClock m){
        int result = -1;
        for (Entry<String, Integer> entry : versionMap.entrySet()) {
            Integer remote = m.versionMap.get(entry.getKey());
            if (remote != null) {
                if (entry.getValue() > remote) {
                    if (result == -1 || result == EQUAL)
                        result = GREATER;
                    else if (result == LESSER)
                        return UNDEFINED;
                } else if (entry.getValue() < remote) {
                    if (result == -1 || result == EQUAL)
                        result = LESSER;
                    else if (result == GREATER)
                        return UNDEFINED;
                } else {
                    if (result == -1)
                        result = EQUAL;
                }
            }
        }
        return result;
    }


}
