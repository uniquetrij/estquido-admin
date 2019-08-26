package com.infy.stg.estquido.admin.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.android.volley.VolleyError;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.CBLService;
import com.infy.stg.estquido.admin.app.services.CBRestService;
import com.infy.stg.estquido.admin.ui.main.fragments.CenterFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CentersActivity extends AppCompatActivity  implements CenterFragment.OnListFragmentInteractionListener {

    private static final String TAG = CentersActivity.class.getName();
    private FusedLocationProviderClient mFusedLocationClient;
    private String center;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());
//        This.MAIN_ACTIVITY.set(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new CenterFragment())
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
                    if (location != null) {
//                        MainActivity.this.location = location;
                        Log.d(TAG, " " + location);

                        new CBRestService().request(This.Static.QUERY_CENTER_URL, new CBRestService.Callback() {
                            @Override
                            public void onError(VolleyError error) {
                                center = null;
                            }

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    center = response.getJSONArray("hits").getJSONObject(0).getString("id").replace("center_", "");
                                } catch (JSONException | ArrayIndexOutOfBoundsException e) {
                                    center = null;
                                }
                            }
                        }, CBRestService.centerRequest(location));
                        timer.cancel();
                    }
                });
            }
        }, 0, 1000);

        CBLService cblCenters = new CBLService(This.Static.COUCHBASE_CENTERS_URL, This.Static.COUCHBASE_DB, This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS);
        cblCenters.async(ReplicatorConfiguration.ReplicatorType.PUSH_AND_PULL, new CBLService.Callback() {
            @Override
            public void onError(ReplicatorChange change) {

            }

            @Override
            public void onUpdate(ReplicatorChange change) {
                Log.d(TAG, "CBL " + change.getStatus().getProgress());
                try {
                    ResultSet execute = QueryBuilder.select(SelectResult.all()).from(DataSource.database(cblCenters.getDatabase())).execute();
                    This.CENTERS.clear();
                    execute.allResults().forEach(result -> {
                        This.CENTERS.add((Map<String, Object>) result.toMap().get("estquido"));
                        Log.d(TAG, "CBL " + ((Map<String, Object>) result.toMap().get("estquido")).get("id"));
                    });
                    Log.d(TAG, "CBL " + This.CENTERS);

                } catch (CouchbaseLiteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onListFragmentInteraction(Map map) {

    }
}
