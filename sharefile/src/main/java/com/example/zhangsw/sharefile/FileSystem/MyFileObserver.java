package com.example.zhangsw.sharefile.FileSystem;

import android.os.Handler;

import com.example.zhangsw.sharefile.FileMonitor.SDFileObserver;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;
import com.example.zhangsw.sharefile.Util.FileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 文件类，用于管理一个文件，包含了文件的监控器，文件的版本，共享该文件的对象，以及文件的父节点及子节点
 * @author zhangsw
 *
 */
public class MyFileObserver{
    private SDFileObserver observer;
    private String path;
    private HashMap <String,Handler> mTargets;
    private ArrayList<MyFileObserver> lChildObserver;
    private MyFileObserver fatherObserver;
    private IFileManager iFOManager;

    private Handler globalMessageHandler;

    private VersionManager versionManager; 		//文件版本控制单元，包含了文件历史，版本号等

    /**whether file is opened*/
    private boolean isFileOpened = false;

    /**
     *
     * @param path		文件的路径
     * @param localDeviceId		本地设备的id
     * @param globalMessageHandler		全局消息处理handler
     * @param i	文件管理接口
     * @param fatherObserver	父节点
     */
    public MyFileObserver(String path,String localDeviceId,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
        this.path = path;
        if(fatherObserver != null && fatherObserver.hasTarget()){
            mTargets = new HashMap<>(fatherObserver.getTargetsAll());
        }
        else{
            mTargets = new HashMap<>();
        }
        lChildObserver = new ArrayList<>();
        iFOManager = i;
        this.fatherObserver = fatherObserver;
        this.globalMessageHandler = globalMessageHandler;
        //添加对文件的监控
        if(FileOperateHelper.fileExist(path)){			//文件存在，才进行监控
            observer = new SDFileObserver(path,globalMessageHandler);
            observer.startWatching();
        }
        initializeVersionManager(localDeviceId,path,mTargets);
    }

    public MyFileObserver(String path,String target,Handler handler,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
        this.path = path;
        mTargets = new HashMap<>();
        lChildObserver = new ArrayList<>();
        mTargets.put(target, handler);
        iFOManager = i;
        this.fatherObserver = fatherObserver;
        this.globalMessageHandler = globalMessageHandler;
        //observer = new SDFileObserver(path,handler);
        initializeVersionManager();
    }

    public MyFileObserver(String path,Handler globalMessageHandler,IFileManager i,MyFileObserver fatherObserver){
        this.path = path;
        this.globalMessageHandler = globalMessageHandler;
        this.fatherObserver = fatherObserver;
        lChildObserver = new ArrayList<>();
        mTargets = new HashMap<>(fatherObserver.getTargetsAll());
        iFOManager = i;

        initializeVersionManager();

    }


    private void initializeVersionManager(){
        //从本地数据库或者文件中查看，是否有该文件的版本信息
        if(versionExist()){			//存在版本信息
            versionManager = getVersion();
        }
        else{			//不存在版本信息

        }
    }


    //初始化版本结点
    private void initializeVersionManager(String localDeviceId,String path,HashMap<String,Handler> targets){
        //从本地数据库或者文件中查看，是否有该文件的版本信息
        if(versionExist()){			//存在版本信息
            versionManager = getVersion();
        }
        else{			//不存在版本信息
            versionManager = new VersionManager(localDeviceId,path);
            if(targets.size() > 0){
                for (Entry<String, Handler> entry : mTargets.entrySet()) {
                    versionManager.addDevice(entry.getKey());
                }
            }
        }
    }

    private boolean versionExist(){
        return false;
    }

    //获取该文件（夹）的版本信息
    private VersionManager getVersion(){
        //TODO
        return new VersionManager();
    }

    public int getFileVersion(){
        return versionManager.getFileVersion();
    }

    //获取本地存储的id设备的版本号
    public int getVersionNumber(String deviceId){
        return versionManager.getVersionNumber(deviceId);
    }

    public void updateVersionNumber(String deviceId,Integer versionNumber){
        versionManager.updateVersionNumber(deviceId,versionNumber);
    }

    public void updateVectorClock(String deviceId,Integer versionNumber){
        versionManager.updateVectorClock(deviceId, versionNumber);
    }

    public void updateVectorClock(VectorClock VectorClock){
        versionManager.updateVectorClock(VectorClock);
    }


    /**
     * 设置文件的VectorClock
     * @param VectorClock
     */
    public void setVectorClock(VectorClock VectorClock){
        versionManager.setVectorClock(VectorClock);
    }
    /**
     * 获取文件的版本Map
     * @return
     */
    public VectorClock getVectorClock(){
        return versionManager.getVectorClock();
    }

    public void setModifiedTime(long time){
        versionManager.getFileMetaData().setModifiedTime(time);
    }

    public long getModifiedTime(){
        return versionManager.getFileMetaData().getModifiedTime();
    }


    /**
     * 获取文件的metaData
     * @return
     */
    public FileMetaData getFileMetaData(){
        return versionManager.getFileMetaData();
    }

    /**
     * 设置文件的metaData
     * @param metaData
     * @return
     */
    public boolean setFileMetaData(FileMetaData metaData){
        versionManager.setFileMetaData(metaData);
        return true;
    }

    //文件被修改，需要更新version
    public void fileModified(String deviceId){
        versionManager.updateVersionNumber(deviceId);
    }

    public void startWatching(){
        if(FileOperateHelper.fileExist(path)){
            if(observer == null){
                observer = new SDFileObserver(path,globalMessageHandler);
                observer.startWatching();
            }
            else{
                System.out.println("----MyFileObserver----start watching----");
                //observer.stopWatching();
                observer.startWatching();
            }
        }
    }


    public void stopWatching(){
        for (MyFileObserver o : lChildObserver) {
            o.stopWatching();
        }
        if(observer != null){
            observer.stopWatching();
        }
    }

    public void modifyPath(String path){
        iFOManager.updateObserverMap(this.path, path);
        this.path = path;
        if(observer != null){//可能observer还没初始化，这是有可能的，当刚开始创建的是emptynode时，即要监控的文件还不存在时，observer就是空的
            observer.updatePath(path);
        }
        //更新metaData中的relativePath
        versionManager.getFileMetaData().setRelativePath(path.substring(FileConstant.DEFAULTSHAREPATH.length()));

        for (MyFileObserver o : lChildObserver) {
            //System.out.println("has child");
            String name = FileUtil.getFileNameFromPath(o.getPath());
            o.modifyPath(path + "/" + name);
            //System.out.println(path + "/" + name);
        }

    }

    public String getPath(){
        return path;
    }

    public boolean hasFather(){
        return fatherObserver != null;
    }

    public void setFather(MyFileObserver observer){
        fatherObserver = observer;
    }

    public MyFileObserver getFather(){
        return fatherObserver;
    }

    public boolean hasChild(){
        return !lChildObserver.isEmpty();
    }

    public ArrayList<MyFileObserver> getChildAll(){
        return lChildObserver;
    }

    public HashMap<String,Handler> getTargetsAll(){
        return mTargets;
    }

    /**
     * 获得共享该文件的所有对象
     * @return
     */

    public List<String> getTargetsList(){
        List <String>targets = new ArrayList<>();
        for (Entry<String, Handler> entry : mTargets.entrySet()) {
            targets.add(entry.getKey());
        }
        return targets;
    }

    public boolean hasTarget(){
        return !mTargets.isEmpty();
    }


    public void addChildObserver(MyFileObserver childObserver){
        for(MyFileObserver fileObserver:lChildObserver)
            if(fileObserver.getPath().equals(childObserver.getPath())) return;
        lChildObserver.add(childObserver);
        //System.out.println("----MyFileObserver----" + path + "add a child observer:" + childObserver.getPath());
    }

    public void deleteChildObserver(String path){

    }

    public void deleteChildObserver(MyFileObserver childObserver){
        lChildObserver.remove(childObserver);
    }

    public boolean addTarget(String target,Handler handler){
        if(mTargets.containsKey(target))
            return false;
        else{
            mTargets.put(target, handler);
            //向VectorClock中添加设备
            versionManager.addDevice(target);
            for (MyFileObserver o : lChildObserver) {
                o.addTarget(target, handler);
            }
            return true;
        }
    }

    public void deleteTarget(String target){
        mTargets.remove(target);
    }

	/*
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if(o == this) return true;
		if(o == null) return false;
		else if(path.equals(((MyFileObserver)o).getPath())) return true;
		else return false;
	}
	*/
}