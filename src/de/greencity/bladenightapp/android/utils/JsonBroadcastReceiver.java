package de.greencity.bladenightapp.android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

public abstract class JsonBroadcastReceiver<T> extends BroadcastReceiver {
    public JsonBroadcastReceiver(String logPrefix, Class<T> clazz) {
        this.clazz = clazz;
        this.logPrefix = logPrefix;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, logPrefix+" onReceive");
        if ( intent.getExtras() == null ) {
            Log.e(TAG,"No extras available in " + intent);
            return;
        }
        String json = (String) intent.getExtras().get("json");
        if ( json == null ) {
            Log.e(TAG,"Failed to get json in " + intent);
            return;
        }
        Log.d(TAG, json);
        T object = (T) new Gson().fromJson(json, clazz);
        if ( object == null ) {
            Log.e(TAG,"Failed to parse json");
            return;
        }
        onReceive(object);
    }

    public abstract void onReceive(T object);

    final String TAG = "JsonBroadcastReceiver";
    private Class<T> clazz;
    private String logPrefix;
}
