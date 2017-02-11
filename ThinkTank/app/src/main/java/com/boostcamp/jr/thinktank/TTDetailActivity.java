package com.boostcamp.jr.thinktank;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;

import org.lucasr.dspec.DesignSpec;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.RealmList;

public class TTDetailActivity extends AppCompatActivity {

    private static final String TAG = "TTDetailActivity";

    private ThinkItem mThinkItem;
    private boolean mDeleted;
    private List<String> mKeywordsInItem = new ArrayList<>();

    @BindView(R.id.activity_tt_detail)
    View mLayout;

    @BindViews({R.id.keyword1, R.id.keyword2, R.id.keyword3})
    List<EditText> mKeywords;

    @BindView(R.id.think_content)
    EditText mContent;

    @OnClick(R.id.image_button)
    void onImageButtonClicked() {

    }

    @OnClick(R.id.share_button)
    void onShareButtonClicked() {

    }

    @OnClick(R.id.delete_button)
    void onDeleteButtonClicked() {
        Log.d("onDelete()", "" + mThinkItem.getId());
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

        getThinkItem();
        setView();

        setEventListener();
    }

    private void getThinkItem() {
        mDeleted = false;
        int position = getIntent().getIntExtra("position", -1);
        ThinkItem passedItem = ThinkObserver.get().selectAll().get(position);
        mThinkItem = ThinkObserver.get().getCopiedObject(passedItem);
        RealmList<KeywordItem> keywordsList = mThinkItem.getKeywords();
        for(int i=0; i<keywordsList.size(); i++) {
            String temp = "#" + keywordsList.get(i).getName();
            mKeywordsInItem.add(temp);
        }
    }

    private void setView() {
        RealmList<KeywordItem> keywords = mThinkItem.getKeywords();
        for(int i=0; i<keywords.size(); i++) {
            String temp = "#" + keywords.get(i).getName();
            mKeywords.get(i).setText(temp);
        }

        mContent.setText(mThinkItem.getContent());
    }

    private void setEventListener() {

        for(int i=0; i<mKeywords.size(); i++) {
            mKeywords.get(i).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keywords = "";
                    for(int j=0; j<mKeywords.size(); j++) {
                        String keyword = mKeywords.get(j).getText().toString();
                        if (keyword.length() != 0) {
                            if(keyword.charAt(0) != '#') {
                                keywords += "#" + keyword + " ";
                                mKeywordsInItem.add(keywords);
                            }
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        mContent.addTextChangedListener(new TextWatcher() {
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
    protected void onPause() {
        super.onPause();
        if (!mDeleted) {
            RealmList<KeywordItem> keywords = new RealmList<>();
            for(String keyword : mKeywordsInItem) {
                KeywordManager.get().createOrUpdateKeyword(keyword);
                keywords.add(KeywordObserver.get().getKeywordByName(keyword));
            }
            mThinkItem.setKeywords(keywords);
            ThinkObserver.get().update(mThinkItem);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
