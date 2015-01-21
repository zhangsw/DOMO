package com.example.zhangsw.sharefile.PsyLine;


import com.example.zhangsw.sharefile.Util.FileConstant;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PsyTcpServer{

    private ServerSocket serverSocket = null;
    private ExecutorService executorServiceSo = null;
    private final int POOL_SIZE = 8;				//线程池大小
    private boolean tag;
    private boolean serverStart;
    private PsyLine psyline;

    //private String savePath;

    public PsyTcpServer(PsyLine p) throws IOException{
        int cpuCount = Runtime.getRuntime().availableProcessors();
        executorServiceSo = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
        serverSocket = new ServerSocket(FileConstant.TCPPORT);
        tag = true;
        serverStart = false;
        psyline = p;
    }

    public synchronized void startServer(){
        if(serverStart) return;
        new Thread(){
            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
                System.out.println("server has been started");
                while(tag){
                    try {
                        // 接收客户连接,只要客户进行了连接,就会触发accept()从而建立连接
                        Socket socket = serverSocket.accept();
                        System.out.println("accept a socket");
                        InetAddress ia = socket.getInetAddress();
                        String ip = ia.getHostAddress();
                        int index = psyline.getIndexByTargetID(ip);
                        if(index != -1){
                            //存在该设备
                            SocketIO si= psyline.getSocketInf(index);
                            si.close();
                            psyline.removeSocket(si);
                        }
                        SocketIO si = new SocketIO(ip,socket,1,psyline);
                        executorServiceSo.execute(si);
                        psyline.addSocket(si);
							/*
							Responser res = new Responser(socket,psyline);
							executorServiceRe.execute(res);//Responser类的定义见后面
							*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        serverStart = true;
    }

    public synchronized void stopServer(){
        tag = false;
        serverStart = false;
    }

    /**
     * 用于测试server状态
     */
    public void serverState(){
        System.out.println(serverSocket);
        System.out.println(serverSocket.isClosed());
    }
}