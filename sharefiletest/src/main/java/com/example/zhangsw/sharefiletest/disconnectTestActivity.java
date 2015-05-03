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

import com.example.zhangsw.sharefile.ShareFileService.SharedMem;
import com.example.zhangsw.sharefile.Util.FileConstant;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class DisconnectTestActivity extends Activity {

    private EditText disRateEt;
    private EditText wRateEt;
    private SharedMem SharedMemService;
    private SharedMem.MyBinder serviceBinder;
    boolean mBound;
    private static int fileNumber = 0;
    private final int MAXOPERATORS = 100;
    private String filePath = FileConstant.DEFAULTSHAREPATH + "/test";
    private int disRate;
    private int wRate;
    private int operation;
    private int modifyCount;
    private int writeNum;
    private int conflictNum;
    private int conflictCount;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            SharedMemService = ((SharedMem.MyBinder) service).getService();
            serviceBinder = (SharedMem.MyBinder) service;
            Toast.makeText(DisconnectTestActivity.this, "Service Connected.", Toast.LENGTH_LONG)
                    .show();
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            SharedMemService = null;
            Toast.makeText(DisconnectTestActivity.this, "Service failed.", Toast.LENGTH_LONG).show();
            mBound = false;
        }
    };

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
        setContentView(R.layout.activity_disconnect_test);
     //   Log.i("Test","enter disActivity on create");
        disRateEt = (EditText)findViewById(R.id.dis_edit1);
        wRateEt = (EditText)findViewById(R.id.dis_edit2);
        Intent intent = getIntent();
        fileNumber = intent.getIntExtra("filenumber",0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disconnect_test, menu);
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
        Intent serviceIntent = new Intent(DisconnectTestActivity.this,SharedMem.class);
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
        operation = 0;
        modifyCount = 0;
        conflictCount = 0;
        disRate = Integer.parseInt(disRateEt.getText().toString());
        wRate = Integer.parseInt(wRateEt.getText().toString());
        writeNum = MAXOPERATORS*wRate/100;
        conflictNum = MAXOPERATORS*wRate*disRate/10000;
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

    public void stopBtOnClick(View view){
        timer.cancel();
    }

    private void test() {
        if (operation < MAXOPERATORS) {
            String path = filePath + "/" + conflictCount + ".txt";
            Random random = new Random();
            if (modifyCount < writeNum) {
                //write file
                if ((operation - modifyCount < MAXOPERATORS - writeNum) && random.nextInt(100)>wRate) {
                    Log.i("Test", filePath + " read before");
                    serviceBinder.read(path);
                    Log.i("Test", filePath + " read after");
                }
                else{
                    if(conflictCount<conflictNum){
                        if((modifyCount-conflictCount<writeNum-conflictNum) && random.nextInt(100)>disRate){
                            serviceBinder.write(path, "abcde\n");
                        }
                        else{
                            serviceBinder.updateRemoteVersion(path, "192.168.1.129");
                            Log.i("Test", path + "conflict begin");
                            serviceBinder.write(path, "abcde\n");
                            conflictCount++;

                        }
                    }else{
                        serviceBinder.write(path, "abcde\n");
                    }
                    modifyCount++;
                }
             }
            else{
                Log.i("Test", filePath + " read before");
                serviceBinder.read(path);
                Log.i("Test", filePath + " read after");
            }
            operation++;
        }
        else{
            Log.i("Test","stop  test");
        }
    }
}
