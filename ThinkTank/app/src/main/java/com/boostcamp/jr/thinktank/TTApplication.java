package com.boostcamp.jr.thinktank;

import android.app.Application;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by jr on 2017-02-09.
 */

public class TTApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NanumBarunGothic.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

}
