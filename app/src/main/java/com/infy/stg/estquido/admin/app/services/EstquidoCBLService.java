package com.infy.stg.estquido.admin.app.services;

import androidx.annotation.NonNull;

import com.couchbase.lite.AbstractReplicator;
import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentFlag;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.infy.stg.estquido.admin.app.This;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.EnumSet;


public class EstquidoCBLService {

    public interface Callback {
        public void onError(AbstractReplicator.Status status);

        public void onSuccess(AbstractReplicator.Status status);
    }

    private static final String TAG = EstquidoCBLService.class.getName();

    public static final Database DATABASE;
    private static ReplicatorConfiguration reConfig;

    static {
        Database database = null;
        try {
            database = new Database(This.Static.COUCHBASE_DB, new DatabaseConfiguration(This.CONTEXT.get()));
            reConfig = new ReplicatorConfiguration(database, new URLEndpoint(new URI(This.Static.COUCHBASE_URL)));
            reConfig.setAuthenticator(new BasicAuthenticator(This.Static.COUCHBASE_USER, This.Static.COUCHBASE_PASS));
        } catch (URISyntaxException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
        DATABASE = database;
    }

    public static void sync(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        reConfig.setReplicatorType(type);
        reConfig.setDocumentIDs(Arrays.asList(documents));
        Replicator replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                callback.onError(change.getStatus());
            } if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
                callback.onSuccess(change.getStatus());
            }
        });
        replicator.start();
    }

    public static void synca(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        reConfig.setReplicatorType(type);
        reConfig.setPullFilter(new ReplicationFilter() {
            @Override
            public boolean filtered(@NonNull Document document, @NonNull EnumSet<DocumentFlag> flags) {
                return true;
            }
        });
        reConfig.setPushFilter(new ReplicationFilter() {
            @Override
            public boolean filtered(@NonNull Document document, @NonNull EnumSet<DocumentFlag> flags) {
                return true;
            }
        });
        Replicator replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                callback.onError(change.getStatus());
            } if (change.getStatus().getActivityLevel().equals(AbstractReplicator.ActivityLevel.STOPPED)) {
                callback.onSuccess(change.getStatus());
            }
        });
        replicator.start();
    }
}
