package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import java.util.Date;

public class TTRandomListActivity extends MyActivity {

    private static final String EXTRA_KEYWORD = "keyword";
    private static final String EXTRA_DATE = "date";

    public static Intent newIntent(Context packageContext, String keyword) {
        Intent intent = new Intent(packageContext, TTRandomListActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        return intent;
    }

    public static Intent newIntent(Context packageContext, Date date) {
        Intent intent = new Intent(packageContext, TTRandomListActivity.class);
        intent.putExtra(EXTRA_DATE, date);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_list);
    }
}
