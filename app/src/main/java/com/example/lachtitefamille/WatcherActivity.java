package com.example.lachtitefamille;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WatcherActivity extends AppCompatActivity {

    private Button download;

    private MapView myOpenMapView;
    private List<List<GeoPoint>> childPositions = new ArrayList<>();
    private List<String> childNames = null;
    int[] couleurs = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW};


    private static final String[] paths = {"item 1", "item 2", "item 3"};

    protected  List<GeoPoint>  getGPSCircle(@NonNull GeoPoint point, double rayon)
    {

        double xN,yN,zN;

        double latitude = point.getLatitude();
        double longitude = point.getLongitude();
        double polaire = Math.toRadians(90.-latitude);
        double azimuth = Math.toRadians(longitude);

        // normale au plan normalisée

        xN = Math.sin(polaire)*Math.cos(azimuth);
        yN = Math.sin(polaire)*Math.sin(azimuth);
        zN = Math.cos(polaire);
        Point3d n = new Point3d(xN,yN,zN);
        n.normalize();

        //  p = V(R^2 - r^2)
        double rho =  Math.sqrt( 6371.*6371. - rayon*rayon );

        // centre du cercle
        double xc = rho * xN;
        double yc = rho * yN;
        double zc = rho * zN;

        // vect1 du plan
        // x
        Point3d v1 = new Point3d(-yN,xN,0.);
        v1.normalize();
        // y
        Point3d v2 = new Point3d(
                yN*v1.getZ() - zN*v1.getY(),
                zN*v1.getX() - xN*v1.getZ(),
                xN*v1.getY() - yN*v1.getX());
        v2.normalize();

        List<GeoPoint> geoPoints = new ArrayList<>();

        for(double i =0.; i < 6.3;i+=0.1)
        {
            double x = xc +  rayon * (Math.sin(i)*v2.getX() + Math.cos(i)*v1.getX());
            double y = yc +  rayon * (Math.sin(i)*v2.getY() + Math.cos(i)*v1.getY());
            double z = zc +  rayon * (Math.sin(i)*v2.getZ() + Math.cos(i)*v1.getZ());

            double alpha = 6371.;//Math.sqrt(x*x +y*y +z*z);
            double theta = Math.acos( z/alpha);
            double phi   = Math.atan(y/x);

            double lon = Math.toDegrees(phi);
            double lat = 90.-Math.toDegrees(theta);

            GeoPoint pointCercle = new GeoPoint(lat,lon);
            geoPoints.add(pointCercle);
        }
        return geoPoints;

    }

    protected Polygon beCircle(GeoPoint point, double rayon,int color)
    {
        List<GeoPoint> geoPoints = getGPSCircle(point, rayon);
        Polygon polygon = new Polygon();    //see note below
        polygon.setFillColor(color);

        polygon.setPoints(geoPoints);
        polygon.setTitle("A sample polygon");
        return polygon;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GeoPoint point = new GeoPoint(43.610769, 3.876716);
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(getPackageName());

        Context context = this.getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        setContentView(R.layout.activity_watcher);
        download = (Button) findViewById(R.id.downloadButton);
        download.setOnClickListener(btnListener2);



        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uuid = tManager.getDeviceId();


        myOpenMapView = (MapView) findViewById(R.id.mapview);
        myOpenMapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        myOpenMapView.setBuiltInZoomControls(true);
        myOpenMapView.setClickable(true);
        myOpenMapView.getController().setZoom(15);
        CompassOverlay compassOverlay = new CompassOverlay(this, myOpenMapView);
        compassOverlay.enableCompass();
        myOpenMapView.getOverlays().add(compassOverlay);

        Marker startMarker = new Marker(myOpenMapView);
        startMarker.setPosition(point);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        myOpenMapView.getOverlays().add(startMarker);

        myOpenMapView.getController().setCenter(point);

        //your items
        ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();
        items.add(new OverlayItem("Title", "Description", point)); // Lat/Lon decimal degrees

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "ça clique bien",Toast.LENGTH_SHORT);
                        toast.show();
                        Log.i("DEBUG", "TAPTAP");
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        Log.i("DEBUG", "TAPTAP");
                        return false;
                    }
                }, context);
        mOverlay.setFocusItemsOnTap(true);

        myOpenMapView.getOverlays().add(mOverlay);

        myOpenMapView.getOverlayManager().add(new Overlay() {
            @Override
            public void draw(Canvas c, MapView osmv, boolean shadow) {
                // do nothing
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                //Toast.makeText(mapView.getContext(), e.getX() +":"+ e.getY(), Toast.LENGTH_LONG).show();
                Projection proj = mapView.getProjection();
                GeoPoint p = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int) e.getX(), (int) e.getY());
                String longitude = Double
                        .toString(((double) loc.getLongitude()) );
                String latitude = Double
                        .toString(((double) loc.getLatitude()) );

                Marker startMarker = new Marker(myOpenMapView);
                startMarker.setPosition(loc);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                myOpenMapView.getOverlays().add(startMarker);
                EditText editText=findViewById(R.id.rayon);
                String temp=editText.getText().toString();
                double value=0.;
                if (!"".equals(temp)){
                    value=Double.parseDouble(temp);
                }

                double rayon = value;
                RadioGroup radioButtonGroup = findViewById(R.id.radioGroup);
                int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
                View radioButton = radioButtonGroup.findViewById(radioButtonID);
                int idx = radioButtonGroup.indexOfChild(radioButton);


                myOpenMapView.getOverlays().add(beCircle(loc,rayon, Color.argb(75, Color.red(couleurs[idx]), Color.green(couleurs[idx]), Color.blue(couleurs[idx]))));
                MyNetworkTask mynet = new MyNetworkTask();
                mynet.execute(uuid,longitude,latitude,Double.toString(rayon));



                return true;

            }
        });




    }
    private class MyNetworkTask extends AsyncTask<String , Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            HttpURLConnection httpURLConnection;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            try {
                URL url = new URL((String) "http://achline.fr/ADDZONE/"+(String)strings[0]+";"+(String)strings[1]+";"+(String)strings[2]+";"+(String)strings[3]);

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
            return null;
        }
    }

    public class DownloadJson extends AsyncTask<String , Void, String> {
        @Override
        protected String doInBackground(String...strings) {
            String result ="";
            URL url;
            HttpURLConnection httpURLConnection;
            InputStream inputStream;
            InputStreamReader inputStreamReader;
            try
            {
                url = new URL(strings[0]);

                Log.i("DEBUG", strings[0]);
                httpURLConnection =(HttpURLConnection) url.openConnection();

                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                int data = inputStreamReader.read();
                while(data != -1)
                {
                    result += (char) data;
                    data = inputStreamReader.read();

                }

            }catch (MalformedURLException e)
            {
                e.printStackTrace();
            }catch (IOException e)
            {
                e.printStackTrace();
            }

            return result;
        }
        @Override
        protected void onPostExecute(String result) {



            try {



                JSONArray enfants  = new JSONArray(result);
                List<GeoPoint> childPosition = null;

                for (int j=0; j < enfants.length(); j++) {
                    JSONArray data = enfants.getJSONObject(j).optJSONArray("data");
                    childPosition = new ArrayList<>();

                    for (int i = 0; i < data.length(); i++) {
                        JSONObject object = data.getJSONObject(i);
                        double lat = object.optDouble("lat");
                        double lng = object.optDouble("lng");

                        Log.i("DEBUG", String.valueOf(lat));
                        childPosition.add(new GeoPoint(lat, lng));



                    }

                    childPositions.add(childPosition);

                    Polygon polygon3 = new Polygon();    //see note below
                    polygon3.setStrokeColor(couleurs[j]);
                    childPosition.add(childPosition.get(0));    //forces the loop to close
                    polygon3.setPoints(childPosition);
                    polygon3.setTitle("Tracé enfant");
                    myOpenMapView.getOverlayManager().add(polygon3);



                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    private View.OnClickListener btnListener2 = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View view) {
            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);

            String uuid = tManager.getDeviceId();

            String url = "http://www.achline.fr/GETMYCHILDREN/"+uuid;


            DownloadJson dj = new DownloadJson();
            dj.execute(url);

        }
    };





}