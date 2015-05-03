package com.example.zhangsw.sharefile.Consistency;

import com.example.zhangsw.sharefile.FileSystem.FileManager;
import com.example.zhangsw.sharefile.FileSystem.FileMetaData;
import com.example.zhangsw.sharefile.MemoryManager.MemoryManager;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;

import java.io.File;

/**
 * Created by zhangsw on 2015-04-21.
 */
public class PullPolicy implements ConsistencyPolicy {

    FileManager fm;
    MemoryManager mm;
    public PullPolicy(FileManager fm,MemoryManager mm){
        this.fm = fm;
        this.mm = mm;
      //  fm.setDispenseMsgTag(false);
    }
    @Override
    public void readFile(String path) {

        mm.fetchFileVersion(path);

    }

    @Override
    public FileMetaData readFileMetaData(String path) {
        return null;
    }

    @Override
    public void writeFile(String path, int offset, String content) {

    }

    @Override
    public void append(String path, String content) {
        fm.append(path,content);
    }

    @Override
    public void deleteFile(String path) {

    }

    @Override
    public void moveFile(String path, String dest) {

    }
}
