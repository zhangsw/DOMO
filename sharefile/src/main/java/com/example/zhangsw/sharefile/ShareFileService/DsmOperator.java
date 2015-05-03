package com.example.zhangsw.sharefile.ShareFileService;

import com.example.zhangsw.sharefile.FileSystem.DSMFileNode;

/**
 * Created by zhangsw on 2015-04-07.
 * 系统需要提供给外部的接口
 */
public interface DsmOperator extends OpForTest {
    public DSMFileNode read(String filePath);

    /**
     * 在文件后部添加内容
     * @param filePath  文件路径
     * @param content   追加的文件内容
     */
    public void write(String filePath,String content);


}
