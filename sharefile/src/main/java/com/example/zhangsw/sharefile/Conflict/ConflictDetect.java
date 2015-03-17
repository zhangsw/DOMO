package com.example.zhangsw.sharefile.Conflict;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;

/**
 * Created by zhangsw on 2015-01-13.
 */
public class ConflictDetect {

    public ConflictDetect(){

    }

    /**
     * 对两个versionMap进行比较
     * @param localVectorClock
     * @param localDeviceId
     * @param remoteVectorClock
     * @param remoteDeviceId
     * @return
     */
    public int detect(VectorClock localVectorClock,String localDeviceId,VectorClock remoteVectorClock,String remoteDeviceId){
        //获取本地versionMap中保存的本地版本号
        int localNumberInLocalVersion = localVectorClock.getVersionNumber(localDeviceId);
        if(localVectorClock.getVersionNumber(remoteDeviceId) == null) System.out.println("version numbers is null");
        //获取本地versinMap中保存的远端版本号
        int remoteNumberInLocalVersion = localVectorClock.getVersionNumber(remoteDeviceId);

        //获取远端versionMap中保存的本地版本号
        int localNumberInRemoteVersion = remoteVectorClock.getVersionNumber(localDeviceId);

        //获取远端versionMap中保存的远端版本号
        int remoteNumberInRemoteVersion = remoteVectorClock.getVersionNumber(remoteDeviceId);

        System.out.println("localNumberInLocalVersion is:" + localNumberInLocalVersion);
        System.out.println("remoteNumberInLocalVersion is:" + remoteNumberInLocalVersion);
        System.out.println("localNumberInRemoteVersion is:" + localNumberInRemoteVersion);
        System.out.println("remoteNumberInRemoteVersion is:" + remoteNumberInRemoteVersion);
        if(localNumberInLocalVersion > localNumberInRemoteVersion){		//本地版本号大于在远端保存的本地版本号，即远端还未获得本地的版本更新
            if(remoteNumberInLocalVersion < remoteNumberInRemoteVersion){	//本地保存的远端版本号小于远端保存的远端版本号，即本地还未获得远端的版本更新
                //发生冲突,双方在都未获得对方的更新的情况下，在各自的设备上发生了修改
                //TODO
                System.out.println("---conflict occurs---");
                return ConflictManager.CONFLICT;

            }
            else if(remoteNumberInLocalVersion == remoteNumberInRemoteVersion){ 	//本地已经获得远端的更新
                //远端需要得到更新
                System.out.println("---remote needs update---");
                return ConflictManager.REMOTENEEDUPDATE;

            }
            else{		//本地保存的远端版本号大于远端保存的远端版本号，这种情况未知
                System.out.println("---unknowing situation---");
            }
        }
        else if(localNumberInLocalVersion == localNumberInRemoteVersion){		//远端已经获悉了本地的修改
            if(remoteNumberInLocalVersion < remoteNumberInRemoteVersion){
                //本地还未获悉远端的更新，本地需要更新
                System.out.println("---local needs update---");
                localVectorClock.put(remoteDeviceId, remoteNumberInRemoteVersion);	//更新本地的versionMap，
                return ConflictManager.LOCALNEEDUPDATE;
            }
            else if(remoteNumberInLocalVersion == remoteNumberInRemoteVersion){
                //双方都获悉了对方的版本
                System.out.println("---both know the versionMap");
                return ConflictManager.BOTHKNOW;
            }
            else{	//这种情况未知
                System.out.println("---unknowing situation---");
            }
        }

        return -1;

    }

    public int detect(VectorClock localVectorClock, String localDeviceId,
                      VectorClock remoteVectorClock, String remoteDeviceId,
                      FileMetaData localMetaData, FileMetaData remoteMetaData) {
        // TODO Auto-generated method stub
        int result = -1;
        int compareResult = localVectorClock.compareTo(remoteVectorClock);
        switch(compareResult){
            case VectorClock.EQUAL:{
               // System.out.println("----ConflictDetect----detect----both know the version");
                result = ConflictManager.BOTHKNOW;
            }break;
            case VectorClock.GREATER:{
                //远端需要更新
                result = ConflictManager.REMOTENEEDUPDATE;
            }break;
            case VectorClock.LESSER:{
                //本地需要更新
                result = ConflictManager.LOCALNEEDUPDATE;
                localVectorClock.merge(remoteVectorClock);
            }break;
            case VectorClock.UNDEFINED:{
                // 没有相对次序，可能会产生冲突，判断metaData
                if(localMetaData.getModifiedTime()==remoteMetaData.getModifiedTime()){
                    //file is same
                    result = ConflictManager.BOTHKNOW;
                    localVectorClock.merge(remoteVectorClock);
                }
                else{
                    result = ConflictManager.CONFLICT;
                }
            }break;
            default:{
               // System.out.println("----ConflictDetect----detect----default situation");
            }
        }
        return result;
    }

}
