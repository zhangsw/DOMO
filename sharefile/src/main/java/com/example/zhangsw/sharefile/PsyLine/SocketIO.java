package com.example.zhangsw.sharefile.PsyLine;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.Util.FileConstant;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;


public class SocketIO implements Runnable{

    private static final int FILEDATA = 1;
    private static final int VERSION = 2;
    private static final int METADATA = 3;
    private static final int COMMAND = 4;
    private static final int FILEUPDATE = 5;
    private static final int DISCONNECT = 6;
    private static final int SYNREADY = 7;
    private static final int HEARTBEAT = 8;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private String targetID;
    private int type;							//0为client连接，1为server连接
    private boolean tag;
    private ObjectOutputStream oos;
    private FileTransferCallBack callBack;

    private LinkedBlockingQueue<IOMessage> messageQueue;

    private Responser responser;

    public SocketIO(String targetID,Socket socket,int type,FileTransferCallBack callBack){
        try {
            this.socket = socket;
            this.type = type;
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            this.targetID = targetID;
            tag = true;
            oos = new ObjectOutputStream(socket.getOutputStream());
            this.callBack = callBack;

            messageQueue = new LinkedBlockingQueue<>();
            responser = new Responser(socket,callBack);
            Thread t = new Thread(responser);
            t.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Socket getSocket(){
        return socket;
    }


    public synchronized boolean getTag(){
        if(tag){
            tag = false;
            return true;
        }
        else return false;
    }

    public synchronized void releaseTag(){
        tag = true;
    }


    private void reconnect(){
        try {

            socket.connect(new InetSocketAddress(targetID,FileConstant.TCPPORT), 10000);
            type = 0;

        }catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            callBack.connectionFailure(targetID);
        }
    }

    /**
     * 发送文件更新的通知（是收到来自别的设备的更新）
     */
    public synchronized void sendFileUpdateInform(FileMetaData fileMetaData){
        IOMessage msg = new IOMessage();
        msg.type = FILEUPDATE;
        msg.obj1 = fileMetaData;
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private void sendFileUpdateInform(IOMessage msg){
        try {
            Assert.assertNotNull(msg.obj1);
            oos.writeUTF(FileTransferHeader.sendFileUpdateHeader());
            oos.writeUnshared(msg.obj1);
            oos.flush();
            oos.reset();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * 发送文件数据,先发送文件的metaData，然后发送data
     * @param absolutePath 文件的绝对路径
     */
    public synchronized void sendFileData(FileMetaData metaData,String absolutePath){
        IOMessage msg = new IOMessage();
        msg.type = FILEDATA;
        msg.obj1 = metaData;
        msg.sArg1 = absolutePath;
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private void sendFileData(IOMessage msg){
        try {
            //testConnection();
            Assert.assertNotNull(msg.obj1);
            Assert.assertNotNull(msg.sArg1);
            File file = new File(msg.sArg1);
            //DataInputStream disfile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            FileInputStream fis = new FileInputStream(file);
           // Assert.assertNotNull("File inputStream for " + ((FileMetaData)msg.obj1).getFileID() + " is null",disfile);
            //发送文件前的处理
            oos.writeUTF(FileTransferHeader.sendFileDataHeader(file.length()));
            //先发送文件的metaData
            oos.writeUnshared(msg.obj1);
            oos.flush();
            oos.reset();
            //发送文件的数据
            int bufferSize = 65536;
            byte[] buf = new byte[bufferSize];
            int read = 0;
            //OutputStream socketOS = socket.getOutputStream();
            while((read = fis.read(buf)) > -1){
                //socketOS.write(buf,0,read);
                oos.write(buf,0,read);
                oos.flush();
            }
           // disfile.close();
            fis.close();
           // socketOS.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void sendFileVectorClock(VectorClock vectorClock, String relativePath,
                                                 String tag) {
        // TODO Auto-generated method stub

    }

    /**
     *发送文件的vectorClock
     * @param vectorClock
     * @param
     * @param relativePath
     */
    public synchronized void sendFileVersion(VectorClock vectorClock,FileMetaData metaData,String relativePath,String tag){
        IOMessage msg = new IOMessage();
        msg.type = VERSION;
        msg.obj1 = vectorClock;
        msg.obj2 = metaData;
        msg.sArg1 = relativePath;
        msg.sArg2 = tag;

        try {
            messageQueue.put(msg);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private synchronized void sendFileVersion(IOMessage msg){
        try {
            //testConnection();
            Assert.assertNotNull(msg.obj1);
            Assert.assertNotNull(msg.obj2);
            Assert.assertNotNull(msg.sArg1);
            Assert.assertNotNull(msg.sArg2);
            oos.writeUTF(FileTransferHeader.sendFileVersionHeader(msg.sArg1, msg.sArg2));
            oos.writeUnshared(msg.obj1);
            oos.writeUnshared(msg.obj2);
            oos.flush();
            oos.reset();
            System.out.println("socketIO has sended vectorClock,relativePath is" + msg.sArg1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 发送一条指令
     * @param cmd
     */
    public synchronized void sendCommand(String cmd){
        IOMessage msg = new IOMessage();
        msg.type = COMMAND;
        msg.sArg1 = cmd;
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    private synchronized void sendCommand(IOMessage msg){
        Assert.assertNotNull(msg.sArg1);
        try {
            //testConnection();
            oos.writeUTF(msg.sArg1);
            oos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 发送断连信息
     */
    public synchronized void sendDisconnectMsg(){
        IOMessage msg = new IOMessage();
        msg.type = DISCONNECT;
        msg.sArg1 = FileTransferHeader.disconnect();
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private synchronized void sendDisconnectMsg(IOMessage msg){
        Assert.assertNotNull(msg.sArg1);
        try {
            //testConnection();
            oos.writeUTF(msg.sArg1);
            oos.flush();
            //释放资源
            close();
            callBack.hasDisconnected(targetID);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 发送同步就绪消息
     */
    public synchronized void sendSynReady() {
        // TODO Auto-generated method stub
        IOMessage msg = new IOMessage();
        msg.type = SYNREADY;
        msg.sArg1 = FileTransferHeader.synReady();
        try {
            System.out.println("----SocketIO----put synready message");
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private synchronized void sendSynReady(IOMessage msg){
        try {
            oos.writeUTF(msg.sArg1);
            oos.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public synchronized void sendHeartBeat() {
        // TODO Auto-generated method stub
        IOMessage msg = new IOMessage();
        msg.type = HEARTBEAT;
        msg.sArg1 = FileTransferHeader.heartBeat();
        try {
            messageQueue.put(msg);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void sendHeartBeat(IOMessage msg){
        try {
            oos.writeUTF(msg.sArg1);
            oos.flush();
            //System.out.println("----SocketIO----send heart beat");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("----SocketIO----send heart beat failure");
            reconnect();
        }
    }


    public String getTargetID(){
        return targetID;
    }

    public int getType(){
        return type;
    }


    /**
     * 关闭socket
     */
    public synchronized void close(){
        try {
            tag = false;
            dis.close();
            dos.close();
            oos.close();
            socket.close();
            responser.stop();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            System.out.println("has closed the socket");
        }


    }

    public void run() {
        // TODO Auto-generated method stub
        while(tag){
            try {
                IOMessage msg = messageQueue.take();
                switch(msg.type){
                    case FILEDATA:{
                        sendFileData(msg);
                    }break;
                    case FILEUPDATE:{
                        sendFileUpdateInform(msg);
                    }break;
                    case VERSION:{
                        sendFileVersion(msg);
                    }break;
                    case COMMAND:{
                        sendCommand(msg);
                    }break;
                    case DISCONNECT:{
                        sendDisconnectMsg(msg);
                    }break;
                    case SYNREADY:{
                        sendSynReady(msg);
                    }break;
                    case HEARTBEAT:{
                        sendHeartBeat(msg);
                    }break;
                    default:{

                    }
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    class IOMessage{

        int type;
        String sArg1,sArg2,sArg3;
        Object obj1,obj2,obj3;

        IOMessage(){

        }
    }
}