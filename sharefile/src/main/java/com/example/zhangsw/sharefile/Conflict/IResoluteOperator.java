package com.example.zhangsw.sharefile.Conflict;


import com.example.zhangsw.sharefile.FileSystem.MyFileObserver;

public interface IResoluteOperator {
	
	public void addConflictFile(MyFileObserver ob, String path);
	
	public void addConflictNode(String path);
		
	public void removeConflictNode(String path);

    /**
     * 新建文件
     */
	public void createEmptyFileNode(String path, String fileID);
	
	public void deleteFile();
	
	public void mergeFile();

    /**
     * 请求文件
     * @param target
     * @param fileID
     * @param relativePath
     */
	public void fetchFile(String target, String fileID, String relativePath);

    /**
     * 重命名本地文件
     * @param oldRelativePath	文件的旧的相对路径
     * @param newRelativePath	文件的新的相对路径
     */
	public void renameLocalFile(String oldRelativePath, String newRelativePath);
}
