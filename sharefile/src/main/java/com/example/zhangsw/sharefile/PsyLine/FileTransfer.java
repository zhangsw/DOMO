package com.example.zhangsw.sharefile.PsyLine;

/**
 * 文件传输接口，定义了文件传输中的各个调用
 * @author zhangsw
 *
 */
public interface FileTransfer {

    /**
     * 发送文件的metaData
     * @param fileMetaData	需要发送的文件的metaData
     * @param target	送往的对象
     */
    public void sendFileMetaData(Object fileMetaData,Object target);

    /**
     * 获取文件的metaData
     * @param fileID	需要获取的文件id
     * @param target	信息来源对象
     */
    public void getFileMetaData(String fileID,Object target);

    /**
     * 发送文件的内容
     * @param fileData	文件的数据
     * @param target	送往的对象
     */
    public void sendFileData(Object fileData,Object target);

    /**
     * 获取文件的具体数据
     * @param fileID	需要获取的文件id
     * @param target	信息来源对象
     */
    public void getFileData(Object fileID,Object target);

    /**
     * 发送文件的版本map
     * @param vectorClock	需要发送的版本map
     * @param target	送往的对象
     */
    public void sendFileVectorClock(Object vectorClock,Object target);

    //发送操作指令
    public void sendCommand(String command);


}
