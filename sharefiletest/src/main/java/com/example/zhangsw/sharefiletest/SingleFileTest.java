package com.example.zhangsw.sharefiletest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhangsw.sharefile.Log.DebugLog;
import com.example.zhangsw.sharefile.ShareFileService.SharedMem;
import com.example.zhangsw.sharefile.Util.FileConstant;
import com.example.zhangsw.sharefile.Util.FileUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class SingleFileTest extends Activity {

    private SharedMem SharedMemService;
    private SharedMem.MyBinder serviceBinder;
    boolean mBound;
    private static int fileNumber = 0;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            SharedMemService = ((SharedMem.MyBinder) service).getService();
            serviceBinder = (SharedMem.MyBinder) service;
            Toast.makeText(SingleFileTest.this, "Service Connected.", Toast.LENGTH_LONG)
                    .show();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            SharedMemService = null;
            Toast.makeText(SingleFileTest.this, "Service failed.", Toast.LENGTH_LONG).show();
            mBound = false;
        }
    };

    private final int MAXOPERATORS = 100;
    private String filePath = FileConstant.DEFAULTSHAREPATH + "/test";
   // private String logFilePath;
    private int writePercent;
    private int modifyCount = 0;
    private int writeNum = 0;
    private int operatorCount = 0;
    private EditText et;

    private final Timer timer = new Timer();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            super.handleMessage(msg);
            switch(msg.what){
                case 1:{
                    test();
                }break;
                default:{

                }break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_file_test);
        et = (EditText)findViewById(R.id.single_test_editText);
        Intent intent = getIntent();
        fileNumber = intent.getIntExtra("filenumber",0);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_file_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(SingleFileTest.this,SharedMem.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBound){
            unbindService(serviceConnection);
            mBound = false;
        }
    }

    public void startBtOnClick(View view){
        if(et.getText().length() > 0) {

           // System.out.println("----SingleFileTest----file number is:" +fileNumber);
            operatorCount = 0;
            modifyCount = 0;
            writePercent = Integer.parseInt(et.getText().toString());
            writeNum = MAXOPERATORS *writePercent/100;
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            };
            timer.schedule(task, 1000, 5000);
        }
    }

    public void stopBtOnClick(View view){
        timer.cancel();

    }

    private void test(){
        Random random = new Random();
        int result = random.nextInt(fileNumber);
        String path = filePath+"/"+result+".txt";
        if(operatorCount < MAXOPERATORS){
            if(modifyCount < writeNum){
                //write file
                if(operatorCount-modifyCount < MAXOPERATORS-writeNum){
                    int r = random.nextInt(100);
                    if(r<=writePercent){
                        serviceBinder.write(path,"abcde\n");
                        modifyCount++;
                    }
                    else{
                        Log.i("Test", filePath + " read before");
                        serviceBinder.read(path);
                        Log.i("Test", filePath + " read after");
                    }
                }
                else {
                    serviceBinder.write(path, "abcde\n");
                    modifyCount++;
                }
            }
            else{
                //read file
                Log.i("Test", filePath + " read before");
                serviceBinder.read(path);
                Log.i("Test", filePath + " read after");
            }
            operatorCount++;
        }
        else{
            Log.i("Test", "Test stop");
        }
    }

}
