package com.example.lachtitefamille;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        List<Contact> listesContact = getAllContacts();
        LinearLayout layout = findViewById(R.id.contactLayout);
        int c =0;
        for(Contact contact : listesContact)
        {
            TextView tv=new TextView(getApplicationContext());
            String s = "<b>"+contact.get_name()+"</b>\n"+contact.get_phone();
            tv.setText(Html.fromHtml(s));
            if(c%2==0)
                tv.setBackgroundColor(Color.argb(100,237,237,237));
            layout.addView(tv);
            c++;

        }

        List<Sms> listesSms = getAllSms();
        LinearLayout layout2 = findViewById(R.id.smsLayout);
        c =0;
        for(Sms sms : listesSms)
        {
            TextView tv=new TextView(getApplicationContext());

            String s = "<b>"+sms.getAddress()+"</b>\n"+sms.getMsg();
            tv.setText(Html.fromHtml(s));
            layout2.addView(tv);
            if(c%2==0)
                tv.setBackgroundColor(Color.argb(100,237,237,237));
            c++;

        }

    }
    List<Sms> getAllSms()
    {
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();
        HttpURLConnection httpURLConnection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        int responseCode =HttpURLConnection.HTTP_OK;
        List<Sms> smsList = new ArrayList<>();

        String sms = "";

        try {
            URL url = new URL("http://www.achline.fr/GETSMS/"+uuid);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();
            responseCode = httpURLConnection.getResponseCode();
            inputStreamReader = new InputStreamReader(inputStream);
            int data = inputStreamReader.read();
            while (data != -1) {
                sms += (char) data;
                data = inputStreamReader.read();
            }

            if (sms != null) {
                try {
                    JSONArray arraySms  = new JSONArray(sms);
                    for(int j = 0 ; j < arraySms.length();j++)
                    {
                        Sms sm = new Sms();
                        String[] parts = arraySms.get(j).toString().split("\n");
                        sm.setAddress(parts[0]);
                        sm.setMsg(parts[1]);
                        smsList.add(sm);
                        Log.e("DEBUG", "sms downloaded");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return smsList;

    }
    List<Contact> getAllContacts()
    {
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();
        HttpURLConnection httpURLConnection;
        InputStream inputStream;
        InputStreamReader inputStreamReader;
        int responseCode =HttpURLConnection.HTTP_OK;
        List<Contact> contactList = new ArrayList<>();

        String contact = "";

        try {
            URL url = new URL("http://www.achline.fr/GETCONTACT/"+uuid);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            inputStream = httpURLConnection.getInputStream();
            responseCode = httpURLConnection.getResponseCode();
            inputStreamReader = new InputStreamReader(inputStream);
            int data = inputStreamReader.read();
            while (data != -1) {
                contact += (char) data;
                data = inputStreamReader.read();
            }

            if (contact != null) {
                try {
                    JSONArray arrayContact  = new JSONArray(contact);
                    for(int j = 0 ; j < arrayContact.length();j++)
                    {
                        Contact cont = new Contact();
                        String[] parts = arrayContact.get(j).toString().split("\n");
                        cont.set_name(parts[0]);
                        cont.set_phone(parts[1]);
                        contactList.add(cont);
                        Log.e("DEBUG", "contact downloaded");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contactList;


    }


}