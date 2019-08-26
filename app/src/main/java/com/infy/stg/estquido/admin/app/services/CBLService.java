package com.infy.stg.estquido.admin.app.services;

import com.couchbase.lite.BasicAuthenticator;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Replicator;
import com.couchbase.lite.ReplicatorChange;
import com.couchbase.lite.ReplicatorConfiguration;
import com.couchbase.lite.URLEndpoint;
import com.infy.stg.estquido.admin.app.This;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;


public class CBLService {

    public interface Callback {
        public void onError(ReplicatorChange change);

        public void onUpdate(ReplicatorChange change);
    }

    private static final String TAG = CBLService.class.getName();

    private final Database database;
    private ReplicatorConfiguration reConfig;

    public CBLService(String url, String bucket, String username, String password) {
        Database database = null;
        try {
            database = new Database(bucket, new DatabaseConfiguration(This.CONTEXT.get()));
            reConfig = new ReplicatorConfiguration(database, new URLEndpoint(new URI(url)));
            reConfig.setAuthenticator(new BasicAuthenticator(username, password));
        } catch (URISyntaxException | CouchbaseLiteException e) {
            e.printStackTrace();
        }
        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    public Replicator sync(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        reConfig.setReplicatorType(type);
        reConfig.setContinuous(false);
        if (documents.length > 0)
            reConfig.setDocumentIDs(Arrays.asList(documents));
        Replicator replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                callback.onError(change);
            } else {
                callback.onUpdate(change);
            }
        });
        replicator.start();
        return replicator;
    }

    public Replicator async(ReplicatorConfiguration.ReplicatorType type, Callback callback, String... documents) {
        reConfig.setReplicatorType(type);
        reConfig.setContinuous(true);
        if (documents.length > 0) {
            reConfig.setPullFilter((document, flags) -> Arrays.asList(documents).contains(document.getId()));
            reConfig.setPushFilter((document, flags) -> Arrays.asList(documents).contains(document.getId()));
        }
        Replicator replicator = new Replicator(reConfig);
        replicator.addChangeListener(change -> {
            if (change.getStatus().getError() != null) {
                callback.onError(change);
            } else {
                callback.onUpdate(change);
            }
        });
        replicator.start();
        return replicator;
    }
}
