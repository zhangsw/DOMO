package com.example.zhangsw.sharefiletest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.zhangsw.sharefile.Util.FileConstant;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class TestActivity extends Activity {

    private EditText et;

    private String filePath = FileConstant.DEFAULTSHAREPATH + "/test";

    private final Timer timer = new Timer();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            super.handleMessage(msg);
            switch(msg.what){
                case 1:{
                    initFile(msg.arg1);
                }break;
                default:{

                }break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        et = (EditText)findViewById(R.id.file_number_edit);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    public void singleFileTestBtOnClick(View view){
        int num = 0;
        if(et.getText().toString().length() !=0)
            num = Integer.parseInt(et.getText().toString());
        Intent intent = new Intent();
        intent.putExtra("filenumber",num);
        intent.setClass(this,SingleFileTest.class);
        startActivity(intent);
    }

    public void initBtOnClick(View view){
        final int num = Integer.parseInt(et.getText().toString());
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message message = new Message();
                message.what = 1;
                message.arg1 = num;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 1000);
    }

    public void multipleFileTestBtOnClick(View view){

    }

    private void initFile(int num){
        File file = new File(filePath);
        file.deleteOnExit();
        file.mkdir();
        for(int i=0;i<num;++i){
            File file1 = new File(filePath + "/" + i +".txt");
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}
