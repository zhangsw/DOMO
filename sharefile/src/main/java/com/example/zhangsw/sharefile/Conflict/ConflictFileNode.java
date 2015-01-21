package com.example.zhangsw.sharefile.Conflict;

import com.example.zhangsw.sharefile.FileSystem.MyFileObserver;

import java.util.ArrayList;
import java.util.List;


/**
 * 用于保存相互间冲突的文件
 * @author zhangsw
 *
 */
public class ConflictFileNode {

    private final String path;
    private final List<MyFileObserver> conflictFile;

    public ConflictFileNode(String path){
        this.path = path;
        conflictFile = new ArrayList<MyFileObserver>();
    }

    /**
     * 添加一个文件
     * @param ob
     */
    public void add(MyFileObserver ob){
        if(!fileExist(ob))
            conflictFile.add(ob);
    }

    /**
     * 删除一个文件
     * @param ob
     */
    public void remove(MyFileObserver ob){
        int index = getIndex(ob);
        if(index >= 0) conflictFile.remove(index);
    }

    /**
     * 是否为空
     * @return
     */
    public boolean isEmpty(){
        return conflictFile.size() == 0;
    }

    /**
     * 冲突文件的个数
     * @return
     */
    public int size(){
        return conflictFile.size();
    }

    public MyFileObserver getFileObserver(int index){
        if((conflictFile.size() >= index) &&(index >= 0)){
            return conflictFile.get(index);
        }
        else return null;
    }

    public MyFileObserver getLocalFileObserver(){
        return conflictFile.get(0);
    }

    private boolean fileExist(MyFileObserver ob){
        for(MyFileObserver fileObserver:conflictFile)
            if(fileObserver.getPath().equals(ob.getPath())) return true;
        return false;
    }

    /**
     * 获取文件下标，如果文件不存在，返回-1
     * @param ob
     * @return
     */
    private int getIndex(MyFileObserver ob){
        for(int i=0;i<conflictFile.size();i++){
            if(conflictFile.get(i).getPath().equals(ob.getPath()))
                return i;
        }
        return -1;
    }
}
