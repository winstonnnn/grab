package com.example.grab;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawer;

Button btn_go;
    EditText username;
    EditText password;

    public final static String driverLoginUrl = "http://192.168.0.14/GAT/android/driverlogin.php";
    public final static String updateDriverAvailabilityUrl = "http://192.168.0.14/GAT/android/update_driverAvailability.php";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_go = findViewById(R.id.btn_go);

        //for log in
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, driverLoginUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        if (response.equals("Success")) {
                            Intent intent = new Intent(MainActivity.this, DriverMap.class);
                            intent.putExtra("username", username.getText().toString());
                            startActivity(intent);

                            //for updating availability. gagawin nyang availableyung driver
                            RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
                            StringRequest stringRequest = new StringRequest(Request.Method.POST, updateDriverAvailabilityUrl, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {


                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> param = new HashMap<>();
                                    param.put("username", username.getText().toString());
                                    param.put("availability", "Available");
                                    return param;
                                }
                            };
                            requestQueue.add(stringRequest);

                        } else {
                            Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> param = new HashMap<>();
                        param.put("username", username.getText().toString());
                        param.put("password", password.getText().toString());
                        return param;
                    }
                };
                requestQueue.add(stringRequest);

            }
        });


    }
}