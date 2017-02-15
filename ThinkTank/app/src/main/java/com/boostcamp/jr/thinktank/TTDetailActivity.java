package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pair;
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
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.realm.RealmList;
import me.drakeet.materialdialog.MaterialDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

// DONE (1) 키워드 Delete bug 수정(업데이트 시 Count 계속 다운 가능), DeleteDialog 수정
// DONE (2) ThinkItem Field(Date) 추가
// DONE (3) UPDATE 버그 수정
// TODO (5) 키워드 추출 기능 추가 (Retrofit 이용)
// TODO (6) Content 꾸미기 기능 추가 (Spannable)
// TODO (7) 사진 추가 기능 추가
// TODO (8) keyword 추가/삭제 UX 수정 - Click/LongClick(?)


/**
 *
 * 메모 추가/수정 작업 동시에 처리하게 구현
 * Intent를 통해 넘어온 Extra가 없으면 -> 메모 추가 작업
 * Intent를 통해 넘어온 Extra가 있으면 -> 메모 수정 작업
 *
 */

public class TTDetailActivity extends MyActivity {

    // TTListActivity가 newIntent 메소드를 통해 TTDetailActivity를 부르는 Intent를 얻음
    // EXTRA_POSITION은 Intent Extra의 Key 값
    public static final String EXTRA_POSITION = "com.boostcamp.jr.thinktank.position";

    public static Intent newIntent(Context packageContext, int position) {
        Intent intent = new Intent(packageContext, TTDetailActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    // Log 찍기 위해 정의한 상수
    private static final String TAG = "TTDetailActivity";

    // Fields...
    private ThinkItem mThinkItem;
    private boolean mDeleted;
    private boolean mIsAdded;
    private List<Pair<String, Boolean>> mKeywordStrings = new ArrayList<>();

    // MaterialDialog library 사용을 위해 정의한 객체
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

    /**
     * 태그를 추가하기 위한 대화상자를 띄움.
     * 대화 상자에서 확인 버튼을 눌렀을 때, Empty String이 입력되지 않았으면 getKeywordFromDialog() 호출
     */

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

            for (Pair<String, Boolean> pair : mKeywordStrings) {
                String keyword = pair.first;
                CheckBox checkBox = new CheckBox(this);
                checkBox.setPadding(8, 32, 8, 32);
                checkBox.setText("#" + keyword);
                checkBox.setTextColor(getResources().getColor(R.color.blue));
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
        for (int i=0; i<checkBoxes.size(); i++) {
            CheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isChecked()) {
                String keywordName = checkBox.getText().toString();
                keywordName = KeywordUtil.removeTag(keywordName);

                if (mKeywordStrings.get(i).second) {
                    KeywordObserver keywordObserver = KeywordObserver.get();
                    KeywordItem item = keywordObserver
                            .getCopiedObject(keywordObserver.getKeywordByName(keywordName));
                    item.setCount(item.getCount() - 1);
                    keywordObserver.update(item);
                }
            }
        }
        for (int i=checkBoxes.size()-1; i>=0; i--) {
            CheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isChecked()) {
                mKeywordStrings.remove(i);
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

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            String keywordName = keyword.getName();
            Pair<String, Boolean> temp = new Pair<>(keywordName, true);
            mKeywordStrings.add(temp);
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
        for (Pair<String, Boolean> keyword : mKeywordStrings) {
            if (!keyword.second) {
                KeywordManager.get().createOrUpdateKeyword(keyword.first);
            }
            keywords.add(KeywordObserver.get().getKeywordByName(keyword.first));
        }
        mThinkItem.setKeywords(keywords);

        ThinkObserver thinkObserver = ThinkObserver.get();
        if (!mIsAdded) {
            thinkObserver.insert(mThinkItem);
        } else if (!mDeleted) {
            mThinkItem.setDateUpdated(new Date());
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

    /**
     * "#" 제거 후 String이 비어있지 않으면 mKeywordStrings List에 추가 후,
     * textView reset을 위해 setKeywordTextView() 호출
     */

    public void getKeywordFromDialog(String keyword) {
        if (keyword.length() != 0) {
            mKeywordStrings.add(new Pair<>(keyword, false));
            setKeywordTextView();
        }
    }

    /**
     * 멤버 변수인 mKeywordStrings가 keyword로 추가된 String을 유지하고 있고,
     * "#" 추가 후 textView에 set!
     * (키워드가 없으면 안내 문자열로 set! - R.string.no_keyword)
     */

    private void setKeywordTextView() {
        String keywordString = "";
        for (Pair<String, Boolean> keyword : mKeywordStrings) {
            keywordString += "#" + keyword.first + " ";
        }

        if (keywordString.equals("")) {
            keywordString = getString(R.string.no_keyword);
        }

        mKeywordTextView.setText(keywordString);
    }

}
