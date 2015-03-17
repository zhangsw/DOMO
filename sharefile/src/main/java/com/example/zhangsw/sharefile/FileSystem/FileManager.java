package com.example.zhangsw.sharefile.FileSystem;


import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.zhangsw.sharefile.FileMonitor.AndEventTranslate;
import com.example.zhangsw.sharefile.FileMonitor.IEventTranslate;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;

import junit.framework.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class FileManager implements IFileManager {
    private HashMap<String,MyFileObserver> mObservers;
    private Handler globalMsgHandler;		//全局消息handler,来自各个observer
    private HandlerThread handlerThread;		//全局消息的thread，主要是用来提供looper的。

    private String localDeviceId;
    private String defaultRootPath;
    private static final String handlerThreadName = "HandlerThread";			//thread的name
    private static final String oldVersionSuffix = ".oldVersion";

    private boolean dispenseMsgTag;	//是否发送文件操作消息的tag

    /**
     * 构建文件树，包括了文件监控以及版本信息
     * @param defaultRootPath	默认路径
     * @param localDeviceId	本地设备id
     */
    public FileManager(String defaultRootPath,String localDeviceId){
        mObservers = new HashMap<>();
        handlerThread = new HandlerThread(handlerThreadName);
        handlerThread.start();		//启动thread
        globalMsgHandler = new GlobalMsgHandler(handlerThread.getLooper());
        Assert.assertNotNull("globalMessageHandler is null,check it!!", globalMsgHandler);
        dispenseMsgTag = true;
        //System.out.println("before initializeObservers");
        this.localDeviceId = localDeviceId;
        this.defaultRootPath = defaultRootPath;
        //初始化，对默认目录进行监听
        initializeObservers(defaultRootPath,localDeviceId);
    }

    public void setDispenseMsgTag(boolean tag){
        dispenseMsgTag = tag;
    }

    /**
     * 创建空的文件结点
     * @param path	文件路径
     * @param fileID	文件id
     * @return
     */
    public boolean createEmptyFileNode(String path,String fileID){
        if(path == null || mObservers.containsKey(path)) return false;
        else{
            //获得父结点路径
            String fatherPath = getFatherPath(path);
            MyFileObserver fatherObserver = mObservers.get(fatherPath);
            if(fatherObserver == null){
                //不存在父结点
                //先构建父节点
                createEmptyFileNode(fatherPath,null);
                fatherObserver = mObservers.get(fatherPath);
            }
            //存在父结点
            MyFileObserver m = new MyFileObserver(path,localDeviceId,globalMsgHandler,this,fatherObserver);
            fatherObserver.addChildObserver(m);
            mObservers.put(path, m);
            //TODO 文件的id是否需要有待考证
            return true;
        }
    }

    public boolean fileObserverExist(String path){
        return mObservers.containsKey(path);
    }


    /**
     * 更新文件的metaData
     * @param path	文件路径
     * @param metaData	文件的metaData
     */
    public boolean updateMetaData(String path,FileMetaData metaData){
        if(mObservers.containsKey(path)){	//存在文件结点
            mObservers.get(path).setFileMetaData(metaData);
            return true;
        }
        else return false;
    }

    public boolean updateVectorClock(String path,String deviceId,Integer versionNumber){
        if(mObservers.containsKey(path)){	//存在文件结点
            //System.out.println("----FileManager----updateVectorClock:observer exists");
            mObservers.get(path).updateVectorClock(deviceId, versionNumber);
            return true;
        }else return false;
    }

    public boolean updateVectorClock(String path,VectorClock VectorClock){
        if(mObservers.containsKey(path)){
            mObservers.get(path).updateVectorClock(VectorClock);
            return true;
        }else return false;
    }


    private void updateVersion(MyFileObserver ob,String deviceId){
        ob.fileModified(deviceId);

        System.out.println(ob.getPath() + " has update its local version,version number is " + ob.getFileVersion());
    }

    public boolean updateLocalVersion(String path,int versionNumber){
        return updateVectorClock(path,localDeviceId,versionNumber);
    }

    /**
     * 移动文件
     * @param oldPath 旧的路径
     * @param newPath 新的路径
     */
    public void moveFile(String oldPath,String newPath){
        modifyObserverPath(oldPath,newPath);
        FileOperateHelper.renameFile(oldPath, newPath);
    }

    /**
     * 接收到来自远端的重命名操作，在本地进行重命名，需要同本地操作相区别
     * @param oldPath
     * @param newPath
     */
    public boolean remoteMoveFile(String oldPath,String newPath){
        if(FileOperateHelper.fileExist(oldPath)){
            FileOperateHelper.renameFile(oldPath, oldPath + oldVersionSuffix);
            FileOperateHelper.renameFile(oldPath + oldVersionSuffix, newPath);
            modifyObserverPath(oldPath,newPath);
            return true;
        }
        else return false;
    }

    public void remoteDeleteFile(String path){
        if(FileOperateHelper.fileExist(path)){
            //先将文件重命名为 (文件名+。oldVersion)的形式
            FileOperateHelper.renameFile(path, path+oldVersionSuffix);
            System.out.println("----FileManager----has rename to oldversion");
            //删除文件
            FileOperateHelper.deleteFile(path + oldVersionSuffix);
            System.out.println("----FileManager---has deleted oldversion");
            deleteObserver(path);
        }
    }

    public void startObserverFile(String path){
        if(mObservers.containsKey(path))
            mObservers.get(path).startWatching();
    }

    public void setLocalDeviceId(String localDeviceId){
        this.localDeviceId = localDeviceId;
        //更新所有文件结点中的localDeviceId，数据库或文件中的localDeviceId（或者将localDeviceId设置成全局可见）
        //TODO
    }

    /**
     * 文件是旧版本，有新版的文件，将旧版本文件删除.
     * @param ob
     */
    public void deleteOldFile(MyFileObserver ob) {
        // TODO Auto-generated method stub
        String path = ob.getPath();
        if(FileOperateHelper.fileExist(path)){
            //先将文件重命名为 (文件名+。oldVersion)的形式
            FileOperateHelper.renameFile(path, path+oldVersionSuffix);
            //删除文件
            FileOperateHelper.deleteFile(path + oldVersionSuffix);
            //deleteObserver(path);
        }
    }

    /**
     * 文件是旧版本，有新版的文件，将旧版本文件删除.
     * @param
     */

    public void deleteOldFile(String path){
        if(FileOperateHelper.fileExist(path)){
            //先将文件重命名为 (文件名+。oldVersion)的形式
            FileOperateHelper.renameFile(path, path+oldVersionSuffix);
            //删除文件
            FileOperateHelper.deleteFile(path + oldVersionSuffix);
            //deleteObserver(path);
        }
    }

    //初始化file tree
    private void initializeObservers(String path,String localDeviceId){
        registerObserver(localDeviceId,path);
    }

    private MyFileObserver registerObserver(String localDeviceId,String absolutePath){
        return registerObserver(localDeviceId,absolutePath, null);
    }

    private MyFileObserver registerObserver(String localDeviceId,String absolutePath,String fatherPath){
        MyFileObserver observer = mObservers.get(absolutePath);
        if(observer != null){	//observer不为null，即已经存在监控的observer
            //TODO
            return observer;
        }
        //Assert.assertNull("observer exists when first initialize the observer tree,check it",observer);

        if(fatherPath != null){
            //System.out.println("-----FileManager-----fatherPath is" + fatherPath);
            MyFileObserver fatherObserver = mObservers.get(fatherPath);
            observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,fatherObserver);
            fatherObserver.addChildObserver(observer);
        }
        else
            observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,null);
        mObservers.put(absolutePath, observer);
        if(FileOperateHelper.isDirectory(absolutePath)){
            //是文件夹，对子文件递归
            File []files = FileOperateHelper.subFiles(absolutePath);
            for(File f:files){
                registerObserver(localDeviceId,f.getAbsolutePath(),f.getParent());
            }
        }
        return observer;
    }

    private MyFileObserver registerObserverNoRecursion(String localDeviceId,String absolutePath,String fatherPath){
        MyFileObserver observer = mObservers.get(absolutePath);
        if(observer == null){
            if(fatherPath != null)
                observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,mObservers.get(fatherPath));
            else
                observer = new MyFileObserver(absolutePath,localDeviceId,globalMsgHandler,this,null);
            mObservers.put(absolutePath, observer);
        }
        return observer;
    }

    /**
     * 获取文件的VectorClock
     * @param
     * @return
     */
    public VectorClock getVectorClock(String path){
        if(mObservers.containsKey(path))
            return mObservers.get(path).getVectorClock();
        else return null;
    }

    public int getLocalVersionNumber(String path){
        if(mObservers.containsKey(path))
            return mObservers.get(path).getVersionNumber(localDeviceId);
        else return -1;
    }

    public FileMetaData getFileMetaData(String path){
        if(mObservers.containsKey(path))
            return mObservers.get(path).getFileMetaData();
        else return null;
    }

    /**
     * 重命名本地文件
     * @param oldRelativePath
     * @param newRelativePath
     */
    public boolean renameLocalFile(String oldRelativePath,String newRelativePath){
        System.out.println("----FileManager----enter renameLocalFile,oldpath is: " +defaultRootPath + oldRelativePath + ";newPath is:" + defaultRootPath + newRelativePath);
        boolean result =  FileOperateHelper.renameFile(defaultRootPath + oldRelativePath, defaultRootPath + newRelativePath);
        if(result) System.out.println("----FileManager----rename successful");
        return result;
    }

    public MyFileObserver registerObserver(Handler handler,String target,String absolutePath){
        return registerObserver(target,absolutePath,handler,null);
    }

    /**
     * 注册observer
     * @param
     * @param target:
     * */
    public MyFileObserver registerObserver(String target,String absolutePath,Handler handler,String fatherPath){
        MyFileObserver observer= mObservers.get(absolutePath);
        if(observer != null){		//已经存在监视该路径的observer
            observer.addTarget(target,handler);
            if((fatherPath != null) && (!observer.hasFather()))
                observer.setFather(mObservers.get(fatherPath));
        }
        else{									//不存在监视该路径的observer，不应该发生。。
			/*
			if(fatherPath != null)
				observer = new MyFileObserver(absolutePath,target,handler,globalMsgHandler,this,mObservers.get(fatherPath));
			else
				observer = new MyFileObserver(absolutePath,target,handler,globalMsgHandler,this,null);
			mObservers.put(absolutePath, observer);*/
        }
        File file = new File(absolutePath);
        if(file.isDirectory()){
            File []files = file.listFiles();
            for(File f:files){		//对子文件递归进行注册
                if(f.isDirectory())
                    System.out.println(f.getAbsolutePath()+"------"+f.getParent());
                assert observer != null;
                observer.addChildObserver(registerObserver(target,f.getAbsolutePath(),handler,f.getParent()));
            }
        }
        return observer;
    }

    /**
     * 注销observer
     * @param target
     * @param path
     */
    public void withdrowObserver(String target,String path){
        MyFileObserver observer = mObservers.get(path);
        moveTarget(target,observer);
    }

    public void deleteObserver(String path) {
        // TODO Auto-generated method stub
        MyFileObserver observer = mObservers.get(path);
        if(observer != null){
            moveObserver(observer);
        }
    }

    public void updateObserverMap(String path,String newPath){
        MyFileObserver observer = mObservers.get(path);
        if(observer != null){
            mObservers.remove(path);
            MyFileObserver newObserver = mObservers.get(newPath);
            if(newObserver == null) mObservers.put(newPath, observer);
            else{
                //TODO
                HashMap<String,Handler> map = observer.getTargetsAll();
                for (Entry<String, Handler> entry : map.entrySet()) {
                    newObserver.addTarget(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    /**
     * 修改observer的路径，在重命名以及移动文件时调用
     */
    public void modifyObserverPath(String path,String newPath){
        MyFileObserver observer = mObservers.get(path);
        if(observer != null){
            //从mObservers中删除路径为path的observer
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            String newParentPath = newPath.substring(0, newPath.lastIndexOf("/"));
            if(parentPath.equals(newParentPath)){				//父目录相同，为重命名
                observer.modifyPath(newPath);
                System.out.println("----FileManager----observer has modifyPath,newPath is : " + newPath);
            }
            else{								//父目录不同，树结构发生变化
                MyFileObserver parent = mObservers.get(parentPath);
                if(parent != null){
                    parent.deleteChildObserver(observer);
                    observer.setFather(null);
                }
                MyFileObserver newParent = mObservers.get(newParentPath);
                if(newParent != null){
                    observer.setFather(newParent);
                    newParent.addChildObserver(observer);
                }
                observer.modifyPath(newPath);
            }
            //observer.startWatching();
        }
    }



    private void moveObserver(String path){			//对路径为path的observer，将其从监控中移除
        MyFileObserver observer = mObservers.get(path);
        moveObserver(observer);
    }

    private void moveObserver(MyFileObserver observer){
        if(observer != null){
            for (MyFileObserver o : observer.getChildAll()) {
                moveObserver(o);
            }
            MyFileObserver father = observer.getFather();
            if(father != null){
                father.deleteChildObserver(observer);
            }
            observer.stopWatching();
            mObservers.remove(observer.getPath());
        }
    }

    private void moveTarget(String target,MyFileObserver observer){
        if(observer != null){
            observer.deleteTarget(target);
            if(observer.hasChild()){
                List <MyFileObserver> list = observer.getChildAll();
                for(MyFileObserver fo:list){
                    moveTarget(target,fo);
                }
            }
			/*
			if(!observer.hasTarget()){
				MyFileObserver father = observer.getFather();
				if(father != null){
					father.deleteChildObserver(observer);
				}
				if(observer.hasChild()){
					List <MyFileObserver> list = observer.getChildAll();
					for(MyFileObserver fo:list){
						fo.setFather(null);
						moveTarget(target,fo);
					}
				}
				observer.stopWatching();
				mObservers.remove(observer.getPath());
			}*/
        }

    }

    public MyFileObserver getMyFileObserver(String path){
        return mObservers.get(path);
    }

    //创建新的文件，其路径为path
    private boolean createFile(String path){
        MyFileObserver observer = mObservers.get(path);
        if(observer != null){
            //已经存在该文件，创建失败
            return false;
        }else{
            registerObserver(localDeviceId,path,getFatherPath(path));
            return true;
        }
    }

    private void createDir(String path) {
        // TODO Auto-generated method stub
        registerObserverNoRecursion(localDeviceId,path,getFatherPath(path));
    }

    public void makeDir(File file){
        if(!file.exists()) {
            createEmptyFileNode(file.getAbsolutePath(), "");
            file.mkdir();
            startObserverFile(file.getAbsolutePath());
        }

    }

    private void addDirectory(String path,MyFileObserver fatherObserver){
        registerObserver(localDeviceId,path,fatherObserver.getPath());
		/*
		MyFileObserver observer = new MyFileObserver(path,localDeviceId,globalMsgHandler,this,fatherObserver);
		mObservers.put(path, observer);
		File file = new File(path);
		if(file.isDirectory()){
			File []files = file.listFiles();
			for(File f:files){		//对子文件夹递归进行注册
				addDirectory(f.getAbsolutePath(),observer);
			}
		}*/

    }

    private void deleteFile(String path){
        deleteObserver(path);
    }

    private String getFatherPath(String path){
        //System.out.println("FileManager------Path is:"+path+"------");
        int index  = path.lastIndexOf("/");
        return path.substring(0, index);
    }

    private boolean dispenseMessage(int result, MyFileObserver o,String s){
        if(!dispenseMsgTag){
            System.out.println("----FileManager----dispenseMessage----dispenseMsgTag is false");
            return false;
        }
        if(o != null){
            System.out.println("----FileManager----dispenseMessage----observer is not null");
            HashMap<String,Handler> targets = o.getTargetsAll();
            for (Entry<String, Handler> entry : targets.entrySet()) {
                System.out.println("----FileManager----dispenseMessage----target is " + entry.getKey());
                Handler handler = entry.getValue();
                handler.sendMessage(Message.obtain(handler, result, s));
            }
            return true;
        }
        else return false;
    }

    private boolean dispenseMessage(int result,MyFileObserver o1,MyFileObserver o2,String s1,String s2){
        if(!dispenseMsgTag) return false;
        if(o1 != null && o2 != null){
            HashMap<String,Handler> targets1 = o1.getTargetsAll();
            HashMap<String,Handler> targets2 = o2.getTargetsAll();
            Iterator<Entry<String, Handler>> iter1 = targets1.entrySet().iterator();
            for (Entry<String, Handler> entry : targets2.entrySet()) {
                String target = entry.getKey();
                Handler handler = entry.getValue();
                if (!targets1.containsKey(target)) {
                    //从未监控的目录转入监控的目录
                    if (result == IEventTranslate.FILERENAMEORMOVE) {
                        handler.sendMessage(Message.obtain(handler, IEventTranslate.FILEMOVETO, s2));
                        registerObserver(target, s2, handler, getFatherPath(s2));
                    } else {
                        handler.sendMessage(Message.obtain(handler, IEventTranslate.DIRMOVETO, s2));
                        registerObserver(target, s2, handler, getFatherPath(s2));
                    }
                }
                else{
                    //从监控的目录转入另一个监控的目录
                }

            }
            while(iter1.hasNext()){
                Map.Entry<String,Handler> entry =iter1.next();
                String target = entry.getKey();
                Handler handler = entry.getValue();
                if(targets2.containsKey(target)){
                    handler.sendMessage(Message.obtain(handler, result, s1 + "$/@@/$" + s2));
                }
                else{
                    if(result == IEventTranslate.FILERENAMEORMOVE){
                        handler.sendMessage(Message.obtain(handler,IEventTranslate.FILEDELETE,s1));
                        withdrowObserver(target,s2);				//注销target对该文件的监控
                    }
                    else{
                        handler.sendMessage(Message.obtain(handler,IEventTranslate.DIRDELETE,s1));
                        withdrowObserver(target,s2);
                    }
                }
            }

            return true;
        }
        return false;
    }



    /**
     * 全局消息处理，在接收到来自各个observer的检测信息后，利用事件分析器，得出文件操作。
     * 并将各个文件操作发送到各个设备的控制队列，即shareinfo中，让对应的一致性控制来进行决策。
     * 此外，还需要依据不同的文件操作，来对变化的文件进行版本的更新（版本更新主要包括了对应文件的observer中
     * 版本信息的更新以及数据库中版本信息的更新）
     * 在更新版本信息时，需要注意是本地更新还是来自远端的更新。
     * */
    private class GlobalMsgHandler extends Handler{
        private IEventTranslate eventTranslate;

        public GlobalMsgHandler(Looper looper){
            super(looper);
            eventTranslate = new AndEventTranslate();
        }

        //处理文件变化信息
        private void handleFileModifiedMsg(String path,int result){
            if(subFileOfCache(path)){			//是cache目录下的文件得到了修改
                //判断是否是新文件
                //更新版本信息
            }
            else{			//不是cache目录下的文件得到了修改
                MyFileObserver ob = getMyFileObserver(path);
                if(ob == null){		//文件未存在
                    //System.out.println("----FileManager----HandleFileModifiedMsg:file not exists,create file observer");
                    createFile(path);
                }
                else{		//文件已经存在
                    long time = FileOperateHelper.getFileModifiedTime(path);
                    if(time != ob.getModifiedTime()){ 	//修改时间变化，说明确实修改了文件
                        System.out.println("file modified,modify time also changed");
                        updateVersion(ob,localDeviceId);
                        //更新metaData中的version号
                        ob.getFileMetaData().setVersionID(ob.getVersionNumber(localDeviceId));
                        //更新文件的修改时间
                        ob.getFileMetaData().setModifiedTime(FileOperateHelper.getFileModifiedTime(path));
                        //更新文件的大小
                        ob.getFileMetaData().setFileSize(FileOperateHelper.getFileLength(path));
                    }
                    else{	//修改时间没变化，说明只是打开了，但内容未修改
                        return;
                    }
                }
                dispenseMessage(result,getMyFileObserver(path),path);
            }
        }

        //处理文件移入信息
        private void handleFileMoveToMsg(String path,int result){
            if(subFileOfCache(path)){				//有文件移入到cache中，理论上不应该存在这种情况
                //TODO
                createFile(path);
            }
            else{			//有文件移入，且不是cache文件夹

                if(createFile(path)){
                    dispenseMessage(result,getMyFileObserver(path),path);
                }
            }
        }

        //处理文件夹的创建
        private void handleCreateDirMsg(String path,int result){
            if(subFileOfCache(path)){			//cache中文件夹的创建
                //TODO
            }
            else{
                createDir(path);
                dispenseMessage(result,getMyFileObserver(path),path);
            }
        }



        //处理文件的移除，且目标文件夹不在监测范围内
        private void handleFileMoveFromMsg(String path,int result){
            if(subFileOfCache(path)){			//有文件从cache中移出，理论上不应该存在这种情况
                //TODO
                deleteFile(path);
            }
            else{
                dispenseMessage(result,getMyFileObserver(path),path);
                deleteFile(path);
            }
        }

        //处理文件的删除
        private void handleDeleteFileMsg(String path,int result){
            if(path.endsWith(oldVersionSuffix)){	//是旧版本文件的删除
                System.out.println("old version :" + path + " has been deleted");
            }
            else{	//不是旧版本文件的删除
                handleFileMoveFromMsg(path,result);
            }
        }

        //处理文件重命名或者移动
        private void handleFileRenameOrMoveMsg(String oldPath,String newPath,int result){
            boolean oldInCache = subFileOfCache(oldPath);
            boolean newInCache = subFileOfCache(newPath);
            if(oldInCache && newInCache){	//旧路径以及新路径都在cache中
                modifyObserverPath(oldPath,newPath);
            }
            else if(oldInCache && !newInCache){		//旧路径在cache中，新路径不在cache中
                //TODO
                modifyObserverPath(oldPath,newPath);
                //从其他设备获取到文件后的操作
            }
            else if(!oldInCache && newInCache){			//从监控目录中移动文件到cache中，目前不允许

            }
            else if(newPath.endsWith(oldVersionSuffix) || oldPath.endsWith(oldVersionSuffix)){	//将文件标记为oldVersion操作，不需要关心,主要用来区分是本地操作还是来自其他设备的操作

            }
            else{
                modifyObserverPath(oldPath,newPath);
                String oldFatherPath = getFatherPath(oldPath);
                String newFatherPath = getFatherPath(newPath);
                MyFileObserver ob1 = getMyFileObserver(oldFatherPath);
                MyFileObserver ob2 = getMyFileObserver(newFatherPath);
                dispenseMessage(result,ob1,ob2,oldPath,newPath);
            }
        }

        private void handleDirRenameOrMoveMsg(String oldPath,String newPath,int result){
            handleFileRenameOrMoveMsg(oldPath,newPath,result);
        }

        /**
         * 用于处理文件夹的删除
         * @param path
         * @param result
         */
        private void handleDeleteDirMsg(String path,int result){
            if(subFileOfCache(path)){			//删除的文件夹是cache中的
                deleteFile(path);
            }
            else{
                MyFileObserver ob = getMyFileObserver(path);
                dispenseMessage(result,ob,path);
                deleteFile(path);
            }
        }

        private void handleDirMoveFromMsg(String path,int result){
            handleDeleteDirMsg(path,result);
        }

        /**
         * 用于处理文件夹的移入
         * @param path
         * @param result
         */
        private void handleDirMoveToMsg(String path,int result){
            if(subFileOfCache(path)){		//文件夹从外部移入到cache中，不应该出现这种情况
                //TODO
            }
            else{
                MyFileObserver ob = getMyFileObserver(getFatherPath(path));

                if(getMyFileObserver(path) == null){ 		//文件本来不存在于监视目录下
                    addDirectory(path,ob);
                }
                dispenseMessage(result,ob,path);
            }
        }

        /**
         *
         */
        private void handleCoverFileMsg(String path,int result){
            MyFileObserver ob = getMyFileObserver(path);
            if(ob != null){
                ob.stopWatching();		//停止当前的监听
                ob.startWatching();		//重新开始监听
                long time = FileOperateHelper.getFileModifiedTime(path);
                if(time != ob.getModifiedTime()){ 	//修改时间变化，说明确实修改了文件
                    System.out.println("file modified,modify time also changed");
                    updateVersion(ob,localDeviceId);
                    //更新metaData中的version号
                    ob.getFileMetaData().setVersionID(ob.getVersionNumber(localDeviceId));
                    //更新文件的修改时间
                    ob.getFileMetaData().setModifiedTime(FileOperateHelper.getFileModifiedTime(path));
                    //更新文件的大小
                    ob.getFileMetaData().setFileSize(FileOperateHelper.getFileLength(path));
                }
                else{	//修改时间没变化，说明只是打开了，但内容未修改
                    return;
                }
                dispenseMessage(result,getMyFileObserver(path),path);
            }
        }

        private boolean subFileOfCache(String path){
            return path.startsWith(FileConstant.DEFAULTSAVEPATH + "/");
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            Message m = Message.obtain(msg);
            String path = m.obj.toString();
            int result = eventTranslate.translate(path, m.what);
            switch(result){
                case IEventTranslate.FILEMODIFIED:{				//文件被修改，将消息发送到共享该文件的对象.
                    //System.out.println(path + " has been modified");
                    System.out.println("----FileManager----FileModified");
                    handleFileModifiedMsg(path,result);
                }break;

                case IEventTranslate.FILEMOVETO:{			//有新文件移动到了受监控的文件夹中，需要发送文件并为该文件添加observer
                    System.out.println("----FileManager----FileMoveto");
                    handleFileMoveToMsg(path,result);
                }break;

                case IEventTranslate.DIRCREATE:{				//文件夹创建
                    System.out.println("----FileManager----DirCreate");
                    handleCreateDirMsg(path,result);
                }break;

                case IEventTranslate.FILEMOVEFROM:{			//文件从监测目录中移走，且目标文件夹不在监测范围内
                    System.out.println("----FileManager----FileMoveFrom");
                    handleFileMoveFromMsg(path,result);
                }break;

                case IEventTranslate.FILEDELETE:{							//文件删除
                    System.out.println("----FileManager----FileDelete");
                    handleDeleteFileMsg(path,result);
                }break;

                case IEventTranslate.FILERENAMEORMOVE:{		//文件重命名或者移动
                    handleFileRenameOrMoveMsg(eventTranslate.getOldPath(),eventTranslate.getNewPath(),result);
                }break;

                case IEventTranslate.DIRRENAMEORMOVE:{					//文件夹重命名或移动
                    handleDirRenameOrMoveMsg(eventTranslate.getOldPath(),eventTranslate.getNewPath(),result);
                }break;

                case IEventTranslate.DIRDELETE:{				//文件夹删除
                    handleDeleteDirMsg(eventTranslate.getOldPath(),result);
                }break;

                case IEventTranslate.DIRMOVEFROM:{			//文件夹移动至未监控目录下，同文件夹删除
                    handleDirMoveFromMsg(eventTranslate.getOldPath(),result);
                }break;

                case IEventTranslate.DIRMOVETO:{			//文件夹移入监控目录
                    System.out.print("----FileManager----DirMoveTo");
                    handleDirMoveToMsg(path,result);
                }break;

                case IEventTranslate.COVERFILE:{			//移入文件时，有相同的名字的文件
                    handleCoverFileMsg(path,result);
                }break;
            }
        }
    }


}