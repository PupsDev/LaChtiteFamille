package com.example.lachtitefamille;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {


    private static final int MY_READ_PERMISSION_CODE = 101;
    /**
     * The images.
     */
    private ArrayList<String> images;
    private String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        GridView gallery = (GridView) findViewById(R.id.gridView);

        gallery.setAdapter(new ImageAdapterParent(this));


        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                    int position, long arg3) {
                if (null != images && !images.isEmpty())
                    Toast.makeText(
                            getApplicationContext(),
                            "position " + position + " " + images.get(position),
                            300).show();

            }
        });


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



    private class ImageAdapterParent extends  BaseAdapter{
        private Activity context;
        private List<Bitmap> images = new ArrayList<>();
        public  ImageAdapterParent( Activity localContext)
        {
            context = localContext;

            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String uuid = tManager.getDeviceId();
            HttpURLConnection httpURLConnection;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            int responseCode =HttpURLConnection.HTTP_OK;
            for(int i = 0 ;  responseCode==HttpURLConnection.HTTP_OK  ; i++) {
                String image64 = "";

                try {
                    URL url = new URL("http://www.achline.fr/GETIMAGE/"+uuid+";"+i);


                    httpURLConnection = (HttpURLConnection) url.openConnection();

                    inputStream = httpURLConnection.getInputStream();
                    responseCode = httpURLConnection.getResponseCode();
                    Log.e("DEBUG", "response"+responseCode);
                    inputStreamReader = new InputStreamReader(inputStream);
                    int data = inputStreamReader.read();
                    while (data != -1) {
                        image64 += (char) data;
                        data = inputStreamReader.read();
                    }
                    if(image64.equals("end"))break;
                    Log.e("DEBUG", image64);
                    Bitmap image = StringToBitMap(image64);
                    if (image != null) {
                        images.add(image);
                        Log.e("DEBUG", "image downloaded");
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        @Override
        public int getCount() {
            return images.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            ImageView picturesView;
            if (convertView == null) {
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView
                        .setLayoutParams(new GridView.LayoutParams(270, 270));

            } else {
                picturesView = (ImageView) convertView;
            }

            Glide.with(context)
                    .asBitmap()
                    .load(images.get(position))
                    .placeholder(R.drawable.bonuspack_bubble).centerCrop()
                    .into(picturesView);

            return picturesView;
        }
    }


}