package com.example.zhangsw.sharefile.MemoryManager;

public class FileInf {
	private String fileName;
	private String absolutePath;
	private String fileMD5;
	private String relativePath;
	
	public FileInf(String fileName, String absolutePath, String fileMD5, String relativePath){
		this.fileName = fileName;
		this.absolutePath = absolutePath;
		this.fileMD5 = fileMD5;
	}
	
	public String getFileName(){
		return fileName;
	}
	
	public String getAbsolutePath(){
		return absolutePath;
	}
	
	public String getFileMD5(){
		return fileMD5;
	}
	
	public String geRelativePath(){
		return relativePath;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		FileInf temp = (FileInf)o;
        return fileName.equals(temp.getFileName()) && absolutePath.equals(temp.getAbsolutePath()) && fileMD5.equals(temp.getFileMD5()) && relativePath.equals(temp.geRelativePath());
	}
	
	
}
