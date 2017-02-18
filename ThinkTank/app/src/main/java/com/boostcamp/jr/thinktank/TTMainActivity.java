package com.boostcamp.jr.thinktank;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.boostcamp.jr.thinktank.utils.TestUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

// TODO (2) textView animation 적용

public class TTMainActivity extends MyActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;
    @BindView(R.id.layout_show_keyword)
    GridLayout mLayoutShowKeyword;
    @BindView(R.id.layout_progress_bar)
    View mLayoutProgressBar;
    @BindView(R.id.input_keyword_edittext)
    AutoCompleteTextView mKeywordInputEditText;

    private ForEffectTask mForEffectTask;
    private List<TextView> mTextViews = new ArrayList<>();
    private boolean mTitleIsShown;

    @OnClick(R.id.add_think_button)
    public void onAddButtonClicked() {
        Intent intent = new Intent(this, TTDetailActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_main);
        ButterKnife.bind(this);

        mTitleIsShown = true;
        setSupportActionBar(mToolbar);

        // 제목 없애기
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // GridLayout에 TextView를 add
        setTextViewsOnGridLayout();

        TestUtil.checkKeyword();

        initLayoutShowKeyword();

        isStoragePermissionGranted();

        setMainAutoComplete();
    }

    private void setMainAutoComplete() {
        List<String> items = KeywordObserver.get().getAllKeywordNames();
        items.add("모든 메모");
        items.add("모든 키워드");

        mKeywordInputEditText.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                items
        ));
    }

    private void initLayoutShowKeyword() {
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
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(GridLayout.UNDEFINED, 1f), GridLayout.spec(GridLayout.UNDEFINED, 1f));
                textView.setLayoutParams(params);
                textView.setTextColor(getResources().getColor(R.color.blue));
                textView.setGravity(Gravity.CENTER);
                CalligraphyUtils.applyFontToTextView(this, textView, "fonts/NanumPen.ttf");
                mLayoutShowKeyword.addView(textView);
                mTextViews.add(textView);
            }
        }

    }

    private void setLayoutShowKeyword(String startKeyword) {

        if(mForEffectTask != null) {
            mForEffectTask.setIsCancelled(true);
        }

        mForEffectTask = new ForEffectTask();
        mForEffectTask.execute(startKeyword);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initLayoutShowKeyword();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mForEffectTask != null) {
            mForEffectTask.cancel(true);
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

        if (id == R.id.action_search) {

            if (mTitleIsShown) {

                hideTitle();
                mTitleIsShown = false;
                mKeywordInputEditText.requestFocus();

            } else {

                if (mKeywordInputEditText.getText().length() == 0) {
                    Toast.makeText(this, getString(R.string.no_keyword), Toast.LENGTH_SHORT).show();
                } else {
                    String keywordName = mKeywordInputEditText.getText().toString();
                    View view = this.getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    setLayoutShowKeyword(keywordName);
                }

            }

        } else if (id == R.id.generate_data) {
            new TestTask().execute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                MyLog.print("Permission is granted");
                return true;
            } else {
                MyLog.print("Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            //permission is automatically granted on sdk<23 upon installation
            MyLog.print("Permission is granted");
            return true;
        }
    }

    public void showTitle() {
        mKeywordInputEditText.setVisibility(View.INVISIBLE);
        mTitle.setVisibility(View.VISIBLE);
    }

    public void hideTitle() {
        mTitle.setVisibility(View.INVISIBLE);
        mKeywordInputEditText.setVisibility(View.VISIBLE);
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
            showResult();
            String startKeyword = KeywordObserver.get().getKeywordNameThatHasMaxCount();
            setLayoutShowKeyword(startKeyword);
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

    private class ForEffectTask extends AsyncTask<String, Integer, Void> {

        List<Integer> mNumbers;
        List<Pair<String, Integer>> mKeywordList;
        Pair<Integer, Integer> mMinMaxCount;
        Boolean mIsCancelled = false;

        @Override
        protected void onPreExecute() {
            for(TextView textView : mTextViews) {
                textView.setText("");
            }
            mNumbers = KeywordUtil.getRandomNumbers();
        }

        @Override
        protected Void doInBackground(String... params) {

            String startKeyword = params[0];

            KeywordManager keywordManager = KeywordManager.get();

            mKeywordList = keywordManager.getKeywordByBFS(startKeyword);

            if (mKeywordList == null) {
                return null;
            }

            mMinMaxCount = keywordManager.getMinMaxCount(mKeywordList);

            try {
                for (int i = 0; i < mKeywordList.size(); i++) {
                    if(mIsCancelled || isCancelled()) {
                        break;
                    }

                    publishProgress(i);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (!mIsCancelled) {
                int i = values[0];
                int idx = mNumbers.get(i);
                TextView textView = mTextViews.get(KeywordUtil.getOrderFromCount(idx));
                textView.setText("#" + mKeywordList.get(i).first);
                float textSize = KeywordUtil.getTextSize(mKeywordList.get(i).second, mMinMaxCount);
                textView.setTextSize(textSize);

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView textView = (TextView) v;
                        if (textView.getText().length() != 0) {
                            String keywordName = KeywordUtil.removeTag(textView.getText().toString());
                            Intent intent = TTListActivity.newIntent(getApplicationContext(), keywordName);
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        public void setIsCancelled(boolean flag) {
            mIsCancelled = flag;
        }

    }

}
