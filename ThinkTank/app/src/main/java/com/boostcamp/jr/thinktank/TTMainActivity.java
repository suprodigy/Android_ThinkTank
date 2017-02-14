package com.boostcamp.jr.thinktank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// TODO (4) Keyword 관계도 구현 (KeywordManager.java) - TestData로 테스트

public class TTMainActivity extends MyActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;

    @OnClick(R.id.add_think_button)
    public void onAddButtonClicked() {
        Intent intent = new Intent(this, TTDetailActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.keyword_search_button)
    public void onSearchButtonClicked() {
        Intent intent = new Intent(this, TTListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // 제목 없애기
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tt_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
