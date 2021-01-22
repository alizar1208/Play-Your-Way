package com.example.playyourway;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GameLocation extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,GoogleMap.OnMarkerDragListener{


    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastlocation;
    private Marker currentLocationmMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude,longitude;
    double end_lat,end_lon;
    private Button am,save;
    private TextView distance;
    private String teamkey,type;
    private DatabaseReference gameRef;
    private FirebaseAuth mAuth;
    private String address;
    private EditText place;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_location);

        am = (Button)findViewById(R.id.addmarker);
        save = (Button)findViewById(R.id.B_hopistals);
        distance = (TextView)findViewById(R.id.dis);
        place = (EditText)findViewById(R.id.place);
        teamkey= getIntent().getExtras().get("teamkey").toString();
        type= getIntent().getExtras().get("type").toString();

        mAuth = FirebaseAuth.getInstance();
        gameRef = FirebaseDatabase.getInstance().getReference().child("Teams").child(type).child(teamkey);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();

        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

         am.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                MarkerOptions markerOptions1 = new MarkerOptions();
                markerOptions1.position(new LatLng(end_lat,end_lon));
                markerOptions1.title("Game Location");
                markerOptions1.draggable(true);
                float results[] = new float[10];
                Location.distanceBetween(latitude,longitude,end_lat,end_lon,results);
                distance.setText("Distance = "+results[0]/1000 + " km");
                mMap.addMarker(markerOptions1);
                Geocoder geocoder = new Geocoder(GameLocation.this, Locale.getDefault());

                List<Address> addresses  = null;
                try {
                    addresses = geocoder.getFromLocation(end_lat,end_lon, 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                address = addresses.get(0).getAddressLine(0);


                distance.setText("Distance = "+results[0]/1000 + " km");
                place.setText(address);

            }
        });

         save.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 HashMap hostmap = new HashMap();
                 hostmap.put("address",place.getText().toString());
                 hostmap.put("latitude",end_lat);
                 hostmap.put("longitude",end_lon);

                 gameRef.updateChildren(hostmap).addOnCompleteListener(new OnCompleteListener() {
                     @Override
                     public void onComplete(@NonNull Task task) {
                         Toast.makeText(GameLocation.this,"Team Created Successfully!",Toast.LENGTH_SHORT).show();
                         Intent intent = new Intent(GameLocation.this,Drawer.class);
                         startActivity(intent);
                     }
                 });

             }
         });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) !=  PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            bulidGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this,"Permission Denied" , Toast.LENGTH_LONG).show();
                }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            bulidGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }


    protected synchronized void bulidGoogleApiClient() {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();

    }

    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        end_lon = longitude;
        end_lat  = latitude;
        lastlocation = location;
        if(currentLocationmMarker != null)
        {
            currentLocationmMarker.remove();

        }
        Log.d("lat = ",""+latitude);
        LatLng latLng = new LatLng(location.getLatitude() , location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Your Location.");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentLocationmMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(12));

        Geocoder geocoder = new Geocoder(GameLocation.this, Locale.getDefault());

        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(latitude,longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
         address = addresses.get(0).getAddressLine(0);


        distance.setText("Distance = 0"+ " km");
        place.setText(address);




        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }


    public boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED )
        {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION },REQUEST_LOCATION_CODE);
            }
            return false;

        }
        else
            return true;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setDraggable(true);
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

        end_lat = marker.getPosition().latitude;
        end_lon = marker.getPosition().longitude;
        float results[] = new float[10];
        Location.distanceBetween(latitude,longitude,end_lat,end_lon,results);



        Geocoder geocoder = new Geocoder(GameLocation.this, Locale.getDefault());

        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(end_lat,end_lon, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        address = addresses.get(0).getAddressLine(0);


        distance.setText("Distance = "+results[0]/1000 + " km");
        place.setText(address);

    }
}