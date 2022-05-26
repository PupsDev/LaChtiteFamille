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
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ParentLoginActivity extends AppCompatActivity {

    private ParentLoginActivity myInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        myInstance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_login);

        Button clickButton = (Button) findViewById(R.id.submitButton);
        clickButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mIntent = new Intent(myInstance, ParentDisplayCodeActivity.class);

                String Nom =        ((EditText)findViewById(R.id.lastNameInput)) .getText().toString();
                String Prenom =     ((EditText)findViewById(R.id.firstNameInput)).getText().toString();
                String Email =      ((EditText)findViewById(R.id.emailInput))    .getText().toString();
                String MotDePasse = ((EditText)findViewById(R.id.passwordInput)) .getText().toString();


                TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                String uuid = tManager.getDeviceId();

                String result = "";
                HttpURLConnection httpURLConnection;
                InputStream inputStream;
                InputStreamReader inputStreamReader;
                try {
                    URL url = new URL("http://achline.fr/REGISTERPARENT/"+uuid+";"+Nom+";"+Prenom+";"+Email+";"+MotDePasse);

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

                mIntent.putExtra("code",result);
                startActivity(mIntent);
            }
        });




    }
}