package com.example.zhangsw.sharefile.LogLine;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.MemoryManager.MemoryManager;
import com.example.zhangsw.sharefile.PsyLine.PsyLine;
import com.example.zhangsw.sharefile.Util.FileConstant;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LogLine {
    private PsyLine psyLine;
    private MemoryManager memoryManager;
    private List <DevicesInf>devices;				//style分为二种，0:TCP,1:bluetooth,
    /**用于保存设备连接信息的备份*/
    private HashMap <String,DevicesInf>devicesBackUp;
    private static final int DURATION = 20;
    private Runnable heartBeat;



    public LogLine(MemoryManager c) throws IOException{
        psyLine = new PsyLine(this);
        devices = new ArrayList<>();
        devicesBackUp = new HashMap<>();
        memoryManager = c;
        heartBeat = new HeartBeat(DURATION);
        new Thread(heartBeat).start();

    }

    /** 通过ip建立设备间的连接(TCP连接)
     * @throws IOException */
    public boolean connect(String ip) throws IOException{

        return psyLine.connect(ip);

    }

    /**
     * 同之前连接的设备重新建立连接
     * @param localIP
     * @throws IOException
     */
    public void reconnectAll(String localIP){
        synchronized(devices){
            if(devices.size()>0){
                for(int i=0;i<devices.size();i++){
                    devicesBackUp.put(devices.get(i).getName(), devices.get(i));
                }
                devices.clear();
            }
        }
        psyLine.abandonAllConnection();

        for (Entry<String, DevicesInf> entry : devicesBackUp.entrySet()) {
            DevicesInf di = entry.getValue();
            switch (di.getStyle()) {
                case 0: {
                    try {
                        connect(di.getID());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                break;
                default: {

                }
            }
        }
    }

    /** 通过蓝牙进行设备间的连接*/
    public void connect(){

    }

    /**
     * 同对象设备断开连接
     * @param target 需要断开连接的设备名
     */
    public boolean disconnect(String target){
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            System.out.println("before enter psyline's disconnect");
            if(psyLine.disconnect(di.getID())){
                System.out.println("psyline has disconnected");
                devicesBackUp.put(target, di);
                devices.remove(index);
                return true;
            }
            else return false;
        }
        else return false;
    }

    public boolean sendFileInf(String target,String relativePath,String MD5){
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            return psyLine.sendFileInf(di.getID(),relativePath,MD5);
        }
        return false;
    }

    public void sendFile(String target, FileMetaData metaData,
                         String absolutePath) {
        // TODO Auto-generated method stub
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            psyLine.sendFile(di.getID(), metaData, absolutePath);
        }
    }

    public boolean makeDir(String target,String relativePath){
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            return psyLine.makeDir(di.getID(), relativePath);
        }
        return false;

    }


    public boolean deleteFile(String target,String relativeFilePath){
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            return psyLine.deleteFile(di.getID(), relativeFilePath);
        }
        return false;
    }

    public boolean receiveDeleteFile(String targetIp, String filepath){
        int index = getIndexByID(targetIp);
        if(index != -1){
            return memoryManager.receiveDeleteFile(devices.get(index).getName(), filepath);
        }
        return false;
    }

    public boolean receiveFileInf(String targetIp, String relativePath, String absolutePath, String MD5) {
        int index = getIndexByID(targetIp);
        System.out.println("target ip is "+targetIp);
        if(index != -1){
            System.out.println("index is not -1");
            return memoryManager.receiveFileInf(devices.get(index).getName(), relativePath, absolutePath, MD5);
        }
        return false;
    }

    public boolean receiveAskFile(String targetIp, String relativePath,String absolutePath) {
        int index = getIndexByID(targetIp);
        if(index != -1){
            return memoryManager.receiveAskFile(devices.get(index).getName(), relativePath, absolutePath);
        }
        return false;
    }

    public boolean receiveRenameFile(String targetIp, String oldPath,String newPath) {
        int index = getIndexByID(targetIp);
        if(index != -1){
            return memoryManager.receiveRenameFile(devices.get(index).getName(),oldPath,newPath);
        }
        return false;
    }

    public boolean receiveMakeDir(String targetIp, String absolutePath) {
        int index = getIndexByID(targetIp);
        if(index != -1){
            System.out.println("before enter consistency receiveMakeDir,absolutePath is "+ absolutePath);
            return memoryManager.receiveMakeDir(devices.get(index).getName(), absolutePath);
        }
        return false;
    }

    /**
     * 发送文件的version,包括map和meta data
     * @param target	目标
     * @param fileID	文件的id
     * @param VectorClock
     * @param relativePath	文件的相对路径
     */
    public void sendFileVersion(String target, FileMetaData metaData,
                                VectorClock VectorClock, String relativePath,String tag) {
        // TODO Auto-generated method stub
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            psyLine.sendFileVersion(di.getID(), metaData,VectorClock,relativePath,tag);
        }
    }

    public void sendFileVectorClock(String target, String fileID,
                                    VectorClock VectorClock, String relativePath, String tag) {
        // TODO Auto-generated method stub
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            psyLine.sendFileVectorClock(di.getID(), VectorClock,relativePath,tag);
        }
    }

    public void receiveFileData(String targetIp, FileMetaData fileMetaData,
                                File file) {
        // TODO Auto-generated method stub
        int index = getIndexByID(targetIp);
        if(index != -1)
            memoryManager.receiveFileData(devices.get(index).getName(), fileMetaData, file);
    }

    /**
     * 接收到文件的VectorClock
     * @param targetIp	目标
     * @param VectorClock
     * @param fileID	文件的id
     * @param relativePath	文件的相对路径
     * @return
     */

    public boolean receiveVectorClock(String targetIp,VectorClock VectorClock,String fileID,String relativePath,String tag){
        int index = getIndexByID(targetIp);
        if(index != -1){
            Assert.assertNotNull("----LogLine----Error,VectorClock is null",VectorClock);
            return memoryManager.receiveVectorClock(devices.get(index).getName(),fileID, VectorClock, relativePath,tag);
        }
        else return false;
    }

    /**
     * 接收到文件的version
     * @param targetIp
     * @param VectorClock
     * @param metaData
     * @param relativePath
     * @param tag
     * @return
     */
    public boolean receiveVersion(String targetIp, VectorClock VectorClock,
                                  FileMetaData metaData, String relativePath, String tag) {
        // TODO Auto-generated method stub
        int index = getIndexByID(targetIp);
        if(index != -1){
            return memoryManager.receiveVersion(devices.get(index).getName(),VectorClock,metaData, relativePath, tag);
        }
        else return false;
    }

    /**
     * 收到文件的更新
     * @param ip
     * @param fileMetaData
     */
    public void receiveFileUpdate(String ip, FileMetaData fileMetaData) {
        // TODO Auto-generated method stub
        int index = getIndexByID(ip);
        if(index != -1)
            memoryManager.receiveFileUpdate(devices.get(index).getName(),fileMetaData);
    }

    /**
     * 收到断连信息
     * @param ip
     */
    public void receiveDisconnect(String ip) {
        // TODO Auto-generated method stub
        int index = getIndexByID(ip);
        if(index != -1){
            DevicesInf di = devices.get(index);
            memoryManager.receiveDisconnect(di.getName());
            devicesBackUp.put(di.getName(),di);
            devices.remove(index);
        }
    }


    /**从目标为target的设备请求文件*/
    public boolean fetchFile(String target,final String relativePath){

        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            return psyLine.fetchFile(di.getID(),di.getStyle(), relativePath);
        }
        else return false;

    }

	/*
	给所有同本设备相连接的设备发送文件
	public boolean sendFileEve(String filePath, String fileName){
		for(int i=0;i<devices.size();i++){
			DevicesInf temp = (DevicesInf)devices.get(i);
			if(temp.getStyle() == 0)
				psyLine.sendFile(temp.getID(), filePath, fileName);
			else
				return true;					//暂时不考虑蓝牙
		}
		return true;
	}*/

    /**向目标target发送修改文件名字信息*/
    public boolean renameFile(String target, String relativeFilePath,String newRelativeFilePath) {
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            return psyLine.renameFile(di.getID(),relativeFilePath,newRelativeFilePath);
        }
        else return false;

    }

    public void sendFileUpdateInform(List<String> targets,
                                     FileMetaData fileMetaData) {
        // TODO Auto-generated method stub
        List <String>deviceId = new ArrayList<>();
        for(int i=0;i<targets.size();i++){
            int index = getIndexByName(targets.get(i));
            if(index != -1)
                deviceId.add(devices.get(index).getID());
        }
        if(deviceId.size()>0)
            psyLine.sendFileUpdateInform(deviceId,fileMetaData);
    }

    /**
     * 发送同步就绪消息
     * @param target
     */
    public void sendSynReady(String target) {
        // TODO Auto-generated method stub
        int index = getIndexByName(target);
        if(index != -1){
            DevicesInf di = devices.get(index);
            psyLine.sendSynReady(di.getID());
        }
    }

    public void receiveSynReady(String id){
        int index = getIndexByID(id);
        if(index != -1){
            DevicesInf di = devices.get(index);
            memoryManager.receiveSynReady(di.getName());
        }
        else
        {
            System.out.println("----logline----receive syn ready---device not existed");
        }
    }

    /**向所有同本设备相连接的设备请求文件*/
    public boolean fetchFile(final String fileName){
        return true;
    }

    public  void addDevice(String name, String ip, int style){
        System.out.println("enter logline addDevice---");
        DevicesInf temp = new DevicesInf(name,ip,style);
        if(!devices.contains(temp)){
            devices.add(temp);
            memoryManager.addShareDevice(FileConstant.DEFAULTSHAREPATH, name, 0);
        }
    }


    public synchronized void removeDevice(String id){
        int index = getIndexByID(id);
        if(index != -1){
            DevicesInf di = devices.get(index);
            devices.remove(index);
            memoryManager.removeShareDevice(di.getName());
        }

    }

    /**
     * 向所有连接的设备发送heartbeat
     */
    private synchronized void sendHeartBeat(){
        if(devices.size() > 0){
            psyLine.sendHeartBeat();
        }
    }

    private int getIndexByName(String target){
        int i = 0;
        System.out.println("----LogLine----devices size is "+devices.size());
        for(;i<devices.size();i++){
            if((devices.get(i)).getName().equals(target))
                return i;
        }
        return -1;
    }

    private int getIndexByID(String ID){
        int i = 0;
        for(;i<devices.size();i++){
            //System.out.println(((DevicesInf)(devices.get(i))).getID());
            if((devices.get(i)).getID().equals(ID)){
                return i;
            }
        }
        return -1;
    }

    public String getDeviceNameByID(String ID){
        int i = getIndexByID(ID);
        if(i != -1){
            DevicesInf dif = devices.get(i);
            return dif.getName();
        }
        else return null;
    }

    private void stopHeartBeat(){
        ((HeartBeat)heartBeat).stop();
    }





    class HeartBeat implements Runnable{

        private long duration;
        private boolean tag;

        public HeartBeat(long duration){
            this.duration = duration;
            tag = true;
        }

        public void run() {
            // TODO Auto-generated method stub
            while(tag){

                try {
                    Thread.sleep(duration*1000);
                    sendHeartBeat();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public void stop(){
            tag = false;
        }

    }
}
