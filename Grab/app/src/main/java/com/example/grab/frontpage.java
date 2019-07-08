package com.example.grab;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class frontpage extends AppCompatActivity {
    Button btn_gotopassenger;
    Button btn_gotodriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frontpage);
        btn_gotopassenger = findViewById(R.id.btn_passenger);
        btn_gotopassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent (frontpage.this,UserLogin.class);
                startActivity(i);

            }
        });
        btn_gotodriver = findViewById(R.id.btn_driver);
        btn_gotodriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i= new Intent (frontpage.this,MainActivity.class);
                startActivity(i);
            }
        });

    }
}
