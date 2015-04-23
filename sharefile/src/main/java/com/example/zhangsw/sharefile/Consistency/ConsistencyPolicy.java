package com.example.zhangsw.sharefile.Consistency;

import com.example.zhangsw.sharefile.FileSystem.DSMFileNode;
import com.example.zhangsw.sharefile.FileSystem.FileMetaData;

import java.io.File;

/**
 * Created by zhangsw on 2015-04-21.
 */
public interface ConsistencyPolicy {

    /**
     * 读文件
     * @param path  文件路径
     * @return 文件句柄
     */
    public File readFile(String path);

    /**
     * 读文件的meta data
     * @param path  文件路径
     * @return  文件的meta data
     */
    public FileMetaData readFileMetaData(String path);

    /**
     *
     * @param path 路径
     * @param offset    偏移量
     * @param content   内容
     */
    public void writeFile(String path,int offset,String content);

    public void deleteFile(String path);

    public void moveFile(String path,String dest);
}
