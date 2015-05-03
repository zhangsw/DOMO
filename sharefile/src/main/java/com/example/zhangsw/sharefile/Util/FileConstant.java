package com.example.zhangsw.sharefile.Util;

import android.os.Environment;

public class FileConstant {
    /** TCP 协议的端口*/
    public static final int TCPPORT = 7000;

    /**删除文件*/
    public static final int DELETEFILE = 25;

    /**文件信息消息*/
    public static final int FILEINF = 26;

    /**请求文件消息*/
    public static final int ASKFILE = 27;

    /**文件数据*/
    public static final int FILEDATA = 28;

    /**重命名文件*/
    public static final int RENAMEFILE = 29;

    public static final int MAKEDIR = 30;

    public static final int FILEVERSION = 31;

    public static final int FILEUPDATE = 32;

    public static final int DISCONNECT = 33;

    /**同步准备就绪*/
    public static final int SYNREADY = 34;

    public static final int HEARTBEAT = 35;

    /**请求文件版本信息*/
    public static final int ASKFILEVERSION = 36;

    /**简单粗暴一致性*/
    public static final int SIMPLECM = 0;

    /**发送文件数据命令*/
    public static final int SENDFILEMESSAGE = 10;

    /**删除文件命令*/
    public static final int DELETEFILEMESSAGE = 11;

    /**重命名文件命令*/
    public static final int RENAMEFILEMESSAGE = 12;

    /**移动文件命令*/
    public static final int MOVEFILEMESSAGE = 13;

    /**创建文件夹命令*/
    public static final int CREATEDIRMESSAGE = 14;

    /**删除文件夹命令*/
    public static final int DELETEDIRMESSAGE = 15;

    public static final int MOVEDIRMESSAGE = 16;

    public static final int RENAMEDIRMESSAGE = 17;

    /**发送文件夹命令*/
    public static final int SENDDIRMESSAGE = 18;

    /***/
    public static final int ISDIR = 0x40000000;

    public static final String DEFAULTAPPDIRECTORY = "/SharedMemoryActivity";

    public static final String DEFAULTDIRECTORY = "/SharedMemory";

    public static final String DEFAULTVERSIONLOG = "/VersionLog";

    public static final String DEFAULTCACHEDIRECTORY = "/.cache";

    public static final String STORAGEROOTPATH = Environment.getExternalStorageDirectory().getPath();

    /**默认的app路径*/
    public static final String DEFAULTAPPPATH = STORAGEROOTPATH + DEFAULTAPPDIRECTORY;

    /**默认的version log保存文件夹目录*/
    public static final String DEFAULTVERSIONLOGPATH = DEFAULTAPPPATH + DEFAULTVERSIONLOG;

    /**默认的共享文件夹目录*/
    public static final String DEFAULTSHAREPATH = DEFAULTAPPPATH + DEFAULTDIRECTORY;

    /**默认的文件保存目录*/
    public static final String DEFAULTSAVEPATH = DEFAULTSHAREPATH + DEFAULTCACHEDIRECTORY;
}
