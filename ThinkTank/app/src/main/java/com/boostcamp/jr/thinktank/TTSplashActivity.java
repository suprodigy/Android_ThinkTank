package com.boostcamp.jr.thinktank;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.wang.avi.AVLoadingIndicatorView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TTSplashActivity extends MyActivity {

    @BindView(R.id.start_indicator)
    AVLoadingIndicatorView mIndicatorView;

    private final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_splash_activity);
        ButterKnife.bind(this);

         /* SPLASH_DISPLAY_LENGTH 뒤에 메뉴 액티비티를 실행시키고 종료한다.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* 메뉴액티비티를 실행하고 로딩화면을 죽인다.*/
                Intent mainIntent = new Intent(TTSplashActivity.this, TTMainActivity.class);
                TTSplashActivity.this.startActivity(mainIntent);
                TTSplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);

        mIndicatorView.show();
    }
}
