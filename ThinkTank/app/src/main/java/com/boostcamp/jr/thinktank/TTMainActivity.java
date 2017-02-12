package com.boostcamp.jr.thinktank;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// TODO (6) Calligraphy library 이용하기
// TODO (7) DesignSpec API 레벨 호환성 해결하기
// TODO (8) Keyword 관계도 구현 (KeywordManager.java)

public class TTMainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

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
