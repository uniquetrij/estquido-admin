package com.infy.stg.estquido.admin.ui.centers;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.MutableDocument;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AddCenterActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLocation;


    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private TextInputEditText etCity;
    private TextInputEditText etLocation;
    private TextInputEditText etCountry;
    private TextInputEditText etCenterName;
    private TextInputEditText etCenterID;
    private Address mAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_center);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etCountry = findViewById(R.id.etCountry);
        etCity = findViewById(R.id.etCity);
        etLocation = findViewById(R.id.etLocation);

        etCenterID = findViewById(R.id.etCenterID);
        etCenterName = findViewById(R.id.etCenterName);

        etCenterID.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {
                String s = et.toString();
                if (!s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    etCenterID.setText(s);
                    etCenterID.setSelection(s.length());
                }
            }
        });

        ProgressBar progressBar = findViewById(R.id.pbLoading);
        progressBar.setVisibility(View.VISIBLE);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(AddCenterActivity.this, location -> {
                    Log.d("LOCATION", location.toString());
                    if (location != null) {
                        mLocation = location;
                        etLatitude.setText(String.valueOf(location.getLatitude()));
                        etLongitude.setText(String.valueOf(location.getLongitude()));

                        runOnUiThread(() -> {
                            try {
                                mAddress = This.GEOCODER.get().getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                etCountry.setText(mAddress.getCountryName());
                                etCity.setText(mAddress.getLocality());
                                etLocation.setText(mAddress.getSubLocality());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            progressBar.setVisibility(View.GONE);
                        });
                        timer.cancel();
                    }
                });
            }
        }, 0, 1000);


    }


    public void fabAddCenterOnClick(View view) {
        String id = etCenterID.getText().toString().trim();
        if (id.length() < 3) {
            etCenterID.setError("Enter at least 3 characters");
            return;
        }
        String name = etCenterName.getText().toString().trim();
        if (name.length() < 4) {
            etCenterName.setError("Enter at least 4 characters");
            return;
        }
        if (This.CBL_CENTERS.get().getDatabase().getDocument("center_" + id) != null) {
            etCenterID.setError("Center with this ID already exists");
            return;
        }

        MutableDocument document = new MutableDocument("center_" + id);
        document.setValue("id", id);
        document.setValue("name", name);
        document.setValue("type", "center");
        document.setValue("locality", etLocation.getText().toString());
        document.setValue("city", etCity.getText().toString());
        document.setValue("country", etCountry.getText().toString());
        Map<String, Object> map = new HashMap<>();
        map.put("lat", mLocation.getLatitude());
        map.put("lon", mLocation.getLongitude());
        document.setValue("geo", map);

        try {
            This.CBL_CENTERS.get().getDatabase().save(document);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }
}
