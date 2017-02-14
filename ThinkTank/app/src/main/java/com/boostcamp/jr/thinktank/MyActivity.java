package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by jr on 2017-02-13.
 */

public class MyActivity extends AppCompatActivity {

    // Calligraphy library 사용을 위한 코드
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
