package com.example.zhangsw.sharefile.Consistency;

import com.example.zhangsw.sharefile.FileSystem.FileMetaData;

import java.io.File;

/**
 * Created by zhangsw on 2015-04-21.
 */
public class PullPolicy implements ConsistencyPolicy {
    @Override
    public File readFile(String path) {
        return null;
    }

    @Override
    public FileMetaData readFileMetaData(String path) {
        return null;
    }

    @Override
    public void writeFile(String path, int offset, String content) {

    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void moveFile(String path, String dest) {

    }
}
