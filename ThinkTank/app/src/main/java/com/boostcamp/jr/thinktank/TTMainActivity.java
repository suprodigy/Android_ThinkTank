package com.boostcamp.jr.thinktank;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.GridLayoutManager.LayoutParams;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.boostcamp.jr.thinktank.utils.TestUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

// TODO (4) Keyword 관계도 구현 (KeywordManager.java) - TestData로 테스트

public class TTMainActivity extends MyActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;
    @BindView(R.id.layout_show_keyword)
    GridLayout mLayoutShowKeyword;
    @BindView(R.id.layout_progress_bar)
    View mLayoutProgressBar;

    List<TextView> mTextViews = new ArrayList<>();

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

        // GridLayout에 TextView를 add
        setTextViewsOnGridLayout();

        TestUtil.checkKeyword();

        // layout_show_keyword Item set
        // Default - 가장 많이 언급된 키워드를 기준으로 BFS
        String startKeyword = KeywordObserver.get().getKeywordNameThatHasMaxCount();
        if (startKeyword != null) {
            setLayoutShowKeyword(startKeyword);
        }
    }

    private void setTextViewsOnGridLayout() {

        for (int i=0; i<5; i++) {
            for (int j = 0; j < 5; j++) {
                TextView textView = new TextView(this);
                textView.setTextColor(getResources().getColor(R.color.blue));
                CalligraphyUtils.applyFontToTextView(this, textView, "fonts/NanumPen.ttf");
                mLayoutShowKeyword.addView(textView);
                mTextViews.add(textView);
            }
        }

    }

    private void setLayoutShowKeyword(String startKeyword) {

        List<Pair<String, Integer>> keywordList = KeywordManager.get().getKeywordByBFS(startKeyword);

        Pair<Integer, Integer> minMaxCount = KeywordManager.get().getMinMaxCount(keywordList);

        for (int i=0; i<keywordList.size(); i++) {
            TextView textView = mTextViews.get(KeywordUtil.getOrderFromCount(i));
            textView.setText("#" + keywordList.get(i).first);
            float textSize = KeywordUtil.getTextSize(keywordList.get(i).second, minMaxCount);
            textView.setTextSize(textSize);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView textView = (TextView) v;
                    String keywordName = KeywordUtil.removeTag(textView.getText().toString());
                    setLayoutShowKeyword(keywordName);
                }
            });
        }

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
            new TestTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // text data 생성 후 mLayoutShowKeyword set
    private class TestTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        // test data 생성
        @Override
        protected Void doInBackground(Void... params) {
            for (int i=0; i<100; i++) {
                TestUtil.generateThink(getApplicationContext());
            }
            return null;
        }

        // 생성된 테스트 데이터로 layout set
        @Override
        protected void onPostExecute(Void aVoid) {
            String startKeyword = KeywordObserver.get().getKeywordNameThatHasMaxCount();
            setLayoutShowKeyword(startKeyword);
            showResult();
        }

        private void showProgressBar() {
            mLayoutShowKeyword.setVisibility(View.INVISIBLE);
            mLayoutProgressBar.setVisibility(View.VISIBLE);
        }

        private void showResult() {
            mLayoutProgressBar.setVisibility(View.INVISIBLE);
            mLayoutShowKeyword.setVisibility(View.VISIBLE);
        }

    }

}
