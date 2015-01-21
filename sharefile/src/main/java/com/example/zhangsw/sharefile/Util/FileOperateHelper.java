package com.example.zhangsw.sharefile.Util;

import java.io.File;

public class FileOperateHelper {

	
	
	//�ļ��Ƿ����
	public static boolean fileExist(String path){
        File file = new File(path);
        return file.exists();
    }
	
	//�ж��Ƿ����ļ���
	public static boolean isDirectory(String path){
		File file = new File(path);
        return file.isDirectory();
	}
	
	//��ȡ���ļ�
	public static File[] subFiles(String path){
		File file = new File(path);
		if(file.isDirectory()){
            return file.listFiles();
		}
		else return null;
	}
	
	//�����ļ���
	public static File makeDir(String path){
        File file = new File(path);
        if(!file.exists())
            file.mkdirs();
        return file;
    }
	
	public static boolean renameFile(String oldPath,String newPath){
		File file = new File(oldPath);
		File newFile = new File(newPath);
		return file.renameTo(newFile);
	}
	
	public static boolean deleteFile(String path){
		File file = new File(path);
		return file.delete();
	}
	
	public static String getFileName(String path){
		if(path != null){
			int index = path.lastIndexOf('/');
			String name = path.substring(index+1, path.length());
			if(name != null) return name;
		}
		return null;
	}
	
	public static long getFileLength(String path){
		File file = new File(path);
		return file.length();
	}
	
	public static long getFileModifiedTime(String path){
		File file = new File(path);
		return file.lastModified();
	}
}
