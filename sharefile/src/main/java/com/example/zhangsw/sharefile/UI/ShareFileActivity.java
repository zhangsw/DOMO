package com.example.zhangsw.sharefile.UI;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.andreabaccega.widget.FormEditText;
import com.example.zhangsw.sharefile.ShareFileService.SharedMem;
import com.example.zhangsw.sharefile.R;

import java.io.IOException;
import java.util.List;

public class ShareFileActivity extends Activity {
    /** Called when the activity is first created. */
	
	private TextView tv;
	private Button bt1;
	private Button bt2;
	private FormEditText IPEt1;
    private FormEditText IPEt2;
	private Button connectBt;
	private TextView IPTv;

	private Button disconnectBt;
	private Button mTestModifyBt;
	
	private SharedMem SharedMemService;
	private Intent serviceIntent;
	private SharedMem.MyBinder serviceBinder;
	
	private WifiManager wifiManager;
	private WifiInfo wifiInfo;
	private String serviceName = "android_programe.ShareFile.SharedMem";
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			SharedMemService = ((SharedMem.MyBinder) service).getService();
			serviceBinder = (SharedMem.MyBinder) service;
			Toast.makeText(ShareFileActivity.this, "Service Connected.", Toast.LENGTH_LONG)
					.show();

		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			SharedMemService = null;
			Toast.makeText(ShareFileActivity.this, "Service failed.", Toast.LENGTH_LONG).show();
		}
	};
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
        tv = (TextView)findViewById(R.id.tv);
        bt1 = (Button)findViewById(R.id.start);
        bt2 = (Button)findViewById(R.id.stop);
        connectBt = (Button)findViewById(R.id.button3);
        IPEt1 = (FormEditText)findViewById(R.id.et_connectip);
        IPTv = (TextView)findViewById(R.id.textView1);
        disconnectBt = (Button)findViewById(R.id.button4);
        IPEt2 = (FormEditText)findViewById(R.id.et_disconnectip);

        //IPfet = (FormEditText)findViewById(R.id.et_connectip);
        
        mTestModifyBt = (Button)findViewById(R.id.test_modify_file_button);
        
        serviceIntent = new Intent(ShareFileActivity.this,SharedMem.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        System.out.println("oncreate ----------1");
        

       // logLine.connect("114.212.87.66");
        tv.setText("connected");     
        
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        IPTv.setText(getLocalAddress());
        

        bt1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        System.out.println("click ----------1");
		        //if(!isServiceRunning(serviceName))
		        	startService(serviceIntent);
				
		        System.out.println("click ----------2");

			}
		});
        
        bt2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopService(serviceIntent);
			}
		});
        
        connectBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub

                if(IPEt1.testValidity()){
                    String ip = IPEt1.getText().toString();
                    System.out.println("input ip is-----"+ip + "-----");

                    if(SharedMemService != null){
                        if(serviceBinder == null) System.out.println("serviceBinder is null");
                        else{
                            new connectTask().execute(ip);
                        }
                    }
                    else
                        System.out.println("service hasn't been started");
                }
                else{
                    Toast.makeText(ShareFileActivity.this, ":)", Toast.LENGTH_LONG).show();

                }
				}
		});
        
        disconnectBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
                if(IPEt2.testValidity()) {
                    String ip = IPEt2.getText().toString();
                    if (SharedMemService != null) {
                        if (serviceBinder != null) {
                            new disconnectTask().execute(ip);
                        }
                    }
                }
                else{
                    Toast.makeText(ShareFileActivity.this, ":)", Toast.LENGTH_LONG).show();
                 }
			}
		});
        
        mTestModifyBt.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
    }
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		unbindService(serviceConnection);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	private String getLocalAddress(){
		int ipAddress = wifiInfo.getIpAddress();    
        if(ipAddress==0)return null;  
        return ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."  
                +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));  
	}
	
	public boolean isServiceRunning(String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(40);

        if (!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    private class connectTask extends AsyncTask<String,Integer,Boolean>{

        private String ip;

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                ip = params[0];
                return serviceBinder.connect(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                Toast connectToast = Toast.makeText(ShareFileActivity.this, "Connected to " + ip, Toast.LENGTH_LONG);
                connectToast.show();
            }

        }
    }

    private class disconnectTask extends AsyncTask<String,Integer,Boolean>{
        private String ip;

        @Override
        protected Boolean doInBackground(String... params) {
            ip = params[0];
            return serviceBinder.disconnect(ip);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if(aBoolean){
                Toast disconnectToast = Toast.makeText(ShareFileActivity.this, "disconnected  " + ip, Toast.LENGTH_LONG);
                disconnectToast.show();
            }
        }
    }
}