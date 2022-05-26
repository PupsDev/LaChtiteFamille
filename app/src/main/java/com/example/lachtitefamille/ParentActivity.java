package com.example.lachtitefamille;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ParentActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final int REQUEST_READ_PHONE_STATE=1;
    Button button;
    private Button GalleryButton;
    private Button ContactButton;
    private Button SMSButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ParentActivity.this, WatcherActivity.class);
                startActivity(intent);

            }
        });
        GalleryButton = (Button) findViewById(R.id.button2);
        GalleryButton.setOnClickListener( new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ParentActivity.this, GalleryActivity.class);
                startActivity(intent);

            }
        });

        ContactButton = (Button) findViewById(R.id.button3);
        ContactButton.setOnClickListener( new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ParentActivity.this, ChildrenActivity.class);
                startActivity(intent);

            }
        });
        SMSButton = (Button) findViewById(R.id.button4);
        SMSButton.setOnClickListener( new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ParentActivity.this, ContactActivity.class);
                startActivity(intent);

            }
        });


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

    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
}