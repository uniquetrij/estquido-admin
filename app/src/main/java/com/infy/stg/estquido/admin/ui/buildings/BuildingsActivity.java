package com.infy.stg.estquido.admin.ui.buildings;

import android.content.Intent;
import android.os.Bundle;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.CBLService;
import com.infy.stg.estquido.admin.ui.buildings.fragments.BuildingFragment;
import com.infy.stg.estquido.admin.ui.mapper.MapperActivity;
import com.infy.stg.estquido.admin.ui.qr.QRScannerActivity;

import java.util.List;
import java.util.Map;

public class BuildingsActivity extends AppCompatActivity implements BuildingFragment.OnListFragmentInteractionListener {

    private static final String TAG = BuildingsActivity.class.getName();
    public static final int QR_REQUEST_CODE = 0;
//    private Replicator mReplicator;

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

        Log.d(TAG, "CBL " + "buildings_" + This.GPS_CENTER.get());


        if (This.CBL_DATABASE.get() == null) {
            This.CBL_DATABASE.set(new CBLService(This.Static.COUCHBASE_DATABASE_URL, This.Static.COUCHBASE_DATABASE, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
        }

        CBLService cblDatabase = This.CBL_DATABASE.get();
        Replicator mReplicator = cblDatabase.async(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
            @Override
            public void onError(ReplicatorChange change) {

            }

            @Override
            public void onUpdate(ReplicatorChange change) {
                Log.d(TAG, "CBL " + change.getStatus().getProgress());
                refresh();
            }
        }, "buildings_" + This.CENTER.get());
        Log.d(TAG, "REPLICATOR " + "START");
    }

    public static void refresh() {
        String id = "buildings_" + This.CENTER.get();
        Log.d(TAG, "BUILDING DOCUMENT "+ id);
        Document document = This.CBL_DATABASE.get().getDatabase().getDocument(id);
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

    @Override
    public void onListFragmentInteraction(Map.Entry<String, Object> item) {
        This.BUILDING.set(item.getKey());
        This.BUILDING_LOCATION.set(((List<Double>) ((Map<String, Object>) (item.getValue())).get("location")));
        Intent intent = new Intent(getApplicationContext(), QRScannerActivity.class);
        startActivityForResult(intent, QR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QR_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                Log.d(TAG, "QR" + contents);
//                mReplicator.stop();
                Log.d(TAG, "REPLICATOR " + "STOP");
                Intent intent = new Intent(getApplicationContext(), MapperActivity.class);
                startActivity(intent);


            }
        }
        if (resultCode == RESULT_CANCELED) {
            Log.d(TAG, "QR" + "CANCELLED");
        }

    }

    public void fabAddBuildingOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AddBuildingActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mReplicator.start();
        Log.d(TAG, "REPLICATOR " + "START");
        refresh();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mReplicator.stop();
        Log.d(TAG, "REPLICATOR " + "STOP");
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mReplicator.stop();
        Log.d(TAG, "REPLICATOR " + "STOP");
    }
}
