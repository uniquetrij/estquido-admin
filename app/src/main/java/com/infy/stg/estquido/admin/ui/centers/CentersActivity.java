package com.infy.stg.estquido.admin.ui.centers;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Expression;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.CBLService;
import com.infy.stg.estquido.admin.app.services.CBRestService;
import com.infy.stg.estquido.admin.ui.buildings.BuildingsActivity;
import com.infy.stg.estquido.admin.ui.centers.fragments.CenterFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CentersActivity extends AppCompatActivity implements CenterFragment.OnListFragmentInteractionListener {

    private static final String TAG = CentersActivity.class.getName();
    private FusedLocationProviderClient mFusedLocationClient;
//    private Replicator mReplicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centers);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerCentersFragment, new CenterFragment())
                    .commitNow();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation().addOnSuccessListener(CentersActivity.this, location -> {
                    Log.d("LOCATION", "DEFAULT: " + location);
                    if (location != null) {
                        This.LOCATION.set(location);
                        new CBRestService().request(This.Static.QUERY_CENTER_URL, new CBRestService.Callback() {
                            @Override
                            public void onError(VolleyError error) {
                                This.GPS_CENTER.set(null);
                            }

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    This.GPS_CENTER.set(response.getJSONArray("hits").getJSONObject(0).getString("id").replace("center_", ""));
                                    This.CENTERS.add(0, null);
                                    This.CENTERS.remove(0);
                                } catch (JSONException | ArrayIndexOutOfBoundsException e) {
                                    This.GPS_CENTER.set(null);
                                }
                            }
                        }, CBRestService.centerRequest(location));
                        timer.cancel();
                    }
                });
            }
        }, 0, 1000);

        if (This.CBL_CENTERS.get() == null) {
            This.CBL_CENTERS.set(new CBLService(This.Static.COUCHBASE_CENTERS_URL, This.Static.COUCHBASE_CENTERS, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
        }
        CBLService cblCenters = This.CBL_CENTERS.get();
        Replicator mReplicator = cblCenters.async(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
            @Override
            public void onError(ReplicatorChange change) {

            }

            @Override
            public void onUpdate(ReplicatorChange change) {
                Log.d(TAG, "CBL " + change.getStatus().getProgress());
                refresh();
            }
        });
        Log.d(TAG, "REPLICATOR " + "START");
    }


    @Override
    public void onListFragmentInteraction(Map map) {
        Log.d(TAG, "CBL " + map);
        This.CENTER.set((String) map.get("id"));
        Intent intent = new Intent(getApplicationContext(), BuildingsActivity.class);
        startActivity(intent);
//        mReplicator.stop();
        Log.d(TAG, "REPLICATOR " + "STOP");

    }

    public void fabAddCenterOnClick(View view) {
        Intent intent = new Intent(getApplicationContext(), AddCenterActivity.class);
        startActivity(intent);
    }

    public static void refresh() {
        try {
            ResultSet execute = QueryBuilder.select(SelectResult.all()).from(DataSource.database(This.CBL_CENTERS.get().getDatabase()))
                    .where(Expression.property("type").equalTo(Expression.string("center"))).execute();
            This.CENTERS.clear();
            execute.allResults().forEach(result -> {
                This.CENTERS.add((Map<String, Object>) result.toMap().get("estquido-centers"));
                Log.d(TAG, "CBL " + ((Map<String, Object>) result.toMap().get("estquido-centers")).get("id"));
            });
            Log.d(TAG, "CBL " + This.CENTERS);

        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
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
