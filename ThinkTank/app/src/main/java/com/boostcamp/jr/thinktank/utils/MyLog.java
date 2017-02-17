package com.boostcamp.jr.thinktank.utils;

import android.util.Log;

/**
 * Created by jr on 2017-02-17.
 */

public class MyLog {

    public static final String TAG = "MYLOG";

    public static final boolean wouldPrint = true;

    public static void print(String log) {
        if (wouldPrint) {
            Log.d(TAG, log);
        }
    }

}
