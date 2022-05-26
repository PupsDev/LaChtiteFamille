package com.example.lachtitefamille;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends FragmentActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final int REQUEST_READ_PHONE_STATE=1;

    private MainActivity myInstance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        myInstance = this;
        setContentView(R.layout.activity_main);
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();


        MyNetworkTask network = new MyNetworkTask();
        network.execute("http://achline.fr/HAVEANACCOUNT/"+uuid);


        super.onCreate(savedInstanceState);


        CompoundButton switchButton = (Switch) findViewById(R.id.switch2);



        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
        }

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        verifyGPSAccess();

        if(!isDataSenderRunning()) {
            Intent serviceIntent = new Intent(this, DataSender.class);
            startForegroundService(serviceIntent);
        }


        Button clickButton = (Button) findViewById(R.id.suivant);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(((Switch) findViewById(R.id.switch2)).isChecked()) {
                    Log.e("TAG", "Open child activity");

                    Intent mIntent = new Intent(myInstance, ChildLoginActivity.class);
                    startActivity(mIntent);

                }else{
                    Log.e("TAG", "Open parent activity");

                    Intent mIntent = new Intent(myInstance, ParentLoginActivity.class);
                        startActivity(mIntent);
                }


            }
        });


    }

    boolean isDataSenderRunning(){
        ActivityManager acManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo service: acManager.getRunningServices(Integer.MAX_VALUE)){
            if(DataSender.class.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    private void verifyGPSAccess() {
        int permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            //grantedSetupGPSandMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //TODO
                }
                break;

            default:
                break;
        }
    }

    private class MyNetworkTask extends AsyncTask<Object,Integer,String> {
        @Override
        protected String doInBackground(Object... Objects) {
            String result = "";
            HttpURLConnection httpURLConnection;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            try {
                URL url = new URL((String) Objects[0]);

                httpURLConnection = (HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while (data != -1) {
                    result += (char) data;
                    data = inputStreamReader.read();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("result",result);
            if(result.equals("parent")){
                Intent mIntent = new Intent(myInstance, ParentActivity.class);
                startActivity(mIntent);
            }else if(result.equals("child")) {
                Intent mIntent = new Intent(myInstance, ChildrenActivity.class);
                startActivity(mIntent);
            }else{

            }
            return null;
        }
    }
}