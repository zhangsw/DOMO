package com.example.zhangsw.sharefile.MemoryManager;

public class SimpleConsistencyRule extends ConsistencyRule{

	@Override
	public boolean deleteFile(String path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean sendFile(String path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean renameFile(String oldPath, String newPath) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean deleteDirectory(String path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean createDirectory(String path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean renameDirectory(String oldPath,String newPath) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean sendDirectory(String path) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean moveFile(String oldPath,String newPath) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean moveDirectory(String oldPath,String newPath) {
		// TODO Auto-generated method stub
		return true;
	}

}
