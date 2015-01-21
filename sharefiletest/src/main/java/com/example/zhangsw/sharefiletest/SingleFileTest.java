package com.example.zhangsw.sharefiletest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class SingleFileTest extends Activity {

    private String filePath;
    private int writePercent;
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
        Button btStart = (Button) findViewById(R.id.single_file_start_button);
        Button btStop = (Button) findViewById(R.id.single_file_stop_button);

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


    public void startBtOnClick(View view){
        writePercent = Integer.parseInt(et.getText().toString());
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task,1000,3000);
    }

    public void stopBtOnClick(View view){
        timer.cancel();
    }

    private void test(){
        Random random = new Random();
        int result = random.nextInt(100)+1;
        if(result<=writePercent){
            //write file
            try {
                File file = new File(filePath);
                if(!file.exists())
                    file.createNewFile();
                FileWriter fw = new FileWriter(file,true);
                fw.write("abcdefghijklmn\n");
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            //read file
        }


    }

}
