package com.infy.stg.estquido.admin.ui.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.ReplicatorConfiguration;
import com.infy.stg.estquido.admin.R;
import com.infy.stg.estquido.admin.app.This;
import com.infy.stg.estquido.admin.app.services.EstquidoCBLService;

import static com.infy.stg.estquido.admin.app.services.EstquidoCBLService.DATABASE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        This.CONTEXT.set(getApplicationContext());
        This.APPLICATION.set(getApplication());
        This.MAIN_ACTIVITY.set(this);

//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.container, CenterFragment.newInstance())
//                    .commitNow();
//        }

        EstquidoCBLService.synca(ReplicatorConfiguration.ReplicatorType.PULL, new EstquidoCBLService.Callback() {
            @Override
            public void onError(AbstractReplicator.Status status) {
                Log.i(TAG, "ERROR " + status.getError());

            }

            @Override
            public void onSuccess(AbstractReplicator.Status status) {
                Log.i(TAG, "SUCCESS " + DATABASE.getDocument("test").toMap().toString());


            }
        },"test");
    }
}
