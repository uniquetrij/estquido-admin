package com.infy.stg.estquido.admin.ui.buildings;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.MutableDocument;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AddBuildingActivity extends AppCompatActivity {

    private FusedLocationProviderClient mFusedLocationClient;

    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private TextInputEditText etBuildingAddress;
    private Location mLocation;
    private Address mAddress;
    private TextInputEditText etCenterID;
    private TextInputEditText etCenterName;
    private TextInputEditText etBuildingID;
    private TextInputEditText etBuildingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_building);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etCenterID = findViewById(R.id.etCenterID);
        etCenterName = findViewById(R.id.etCenterName);
        etBuildingAddress = findViewById(R.id.etBuildingAddress);

        etBuildingID = findViewById(R.id.etBuildingID);
        etBuildingName = findViewById(R.id.etBuildingName);

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
                mFusedLocationClient.getLastLocation().addOnSuccessListener(AddBuildingActivity.this, location -> {
                    Log.d("LOCATION", location.toString());
                    if (location != null) {
                        mLocation = location;
                        etLatitude.setText(String.valueOf(location.getLatitude()));
                        etLongitude.setText(String.valueOf(location.getLongitude()));
                        etCenterID.setText(This.CENTER.get());
                        etCenterName.setText((String) This.CBL_CENTERS.get().getDatabase().getDocument("center_" + This.CENTER.get()).toMap().get("name"));
                        runOnUiThread(() -> {
                            try {
                                mAddress = This.GEOCODER.get().getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
                                etBuildingAddress.setText(mAddress.getAddressLine(0));
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

    public void fabAddBuildingOnClick(View view) {
        String id = etBuildingID.getText().toString().trim();
        if (id.length() < 3) {
            etBuildingID.setError("Enter at least 2 characters");
            return;
        }

        MutableDocument document = This.CBL_DATABASE.get().getDatabase().getDocument("buildings_" + This.CENTER.get()).toMutable();

        if (document.toMap().keySet().contains(id)) {
            etBuildingID.setError("Building with this ID already exists");
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("location", Arrays.asList(mLocation.getLatitude(), mLocation.getLongitude()));
        map.put("name", etBuildingName.getText().toString().trim());
        map.put("address", etBuildingAddress.getText().toString().trim());
        document.setValue(id, map);
        try {
            This.CBL_DATABASE.get().getDatabase().save(document);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        } finally {
            finish();
        }

    }
}
