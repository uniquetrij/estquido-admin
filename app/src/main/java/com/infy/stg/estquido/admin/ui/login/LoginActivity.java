package com.infy.stg.estquido.admin.ui.login;

import android.content.Intent;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.ui.centers.CentersActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());
        This.GEOCODER.set(new Geocoder(getApplicationContext()));

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CentersActivity.class);
                startActivity(intent);
            }
        });
    }
}
