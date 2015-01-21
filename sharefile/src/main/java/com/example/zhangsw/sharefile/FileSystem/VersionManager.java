package com.example.zhangsw.sharefile.FileSystem;

import android.util.Log;

import com.example.zhangsw.sharefile.BuildConfig;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;

import java.util.UUID;

/**
 * 管理文件的版本信息
 * @author zhangsw
 *
 */
public class VersionManager {

    //版本历史
    private VersionHistory versionHistory;

    //逻辑时钟，用于记录自己以及其他设备上文件版本号
    private VectorClock vectorClock;

    private FileMetaData fileMetaData;

    //默认的初始版本号，为0
    public static int INITIAL_VERSION_NUMBER = 0;

    //如果文件不存在，则版本号为-1
    public static int FILENOTEXIST = -1;

    //默认的每次更新增加的数值，为1
    private static int DEFAULT_ADDITION = 1;


    public VersionManager(){

    }
    //初始化vectorClock,由于需要保存本地文件版本号，因此，vector中至少有一个设备
    public VersionManager(String deviceName,String path){
        if(FileOperateHelper.fileExist(path)){
            vectorClock = new VectorClock();
            System.out.println("----VersionManager----File:" + path + " exists");
            vectorClock.put(deviceName, INITIAL_VERSION_NUMBER);
            //为文件生成全局唯一的id
            String fileID = UUID.randomUUID().toString();
            long fileLength = FileOperateHelper.getFileLength(path);
            String relaitvePath = path.substring(FileConstant.DEFAULTSHAREPATH.length());
            fileMetaData = new FileMetaData(fileID,INITIAL_VERSION_NUMBER, relaitvePath, path, fileLength, deviceName,FileOperateHelper.getFileModifiedTime(path));
        }
        else{
            vectorClock = new VectorClock();
            vectorClock.put(deviceName, FILENOTEXIST);
            fileMetaData = new FileMetaData();
            fileMetaData.setVersionID(FILENOTEXIST);
        }
    }

	/*
	public VersionManager(String deviceName,int versionNumber){
		vectorClock = new vectorClock();
		vectorClock.put(deviceName, versionNumber);
	}

	*/

    //更新map中的一个设备及其版本号
    public void updateVersionNumber(String deviceName,int number){
        vectorClock.put(deviceName, number);
        System.out.println("local update version number,device: " + deviceName +" ,number is:" + number);
        fileMetaData.setVersionID(number);
    }

    //更新文件版本，包括了map及metaData
    public void updateVersionNumber(String deviceName){
        //更新map
        addVersionNumber(deviceName,DEFAULT_ADDITION);
        //更新metaData
        int old = fileMetaData.getVersionID();
        fileMetaData.setVersionID(++old);
        System.out.println("----VersionManager----updateVersionNumber----version number is:" + old);
    }

    public int getFileVersion(){
        return fileMetaData.getVersionID();
    }

    public void updateVectorClock(String deviceName,int number){
        vectorClock.put(deviceName, number);
    }

    public void updateVectorClock(VectorClock vectorClock){
        this.vectorClock = vectorClock;
    }

    //向map中添加一个新设备，版本号为文件不存在
    public void addDevice(String deviceName){
        vectorClock.put(deviceName, FILENOTEXIST);
    }

    //向map中添加一个新设备
    public void addDevice(String deviceName,int number){
        vectorClock.put(deviceName, number);
    }

    //从map中删除一个设备及其版本号
    public void deleteDevice(String deviceName){
        vectorClock.remove(deviceName);
    }



    //将指定对象的versionNumber增加number
    public void addVersionNumber(String deviceName,int number){
        Integer versionNumber = vectorClock.getVersionNumber(deviceName);
        if(versionNumber != null){      //存在该设备
            versionNumber += number;
            vectorClock.put(deviceName, versionNumber);
        }
    }

    //初始化map中的一个设备的版本号
    public void initialVersionNumber(String deviceName){
        vectorClock.put(deviceName, INITIAL_VERSION_NUMBER);
    }

    //获取指定设备的versionNumber
    public Integer getVersionNumber(String deviceName){
        return vectorClock.getVersionNumber(deviceName);
    }

    /**
     * merge两个vectorClock
     * @param vectorClock
     */
    public void mergeVectorClock(VectorClock vectorClock){
        this.vectorClock.merge(vectorClock);
    }

    /**
     * 设置文件的vectorClock
     * @param vectorClock
     */
    public void setVectorClock(VectorClock vectorClock){
        this.vectorClock = vectorClock;
    }

    /**
     * 获取文件的vectorClock
     * @return
     */
    public VectorClock getVectorClock(){
        return vectorClock;
    }

    /**
     * 设置文件的metaData
     * @param metaData
     */
    public void setFileMetaData(FileMetaData metaData){
        fileMetaData = metaData;
    }

    /**
     * 获取文件的metaData
     * @return
     */
    public FileMetaData getFileMetaData(){
        return fileMetaData;
    }
}