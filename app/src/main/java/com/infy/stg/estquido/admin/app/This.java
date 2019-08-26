package com.infy.stg.estquido.admin.app;

import android.app.Application;
import android.content.Context;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

import com.infy.stg.estquido.admin.ui.main.fragments.CenterRecyclerViewAdapter;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public class This {

    public static final AtomicReference<Context> CONTEXT = new AtomicReference<>();
    public static final AtomicReference<Application> APPLICATION = new AtomicReference<>();

    public static final ObservableList<Map> CENTERS = new ObservableArrayList<>();
    public static CenterRecyclerViewAdapter K;


    public static class Static {
        public static final String COUCHBASE_URL = "ws://192.168.1.101:4984/estquido";
        public static final String COUCHBASE_CENTERS_URL = "ws://192.168.1.101:4986/estquido";
        public static final String COUCHBASE_DB = "estquido";
        public static final String COUCHBASE_USER = "estquido";
        public static final String COUCHBASE_PASS = "estquido";
        public static final String QUERY_CENTER_URL = "http://192.168.1.101:8094/api/index/estquido-centers-geo-search/query";
    }
}
