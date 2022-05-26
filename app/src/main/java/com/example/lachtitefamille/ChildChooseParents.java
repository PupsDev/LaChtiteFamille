package com.example.lachtitefamille;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChildChooseParents extends AppCompatActivity {

    private ChildChooseParents mySelf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mySelf = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_choose_parents);



        Button clickButton = (Button) findViewById(R.id.button2);
        clickButton.setOnClickListener( new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String uuid = tManager.getDeviceId();

                String code = ((EditText)findViewById(R.id.editTextNumber)).getText().toString();

                String result = "";
                HttpURLConnection httpURLConnection;
                InputStream inputStream;
                InputStreamReader inputStreamReader;
                try {
                    URL url = new URL("http://achline.fr/SETASPARENT/"+uuid+";"+code);


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

                Log.e("Received: ",result   );

                String ptext = ((TextView) findViewById(R.id.textView10)).getText().toString();
                ((TextView) findViewById(R.id.textView10)).setText(ptext+"\n"+result);

            }
        }
        );

        ((Button) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(mySelf, ChildrenActivity.class);
                startActivity(myIntent);
            }
        });



    }
}