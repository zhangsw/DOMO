package com.example.zhangsw.sharefile.FileSystem;

import android.os.Handler;

public interface IFileManager{
	public void deleteObserver(String path);
	public DSMFileNode registerObserver(String target, String absolutePath, Handler handler, String fatherPath);
	public void updateObserverMap(String path, String newPath);
	public void modifyObserverPath(String path, String newPath);
	public void withdrowObserver(String target, String path);
	
}
