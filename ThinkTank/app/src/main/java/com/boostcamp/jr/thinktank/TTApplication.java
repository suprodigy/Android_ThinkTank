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

        // Realm 데이터 베이스 초기화
        Realm.init(this);

        /**
         * Calligraphy library 사용을 위한 초기화,
         * Activity.attachBaseContext(Context)에 추가적으로 정의해줘야...
         * 해당 어플리케이션에서는 MyActivity에 이를 정의해서 Activity는 MyActivity를 상속받도록 함.
         */
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/NanumBarunGothic.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

}
