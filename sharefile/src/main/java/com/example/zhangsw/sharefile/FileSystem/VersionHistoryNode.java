package com.example.zhangsw.sharefile.FileSystem;

import java.util.ArrayList;
import java.util.List;


public class VersionHistoryNode {

	private int mVersionId;
	private int mNumOfFathers;
	private List <VersionHistoryNode> mFatherNodes;
	private FileMetaData mMetaData;
	
	public VersionHistoryNode(int id,List<VersionHistoryNode> fathers){
		mVersionId = id;
		mFatherNodes = new ArrayList<>();
		mFatherNodes = fathers;
		mNumOfFathers = fathers.size();
	}
	
	public VersionHistoryNode(int id){
		mVersionId = id;
		mNumOfFathers = 0;
		mFatherNodes = new ArrayList<>();
	}
	
	public int getVersionId(){
		return mVersionId;
	}
	
	public int getNumOfFathers(){
		return mNumOfFathers;
	}
	
	public List getFathers(){
		return mFatherNodes;
	}
}
