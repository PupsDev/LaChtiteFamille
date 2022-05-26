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
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ChildLoginActivity extends AppCompatActivity {

    private ChildLoginActivity mySelf;
    private String age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mySelf = this;
        age = "1";
        setContentView(R.layout.activity_child_login);



        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        final TextView seekBarValue = (TextView)findViewById(R.id.ageTextView);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                age = String.valueOf(progress);
                seekBarValue.setText("Age : "+age);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });



        Button clickButton = (Button) findViewById(R.id.nextButton);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(mySelf, ChildChooseParents.class);

                TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String uuid = tManager.getDeviceId();

                String nom = ((EditText)findViewById(R.id.lastNameInput)).getText().toString();
                String prenom = ((EditText)findViewById(R.id.firstNameInput)).getText().toString();

                myIntent.putExtra("Nom",nom);
                myIntent.putExtra("Prenom",prenom);
                myIntent.putExtra("Age",age);



                String result = "";
                HttpURLConnection httpURLConnection;
                InputStream inputStream;
                InputStreamReader inputStreamReader;
                try {
                    URL url = new URL("http://achline.fr/REGISTERCHLID/"+uuid+";"+prenom+";"+nom+";"+age);


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

                startActivity(myIntent);
            }
        });
    }
}