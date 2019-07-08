package com.example.grab;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserMap<navigationMenuItems> extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnMarkerClickListener,
        NavigationView.OnNavigationItemSelectedListener{

    GoogleMap mMap;
    private Location lastlocation;
    private Marker userLocation;
    private LocationRequest locationRequest;
    private static final int Request_User_Location_Code = 99;
    private GoogleApiClient googleApiClient;
    /**private DrawerLayout drawer;*/

    //for searching address
    EditText searchAddress;
    ImageButton btnSearch;

    //for address information
    BottomSheetBehavior bottomSheetBehavior;
    View address_fare_layout;
    TextView hidebottomsheet;
    TextView destination;
    TextView fareko;
    Button findDriver;

    String name_Info_txt;
    String username_txt;
    String contact_info_txt;
    String body_info_txt;
    Marker marker1;
    int fareholder;
    String destinationholder;
    //fare and destination holder


    //links
    public static final String displayDriverUrl = "http://192.168.0.14/GAT/android/display_driver.php";
    public static final String insertBookingUrl = "http://192.168.0.14/GAT/android/insert_booking.php";
    public static final String displayBookingUrl = "http://192.168.0.14/GAT/android/display_booking.php";

    //google sign in
    GoogleSignInClient googleSignInClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_map);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);




    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            checkUserLocationPermission();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapUser);
        mapFragment.getMapAsync(this);

        //for address information
        address_fare_layout = findViewById(R.id.address_fare);
        bottomSheetBehavior = BottomSheetBehavior.from(address_fare_layout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


        NavigationView navigationView = findViewById(R.id.nav_menu);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            Intent intent = new Intent(UserMap.this, AboutFragments.class);
            startActivity(intent);

        }else if(id == R.id.nav_terms) {
            Intent intent = new Intent(UserMap.this, TermsFragments.class);
            startActivity(intent);
        }else if(id == R.id.nav_contact){
                Intent intent = new Intent(UserMap.this,ContactFragments.class);
                startActivity(intent);

        }else if (id == R.id.nav_logout){
           googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                    Intent intent = new Intent(UserMap.this, frontpage.class);
                    startActivity(intent);
               }
           });
        }
        return false;
    }


    // private void setSupportActionBar(Toolbar toolbar)


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);

        }

        searchLocation();

        mMap.setOnMarkerClickListener(this);



    }

    @Override
    public void onLocationChanged(Location location) {
        show_notification();
        show_ignoreNotification();
        lastlocation = location;
        if(userLocation != null)
        {
            userLocation.remove();
        }


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

    private Marker addressMarker;
    //for searching user address
    private void searchLocation(){
        searchAddress = findViewById(R.id.searchAddress);
        btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geocoder geocoder = new Geocoder(UserMap.this);
                List<Address> addresses;

                if (addressMarker != null){
                    addressMarker.remove();
                }

                if (searchAddress == null){
                    Toast.makeText(UserMap.this, "Type an address", Toast.LENGTH_SHORT).show();
                }
                {
                    try {
                        addresses = geocoder.getFromLocationName(searchAddress.getText().toString(), 10);
                        if (addresses == null) {
                            Toast.makeText(UserMap.this, "No Address", Toast.LENGTH_SHORT).show();
                        }

                        Address location = addresses.get(0);

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
                        markerOptions.title(searchAddress.getText().toString());
                        markerOptions.snippet("address");
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                        addressMarker = mMap.addMarker(markerOptions);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                    } catch (IOException e) {
                        Toast.makeText(UserMap.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    } catch (Exception e){
                        Toast.makeText(UserMap.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        marker1 = marker;

        if (marker.getSnippet().equals("driver")){

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.driver_information);

            TextView name_Info = dialog.findViewById(R.id.name_Info);
            TextView contact_info = dialog.findViewById(R.id.contact_info);
            TextView body_info = dialog.findViewById(R.id.body_info);
            Button book = dialog.findViewById(R.id.book);

            final RequestQueue requestQueue = Volley.newRequestQueue(UserMap.this);
            StringRequest request = new StringRequest(Request.Method.GET, displayDriverUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject jsonObject = null;

                    try {
                        jsonObject = new JSONObject(response);
                        JSONArray drivers = jsonObject.getJSONArray("driver");

                        //ipopoint sa pinakamalapit na driver
                        for (int i = 0; i < drivers.length(); i++){
                            String driver_id = drivers.getJSONObject(i).getString("driver_id");
                            String username = drivers.getJSONObject(i).getString("username");
                            String fname = drivers.getJSONObject(i).getString("fname");
                            String lname = drivers.getJSONObject(i).getString("lname");
                            String mobile_no = drivers.getJSONObject(i).getString("mobile_no");


                            if (driver_id.equals(marker1.getTitle())){
                                // Toast.makeText(UserMap.this, driver_id + ", " + marker1.getTitle(), Toast.LENGTH_SHORT).show();
                                name_Info_txt = fname + " " + lname ;
                                contact_info_txt = mobile_no;
                                body_info_txt = driver_id;
                                username_txt = username;
                            }
                        }


                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(UserMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            requestQueue.add(request);

            name_Info.setText(name_Info_txt);
            contact_info.setText(contact_info_txt);
            body_info.setText(body_info_txt);

            //for booking
            book.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    RequestQueue requestQueue1 = Volley.newRequestQueue(UserMap.this);
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, insertBookingUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(UserMap.this, response, Toast.LENGTH_SHORT).show();
                            if (response.equals("success")){
                                Toast.makeText(UserMap.this, "Your request has been sent to this driver ", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(UserMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String,String>param = new HashMap<>();
                            param.put("username", username_txt);
                            param.put("driver_username" , name_Info_txt);
                            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UserMap.this);
                            param.put("passenger_name" ,account.getDisplayName() );
                            param.put("vehicle_body_no" , body_info_txt);
                            param.put("fare" , String.valueOf(fareholder));
                            param.put("destination" , destinationholder);
                            param.put("latitude" , String.valueOf(lastlocation.getLatitude()));
                            param.put("longitude" , String.valueOf(lastlocation.getLongitude()));
                            param.put("ignore", "not");

                            return param;

                        }
                    };

                    requestQueue1.add(stringRequest);

                }
            });
            dialog.show();

        }else{
            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            int fare = 0;
            Location markerLocation = new Location("markerLocation");
            markerLocation.setLatitude(marker.getPosition().latitude);
            markerLocation.setLongitude(marker.getPosition().longitude);

            Location myLocation = new Location("myLocation");
            myLocation.setLatitude(lastlocation.getLatitude());
            myLocation.setLongitude(lastlocation.getLongitude());

            for (int i = 1; i < 1000000 ; i++){
                if((int) myLocation.distanceTo(markerLocation ) == i){
                    fare =  ((int)myLocation.distanceTo(markerLocation)/10) / 3;

                }
            }

            //fare and destination holder
            fareholder = fare;
            destinationholder = marker.getTitle();

            destination = findViewById(R.id.destination);
            fareko = findViewById(R.id.fareko);
            findDriver = findViewById(R.id.btn_findDriver);
            destination.setText(marker.getTitle());
            fareko.setText(fare + "pesos");


            hidebottomsheet = findViewById(R.id.hidebottomsheet);
            hidebottomsheet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED){
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }

                }
            });

            findDriver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().zoom(13).build()));
                    RequestQueue requestQueue = Volley.newRequestQueue(UserMap.this);
                    StringRequest request = new StringRequest(Request.Method.GET, displayDriverUrl, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            JSONObject jsonObject = null;

                            try {
                                jsonObject = new JSONObject(response);
                                JSONArray drivers = jsonObject.getJSONArray("driver");

                                for (int i = 0; i < drivers.length(); i++){
                                    String driver_id = drivers.getJSONObject(i).getString("driver_id");
                                    String fname = drivers.getJSONObject(i).getString("fname");
                                    String lname = drivers.getJSONObject(i).getString("lname");
                                    String mobile_no = drivers.getJSONObject(i).getString("mobile_no");
                                    String latitude = drivers.getJSONObject(i).getString("latitude");
                                    String longitude = drivers.getJSONObject(i).getString("longitude");
                                    String availability = drivers.getJSONObject(i).getString("availability");

                                    if (availability.equalsIgnoreCase("Available")){
                                        mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)))
                                                .title(driver_id)
                                                .snippet("driver")
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                                    }

                                }

                                //hold the nearest driver
                                Location nearestLocation = new Location("nearestLocation");
                                double count = 0;

                                //ipopoint sa pinakamalapit na driver
                                for (int i = 0; i < drivers.length(); i++){
                                    String driver_id = drivers.getJSONObject(i).getString("driver_id");
                                    String fname = drivers.getJSONObject(i).getString("fname");
                                    String lname = drivers.getJSONObject(i).getString("lname");
                                    String mobile_no = drivers.getJSONObject(i).getString("mobile_no");
                                    String latitude = drivers.getJSONObject(i).getString("latitude");
                                    String longitude = drivers.getJSONObject(i).getString("longitude");
                                    String availability = drivers.getJSONObject(i).getString("availability");


                                    if (availability.equalsIgnoreCase("Available")){

                                        Location driverLocation = new Location("driverLocation");
                                        driverLocation.setLatitude(Double.parseDouble(latitude));
                                        driverLocation.setLongitude(Double.parseDouble(longitude));

                                        Location myLocation = new Location("myLocation");
                                        myLocation.setLatitude(lastlocation.getLatitude());
                                        myLocation.setLongitude(lastlocation.getLongitude());



                                        if (myLocation.distanceTo(driverLocation) < count){

                                        }
                                    }
                                }


                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(UserMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                    requestQueue.add(request);

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
        }

        return false;
    }

    //for showing notification if may nagbook
    private void show_notification(){


        RequestQueue requestQueue = Volley.newRequestQueue(UserMap.this);
        StringRequest request = new StringRequest(Request.Method.GET, displayBookingUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response ) {
                JSONObject jsonObject = null;


                try {
                    jsonObject = new JSONObject(response);
                    JSONArray booking = jsonObject.getJSONArray("booking");



                        String driver_name = booking.getJSONObject(0).getString("driver_username");
                        String passenger_name = booking.getJSONObject(0).getString("passenger_name");
                        final String vehicle_body_no = booking.getJSONObject(0).getString("vehicle_body_no");
                        final String fare = booking.getJSONObject(0).getString("fare");
                        final String destination = booking.getJSONObject(0).getString("destination");
                        final String latitude = booking.getJSONObject(0).getString("latitude");
                        final String longitude = booking.getJSONObject(0).getString("longitude");

                        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UserMap.this);

                        if (passenger_name.equalsIgnoreCase(account.getDisplayName())){

                            final Dialog dialog = new Dialog(UserMap.this);
                            dialog.setContentView(R.layout.passenger_notifications);

                            //calculate the arrival time
                            int arrival = 0;
                            Location driverLocation = new Location("driverLocation");
                            driverLocation.setLatitude(Double.parseDouble(latitude));
                            driverLocation.setLongitude(Double.parseDouble(longitude));

                            Location userLocation = new Location("userLocation");
                            userLocation.setLatitude(lastlocation.getLatitude());
                            userLocation.setLongitude(lastlocation.getLongitude());

                            for (int a = 1; a < 1000000 ; a++){

                                if((int) userLocation.distanceTo(driverLocation ) == a){
                                    arrival =  (int)userLocation.distanceTo(driverLocation)/200;

                                }
                            }
                            TextView arrival_time = dialog.findViewById(R.id.arrival);
                            TextView driverName = dialog.findViewById(R.id.driver_name);


                            arrival_time.setText(arrival + " minutes");
                            driverName.setText(driver_name);

                            Button okay = dialog.findViewById(R.id.okay);

                            okay.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.show();
                        }




                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);

    }

    private void show_ignoreNotification(){

        RequestQueue requestQueue = Volley.newRequestQueue(UserMap.this);
        StringRequest request = new StringRequest(Request.Method.GET, displayBookingUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response ) {
                JSONObject jsonObject = null;


                try {
                    jsonObject = new JSONObject(response);
                    JSONArray booking = jsonObject.getJSONArray("booking");

                    String driver_name = booking.getJSONObject(0).getString("driver_username");
                    String passenger_name = booking.getJSONObject(0).getString("passenger_name");
                    final String ignore = booking.getJSONObject(0).getString("ignore");

                    GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(UserMap.this);

                    if (passenger_name.equalsIgnoreCase(account.getDisplayName()) && ignore.equalsIgnoreCase("ignore")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(UserMap.this);
                        builder.setTitle("Sorry");
                        builder.setMessage("You have been ignored by "+ driver_name);
                        builder.setPositiveButton("Okay", null);
                        Dialog dialog = builder.create();
                        dialog.show();
                    }




                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserMap.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(request);
    }

}