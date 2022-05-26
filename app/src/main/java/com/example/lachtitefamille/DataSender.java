package com.example.lachtitefamille;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class DataSender extends Service implements LocationListener {
    private String uuid;
    private DataSender DataSenderinstrance;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DataSenderinstrance = this;

        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        uuid = tManager.getDeviceId();

        Toast.makeText(this, "Le service est lancé, uuid : "+uuid, Toast.LENGTH_LONG).show();

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return 0;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, this);

        final String channelid = "Foregroud service id";
        NotificationChannel channel = new NotificationChannel(
                channelid,
                channelid,
                NotificationManager.IMPORTANCE_LOW
        );

        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, channelid)
                .setContentText("Big brother is watching you")
                .setContentTitle("LaChtiteFamille est en cours d'exécution")
                .setSmallIcon(android.R.drawable.ic_dialog_map);

        startForeground(1001, notification.build());
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location loc) {
        PositionSender dj = new PositionSender();

        Log.e("onLocationChanged: ","Cangement de position Lat: " + loc.getLatitude() + " Lng: "
                + loc.getLongitude() );

        dj.execute(new String[]{Double.toString(loc.getLatitude()), Double.toString(loc.getLongitude())});


        Toast.makeText(
                getBaseContext(),
                "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                        + loc.getLongitude(), Toast.LENGTH_LONG).show();
    }

    public class PositionSender extends AsyncTask<String , Void, String> {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            HttpURLConnection httpURLConnection;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            try {
                URL url = new URL("http://achline.fr/SET/uuid="+uuid+";lat=" + strings[0] + ";lng=" + strings[1]);

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

            if(result.equals("alert")){
                Log.e("alert","L'enfant est pas au mon endroit");

                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));


            }


            return result;
        }
    };




}
