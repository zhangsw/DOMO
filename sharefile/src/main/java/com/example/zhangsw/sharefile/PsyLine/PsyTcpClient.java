package com.example.zhangsw.sharefile.PsyLine;


import com.example.zhangsw.sharefile.Util.FileConstant;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PsyTcpClient{

    private int timeout;
    private ExecutorService executorServiceSo = null;
    private final int POOL_SIZE = 8;				//线程池大小
    private PsyLine psyline;


    public PsyTcpClient(PsyLine p){
        timeout = 10000;
        int cpuCount = Runtime.getRuntime().availableProcessors();
        executorServiceSo = Executors.newFixedThreadPool(cpuCount*POOL_SIZE);
        psyline = p;
    }

    public boolean connect(String ip){
        if(psyline.getIndexByTargetID(ip) == -1){				//还未同该设备进行连接
            System.out.println("----PsyTcpClient----enter psytcpclient connect");
            Socket s = new Socket();
            try {
                s.connect(new InetSocketAddress(ip,FileConstant.TCPPORT), timeout);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            SocketIO socketIO = new SocketIO(ip,s,0,psyline);
            executorServiceSo.execute(socketIO);
            psyline.addSocket(socketIO);
			/*
			Responser res = new Responser(s,psyline);
			executorServiceRe.execute(res);*/
            return true;
        }
        else{
            System.out.println("----PsyTcpClient----device existed");
        }
        return false;
    }
}
