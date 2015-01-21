package com.example.zhangsw.sharefile.MemoryManager;

public interface IMemoryManager {

    public void fetchFile(String target,String relativePath);

    /**
     * 重命名本地文件
     * @param oldRelativePath	文件旧的相对路径
     * @param newRelativePath	文件新的相对路径
     */
    public void renameLocalFile(String oldRelativePath,String newRelativePath);

    /**
     * 新建空的文件（结点）
     * @param path	路径
     * @param fileID 文件的id
     */
    public void createEmptyFileNode(String path,String fileID);

}
