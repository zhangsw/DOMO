package com.example.zhangsw.sharefile.MemoryManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.example.zhangsw.sharefile.FileMonitor.IEventTranslate;
import com.example.zhangsw.sharefile.FileSystem.IFileManager;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileUtil;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;

public class ShareInfo{
    private String sharedFilePath;
    private String target;
    private int type;		//共享类型，暂时为simple
    private ConsistencyRule conRule;
    private List fileList;
    private Handler handlerFa;
    private IFileManager fom;
    private Handler handlerCh;
    private HandlerThread handlerThread;
    private String threadName;
    public ShareInfo(){

    }

    public ShareInfo(String sharedFilePath,String target,int type,Handler handler,IFileManager fom){
        this.sharedFilePath = sharedFilePath;
        this.target = target;
        this.type = type;
        this.fom = fom;

        fileList =new ArrayList<FileInf>();
        FileUtil.getFileInfList(sharedFilePath,fileList);
        handlerFa = handler;

        switch(type){
            case FileConstant.SIMPLECM:{
                conRule = new SimpleConsistencyRule();
                //System.out.println("simpleCm has been started----");
            }break;
        }
        Assert.assertNotNull("----ShareInfo----handlerFa is null", handlerFa);

        threadName = target + "ShareThread";
        handlerThread = new HandlerThread(threadName);
        handlerThread.start();
        handlerCh = new MyHandler(handlerThread.getLooper());
        this.fom.registerObserver(target, sharedFilePath, handlerCh, null);

    }

    public String getSharedFilePath(){
        return sharedFilePath;
    }

    public String getTarget(){
        return target;
    }

    public int getType(){
        return type;
    }

    public void setSharedFilePath(String sharedFilePath){
        this.sharedFilePath = sharedFilePath;
    }

    public void setTarget(String target){
        this.target = target;
    }

    public void setType(int type){
        this.type = type;
    }

    public List getFileList(){
        return fileList;
    }

    public void setFileList(List fileList){
        this.fileList = fileList;
    }

    public boolean IsContainFile(String filename,String MD5){
        System.out.println("fileList's size is "+fileList.size());
        System.out.println(filename + "___" + MD5);
        for(int i=0;i<fileList.size();i++){
            FileInf fi = (FileInf)fileList.get(i);
            if((fi.getFileName().equals(filename)) && (fi.getFileMD5().equals(MD5)))
                return true;
        }
        return false;
    }

    public boolean IsContainFile(String filename){
        for(int i=0;i<fileList.size();i++){
            FileInf fi = (FileInf)fileList.get(i);
            if(fi.getFileName().equals(filename))
                return true;
        }
        return false;
    }

    private boolean isSubDirectory(String path){
        return path.startsWith(sharedFilePath + "/");
    }

    private void fileModifiedMessage(String target,int type,String absoluteFilePath,String relativeFilePath,Handler handler){
        Message m1 = handler.obtainMessage();
        MessageObj temp = new MessageObj(type);
        temp.setTarget(target);
        temp.setFilepath(absoluteFilePath);
        temp.setFileName(FileUtil.getFileNameFromPath(absoluteFilePath));
        temp.setRelativeFilepath(relativeFilePath);
        m1.obj = temp;
        handler.sendMessage(m1);
    }

    private void renameOrMoveMessage(String target,int type,String relativeFilePath,String newRelativeFilePath,Handler handler){
        Message m1 = handler.obtainMessage();
        MessageObj temp = new MessageObj(type);
        temp.setTarget(target);
        temp.setRelativeFilepath(relativeFilePath);
        temp.setNewRelativeFilepath(newRelativeFilePath);
        m1.obj = temp;
        handler.sendMessage(m1);
    }

    private void createDirMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
        dirModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
    }

    private void dirModifiedMessage(String target,int type,String absoluteDirPath,String relativeDirPath,Handler handler){
        fileModifiedMessage(target,type,absoluteDirPath,relativeDirPath,handler);
    }

    private void deleteMessage(String target,int type,String relativeFilePath,Handler handler){
        Message m1 = handler.obtainMessage();
        MessageObj temp = new MessageObj(type);
        temp.setTarget(target);
        temp.setRelativeFilepath(relativeFilePath);
        m1.obj = temp;
        handler.sendMessage(m1);
    }

    private String getFatherPath(String path){
        int index  = path.lastIndexOf("/");
        return path.substring(0, index);
    }

    private class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            super.handleMessage(msg);
            String obj = msg.obj.toString();
            switch(msg.what){
                case IEventTranslate.FILEMODIFIED:				//文件被修改
                case IEventTranslate.COVERFILE:				//文件被覆盖
                case IEventTranslate.FILEMOVETO:{					//文件移入
                    if(conRule.sendFile(obj)){
                        String relativePath = obj.substring(FileConstant.DEFAULTSHAREPATH.length());
                        fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,obj,relativePath,handlerFa);
                    }
                }break;

                case IEventTranslate.FILEDELETE:				//文件删除
                case IEventTranslate.FILEMOVEFROM:{				//文件移入
                    if(conRule.deleteFile(obj)){
                        String relativePath = obj.substring(FileConstant.DEFAULTSHAREPATH.length());
                        deleteMessage(target,FileConstant.DELETEFILEMESSAGE,relativePath,handlerFa);
                    }
                }break;

                case IEventTranslate.FILERENAMEORMOVE:{			//文件重命名或者移动
                    int index = obj.indexOf("$/@@/$");
                    String oldPath = obj.substring(0, index);
                    String newPath = obj.substring(index+6);
                    if(getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.renameFile(oldPath,newPath)){
                        renameOrMoveMessage(target,FileConstant.RENAMEFILEMESSAGE,oldPath.substring(FileConstant.DEFAULTSHAREPATH.length()),newPath.substring(FileConstant.DEFAULTSHAREPATH.length()),handlerFa);
                    }
                    else if(!getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.moveFile(oldPath,newPath))
                        renameOrMoveMessage(target,FileConstant.MOVEFILEMESSAGE,oldPath.substring(FileConstant.DEFAULTSHAREPATH.length()),newPath.substring(FileConstant.DEFAULTSHAREPATH.length()),handlerFa);
				/*
				else if(isSubDirectory(oldPath) && conRule.deleteFile(oldPath)){

					deleteMessage(target,FileConstant.DELETEFILEMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
				}
				else if(isSubDirectory(newPath) && conRule.sendFile(newPath))
					fileModifiedMessage(target,FileConstant.SENDFILEMESSAGE,newPath,newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					*/
                }break;

                case IEventTranslate.DIRCREATE:{			//文件夹创建
                    if(conRule.createDirectory(obj)){
                        System.out.println("dir has been created");
                        String relativePath = obj.substring(FileConstant.DEFAULTSHAREPATH.length());
                        createDirMessage(target,FileConstant.CREATEDIRMESSAGE,obj,relativePath,handlerFa);
                    }
                }break;

                case IEventTranslate.DIRDELETE:				//文件夹删除
                case IEventTranslate.DIRMOVEFROM:{			//文件夹移除
                    if(conRule.deleteDirectory(obj)){
                        String relativePath = obj.substring(FileConstant.DEFAULTSHAREPATH.length());
                        deleteMessage(target,FileConstant.DELETEDIRMESSAGE,relativePath,handlerFa);
                    }
                }break;

                case IEventTranslate.DIRMOVETO:{			//文件夹移入
                    if(conRule.sendDirectory(obj)){
                        System.out.println("dir has been move to");
                        String relativePath = obj.substring(FileConstant.DEFAULTSHAREPATH.length());
                        dirModifiedMessage(target,FileConstant.SENDDIRMESSAGE,obj,relativePath,handlerFa);
                    }
                }break;

                case IEventTranslate.DIRRENAMEORMOVE:{			//文件夹重命名或移动
                    int index = obj.indexOf("$/@@/$");
                    String oldPath = obj.substring(0, index);
                    String newPath = obj.substring(index+6);
                    if(getFatherPath(oldPath).equals(getFatherPath(newPath)) && conRule.renameDirectory(oldPath,newPath)){
                        renameOrMoveMessage(target,FileConstant.RENAMEDIRMESSAGE,oldPath.substring(FileConstant.DEFAULTSHAREPATH.length()),newPath.substring(FileConstant.DEFAULTSHAREPATH.length()),handlerFa);
                    }
                    else if(conRule.moveDirectory(oldPath,newPath))
                        renameOrMoveMessage(target,FileConstant.MOVEDIRMESSAGE,oldPath.substring(FileConstant.DEFAULTSHAREPATH.length()),newPath.substring(FileConstant.DEFAULTSHAREPATH.length()),handlerFa);
				/*
				else if(isSubDirectory(oldPath) && conRule.deleteDirectory(oldPath))
					deleteMessage(target,FileConstant.DELETEDIRMESSAGE,oldPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
				else if(isSubDirectory(newPath) && conRule.sendDirectory(newPath))
					dirModifiedMessage(target,FileConstant.CREATEDIRMESSAGE,newPath,newPath.substring(FileConstant.ROOTPATH.length()),handlerFa);
					*/
                }break;
            }
        }
    }

}