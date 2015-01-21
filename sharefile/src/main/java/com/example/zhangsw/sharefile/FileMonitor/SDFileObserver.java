package com.example.zhangsw.sharefile.FileMonitor;


import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

import com.example.zhangsw.sharefile.Util.FileConstant;

public class SDFileObserver extends FileObserver{

    private Handler handler;	//全局消息handler，所有SDFileObserver共用
    private String path;


    public SDFileObserver(String path, int mask, Handler handler) {
        super(path, mask);
        this.handler = handler;
        this.path = path;
    }

    public SDFileObserver(String path, Handler handler) {
        super(path);
        this.handler = handler;
        this.path = path;
        // TODO Auto-generated constructor stub
    }

    public void updatePath(String newpath){
        path = newpath;
    }

    @Override
    public void onEvent(int event, String path) {
        // TODO Auto-generated method stub

        switch(event){
            case FileObserver.ACCESS:{
                //System.out.println(path+"has been Accessed");
            }break;

            case FileObserver.ATTRIB:{
                //System.out.println(path+"has been Attribed");
            }break;

            case FileObserver.CLOSE_NOWRITE:{
                //System.out.println(path+"has been close_nowrite");
            }break;

            case FileObserver.MOVE_SELF:{
                //System.out.println(this.path + ":" + path + "has been move_self");

                Message msg = new Message();
                msg.obj = this.path;
                msg.what = FileObserver.MOVE_SELF;
                handler.sendMessage(msg);

            }break;

            case FileObserver.DELETE_SELF:{
                //System.out.println(this.path + ":" +path+"has been delete_self");

                Message msg = new Message();
                msg.obj = this.path;
                msg.what = FileObserver.DELETE_SELF;
                handler.sendMessage(msg);


            }break;

            case FileObserver.OPEN:{
                //System.out.println(path+"has been open");
            }break;

            case FileObserver.DELETE:{					//文件被删除
                //System.out.println(this.path + ":" +path+"has been deleted");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.DELETE;
                handler.sendMessage(msg);

            }break;

            case FileObserver.CREATE:{					//有新文件被创建
                //System.out.println(this.path + ":" +path+"has been created");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.CREATE;
                handler.sendMessage(msg);

            }break;

            case FileObserver.MODIFY:{				//文件被修改
                //System.out.println(path+"has been modified");
			/*
			Message msg = new Message();
			msg.obj = path;
			msg.what = FileObserver.MODIFY;
			handler.sendMessage(msg);
			*/
            }break;

            case FileObserver.MOVED_FROM:{
                //System.out.println(this.path + ":" +path+" has been moved from");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.MOVED_FROM;

                handler.sendMessage(msg);


            }break;

            case FileObserver.MOVED_TO:{
                //System.out.println(this.path + ":" +path+" has been moved to");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.MOVED_TO;

                handler.sendMessage(msg);

            }break;

            case FileObserver.CLOSE_WRITE:{
                //System.out.println(this.path + ":" +path+"has been written and closed");
                if(path != null){
                    Message msg = new Message();
                    msg.obj = this.path + "/" + path;
                    msg.what = FileObserver.CLOSE_WRITE;
                    handler.sendMessage(msg);
                }


            }break;

            case (FileObserver.MODIFY|FileConstant.ISDIR):{
                //System.out.println(path+" dir has been modified");

            }break;

            case (FileObserver.CREATE|FileConstant.ISDIR):{
                //	System.out.println(this.path + ":" +path+" dir has been created");


                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.CREATE|FileConstant.ISDIR;
                handler.sendMessage(msg);


            }break;

            case (FileObserver.DELETE|FileConstant.ISDIR):{
                //System.out.println(this.path + ":" +path + " dir has been deleted");


                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.DELETE|FileConstant.ISDIR;
                handler.sendMessage(msg);

            }

            case FileObserver.MOVED_FROM|FileConstant.ISDIR:{
                //System.out.println(this.path + ":" +path+" dir has been moved from");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.MOVED_FROM|FileConstant.ISDIR;
                handler.sendMessage(msg);


            }break;

            case FileObserver.MOVED_TO|FileConstant.ISDIR:{
                //System.out.println(this.path + ":" +path+" dir has been moved to");

                Message msg = new Message();
                msg.obj = this.path + "/" + path;
                msg.what = FileObserver.MOVED_TO|FileConstant.ISDIR;
                handler.sendMessage(msg);

            }break;

            case FileObserver.MOVE_SELF|FileConstant.ISDIR:{
                //System.out.println(this.path + ":" + path +" dir has been move_SELF");

                Message msg = new Message();
                msg.obj = this.path;
                msg.what = FileObserver.MOVE_SELF|FileConstant.ISDIR;
                handler.sendMessage(msg);

            }break;

            case FileObserver.ATTRIB|FileConstant.ISDIR:{
                //System.out.println(path + " dir has been attrib");
            }break;

            case FileObserver.DELETE_SELF|FileConstant.ISDIR:{
                //System.out.println(this.path + ":" + path + " dir has been deleted_self");
            }break;

            case FileObserver.ALL_EVENTS:{
                //System.out.println(this.path+path+" all_events");
            }break;
            default:{
                //System.out.println(this.path+path+"has something to do");
            }break;
        }

    }



}