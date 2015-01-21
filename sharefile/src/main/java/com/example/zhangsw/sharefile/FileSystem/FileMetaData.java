package com.example.zhangsw.sharefile.FileSystem;

import java.io.Serializable;

public class FileMetaData implements Serializable{

    private String mFileID;	//文件的id

    private int  mVersionID;	//版本号

    private String mRelativePath;	//文件的相对路径

    //private String mAbsolutePath;	//文件的绝对路径

    private long mFileSize;	//文件大小

    //private String mFileCreator;	//文件作者

    private long mModifiedTime;	//文件的修改日期

    //private boolean mLabel = true;	//标记文件是新版本还是旧版本，新版本：True,旧版本：false

    public FileMetaData(){

    }

    /**
     * 构建一个文件的meta data
     * @param fileID	文件的id
     * @param versionID	文件的版本号
     * @param relativePath	相对路径
     * @param absolutePath 绝对路径
     * @param fileSize	文件的大小
     * @param creator	文件的作者
     */
    public FileMetaData(String fileID, int versionID, String relativePath,String absolutePath, long fileSize,String creator,long modifiedTime){
        mFileID = fileID;
        mVersionID = versionID;
        mRelativePath = relativePath;
        //mAbsolutePath = absolutePath;
        mFileSize = fileSize;
        //mFileCreator = creator;
        mModifiedTime = modifiedTime;
    }

    public void setFileID(String fileID){
        mFileID = fileID;
    }

    public String getFileID(){
        return mFileID;
    }

    public void setVersionID(int versionID){
        mVersionID = versionID;
    }

    public int getVersionID(){
        return mVersionID;
    }

    public void setRelativePath(String relativePath){
        mRelativePath = relativePath;
    }

    public String getRelativePath(){
        return mRelativePath;
    }

	/*
	public void setAbsolutePath(String absolutePath){
		mAbsolutePath = absolutePath;
	}

	public String getAbsolutePath(){
		return mAbsolutePath;
	}*/

    public void setFileSize(long size){
        mFileSize = size;
    }

    public long getFileSize(){
        return mFileSize;
    }

	/*
	public void setFileCreator(String creator){
		mFileCreator = creator;
	}

	public String getFileCreator(){
		return mFileCreator;
	}*/

    public void setModifiedTime(long time){
        mModifiedTime = time;
    }

    public long getModifiedTime(){
        return mModifiedTime;
    }

    @Override
    public boolean equals(Object o) {
        // TODO Auto-generated method stub
        if(this == o) return true;
        if(!(o instanceof FileMetaData)) return false;
        FileMetaData fmd = (FileMetaData) o;
        return (mVersionID == fmd.mVersionID) && (mFileSize == fmd.mFileSize) && (mModifiedTime == fmd.mModifiedTime) &&
                (mFileID == null ? fmd.mFileID == null:mFileID.equals(fmd.mFileID)) &&
                (mRelativePath == null ?fmd.mRelativePath == null:mRelativePath.equals(fmd.mRelativePath));
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        int result = 17;
        result = 31*result + (mFileID == null ? 0:mFileID.hashCode());
        result = 31*result + (mRelativePath == null ? 0:mRelativePath.hashCode());
        result = 31*result + mVersionID;
        result = 31*result + (int)(mFileSize^(mFileSize >>> 32));
        result = 31*result + (int)(mModifiedTime^(mModifiedTime >>> 32));
        return result;
    }



    /**
     * 标记文件为新版本
     */
	/*
	public void setVersionNew(){
		mLabel = true;
	}

	/**
	 * 标记文件为旧版本
	 */
	/*
	public void setVersionOld(){
		mLabel = false;
	}
	
	public boolean getLabel(){
		return mLabel;
	}	*/
}
