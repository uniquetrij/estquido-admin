package com.infy.stg.estquido.admin.ui.centers;

import android.location.Address;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.MutableDocument;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddCenterActivity extends AppCompatActivity {

    private TextInputEditText etLatitude;
    private TextInputEditText etLongitude;
    private TextInputEditText etCity;
    private TextInputEditText etLocation;
    private TextInputEditText etCountry;
    private TextInputEditText etCenterName;
    private TextInputEditText etCenterID;
    private Address address;

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
        runOnUiThread(() -> {
            try {
                etLatitude.setText(String.valueOf(This.LOCATION.get().getLatitude()));
                etLongitude.setText(String.valueOf(This.LOCATION.get().getLongitude()));
                address = This.GEOCODER.get().getFromLocation(This.LOCATION.get().getLatitude(), This.LOCATION.get().getLongitude(), 1).get(0);
                etCountry.setText(address.getCountryName());
                etCity.setText(address.getLocality());
                etLocation.setText(address.getSubLocality());
            } catch (IOException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
        });
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
        document.setValue("locality", address.getSubLocality());
        document.setValue("city", address.getLocality());
        document.setValue("country", address.getCountryName());
        Map<String, Object> map = new HashMap<>();
        map.put("lat", This.LOCATION.get().getLatitude());
        map.put("lon", This.LOCATION.get().getLongitude());
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
