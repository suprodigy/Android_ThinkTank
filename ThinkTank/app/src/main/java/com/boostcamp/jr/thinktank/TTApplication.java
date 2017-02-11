package com.boostcamp.jr.thinktank;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by jr on 2017-02-09.
 */

public class TTApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }

}
