package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;
import com.github.clans.fab.FloatingActionButton;

import org.lucasr.dspec.DesignSpec;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;

// DONE (1) Add/Detail activity 합치기
// DONE (2) 빈 문자열 처리
// DONE (3) Keyword View 수정 - KeywordAddDialog 추가
// DONE (4) keyword 앞에 '#' 중복으로 쓰는 경우 처리
// TODO (5) 삭제하는 경우 KeywordItem count field update
// TODO (9) 키워드 추출 기능 추가 (Retrofit 이용)
// TODO (10) Content 꾸미기 기능 추가 (Spannable)
// TODO (11) 사진 추가 기능 추가

public class TTDetailActivity extends AppCompatActivity {

    private static final String DIALOG_KEYWORD = "keyword";

    public static final String EXTRA_POSITION = "com.boostcamp.jr.thinktank.position";

    public static Intent newIntent(Context packageContext, int position) {
        Intent intent = new Intent(packageContext, TTDetailActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    private static final String TAG = "TTDetailActivity";

    private ThinkItem mThinkItem;
    private boolean mDeleted;
    private boolean mIsAdded;
    private List<String> mKeywordStrings = new ArrayList<>();

    @BindView(R.id.activity_tt_detail)
    View mLayout;

    @BindView(R.id.think_keyword)
    TextView mKeywordTextView;

    @BindView(R.id.think_content)
    EditText mContentEditText;

    @BindView(R.id.share_button)
    FloatingActionButton mShareButton;

    @BindView(R.id.delete_button)
    FloatingActionButton mDeleteButton;

    @BindView(R.id.add_tag_button)
    ImageButton mAddTagButton;

    @OnClick(R.id.add_tag_button)
    void onAddTagButtonClicked() {
        if (mKeywordStrings.size() == 3) {
            Toast.makeText(this, R.string.cannot_add_keyword, Toast.LENGTH_SHORT).show();
        } else {
            FragmentManager manager = getSupportFragmentManager();
            AddKeywordDialog dialog = new AddKeywordDialog();
            dialog.show(manager, DIALOG_KEYWORD);
        }
    }

    @OnClick(R.id.image_button)
    void onImageButtonClicked() {

    }

    @OnClick(R.id.share_button)
    void onShareButtonClicked() {

    }

    @OnClick(R.id.delete_button)
    void onDeleteButtonClicked() {
        // Log.d("onDelete()", "" + mThinkItem.getId());
        ThinkObserver.get().delete(mThinkItem);
        mDeleted = true;
        finish();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_detail);
        ButterKnife.bind(this);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "No action bar in " + TAG);
            e.printStackTrace();
        }

        DesignSpec background = DesignSpec.fromResource(mLayout, R.raw.background);
        mLayout.getOverlay().add(background);

        init();
        setEventListener();
    }

    @Override
    protected void onPause() {
        super.onPause();

        String content = mThinkItem.getContent();
        if (content == null || content.length() == 0) {
            return;
        }

        RealmList<KeywordItem> keywords = new RealmList<>();
        for (String keyword : mKeywordStrings) {
            KeywordManager.get().createOrUpdateKeyword(keyword);
            keywords.add(KeywordObserver.get().getKeywordByName(keyword));
        }
        mThinkItem.setKeywords(keywords);

        if (!mIsAdded) {
            ThinkObserver.get().insert(mThinkItem);
        } else if (!mDeleted) {
            ThinkObserver.get().update(mThinkItem);
        }
    }

    private void init() {
        mDeleted = false;
        int position = getIntent().getIntExtra(EXTRA_POSITION, -1);

        if (position == -1) {
            setIfItemNotAdded();
        } else {
            setIfItemAdded(position);
        }
    }

    private void setIfItemNotAdded() {
        mIsAdded = false;
        mThinkItem = new ThinkItem();
        mShareButton.setEnabled(false);
        mDeleteButton.setEnabled(false);
    }

    private void setIfItemAdded(int position) {
        mIsAdded = true;
        ThinkItem passedItem = ThinkObserver.get().selectAll().get(position);
        mThinkItem = ThinkObserver.get().getCopiedObject(passedItem);
        setView();
    }

    private void setView() {
        RealmList<KeywordItem> keywordsInItem = mThinkItem.getKeywords();
        String keywordString = "";
        for(KeywordItem keyword : keywordsInItem) {
            String name = keyword.getName();
            mKeywordStrings.add(name);
            keywordString += "#" + name + " ";
        }
        mKeywordTextView.setText(keywordString);
        mContentEditText.setText(mThinkItem.getContent());
    }

    private void setEventListener() {

        mContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mThinkItem.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void getKeywordFromDialog(String keyword) {
        if (keyword.length() != 0) {
            mKeywordStrings.add(keyword);
            setKeywordTextView();
        }
    }

    private void setKeywordTextView() {
        String keywordString = "";
        for (String string : mKeywordStrings) {
            keywordString += "#" + string + " ";
        }
        mKeywordTextView.setText(keywordString);
    }
}
