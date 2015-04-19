package com.example.zhangsw.sharefile.MemoryManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.zhangsw.sharefile.Conflict.ConflictManager;
import com.example.zhangsw.sharefile.FileSystem.DSMFileNode;
import com.example.zhangsw.sharefile.FileSystem.FileManager;
import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.FileSystem.VersionManager;
import com.example.zhangsw.sharefile.Log.DebugLog;
import com.example.zhangsw.sharefile.LogLine.LogLine;
import com.example.zhangsw.sharefile.ShareFileService.DsmOperator;
import com.example.zhangsw.sharefile.Storage.StorageOperator;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;
import com.example.zhangsw.sharefile.Util.FileUtil;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * 共享内存管理类
 * @author zhangsw
 *
 */
public class MemoryManager implements IMemoryManager,DsmOperator{

	private LogLine logLine;
	private String defaultRootPath;		//默认路径，即存放共享文件的文件夹路径
	private String localDeviceId;				//本机名字
	private List <ShareInfo>shareInfList;	//共享设备信息表
	private HandlerThread fileTranManager;
	private Handler handler;
	private FileManager fileManager;
	private ConflictManager conflictManager;	//冲突管理，用于判断是否发生冲突，以及发生冲突后需要进行的操作
	
	private ArrayList <String> readyForSyn;
	
	private static String vectorClock = "one";	//第一次发送VectorClock的标记
	private static String vectorClockACK = "two";	//已经发送过VectorClock,接收到的是回应，用于判断对方是否已经知道了自己的VectorClock
	
	public MemoryManager(String id) throws IOException{
		
		shareInfList = new ArrayList<>();
		logLine = new LogLine(this);
		//新建一个带looper的线程
		fileTranManager = new HandlerThread("fileTranManagerHandleThread");
		fileTranManager.start();
		//将looper同handler绑定，则handler在进行消息处理时，会在looper的线程中进行
		handler = new FileTranHandler(fileTranManager.getLooper());
		
		defaultRootPath = getRootPath();
		//localDeviceId = getLocalDeviceId();
		localDeviceId = id;
		fileManager = new FileManager(defaultRootPath,localDeviceId);
		conflictManager = new ConflictManager(this,fileManager);
		
		readyForSyn = new ArrayList<>();
	}
	
	/**
	 * 获取本地设备的id
	 * @return
	 */
	private String getLocalDeviceId(){
		return "Device A";
	}
	
	/**
	 * 获取默认的根目录
	 * @return
	 */
	private String getRootPath(){
        return FileConstant.DEFAULTSHAREPATH;
	}
	
	private void deleteLocalFile(String filepath){
		System.out.println("enter consistency deletefile, path is " + filepath);
		File file = new File(filepath);
		if(file.exists()){
			if(file.isDirectory()){
				File []child = file.listFiles();
                for (File aChild : child) {
                    deleteLocalFile(aChild.getAbsolutePath());
                }
				fileManager.remoteDeleteFile(file.getAbsolutePath());
			}
			else
				fileManager.remoteDeleteFile(file.getAbsolutePath());
		}
	}
	
	private void sendFileMetaData(String target,FileMetaData fileMetaData){
		
	}
	
	/**
	 * 发送文件的version，包括了metadata，vectorClock
	 * @param target	目标
	 * @param relativePath	文件的相对路径
	 * @param tag	标记
	 */
	private void sendFileVersion(String target,String relativePath,String tag){
		DSMFileNode ob = fileManager.getDSMFileNode(defaultRootPath + relativePath);
		if(ob != null){
			System.out.println("before logline sendFileVectorClock");
			logLine.sendFileVersion(target, ob.getFileMetaData(), ob.getVectorClock(), relativePath,tag);
		}
	}
	
	private void sendFileVersion(String target, DSMFileNode m, String tag){
		String relativePath = m.getPath().substring(defaultRootPath.length());
		logLine.sendFileVersion(target, m.getFileMetaData(), m.getVectorClock(), relativePath, tag);
	
	}
	
//	private void sendFileVectorClock(String target,DSMFileNode ob,String tag){
//		if(ob != null){
//			logLine.sendFileVectorClock(target, ob.getFileMetaData().getFileID(), ob.getVectorClock(), ob.getFileMetaData().getRelativePath(), tag);
//		}
//	}
	
	/**
	 * 发送文件的VectorClock
	 * @param fileID	需要发送的文件id
	 * @param VectorClock	版本map
	 * @param target	目的地
	 */
	private void sendFileVectorClock(String target,String fileID,VectorClock VectorClock,String relativePath,String tag){
		logLine.sendFileVectorClock(target,fileID,VectorClock,relativePath,tag);
	}
	
	/**
	 * 接收到来自target的VectorClock
	 * @param target	信息来源
	 * @param fileID	文件id
	 * 
	 */
	
	public boolean receiveVectorClock(String target,String fileID,VectorClock remoteVectorClock,String relativePath,String tag){
		System.out.println("receive VectorClock,relativePath is " + relativePath);
		String path = defaultRootPath + relativePath;
		VectorClock localVectorClock = fileManager.getVectorClock(path);
		
		if(localVectorClock != null){	//存在该文件的文件结点
			//对收到的versinMap进行冲突检测，若需要更新本地的版本号，则进行更新
			int detectResult = conflictManager.detect(localVectorClock, localDeviceId, remoteVectorClock, target);
			//if发生冲突
			if(detectResult == ConflictManager.CONFLICT){
				//冲突消解
				if(tag.equals(vectorClock)){	//对方还不知道自己的VectorClock
					sendFileVectorClock(target,fileID,localVectorClock,relativePath,vectorClockACK);
				}
				conflictManager.resolute(fileID,localVectorClock, localDeviceId, remoteVectorClock,target,relativePath);
			}
			// 远端有更新
			else if(detectResult == ConflictManager.LOCALNEEDUPDATE){
				//修改远端发过来的map中自己的版本号
				remoteVectorClock.put(localDeviceId, localVectorClock.getVersionNumber(localDeviceId));
				//将修改的VectorClock转发到其他设备
				List<String>targets = fileManager.getDSMFileNode(path).getTargetsList();
				//转发VectorClock
				//forwardVectorClock(targets,target,fileID,remoteVectorClock,relativePath);
				//向target请求该文件
				fetchFile(target,relativePath);
			}
			else if(detectResult == ConflictManager.REMOTENEEDUPDATE){
				//如果远端还未获得本地更新，向远端发送通知
				sendFileVectorClock(target,fileID,localVectorClock,relativePath,vectorClock);
			}
		}
		else{	//本地不存在该文件的文件结点
			//TODO
			if(tag.equals(vectorClock)){
				//创建新的文件结点
				boolean success = fileManager.createEmptyFileNode(path, fileID);
				if(success){
					//更新这个文件结点的VectorClock，本地version为-1（不存在该文件），远端version则为发送过来的
					//更新本地保存的远端的版本号
					Assert.assertNotNull("----MemoryManager----Error,VersionManager is null",remoteVectorClock);
					System.out.println("----MemoryManager----before updateVectorClock");
					if(fileManager == null) System.out.println("fileManager is null");
					if(remoteVectorClock == null) System.out.println("----MemoryManager----remoteVectorClock is null");
					fileManager.updateVectorClock(path, target, remoteVectorClock.getVersionNumber(target));
					//修改远端发过来的map中自己的版本号
					remoteVectorClock.put(localDeviceId, VersionManager.FILENOTEXIST);
					//将修改的VectorClock转发到其他设备
					List<String>targets = fileManager.getDSMFileNode(path).getTargetsList();
					//转发VectorClock
					//forwardVectorClock(targets,target,fileID,remoteVectorClock,relativePath);
					//向target请求该文件
					fetchFile(target,relativePath);
				}
			}else{
				//忽略
			}
		}
		
		//TODO
		return true;
	}
	
	public boolean receiveVersion(String target, VectorClock remoteVectorClock,
			FileMetaData remoteMetaData, String relativePath, String tag) {
		// TODO Auto-generated method stub
		String path = defaultRootPath + relativePath;
		VectorClock localVectorClock = fileManager.getVectorClock(path);
		FileMetaData localMetaData = fileManager.getFileMetaData(path);
		
		if(localVectorClock != null){	//存在该文件的文件结点
			//对收到的versinMap进行冲突检测，若需要更新本地的版本号，则进行更新
			int detectResult = conflictManager.detect(localVectorClock, localDeviceId, remoteVectorClock, target, localMetaData, remoteMetaData);
			//if发生冲突
			if(detectResult == ConflictManager.CONFLICT){
				//冲突消解
				if(tag.equals(vectorClock)){	//对方还不知道自己的VectorClock
					sendFileVersion(target,relativePath,vectorClockACK);
				}
				conflictManager.resolute(localVectorClock, localDeviceId, remoteVectorClock,target,relativePath,remoteMetaData);
			}
			// 远端有更新
			else if(detectResult == ConflictManager.LOCALNEEDUPDATE){
				//向target请求该文件
				fetchFile(target,relativePath);
			}
			else if(detectResult == ConflictManager.REMOTENEEDUPDATE){
				//如果远端还未获得本地更新，向远端发送通知
				sendFileVersion(target,relativePath,vectorClock);
			}
		}
		else{	//本地不存在该文件的文件结点
			//TODO
            System.out.println("----MemoryManager----not exist file node,tag is:" + tag);
			if(tag.equals(vectorClock)){
				//创建新的文件结点
				boolean success = fileManager.createEmptyFileNode(path, remoteMetaData.getFileID());
				if(success){
					//更新这个文件结点的VectorClock，本地version为-1（不存在该文件），远端version则为发送过来的
					//更新本地保存的远端的版本号
					Assert.assertNotNull("----MemoryManager----Error,VersionManager is null",remoteVectorClock);
					System.out.println("----MemoryManager----before updateVectorClock");
					if(fileManager == null) System.out.println("fileManager is null");
					if(remoteVectorClock == null) System.out.println("----MemoryManager----remoteVectorClock is null");
					//更新VectorClock
					fileManager.updateVectorClock(path, remoteVectorClock);
					System.out.println("----MemoryManager----receiveVersion----has update vector clock");
					/*
					fileManager.updateVectorClock(path, target, remoteVectorClock.getVersionNumber(target));
					//修改远端发过来的map中自己的版本号
					remoteVectorClock.put(localDeviceId, VersionManager.FILENOTEXIST);
					//将修改的VectorClock转发到其他设备
					List<String>targets = fileManager.getDSMFileNode(path).getTargetsList();
					//转发VectorClock
					//forwardVectorClock(targets,target,fileID,remoteVectorClock,relativePath);
					 * 
					 */
					//向target请求该文件
					fetchFile(target,relativePath);
				}
			}else{
				//忽略
			}
		}
		
		//TODO
		return true;
	}
	
	/**
	 * 转发收到的VectorClock
	 * @param targets	需要转发的目标
	 * @param sourceID	不需要转发的目标
	 * @param fileID	文件的id
	 * @param VectorClock	需要转发的VectorClock
	 * @param relativePath	文件的相对路径
	 */
	public void forwardVectorClock(List<String> targets,String sourceID,String fileID,VectorClock VectorClock,String relativePath){
		
	}
	
	/**
	 * 接收到文件数据
	 * @param target	信息来源
	 * @param fileMetaData	文件的metaData
	 * @param file	接收到的文件
	 */
	public void receiveFileData(String target,FileMetaData fileMetaData,File file){
		//由于收到的文件是保存在默认的cache中的，我们需要将它移动到它理应所在的位置，即relativePath下。
		//
		System.out.println("recevice file,relativePath is " + fileMetaData.getRelativePath()+":"+FileUtil.getCurrentTime());
		String relativePath = fileMetaData.getRelativePath();
		file.setLastModified(fileMetaData.getModifiedTime());
		if(fileManager.fileObserverExist(defaultRootPath + relativePath)){
			//已经存在observer
			
			DSMFileNode ob = fileManager.getDSMFileNode(defaultRootPath + relativePath);
			if(fileMetaData.equals(ob.getFileMetaData())){	//本地保存的文件同收到的文件是相同的，即是冗余数据，不需要
				file.delete();
				System.out.println("File redundancy");
			}
			else{	
				//先更新metaData以及versinMap
				System.out.println("----MemoryManager----receive file that is wanted,file version is " + fileMetaData.getVersionID());
				ob.setFileMetaData(fileMetaData);
				//ob.updateVectorClock(localDeviceId, fileMetaData.getVersionID());
				if(FileOperateHelper.fileExist(defaultRootPath + relativePath)){
					//有旧版本文件存在，删除它
					//TODO
					fileManager.deleteOldFile(ob);
				}
				//将新文件移到指定目录下
				FileOperateHelper.renameFile(file.getAbsolutePath(), defaultRootPath + fileMetaData.getRelativePath());
				//System.out.println("file has been moved from cache");
				//监听收到的文件
				ob.startWatching();
				//sendFileUpdateInform(ob.getTargetsList(),fileMetaData);
			}
			
		}
		else{
			//可能是冲突文件
			if(conflictManager.isConflictFile(target, fileMetaData, file)){
				//是按照冲突重命名机制命名的文件
				System.out.println("----MemoryManager----receive conflictFile");
				conflictManager.receiveConflictFileData(target, fileMetaData, file);
				//发送本地收到文件通知
				//sendFileUpdateInform(fileManager.getDSMFileNode(defaultRootPath + relativePath).getTargetsList(),fileMetaData);
				
			}
			else{	
				//TODO
				/*
				boolean success = fileManager.createEmptyFileNode(defaultRootPath + fileMetaData.getRelativePath(), fileMetaData.getFileID());
				if(success){
					DSMFileNode ob = fileManager.getDSMFileNode(defaultRootPath + relativePath);
					ob.setFileMetaData(fileMetaData);
					ob.updateVersionNumber(target, fileMetaData.getVersionID());
					ob.updateVersionNumber(localDeviceId, fileMetaData.getVersionID());
					if(FileOperateHelper.fileExist(defaultRootPath + relativePath)){
						//有旧版本文件存在，删除它
						//TODO
						fileManager.deleteOldFile(ob);
					}
					//将新文件移到指定目录下
					FileOperateHelper.renameFile(file.getAbsolutePath(), defaultRootPath + fileMetaData.getRelativePath());
					System.out.println("file has been moved from cache");
					//监听收到的文件
					ob.startWatching();
					sendFileUpdateInform(ob.getTargetsList(),fileMetaData);
				}*/
			}
		}
	}
	
	/**
	 * 收到来自别的设备的文件更新信息
	 * @param target
	 * @param fileMetaData
	 */
	public void receiveFileUpdate(String target, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		System.out.println("receive file update inform,relativePath is " + fileMetaData.getRelativePath()+",version number is " + fileMetaData.getVersionID());
		String absolutePath = defaultRootPath + fileMetaData.getRelativePath();
		if(fileManager.fileObserverExist(absolutePath)){	//本地存在该文件的结点
			System.out.println("local has file node");
			FileMetaData localMetaData = fileManager.getDSMFileNode(absolutePath).getFileMetaData();
			System.out.println("local: " + localMetaData.getFileSize() +", " + localMetaData.getFileID() + "," +localMetaData.getRelativePath() +", "+ localMetaData.getVersionID() +", "+localMetaData.getModifiedTime());
			System.out.println("remote: "+ fileMetaData.getFileSize() + ", " + fileMetaData.getFileID() + ","+fileMetaData.getRelativePath() + ", "+ fileMetaData.getVersionID() + ", " +fileMetaData.getModifiedTime());
			if(localMetaData.equals(fileMetaData)){
				System.out.println("metaData is the same");
				fileManager.getDSMFileNode(absolutePath).updateVectorClock(target, fileMetaData.getVersionID());
			}
			else{
				//TODO
			}
			
		}
		
	}
	
	public boolean receiveDeleteFile(String target, String filepath){					//删除文件需要判断是文件还是文件夹，同时需要考虑是否还有其他线程在使用该文件夹下的文件
		//TODO
		
		deleteLocalFile(filepath);
		return true;
	}
	
	public boolean receiveFileInf(String target,String relativePath,String absolutePath,String MD5){
		int index = getIndexByName(target);
		System.out.println("enter consistency receiveFileInf---------");
		if(index != -1){
			System.out.println("exist this device--------");
			String MD5OfLocalFile = FileUtil.getFileMD5(new File(absolutePath));
			System.out.println("receivefile md5 is " + MD5 + " , local is " + MD5OfLocalFile);
			if(MD5.equals(MD5OfLocalFile)){			//存在该文件
				System.out.println("file has been existed-----");
				return false;
			}
			else{											//不存在文件，接收该文件
				//TODO
				System.out.println("want to receive file---"+relativePath);
				fetchFile(target,relativePath);
				return true;
			}
		}
		return false;
	}
	
	public boolean receiveAskFile(String target, String relativePath, String absolutePath) {
		// TODO Auto-generated method stub
		int index = getIndexByName(target);
		System.out.println("enter receiveAskFile,absolutePath is " + absolutePath);
        Log.i("Test","receive ask " + absolutePath);
		if(index != -1){
			File file = new File(absolutePath);
			if(file.exists()){				//存在请求的文件，可以进行发送
				//TODO
				System.out.println("----MemoryManager----file exist，before send");
				sendFile(target,absolutePath,relativePath);			//文件路径？？？

				return true;
			}
			else{										//不存在请求的文件
				//判断请求的是否是冲突文件
				if(conflictManager.conflictFileNodeExist(absolutePath)){	//请求的是冲突文件
					//发送冲突文件
					
					DSMFileNode ob = conflictManager.getConflictFileNode(absolutePath).getLocalFileObserver();
					System.out.println("----MemoryManager----ask conflictFile,relativePath is:" + ob.getFileMetaData().getRelativePath());
					sendFile(target,ob.getPath(),ob.getFileMetaData().getRelativePath());
					return true;
				}
				return false;
			}
		}
		return false;
	}
	
	public boolean receiveRenameFile(String target, String oldPath, String newPath) {
		// TODO Auto-generated method stub
		return fileManager.remoteMoveFile(oldPath, newPath);
	}
	
	public boolean receiveMakeDir(String target, String absolutePath){
		System.out.println("enter MemoryManager receiveMakeDir----path is "+ absolutePath);
		File file = new File(absolutePath);
		if(!file.exists()){
            //TODO
            fileManager.makeDir(file);
            return true;
        }
		else return false;
	}


	
	 
	private void sendFileUpdateInform(List<String> targets,FileMetaData fileMetaData){
		System.out.println("enter memory manager sendFileUpdateInform");
		logLine.sendFileUpdateInform(targets,fileMetaData);
	}
	
	/**
	 * 发送文件数据，包括了meta data以及concrete data
	 * @param target
	 * @param absolutePath
	 * @param relativeFilePath
	 */
	private void sendFile(String target,String absolutePath,String relativeFilePath){
        Log.i("Test",absolutePath +" data will be sent");
		logLine.sendFile(target, fileManager.getFileMetaData(absolutePath),absolutePath);
	}
	
	private void deleteFile(String target,String relativeFilePath){
		logLine.deleteFile(target, relativeFilePath);
	}
	
	private void renameFile(String target,String relativeFilePath,String newRelativeFilePath){
		logLine.renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveFile(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void renameDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void moveDir(String target,String relativeFilePath,String newRelativeFilePath){
		renameFile(target,relativeFilePath,newRelativeFilePath);
	}
	
	private void sendDir(String target,String absolutePath,String relativePath){
		File file = new File(absolutePath);
		if(file.exists() && file.isDirectory()){
			makeDir(target,relativePath);
			System.out.println("senddir------relativePath is "+relativePath);
			File []child = file.listFiles();
            for (File aChild : child) {
                String ab = aChild.getAbsolutePath();
                String re = relativePath + ab.substring(absolutePath.length());
                if (aChild.isDirectory()) sendDir(target, ab, re);
                else {
                    System.out.println("send dir --- send list files: " + aChild.getAbsolutePath());
                    //发送的是version，但其实由于发送文件夹，里面的文件肯定是要发送过去的
                   //TODO
                    sendFileVersion(target,re,vectorClock);
                   // sendFile(target, ab, re);
                }
            }
		}
	}
	
	private void makeDir(String target,String relativePath){
		logLine.makeDir(target, relativePath);
	}
	
	
	public void fetchFile(String target,String relativePath){
        System.out.println("fetch file:"+relativePath+":"+FileUtil.getCurrentTime());
		logLine.fetchFile(target,relativePath);
	}
	
	/**
	 * 发送同步就绪信息
	 */
	private void sendSynReady(String target){
		logLine.sendSynReady(target);
	}
	
	/**
	 * 接收到同步就绪信息
	 * @param target
	 */
	public void receiveSynReady(String target){
		if(readyForSyn.contains(localDeviceId + target)){
			//本地已经就绪，可以开始同步
			readyForSyn.remove(localDeviceId + target);
			synchronizeFiles(target);
			
		}else{
			readyForSyn.add(target);
		}
	}
	
	/**
	 * 构建一个文件结点
	 * @param fileID	文件的id
	 * @param relaitvePath	文件的相对路径
	 * @param nodePath 文件结点的路径
	 */
	public void constructFileNode(String fileID,String relaitvePath,String nodePath){
		//fileManager.registerObserver(fileID, relaitvePath, nodePath);
	}
	
	
	public synchronized void addShareDevice(String sharedFilePath,String target,int type){
		System.out.println("SharedFilePath is"+sharedFilePath+",target is "+target);
		ShareInfo si = new ShareInfo(sharedFilePath,target,type,handler,fileManager);
		System.out.println("enter Consistency addShareDevice-----");
		//TODO
		if(!shareInfList.contains(si)){						//不能如此简单的判断！！！
			System.out.println("----MemoryManager----shareInfo not existed,add it");
			shareInfList.add(si);
		}
		initializeVersionNumber(target);
		//本地已经初始化完毕，发送初始化完毕信息
		//TODO
		sendSynReady(target);
		if(readyForSyn.contains(target)){
			//对方已经就绪，可以进行同步
			readyForSyn.remove(target);
			synchronizeFiles(target);
		}else{
			//还没确认对方是否已经就绪，先不同步，并保存自己同对方的状态为ready
			readyForSyn.add(localDeviceId + target);
		}
	}
	
	/**
	 * 初始化对象的versionNumber
	 * @param target
	 */
	private void initializeVersionNumber(String target){
		File file = new File(FileConstant.DEFAULTVERSIONLOGPATH + "/" + target);
		if(file.exists()){
			try {
				//System.out.println("versionlog exists,initialize versionlog");
				BufferedReader br = new BufferedReader(new FileReader(file));
				//DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				LinkedHashMap<String,Integer> VectorClock = StorageOperator.readVersionNumber(br);
				//System.out.println("----MemoryManager----initializeVersionNumber:vertsionMap's size is:" + VectorClock.size());
                for (Entry<String, Integer> entry : VectorClock.entrySet()) {
                    //System.out.println("----MemoryManager----initializeVersionNumber: " + entry.getKey() + ":" + entry.getValue());
                    fileManager.updateVectorClock(entry.getKey(), target, entry.getValue());
                }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 停止share memory，保存数据
	 */
	public synchronized void stop(){
		for(int i=0;i<shareInfList.size();i++){
			saveShareInformation(shareInfList.get(i).getTarget());
		}
	}
	
	public synchronized void removeShareDevice(String target){
		int index = getIndexByName(target);
		if(index != -1){
			saveShareInformation(target);
			ShareInfo sio =  shareInfList.get(index);
			fileManager.withdrowObserver(target, sio.getSharedFilePath());
			shareInfList.remove(index);
		}
	}
	
	
	
	/**
	 * 删除所有共享的设备，并且保存共享信息
	 */
	public synchronized void removeShareDeviceAll(){
		for(int i=0;i<shareInfList.size();i++){
			saveShareInformation(shareInfList.get(i).getTarget());
			fileManager.withdrowObserver(shareInfList.get(i).getTarget(), shareInfList.get(i).getSharedFilePath());
		}
		shareInfList.clear();
	}
	
	/**
	 * 网络不可用，停止文件变化信息的发送
	 */
	public void networkDisabled(){
		fileManager.setDispenseMsgTag(false);
		//TODO
	}
	
	/**
	 * 抛弃目前的所有连接，同其他设备重新建立连接
	 * @param localIP 
	 */
	public void reconnectAll(String localIP){
		removeShareDeviceAll();
		logLine.reconnectAll(localIP);
		fileManager.setDispenseMsgTag(true);
	}
	
	public boolean connect(String ip) throws IOException{
		if(logLine.connect(ip)){
			//TODO
			String targetName = logLine.getDeviceNameByID(ip);
			System.out.println("has connected: " + ip + ",before synchronzeFiles,targetname is :" + targetName);
			if(targetName != null){
				synchronizeFiles(targetName);
			}
			return true;
		}
		else return false;
			
	}
	
	/**
	 * 同步文件
	 * @param target
	 */
	private void synchronizeFiles(String target){
		int index = getIndexByName(target);
		if(index != -1){
			ShareInfo s = shareInfList.get(index);
			String path = s.getSharedFilePath();
			System.out.println("----MemoryManager---share path is :" + path);
			DSMFileNode m = fileManager.getDSMFileNode(path);
			if(path.equals(defaultRootPath)){
				//共享的默认路径，需要排除。cache文件夹
				if(m.hasChild()){
					ArrayList<DSMFileNode> list = m.getChildAll();
					for(int i=0;i<list.size();i++){
						System.out.println(list.get(i).getPath());
						if(!list.get(i).getPath().equals(FileConstant.DEFAULTSAVEPATH))
							synchronizeFiles(target,list.get(i));
					}
				}
			}
			else if(m.hasChild()){
				List<DSMFileNode> list = m.getChildAll();
				for(int i=0;i<list.size();i++){
					synchronizeFiles(target,list.get(i));
				}
			}
		}
	}
	
	/**
	 * 
	 */
	private void synchronizeFiles(String target,DSMFileNode m){
		if(m != null){
			String path = m.getPath();
			if(FileOperateHelper.isDirectory(path)){
				//文件夹
				makeDir(target,m.getFileMetaData().getRelativePath());
				if(m.hasChild()){
					ArrayList<DSMFileNode> list = m.getChildAll();
					for(int i=0;i<list.size();i++){
						synchronizeFiles(target,list.get(i));
					}
				}
			}else{
				//文件
				sendFileVersion(target,m,vectorClock);
			}
		}
	}
	
	/**
	 * 同设备断连
	 * @param target 需要断连的设备对象
	 */
	public boolean disconnect(String target){
		int index = getIndexByName(target);
		if(index != -1){
			//存在该设备，进行断连
			System.out.println("----MemoryManager----before enter logline's disconnect");
			if(logLine.disconnect(target)){
				//TODO
				System.out.println("logline has disconnected");
				saveShareInformation(target);
				ShareInfo s = (ShareInfo)(shareInfList.get(index));
				fileManager.withdrowObserver(target, s.getSharedFilePath());
				shareInfList.remove(index);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 接收到来自其他设备的断连信息
	 * @param targetName
	 */
	
	public synchronized void receiveDisconnect(String targetName) {
		// TODO Auto-generated method stub
		int index = getIndexByName(targetName);
		if(index != -1){
            System.out.println("----MemoryManager----receive Disconnect");
			saveShareInformation(targetName);
			ShareInfo s = shareInfList.get(index);
			fileManager.withdrowObserver(targetName, s.getSharedFilePath());
			shareInfList.remove(index);
		}
	}
	
	/**
	 * 保存对象的文件版本号
	 * @param target
	 */
	private void saveShareInformation(String target){
		System.out.println("----MemoryManager----save share information,target is " + target);
		//将target的信息保存在本地文件中
		int index = getIndexByName(target);
		if(index != -1){
			System.out.println("----MemoryManager----saveShareInformation----target exists,begin saving");
			ShareInfo s = (ShareInfo)(shareInfList.get(index));
			String path = s.getSharedFilePath();
			File file = new File(FileConstant.DEFAULTVERSIONLOGPATH + "/" + target);
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter(file));
				DSMFileNode mo= fileManager.getDSMFileNode(path);
				if(mo != null){
					saveVersionNumber(bw,mo,target);
				}
				
				bw.flush();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void saveShareInformation(ShareInfo si){
		Assert.assertNotNull(si);
		String path = si.getSharedFilePath();
		File file = new File(FileConstant.DEFAULTVERSIONLOGPATH + "/" + si.getTarget());
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			DSMFileNode mo= fileManager.getDSMFileNode(path);
			if(mo != null){
				saveVersionNumber(bw,mo,si.getTarget());
			}
			
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void saveVersionNumber(BufferedWriter bw,DSMFileNode mo,String target) throws IOException{
		//TODO
		int versionNumber = mo.getVersionNumber(target);
		StorageOperator.writeVersionNumber(bw, mo.getPath(), versionNumber);
		if(mo.hasChild()){
			
			ArrayList<DSMFileNode> list = mo.getChildAll();
			//System.out.println(mo.getPath()+" has child,number is " + list.size());
			for(int i=0;i<list.size();i++){
				System.out.println(list.get(i).getPath());
				saveVersionNumber(bw,list.get(i),target);
			}
		}
	}
	
	
	private int getIndexByName(String target){
		int i = 0;
		for(;i<shareInfList.size();i++){
			if(((ShareInfo)(shareInfList.get(i))).getTarget().equals(target))
				return i;
		}
		return -1;
	}

    @Override
    public DSMFileNode read(String filePath) {
        return fileManager.getDSMFileNode(filePath);
    }

    @Override
    public void write(String filePath,String content) {
        FileOperateHelper.writeApend(filePath,content);
        Log.i("Test", filePath + " has been modified");
    }


    private class FileTranHandler extends Handler{
		
		public FileTranHandler(Looper looper){
			super(looper);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			MessageObj mo = (MessageObj)msg.obj;
			switch(mo.getType()){
			case FileConstant.SENDFILEMESSAGE:{											//发送文件数据,其实是发送VectorClock
				
				System.out.println("-----sendfilemessage------relativeFilePath is " + mo.getRelativeFilepath() + ",target is " + mo.getTarget());
                Date date = new Date();
                DebugLog.d(mo.getRelativeFilepath() + ":send file message:"+FileUtil.getTimeFromLong(date.getTime()));
				sendFileVersion(mo.getTarget(),mo.getRelativeFilepath(),vectorClock);
				
				//sendFile(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
				
			}break;
			
			case FileConstant.DELETEFILEMESSAGE:{									//删除文件
				System.out.println("-----deletefilemessage----");
				deleteFile(mo.getTarget(),mo.getRelativeFilepath());
				
			}break;
			
			case FileConstant.RENAMEFILEMESSAGE:{								//重命名文件
				System.out.println("-----renamefilemessage----");
				renameFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.MOVEFILEMESSAGE:{									//移动文件,同重命名文件
				System.out.println("----movefilemessage-------");
				moveFile(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.CREATEDIRMESSAGE:{						//创建文件夹
				System.out.println("----createDirMessage------");
				makeDir(mo.getTarget(),mo.getRelativeFilepath());
				//sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
			}break;
			
			case FileConstant.DELETEDIRMESSAGE:{						//删除文件夹
				System.out.println("---deleteDirMessage-------");
				deleteFile(mo.getTarget(),mo.getRelativeFilepath());
			}break;
			
			case FileConstant.MOVEDIRMESSAGE:{							//移动文件夹
				System.out.println("---moveDirMessage---------");
				moveDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			
			case FileConstant.RENAMEDIRMESSAGE:{						//重命名文件夹
				System.out.println("---renameDirMessage-------");
				renameDir(mo.getTarget(),mo.getRelativeFilepath(),mo.getNewRelativeFilepath());
			}break;
			case FileConstant.SENDDIRMESSAGE:{					//发送文件夹
				System.out.println("dir moved to,send dir message");
				sendDir(mo.getTarget(),mo.getFilepath(),mo.getRelativeFilepath());
			}break;
			}
		}
	}

	public void createEmptyFileNode(String path, String fileID) {
		// TODO Auto-generated method stub
		fileManager.createEmptyFileNode(path, fileID);
	}

	public void renameLocalFile(String oldRelativePath, String newRelativePath) {
		// TODO Auto-generated method stub
		fileManager.renameLocalFile(oldRelativePath, newRelativePath);
	}

}
