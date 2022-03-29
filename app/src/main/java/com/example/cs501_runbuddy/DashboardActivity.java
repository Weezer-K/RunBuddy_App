package com.example.cs501_runbuddy;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class DashboardActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mapAPI;
    private SupportMapFragment mapFragment;
    private Button nextLoc;
    private LatLng Point;
    private ArrayList<LatLng> pointList = new ArrayList<LatLng>();

    private Polyline poly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAPI);
        mapFragment.getMapAsync(this);

        nextLoc = (Button) findViewById(R.id.nexLoc);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapAPI = googleMap;
        poly = googleMap.addPolyline(new PolylineOptions().add(new LatLng(10.349895, -71.107755)));
        Point = new LatLng(10.349895, -71.107755);
        pointList.add(Point);
        mapAPI.addMarker(new MarkerOptions().position(Point).title("TestPoint"));
        mapAPI.moveCamera(CameraUpdateFactory.newLatLng(Point));

        nextLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Double y = Point.latitude + 5;
                Double x = Point.longitude;
                Point = new LatLng(y, x);
                pointList.add(Point);
                mapAPI.clear();
                poly = googleMap.addPolyline(new PolylineOptions().add(pointList.get(0)));
                poly.setPoints(pointList);
                poly.setVisible(true);
                mapAPI.addMarker(new MarkerOptions().position(Point).title("TestPoint"));
                mapAPI.moveCamera(CameraUpdateFactory.newLatLng(Point));
            }
        });
    }
}