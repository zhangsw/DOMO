package com.example.zhangsw.sharefile.PsyLine;

import com.example.zhangsw.sharefile.Util.FileConstant;

public  class FileTransferHeader {

    /**
     * 发送文件数据前的消息
     * @param fileLength	文件长度
     * @param fileID	文件id
     * @param relativePath	文件相对路径
     * @return
     */
    public static String sendFileDataHeader(long fileLength){
        return FileConstant.FILEDATA + "$SIZE$" + fileLength + "\n";
    }

    /**
     * 发送文件version前的消息
     * @param fileID	文件id
     * @param relativePath	文件相对路径
     * @return
     */
    public static String sendFileVersionHeader(String relativePath,String tag){
        return FileConstant.FILEVERSION +"$TAG$" + tag + "$PATH$" + relativePath + "\n";
    }

    public static String sendFileUpdateHeader(){
        return FileConstant.FILEUPDATE + "\n";
    }

    public static String disconnect(){
        return FileConstant.DISCONNECT + "\n";
    }

    /**
     * 本地已初始化version完毕，可以进行同步
     * @return
     */
    public static String synReady(){
        return FileConstant.SYNREADY + "\n";
    }

    public static String heartBeat(){
        return FileConstant.HEARTBEAT + "\n";
    }

    /**
     * 删除文件命令
     * @param relativePath
     * @return
     */
    public static String deleteFileCmd(String relativePath){
        return FileConstant.DELETEFILE + "$PATH$" + relativePath+"\n";
    }

    public static String fetchFileCmd(String relativePath){
        return FileConstant.ASKFILE + "$PATH$" + relativePath + "\n";
    }

    public static String makeDirCmd(String relativePath){
        return FileConstant.MAKEDIR + "$PATH$" + relativePath + "\n";
    }

    public static String renameFileCmd(String oldRelativePath,String newRelativePath){
        return FileConstant.RENAMEFILE + "$OLDPATH$" + oldRelativePath + "$NEWPATH$" + newRelativePath + "\n";
    }
}
