package com.example.zhangsw.sharefile.PsyLine;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.LogLine.LogLine;
import com.example.zhangsw.sharefile.Util.FileConstant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

public class PsyLine extends Observable implements FileTransfer,FileTransferCallBack{

    private int maxTCPConnections;
    private int TCPConCount;
    private PsyTcpClient psyTcpClient;
    private PsyTcpServer psyTcpServer;
    private LogLine logline;
    private List<SocketIO> socketList;


    //private List psyTcpClients;
    public PsyLine(LogLine l) throws IOException{
        maxTCPConnections = 10;				//设置tcp连接最大个数
        logline = l;
        socketList = new ArrayList<>();
        psyTcpClient = new PsyTcpClient(this);
        psyTcpServer = new PsyTcpServer(this);
        psyTcpServer.startServer();

        //	psyTcpClients = new ArrayList<PsyTcpClient>();
    }


    /** 通过ip进行设备连接(TCP)
     * @throws IOException */
    public boolean connect(String ip) throws IOException{
        //psyTcpClient = new PsyTcpClient(ip);
        if (psyTcpClient.connect(ip)){
            TCPConCount ++;
            return true;
        }
        else return false;
    }


    /**
     * 同之前连接的设备重新建立连接
     * @param localIP
     */
    public void reconnectAll(){
        psyTcpServer.serverState();
        removeSocketAll();

    }

    /**
     * 抛弃所有连接
     */
    public void abandonAllConnection(){
        removeSocketAll();
    }

    /**
     * 断开连接
     * @param id 需要断开连接的设备id
     */
    public boolean disconnect(String id){
        int index = getIndexByTargetID(id);
        if(index != -1){
            System.out.println("enter psyline's disconnect");
            SocketIO sio = (SocketIO)socketList.get(index);
            sio.sendDisconnectMsg();
            return true;
        }
        else return false;
    }

    public synchronized  void addSocket(SocketIO si){
        int index = getIndexByTargetID(si.getTargetID());
        if(index == -1){				//该ip不在连接表中
            socketList.add(si);
            logline.addDevice(si.getSocket().getInetAddress().getHostName(), si.getTargetID(), 0);
            System.out.println("add a device:"+si.getSocket().getInetAddress().getHostName()+"  ip is "+si.getTargetID());
        }
    }

    public synchronized void removeSocket(SocketIO si){
        int index = getIndexByTargetID(si.getTargetID());
        if(index != -1){
            //存在该socket
            si.close();
            socketList.remove(index);
        }
    }

    /**
     *弃用所有现有的连接
     */
    public synchronized void removeSocketAll(){
        for(int i =0;i<socketList.size();i++){
            socketList.get(i).close();
        }
        socketList.clear();
    }



    public boolean sendFile(String ip,final FileMetaData metaData,final String absolutePath){
        int index = getIndexByTargetID(ip);
        if(index != -1){
            (socketList.get(index)).sendFileData(metaData, absolutePath);
            return true;
        }
        return false;
		/*
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFile(ip, metaData, absolutePath);
			else if(type == 1) return psyTcpServer.sendFile(ip, metaData, absolutePath);
		}
		return false;*/

    }

    public boolean deleteFile(String ip,String relativePath){
        int index = getIndexByTargetID(ip);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendCommand(FileTransferHeader.deleteFileCmd(relativePath));
            return true;
        }
        return false;
		/*
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.deleteFile(ip, relativePath);
			else if(type == 1) return psyTcpServer.deleteFile(ip, relativePath);
		}
		return false;*/
    }

    public boolean renameFile(String ip, String relativeFilePath,String newRelativeFilePath) {
        int index = getIndexByTargetID(ip);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendCommand(FileTransferHeader.renameFileCmd(relativeFilePath, newRelativeFilePath));
            return true;
        }
        return false;
		/*
		if(index != -1){
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.renameFile(ip,relativeFilePath,newRelativeFilePath);
			else if(type == 1) return psyTcpServer.renameFile(ip, relativeFilePath,newRelativeFilePath);
		}
		return false;
		*/
    }

    public boolean sendFileInf(String ip,String relativePath,String MD5){
        int index = getIndexByTargetID(ip);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendCommand(FileConstant.FILEINF + "$PATH$" + relativePath + "$MD5$" + MD5 + "\n");
            return true;
			/*
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.sendFileInf(ip,relativePath,MD5);
			else if(type == 1) return psyTcpServer.sendFileInf(ip, relativePath, MD5);
			*/
        }
        return false;
    }

    public boolean makeDir(String ip,String relativePath){
        int index = getIndexByTargetID(ip);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendCommand(FileTransferHeader.makeDirCmd(relativePath));
            return true;
			/*
			int type = ((SocketIO)socketList.get(index)).getType();
			if(type == 0) return psyTcpClient.makeDir(ip, relativePath);
			else if(type == 1) return psyTcpServer.makeDir(ip, relativePath);
			*/
        }
        return false;
    }

    /**向设备ID请求文件*/
    public boolean fetchFile(String ID,int style,String relativePath){
        switch(style){
            case 0:{							//为TCP/IP连接
                int index = getIndexByTargetID(ID);
                if(index != -1){
                    ((SocketIO)socketList.get(index)).sendCommand(FileTransferHeader.fetchFileCmd(relativePath));
                    return true;
				/*
				int type = ((SocketIO)socketList.get(index)).getType();
				if(type == 0) return psyTcpClient.fetchFile(ID, relativePath);
				else if(type == 1) return psyTcpServer.fetchFile(ID, relativePath);*/
                }
            }break;
            //TODO
        }
        return false;
    }

    public boolean sendFileEve(String filePath, String fileName){
        return true;
    }

    public  int getIndexByTargetID(String targetID){
        int i = 0;
        for(;i<socketList.size();i++){
            if(((SocketIO)(socketList.get(i))).getTargetID().equals(targetID))
                return i;
        }
        return -1;
    }

    public SocketIO getSocketInf(int index){
        if(index < socketList.size())
            return (SocketIO)socketList.get(index);
        else return null;
    }

    public boolean receiveFileInf(String targetIp,String relativePath,String absolutePath,String MD5){
        System.out.println("enter psyline receiveFileInf---------");
        return logline.receiveFileInf(targetIp, relativePath, absolutePath, MD5);
    }

    public boolean receiveAskFile(String targetIp,String relativePath,String absolutePath){
        return logline.receiveAskFile(targetIp,relativePath,absolutePath);
    }

    public boolean receiveDeleteFile(String targetIp,String filepath){
        return logline.receiveDeleteFile(targetIp, filepath);
    }

    public boolean receiveRenameFile(String targetIp, String oldPath, String newPath) {
        return logline.receiveRenameFile(targetIp,oldPath,newPath);

    }

    public boolean receiveMakeDir(String targetIp, String absolutePath){
        return logline.receiveMakeDir(targetIp, absolutePath);
    }


    public void receiveFileData(String targetIp, FileMetaData fileMetaData,
                                File file) {
        // TODO Auto-generated method stub
        logline.receiveFileData(targetIp,fileMetaData,file);
    }

    public boolean receiveVersion(String targetIp, VectorClock VectorClock,
                                  FileMetaData metaData, String relativePath, String tag) {
        // TODO Auto-generated method stub
        return logline.receiveVersion(targetIp, VectorClock, metaData, relativePath, tag);
    }

    /**
     * 收到断连信息，断开同目标设备的连接
     * @param ip
     */
    public void receiveDisconnect(String ip) {
        // TODO Auto-generated method stub
        int index = getIndexByTargetID(ip);
        if(index != -1){
            SocketIO sio = socketList.get(index);
            sio.close();
            logline.receiveDisconnect(ip);
            socketList.remove(index);
        }
    }



    /**
     *收到文件更新通知
     * @param ip
     * @param fileMetaData
     */
    public void receiveFileUpdate(String ip, FileMetaData fileMetaData) {
        // TODO Auto-generated method stub
        logline.receiveFileUpdate(ip,fileMetaData);
    }

    public void sendFileMetaData() {
        // TODO Auto-generated method stub

    }

    public void getFileMetaData() {
        // TODO Auto-generated method stub

    }

    public void sendFileData() {
        // TODO Auto-generated method stub

    }

    public void getFileData() {
        // TODO Auto-generated method stub

    }

    public void sendFileVectorClock(VectorClock VectorClock, String target) {
        // TODO Auto-generated method stub

    }

    public void sendCommand(int command) {
        // TODO Auto-generated method stub

    }


    public void sendFileMetaData(Object fileMetaData, Object target) {
        // TODO Auto-generated method stub

    }


    public void getFileMetaData(String fileID, Object target) {
        // TODO Auto-generated method stub

    }


    public void sendFileData(Object fileData, Object target) {
        // TODO Auto-generated method stub

    }


    public void getFileData(Object fileID, Object target) {
        // TODO Auto-generated method stub

    }


    public void sendFileVectorClock(Object VectorClock, Object target) {
        // TODO Auto-generated method stub

    }


    public void sendCommand(String command) {
        // TODO Auto-generated method stub

    }

    public void sendFileVectorClock(String id,VectorClock VectorClock, String relativePath,String tag){
        System.out.println("enter psyline send VectorClock");
        int index = getIndexByTargetID(id);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendFileVectorClock(VectorClock, relativePath, tag);
        }
    }


    public void sendFileVersion(String id, FileMetaData metaData,
                                VectorClock VectorClock, String relativePath,String tag) {
        // TODO Auto-generated method stub
        System.out.println("enter psyline send VectorClock");
        int index = getIndexByTargetID(id);
        if(index != -1){
            ((SocketIO)socketList.get(index)).sendFileVersion(VectorClock, metaData, relativePath, tag);
			/*
			SocketIO sio = (SocketIO)socketList.get(index);
			int type = sio.getType();
			if(type == 0)  psyTcpClient.sendFileVectorClock(sio,VectorClock,fileID,relativePath,tag);
			else if(type == 1) psyTcpServer.sendFileVectorClock(sio,VectorClock,fileID,relativePath,tag);
			*/

        }

    }


    public void sendFileUpdateInform(List<String> deviceId,
                                     FileMetaData fileMetaData) {
        // TODO Auto-generated method stub
        for(int i=0;i<deviceId.size();i++){
            int index = getIndexByTargetID(deviceId.get(i));
            if(index != -1){
                SocketIO sio = (SocketIO)socketList.get(index);
                sio.sendFileUpdateInform(fileMetaData);
				/*
				int type = sio.getType();
				if(type == 0)  psyTcpClient.sendFileUpdateInform(sio,fileMetaData);
				else if(type == 1) psyTcpServer.sendFileUpdateInform(sio,fileMetaData);
				*/
            }
        }
    }

    public void connectionFailure(String id) {
        // TODO Auto-generated method stub
        System.out.println("-----PsyLine-----connection failure,delete connection information");
        receiveDisconnect(id);
    }


    public void sendSynReady(String id) {
        // TODO Auto-generated method stub
        int index = getIndexByTargetID(id);
        if(index != -1){
            System.out.println("----PsyLine----SendSynReady:target is:" + id);
            SocketIO sio = (SocketIO)socketList.get(index);
            sio.sendSynReady();
        }
    }

    public void receiveSynReady(String id){
        logline.receiveSynReady(id);
    }


    public void sendHeartBeat() {
        // TODO Auto-generated method stub
        //System.out.println("----PsyLine----send heart beat");
        for (SocketIO aSocketList : socketList) {
            (aSocketList).sendHeartBeat();
        }
    }


    public void hasDisconnected(String targetID) {
        // TODO Auto-generated method stub
        int index = getIndexByTargetID(targetID);
        if(index != -1){
            socketList.remove(index);
        }
    }
}