package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.github.clans.fab.FloatingActionButton;

import org.lucasr.dspec.DesignSpec;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.RealmList;
import me.drakeet.materialdialog.MaterialDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

// DONE (1) Add/Detail activity 합치기
// DONE (2) 빈 문자열 처리
// DONE (3) Keyword View 수정 - KeywordAddDialog 추가
// DONE (4) keyword 앞에 '#' 중복으로 쓰는 경우 처리
// DONE (5) 삭제하는 경우 KeywordItem count field update - KeywordDeleteDialog 추가
// TODO (8) 키워드 Delete bug 수정(업데이트 시 Count 계속 다운 가능), DeleteDialog 수정
// TODO (10) 키워드 추출 기능 추가 (Retrofit 이용)
// TODO (11) Content 꾸미기 기능 추가 (Spannable)
// TODO (12) 사진 추가 기능 추가

public class TTDetailActivity extends MyActivity {

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

    private MaterialDialog mDialog;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;

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
            showAddTagDialog();
        }
    }

    private void showAddTagDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_keyword, null);

        final EditText keywordEditText = (EditText) v.findViewById(R.id.keyword_edit_text);

        mDialog = new MaterialDialog(this)
                .setView(v)
                .setPositiveButton(R.string.add_dialog_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String keyword = keywordEditText.getText().toString();
                        if (keyword.length() != 0) {
                            keyword = KeywordUtil.removeTag(keyword);
                            getKeywordFromDialog(keyword);
                            mDialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

        mDialog.show();
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

    @OnLongClick(R.id.think_keyword)
    boolean onKeywordViewLongClicked() {
        showDeleteKeywordDialog();
        return true;
    }

    private void showDeleteKeywordDialog() {
        View v = getLayoutInflater().inflate(R.layout.dialog_delete_keyword, null);

        View layoutIfKeywordsExist = v.findViewById(R.id.layout_if_keywords_exist);
        View layoutIfKeywordsNotExist = v.findViewById(R.id.layout_if_keywords_not_exist);

        final List<CheckBox> checkBoxes = new ArrayList<>();

        if (mKeywordStrings.isEmpty()) {
            layoutIfKeywordsExist.setVisibility(View.INVISIBLE);
            layoutIfKeywordsNotExist.setVisibility(View.VISIBLE);
        } else {
            LinearLayout layoutForCheckbox = (LinearLayout) v.findViewById(R.id.layout_for_checkbox);

            for (String keyword : mKeywordStrings) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setPadding(8, 32, 8, 32);
                checkBox.setText(keyword);
                CalligraphyUtils.applyFontToTextView(this, checkBox, "fonts/NanumPen.ttf");
                checkBoxes.add(checkBox);
                layoutForCheckbox.addView(checkBox);
            }
        }

        mDialog = new MaterialDialog(this)
                .setView(v)
                .setNegativeButton(R.string.dialog_cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

        if (!mKeywordStrings.isEmpty()) {
            mDialog.setPositiveButton(R.string.delete_dialog_ok, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onKeywordDeleted(checkBoxes);
                    mDialog.dismiss();
                }
            });
        }

        mDialog.show();
    }

    private void onKeywordDeleted(List<CheckBox> checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                String keywordName = checkBox.getText().toString();

                if (mIsAdded) {
                    KeywordObserver keywordObserver = KeywordObserver.get();
                    try {
                        KeywordItem item = keywordObserver
                                .getCopiedObject(keywordObserver.getKeywordByName(keywordName));
                        item.setCount(item.getCount() - 1);
                        keywordObserver.update(item);
                    } catch (IllegalArgumentException e) {}
                }

                mKeywordStrings.remove(keywordName);
            }
        }
        setKeywordTextView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_detail);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            Log.e(TAG, "No action bar in " + TAG);
            e.printStackTrace();
        }

        DesignSpec background = DesignSpec.fromResource(mLayout, R.raw.background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mLayout.getOverlay().add(background);
        } else {
            mLayout.setBackground(background);
        }

        init();
        setEventListener();
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
        ThinkObserver observer = ThinkObserver.get();
        ThinkItem passedItem = observer.selectAll().get(position);
        mThinkItem = observer.getCopiedObject(passedItem);
        setView();
    }

    private void setView() {
        RealmList<KeywordItem> keywordsInItem = mThinkItem.getKeywords();
        for(KeywordItem keyword : keywordsInItem) {
            mKeywordStrings.add(keyword.getName());
        }
        setKeywordTextView();
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

        ThinkObserver thinkObserver = ThinkObserver.get();
        if (!mIsAdded) {
            thinkObserver.insert(mThinkItem);
        } else if (!mDeleted) {
            thinkObserver.update(mThinkItem);
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

        if (keywordString.equals("")) {
            keywordString = getString(R.string.no_keyword);
        }

        mKeywordTextView.setText(keywordString);
    }

}
