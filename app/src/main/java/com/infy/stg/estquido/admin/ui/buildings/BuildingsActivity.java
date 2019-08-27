package com.infy.stg.estquido.admin.ui.buildings;

import android.content.Intent;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.CBLService;
import com.infy.stg.estquido.admin.ui.buildings.fragments.BuildingFragment;
import com.infy.stg.estquido.admin.ui.centers.AddCenterActivity;
import com.infy.stg.estquido.admin.ui.centers.fragments.CenterFragment;

import java.util.Map;

public class BuildingsActivity extends AppCompatActivity implements BuildingFragment.OnListFragmentInteractionListener {

    private static final String TAG = BuildingsActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerBuildingsFragment, new BuildingFragment())
                    .commitNow();
        }

        Log.d(TAG, "CBL " + "buildings_" + This.CENTER.get());


        CBLService cblDatabase = new CBLService(This.Static.COUCHBASE_URL, This.Static.COUCHBASE_DB, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS);
        This.CBL_DATABASE.set(cblDatabase);
        cblDatabase.async(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
            @Override
            public void onError(ReplicatorChange change) {

            }

            @Override
            public void onUpdate(ReplicatorChange change) {
                Log.d(TAG, "CBL " + change.getStatus().getProgress());
                Document document = cblDatabase.getDatabase().getDocument("buildings_" + This.CENTER.get());
                if (document == null) {
                    document = new MutableDocument("buildings_" + This.CENTER.get());
                    try {
                        This.CBL_DATABASE.get().getDatabase().save((MutableDocument) document);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                }
                This.BUILDINGS.clear();
                Log.d(TAG, "CBL " + document.toMap());
                document.toMap().entrySet().forEach((Map.Entry<String, Object> o) -> {
                    Log.d(TAG, "CBL " + o);
                    This.BUILDINGS.add(o);
                });
            }
        }, "buildings_" + This.CENTER.get());
    }

    @Override
    public void onListFragmentInteraction(Map.Entry<String, Object> item) {

    }

    public void fabAddBuildingOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AddBuildingActivity.class);
        startActivity(intent);
    }
}
