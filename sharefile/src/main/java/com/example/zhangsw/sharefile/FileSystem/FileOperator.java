package com.example.zhangsw.sharefile.FileSystem;

import java.io.File;

/**
 * Created by zhangsw on 2015-04-26.
 */
public interface FileOperator {
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

    public void append(String path, String content);

    public void delete(String path);

    public void move(String path,String dest);
}
