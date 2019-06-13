package com.example.fuentesmadrid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.example.fuentesmadrid.Model.Fountain;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 1000;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0]==PackageManager.PERMISSION_DENIED){

                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat= location.getLatitude();
                            double lon = location.getLongitude();
                            LatLng currentLocation = new LatLng(lat,lon);
                            CameraUpdate center= CameraUpdateFactory.newLatLng(currentLocation);
                            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                            mMap.moveCamera(center);
                            mMap.animateCamera(zoom);
                        }else{
                            LatLng madrid = new LatLng(40.416905, -3.703563);
                            CameraUpdate center= CameraUpdateFactory.newLatLng(madrid);
                            CameraUpdate zoom=CameraUpdateFactory.zoomTo(15);

                            mMap.moveCamera(center);
                            mMap.animateCamera(zoom);
                        }
                    }
                });
        parseXML();
    }

    private void parseXML(){
        XmlPullParserFactory parserFactory;

        try {
            parserFactory= XmlPullParserFactory.newInstance();
            XmlPullParser parser= parserFactory.newPullParser();
            InputStream is= getResources().openRawResource(R.raw.fuentes2019);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,false);
            parser.setInput(is,null);
            getFountainList(parser);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getFountainList(XmlPullParser parser) throws XmlPullParserException, IOException {
        ArrayList<Fountain> fountains = new ArrayList<>();

        int eventType=parser.getEventType();
        Fountain fountain=null;

        while (eventType!=XmlPullParser.END_DOCUMENT){
            String parserName=null;

            switch (eventType){
                case XmlPullParser.START_TAG:
                    parserName=parser.getName();

                    if("entry".equals(parserName)){
                        fountain=new Fountain();
                        fountains.add(fountain);
                    } else if(fountain!=null){
                        if("title".equals(parserName)) {
                            fountain.setTitle(parser.nextText());
                        }else if("geo:lat".equals(parserName)){
                            fountain.setGeolat(parser.nextText());
                        } else if("geo:long".equals(parserName)) {
                            fountain.setGeolong(parser.nextText());
                        }
                    }
                    break;
            }
            eventType=parser.next();
        }

        for (Fountain fountain2: fountains) {
                LatLng fountainLocation = new LatLng(Double.parseDouble(fountain2.getGeolat()),Double.parseDouble(fountain2.getGeolong()));
                mMap.addMarker(new MarkerOptions().position(fountainLocation).title(""+fountain2.getTitle()));
            }
        }



}
