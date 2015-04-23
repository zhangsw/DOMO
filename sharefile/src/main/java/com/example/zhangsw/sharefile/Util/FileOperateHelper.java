package com.example.zhangsw.sharefile.Util;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileOperateHelper {

	
	public static boolean writeApend(String path,String content){
        try {
            FileWriter fw = new FileWriter(path,true);
            fw.write(content);
            fw.flush();
            Log.i("Test", path + " has been modified");
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
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
