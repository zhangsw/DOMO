package com.example.zhangsw.sharefile.Conflict;

import com.example.zhangsw.sharefile.FileSystem.FileManager;
import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.MyFileObserver;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.MemoryManager.IMemoryManager;

import java.io.File;
import java.util.HashMap;


public class ConflictManager implements IResoluteOperator{
	private ConflictDetect conflictDetect;
	private ConflictResolute conflictResolute;
	private IMemoryManager imm;
	private FileManager fileManager;
    //保存冲突文件
	private HashMap<String,ConflictFileNode> conflictFiles;

    /**产生冲突*/
	public static int CONFLICT = 1;

    /**远端还未获悉本地的版本，需要得到更新*/
	public static int REMOTENEEDUPDATE = 2;

    /**本地还未获悉远端的版本，需要得到更新*/
	public static int LOCALNEEDUPDATE = 3;

    /**双方都已经获悉对方的版本号*/
	public static int BOTHKNOW = 4;
	
	public ConflictManager(IMemoryManager imm,FileManager fileManager){
		conflictDetect = new ConflictDetect();
		conflictResolute = new MyConflictResolute(fileManager);
		this.imm = imm;
		this.fileManager = fileManager;
		conflictFiles = new HashMap<>();
	}
	
	/**
	 * 检测两个versionMap,是否发生冲突
	 * @param localVersionMap
	 * @param localDeviceId
	 * @param remoteVersionMap
	 * @param remoteDeviceId
	 * @return
	 */
	public int detect(VectorClock localVersionMap,String localDeviceId,VectorClock remoteVersionMap,String remoteDeviceId){
		return conflictDetect.detect(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId);
	}
	
	public int detect(VectorClock localVersionMap, String localDeviceId,
			VectorClock remoteVersionMap, String remoteDeviceId,
			FileMetaData localMetaData, FileMetaData remoteMetaData) {
		// TODO Auto-generated method stub
		return conflictDetect.detect(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, localMetaData, remoteMetaData);
	}
	
	public void resolute(String fileID,VectorClock localVersionMap,String localDeviceId,VectorClock remoteVersionMap,String remoteDeviceId,String relativePath){
		conflictResolute.resoluteConfliction(fileID,localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, relativePath,this);
	}
	
	public void resolute(VectorClock localVersionMap, String localDeviceId,
			VectorClock remoteVersionMap, String remoteDeviceId, String relativePath,
			FileMetaData remoteMetaData) {
		// TODO Auto-generated method stub
		conflictResolute.resoluteConfliction(localVersionMap, localDeviceId, remoteVersionMap, remoteDeviceId, relativePath, remoteMetaData,this);
	}

    /**
     * 判断收到的文件是否是以冲突消解机制重命名的冲突文件
     */
	public boolean isConflictFile(String id,FileMetaData fileMetaData,File file){
		return conflictResolute.isConflictFile(id, fileMetaData, file);
	}

    /**
     * 收到冲突文件的数据
     */
	public void receiveConflictFileData(String target,FileMetaData fileMetaData,File file){
		conflictResolute.receiveConflictFileData(target, fileMetaData, file,this);
	}
	
	public boolean conflictFileNodeExist(String path){
        return conflictFiles.containsKey(path);
	}
	
	public void addConflictFile(MyFileObserver ob,String path){
		if(conflictFileNodeExist(path)){
			conflictFiles.get(path).add(ob);
		}
		else{
			ConflictFileNode node = new ConflictFileNode(path);
			node.add(ob);
			conflictFiles.put(path, node);
		}
	}
	
	public void addConflictNode(String path){
		if(!conflictFileNodeExist(path)){
			ConflictFileNode node = new ConflictFileNode(path);
			conflictFiles.put(path, node);
		}
	}
	
	public void removeConflictNode(String path){
		conflictFiles.remove(path);
	}
	

	public void deleteFile() {
		// TODO Auto-generated method stub
		
	}

	public void mergeFile() {
		// TODO Auto-generated method stub
		
	}

	public void fetchFile(String target, String fileID, String relativePath) {
		// TODO Auto-generated method stub
		imm.fetchFile(target, relativePath);
	}

	public void renameLocalFile(String oldRelativePath,
			String newRelativePath) {
		// TODO Auto-generated method stub
		System.out.println("----ConflictManager----enter renameLocalFile");
		imm.renameLocalFile(oldRelativePath, newRelativePath);
		
	}

	public void createEmptyFileNode(String path, String fileID) {
		// TODO Auto-generated method stub
		imm.createEmptyFileNode(path, fileID);
	}
	
	public ConflictFileNode getConflictFileNode(String path){
		return conflictFiles.get(path);
	}

	

	
	
	

}
