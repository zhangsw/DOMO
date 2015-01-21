package com.example.zhangsw.sharefile.FileMonitor;

import android.os.FileObserver;

import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileOperateHelper;

public class AndEventTranslate implements IEventTranslate{

	private String mOldPath;
	private String mNewPath;
	private int mType;
	private boolean tag;
	private boolean isDuplication;//move_to + delete_self
	private boolean isTmpRename;
	private String tmpPath;
	
	
	public AndEventTranslate(){
		mOldPath = null;
		mNewPath = null;
		mType = 0;
		tag = false;
		isDuplication = false;
		isTmpRename = false;
		tmpPath = null;
	}
	
	public int translate(String path, int type) {
		// TODO Auto-generated method stub
		int m = 0;
		if(!path.endsWith(".tmp")){			//����tmp�ļ�
			if(tag) init();
			if(isDuplication && type != FileObserver.DELETE_SELF) isDuplication = false;
			switch(type){
			case FileObserver.CLOSE_WRITE:{
				m = IEventTranslate.FILEMODIFIED;
			}break;
			
			case FileObserver.MOVED_FROM:{
				mOldPath = path;
				mType = IEventTranslate.ISFILE;
			}break;
			
			case FileObserver.MOVED_TO:{
				if(isTmpRename && tmpPath.startsWith(path)){
					m = IEventTranslate.COVERFILE;
					isTmpRename = false;
					tmpPath = null;
				}else{
					if(mOldPath == null){
						m = IEventTranslate.FILEMOVETO;
						isDuplication = true;
					}
					else mNewPath = path;
				}
			}break;
			
			case FileObserver.MOVE_SELF:{
				if(mType == IEventTranslate.ISFILE){
					if(mOldPath != null && mNewPath != null)
						m = IEventTranslate.FILERENAMEORMOVE;
					else if(mOldPath != null)
						m = IEventTranslate.FILEMOVEFROM;
				}
				else if(mType == IEventTranslate.ISDIR){
					if(mOldPath != null && mNewPath != null)
						m = IEventTranslate.DIRRENAMEORMOVE;
					else if(mOldPath != null)
						m = IEventTranslate.DIRMOVEFROM;
				}
				tag = true;
			}break;
			
			case FileObserver.DELETE:{
				//�ж��Ƿ���tmp�ļ�������У���������Ϣ
				if(!FileOperateHelper.fileExist(path + ".tmp"))
					m = IEventTranslate.FILEDELETE;
				else
					System.out.println("----AndEventTranslate----.tmp file exists");
			}break;
			
			case FileObserver.DELETE_SELF:{
				if(mType == IEventTranslate.ISDIR && mOldPath != null)
					m = IEventTranslate.DIRDELETE;
				else if(isDuplication){
					m = IEventTranslate.COVERFILE;
					isDuplication = false;
				}
				tag = true;
			}break;
			
			case FileObserver.CREATE | FileConstant.ISDIR:{
				m = IEventTranslate.DIRCREATE;
			}break;
			
			case FileObserver.MOVED_FROM | FileConstant.ISDIR:{
				mOldPath = path;
				mType = IEventTranslate.ISDIR;
			}break;
			
			case FileObserver.MOVED_TO | FileConstant.ISDIR:{
				if(mOldPath == null)
					m = IEventTranslate.DIRMOVETO;
				else mNewPath = path;
			}break;
			
			}
		}
		else{					//��tmp�ļ�
			if(type == FileObserver.MOVED_FROM){
				isTmpRename = true;
				tmpPath = path;
			}
		}
		
		return m;
	}
	
	public String getOldPath(){
		return mOldPath;
	}
	
	public String getNewPath(){
		return mNewPath;
	}
	
	private void init(){
		mOldPath = null;
		mNewPath = null;
		mType = 0;
		tag = false;
	}

}
