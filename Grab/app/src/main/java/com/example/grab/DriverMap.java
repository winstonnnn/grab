package com.example.grab;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DriverMap extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, NavigationView.OnNavigationItemSelectedListener {

    GoogleMap mMap;
    private Location lastlocation;
    private Marker userLocation;
    private LocationRequest locationRequest;
    private static final int Request_User_Location_Code = 99;
    private GoogleApiClient googleApiClient;

    //send my location
    Button sendLocation;

    //for links
    public static final String sendLocationUrl = "http://192.168.0.14/GAT/android/insert_driver_location.php";
    public static final String displayBookingUrl = "http://192.168.0.14/GAT/android/display_booking.php";
    public static final String insertBookingUrl = "http://192.168.0.14/GAT/android/insert_booking.php";


    Marker passenger_marker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapDriver);
        mapFragment.getMapAsync(this);

        //send my location
        sendLocation = findViewById(R.id.sendLocation1);

      NavigationView navigationView = findViewById(R.id.nav_menu);
      navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(DriverMap.this, AboutFragments.class);
            startActivity(intent);
        }
            else if(id == R.id.nav_terms) {
            Intent intent = new Intent(DriverMap.this, TermsFragments.class);
            startActivity(intent);
        }else if(id == R.id.nav_contact){
                Intent intent = new Intent(DriverMap.this,ContactFragments.class);
                startActivity(intent);

            }else if (id == R.id.nav_logout){

        }
        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        sendLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                RequestQueue queue = Volley.newRequestQueue(DriverMap.this);


                StringRequest stringRequest = new StringRequest(Request.Method.POST, sendLocationUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(DriverMap.this, response, Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected java.util.Map<String, String> getParams() throws AuthFailureError {
                        java.util.Map<String, String> params = new HashMap<>();
                        params.put("latitude", String.valueOf(lastlocation.getLatitude()));
                        params.put("longitude", String.valueOf(lastlocation.getLongitude()));
                        params.put("username", getIntent().getStringExtra("username"));

                        return params;
                    }
                };

                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        show_notification();
        lastlocation = location;
        if(userLocation != null)
        {
            userLocation.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("user current location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        //userLocation = map.addMarker(markerOptions);

        CameraPosition cameraPosition = CameraPosition.builder()
                .zoom(22)
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if (googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }



    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest ();
        locationRequest.setInterval(1100);
        locationRequest.setFastestInterval(1100);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        }




    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case Request_User_Location_Code:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if (googleApiClient == null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }else{
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }

                    return;
                }
        }
    }

    protected synchronized void buildGoogleApiClient ()
    {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public boolean checkUserLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else{
            return true;
        }
    }

    //for showing notification if may nagbook
    private void show_notification(){


        RequestQueue requestQueue = Volley.newRequestQueue(DriverMap.this);
        StringRequest request = new StringRequest(Request.Method.GET, displayBookingUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response ) {
                JSONObject jsonObject = null;


                try {
                    jsonObject = new JSONObject(response);
                    JSONArray booking = jsonObject.getJSONArray("booking");


                    for (int i = 0; i < booking.length(); i++){
                        String username = booking.getJSONObject(i).getString("username");
                        final String driver_name = booking.getJSONObject(i).getString("driver_username");
                        final String passenger_name = booking.getJSONObject(i).getString("passenger_name");
                        final String vehicle_body_no = booking.getJSONObject(i).getString("vehicle_body_no");
                        final String fare = booking.getJSONObject(i).getString("fare");
                        final String destination = booking.getJSONObject(i).getString("destination");
                        final String latitude = booking.getJSONObject(i).getString("latitude");
                        final String longitude = booking.getJSONObject(i).getString("longitude");


                        if (username.equalsIgnoreCase(getIntent().getStringExtra("username"))){

                            final Dialog dialog = new Dialog(DriverMap.this);
                            dialog.setContentView(R.layout.driver_notification);

                            TextView pass_name = dialog.findViewById(R.id.pass_name);
                            final TextView pass_destination = dialog.findViewById(R.id.pass_destination);
                            TextView pass_fare = dialog.findViewById(R.id.pass_fare);
                            Button accept = dialog.findViewById(R.id.accept);
                            Button ignore = dialog.findViewById(R.id.ignore);

                            pass_name.setText(passenger_name);
                            pass_destination.setText(destination);
                            pass_fare.setText(fare);

                            ignore.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //ilalagay dito na magnonotif sa passenger na inignore sya
                                    RequestQueue requestQueue1 = Volley.newRequestQueue(DriverMap.this);
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, insertBookingUrl, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            if (response.equals("success")){
                                                Toast.makeText(DriverMap.this, "Message sent to Passenger ", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                        .title(passenger_name));
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(DriverMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }){
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String,String>param = new HashMap<>();
                                            param.put("username", getIntent().getStringExtra("username"));
                                            param.put("driver_username" , driver_name);
                                            param.put("vehicle_body_no" , vehicle_body_no);
                                            param.put("passenger_name", passenger_name);
                                            param.put("fare" , fare);
                                            param.put("destination" , destination);
                                            param.put("latitude", String.valueOf(lastlocation.getLatitude()));
                                            param.put("longitude", String.valueOf(lastlocation.getLongitude()));
                                            param.put("ignore", "ignore");
                                            return param;

                                        }
                                    };

                                    requestQueue1.add(stringRequest);

                                    dialog.dismiss();
                                }
                            });



                            accept.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //accepted the book
                                    RequestQueue requestQueue1 = Volley.newRequestQueue(DriverMap.this);
                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, insertBookingUrl, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            if (response.equals("success")){
                                                Toast.makeText(DriverMap.this, "Message sent to Passenger ", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                                if (passenger_marker != null){
                                                    passenger_marker.remove();
                                                }
                                                passenger_marker = mMap.addMarker(new MarkerOptions()
                                                        .position(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude)))
                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                                        .title(passenger_name));
                                                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(latitude),Double.parseDouble(longitude))));
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Toast.makeText(DriverMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }){
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {
                                            Map<String,String>param = new HashMap<>();
                                            param.put("username", getIntent().getStringExtra("username"));
                                            param.put("driver_username" , driver_name);
                                            param.put("vehicle_body_no" , vehicle_body_no);
                                            param.put("passenger_name", passenger_name);
                                            param.put("fare" , fare);
                                            param.put("destination" , destination);
                                            param.put("latitude", String.valueOf(lastlocation.getLatitude()));
                                            param.put("longitude", String.valueOf(lastlocation.getLongitude()));
                                            param.put("ignore", "not");
                                            return param;

                                        }
                                    };

                                    requestQueue1.add(stringRequest);

                                }
                            });


                            dialog.show();
                        }

                    }


                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DriverMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);

    }


}
