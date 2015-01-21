package com.example.zhangsw.sharefile.PsyLine;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.FileSystem.VectorClock;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileUtil;

import junit.framework.Assert;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Responser implements Runnable{
    private Socket socket = null;
    private String ip;
    private InputStream inputStream;
    private String line;
    private DataOutputStream dos;
    private String savePath;
    private int bufferSize = 8192;
    private byte[] buf;
    private FileTransferCallBack callBack;
    private ObjectInputStream ois;
    private boolean tag;

    private static final int SOCKETTIMEOUT = 30;

    public Responser(Socket socket,FileTransferCallBack callBack){
        this.socket=socket;
        this.callBack = callBack;
        ip = socket.getInetAddress().getHostAddress();
        tag = true;
        try {
            inputStream = socket.getInputStream();
            ois = new ObjectInputStream(inputStream);
            savePath = FileConstant.DEFAULTSAVEPATH;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop(){
        tag = false;
    }

    public void run() {
        // TODO
        try{
            socket.setSoTimeout(SOCKETTIMEOUT*1000);
            while(tag){
                if(socket.isClosed()){
                    System.out.println("socket is closed");
                    break;
                }
                else{
                    line = ois.readUTF();
                    //System.out.println("receive line,msg is:" + line);
                    if(line!=null){
                        int type = Integer.parseInt(line.substring(0,2));
                        switch(type){
                            case FileConstant.FILEDATA:{
                                System.out.println("receive filedata------");
                                long passedLength = 0;
                                long size = Long.parseLong(line.substring(line.indexOf("$SIZE$")+6,line.length()-1));
                                FileMetaData fileMetaData = (FileMetaData)ois.readUnshared();

                                String Path = savePath + "/" + FileUtil.getFileNameFromPath(fileMetaData.getRelativePath());

                                File file = new File(Path);
                                File parent = file.getParentFile();
                                if(!parent.exists()) parent.mkdirs();

                                System.out.println("size is " + size + "\n" + "relativeFilePath is " + fileMetaData.getRelativePath());
                                System.out.println("savePath is "+ Path);
                                buf = new byte[bufferSize];
                                try{
                                    dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(Path)));
                                }catch(IOException e){
                                    e.printStackTrace();
                                }
                                while(true){
                                    int read = 0;
                                    if (ois != null){
                                        try{
                                            if(passedLength >= size) break;
                                            read = ois.read(buf);
                                        }catch(IOException e){
                                            e.printStackTrace();
                                        }

                                        if(read != -1){
                                            passedLength +=read;

                                            try{
                                                dos.write(buf,0,read);
                                                dos.flush();
                                            }catch(IOException e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                                if(dos != null){
                                    try{
                                        dos.close();
                                        System.out.println("dos has been closed");
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                }

                                callBack.receiveFileData(ip,fileMetaData,file);
                            }break;

                            case FileConstant.FILEINF:{									//接收到的是文件信息
                                System.out.println("receive fileinf---------");
                                String relativePath = line.substring(line.indexOf("$PATH$")+6, line.indexOf("$MD5$"));
                                String MD5 = line.substring(line.indexOf("$MD5$")+5, line.length()-1);
                                callBack.receiveFileInf(ip,relativePath, FileConstant.DEFAULTSHAREPATH+relativePath, MD5);
                            }break;

                            case FileConstant.ASKFILE:{									//接收到的是请求文件信息
                                System.out.println("receive askfile---------");
                                String relativePath = line.substring(line.indexOf("$PATH$")+6, line.length()-1);
                                callBack.receiveAskFile(ip, relativePath, FileConstant.DEFAULTSHAREPATH+relativePath);
                            }break;

                            case FileConstant.DELETEFILE:{								//接收到的是删除文件信息
                                System.out.println("receive deletefile-----");
                                String relativeFilePath = line.substring(line.indexOf("$PATH$")+6, line.length()-1);
                                callBack.receiveDeleteFile(ip, FileConstant.DEFAULTSHAREPATH+relativeFilePath);
                            }break;

                            case FileConstant.RENAMEFILE:{								//收到的是重命名文件信息
                                System.out.println("receive renamefile-----");
                                String oldRelativeFilePath = line.substring(line.indexOf("$OLDPATH$")+9, line.indexOf("$NEWPATH$"));
                                String newRelativeFilePath = line.substring(line.indexOf("$NEWPATH$")+9, line.length()-1);
                                callBack.receiveRenameFile(ip,FileConstant.DEFAULTSHAREPATH+oldRelativeFilePath,FileConstant.DEFAULTSHAREPATH+newRelativeFilePath);
                            }break;

                            case FileConstant.MAKEDIR:{
                                System.out.println("receive makedir--------");
                                String relativePath = line.substring(line.indexOf("$PATH$")+6,line.length()-1);
                                System.out.println("relativePath is "+relativePath);
                                callBack.receiveMakeDir(ip, FileConstant.DEFAULTSHAREPATH+relativePath);
                            }break;

                            case FileConstant.FILEVERSION:{
                                try {
                                    VectorClock versionMap = (VectorClock)ois.readUnshared();
                                    FileMetaData metaData = (FileMetaData)ois.readUnshared();
                                    Assert.assertNotNull("----Responser----Error,versionMap is null",versionMap);
                                    if(versionMap == null) System.out.println("----Responser----versionMap is null");
                                    String tag = line.substring(line.indexOf("$TAG$")+5, line.indexOf("$PATH$"));
                                    String relativePath = line.substring(line.indexOf("$PATH$")+6,line.length()-1);
                                    callBack.receiveVersion(ip, versionMap, metaData, relativePath,tag);
                                } catch (ClassNotFoundException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }break;

                            case FileConstant.FILEUPDATE:{
                                FileMetaData fileMetaData = (FileMetaData)ois.readUnshared();
                                callBack.receiveFileUpdate(ip,fileMetaData);
                            }break;

                            case FileConstant.DISCONNECT:{
                                System.out.println("receive disconnect--------");
                                callBack.receiveDisconnect(ip);
                            }break;

                            case FileConstant.SYNREADY:{
                                System.out.println("----Responser----receive Synready message----");
                                callBack.receiveSynReady(ip);
                            }break;

                            case FileConstant.HEARTBEAT:{
                                //System.out.println("----Responser----receive heart beat----");
                            }break;
                            default:{

                                System.out.println("meaningless command:,command's number is " + type);
                            }
                        }
                    }
                }
            }
        }catch (ClassNotFoundException | SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SocketTimeoutException e1){
            //
            e1.printStackTrace();
            //读超时，认为同对方丢失连接
            System.out.println("----Responser----connection failure");
            callBack.connectionFailure(ip);

        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

}
