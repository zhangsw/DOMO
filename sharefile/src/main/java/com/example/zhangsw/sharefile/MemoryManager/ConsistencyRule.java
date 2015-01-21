package com.example.zhangsw.sharefile.MemoryManager;

public abstract class ConsistencyRule {

    /*删除文件*/
    public abstract boolean deleteFile(String path);

    /*发送文件数据*/
    public abstract boolean sendFile(String path);

    /*重命名文件*/
    public abstract boolean renameFile(String oldPath, String newPath);

    /*移动文件*/
    public abstract boolean moveFile(String oldPath, String newPath);

    /*删除文件夹*/
    public abstract boolean deleteDirectory(String path);

    /*新建文件夹*/
    public abstract boolean createDirectory(String path);

    /*重命名文件夹*/
    public abstract boolean renameDirectory(String oldPath, String newPath);

    /*移动文件夹*/
    public abstract boolean moveDirectory(String oldPath,String newPath);

    /*发送文件夹*/
    public abstract boolean sendDirectory(String path);

}
