package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.boostcamp.jr.thinktank.model.RandomKeyword;
import com.boostcamp.jr.thinktank.model.RandomKeywordObserver;
import com.boostcamp.jr.thinktank.model.RandomThink;
import com.boostcamp.jr.thinktank.model.RandomThinkObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;

import org.lucasr.dspec.DesignSpec;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

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

    private RTTAdapter mAdapter;

    private Menu mMenu;
    @BindView(R.id.think_list_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.input_keyword_textview)
    TextView mInputKeywordTextView;
    @BindView(R.id.input_keyword_edittext)
    AutoCompleteTextView mInputKeywordEditText;
    private boolean mInputKeywordEditTextIsVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_list);
        ButterKnife.bind(this);

        Date date = (Date) getIntent().getSerializableExtra(EXTRA_DATE);
        if (date != null) {
            setRecyclerView(date);
            String text = DateFormat.format("MMM d일", date).toString();
            setToolbar(text);
        } else {
            String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
            setRecyclerView(keyword);
            setToolbar(keyword);
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setSwipeEvent();

        List<String> items = RandomKeywordObserver.get().getAllKeywordNames();

        mInputKeywordEditText.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                items
        ));

    }

    private void setToolbar(String text) {
        mInputKeywordEditTextIsVisible = false;
        mInputKeywordTextView.setText("#" + text);
        mInputKeywordEditText.setText("");

        mInputKeywordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    MenuItem item = mMenu.findItem(R.id.action_search);
                    onOptionsItemSelected(item);
                    return true;
                }

                return false;
            }
        });
    }

    private void setRecyclerView(String keyword) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        RandomThinkObserver observer = RandomThinkObserver.get();
        if (keyword.equals(getString(R.string.all_think))) {
            mAdapter = new RTTAdapter(this, observer.selectAll());
        } else {
            mAdapter = new RTTAdapter(this, observer.selectThatHasKeyword(keyword));
        }

        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();
    }

    private void setRecyclerView(Date date) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RTTAdapter(this, RandomThinkObserver.get().selectByDate(date));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.requestFocus();
    }

    // swipe event 등록: 이벤트 발생 시 해당 position의 Memo Data 삭제
    private void setSwipeEvent() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT){

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RandomThink swipedItem = mAdapter.getData().get(position);
                RandomThinkObserver.get().delete(swipedItem);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSoftInput();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tt_list, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            if (!mInputKeywordEditTextIsVisible) {
                showInputKeywordEditText();
                mInputKeywordEditText.requestFocus();
                showSoftInput();
            } else if (mInputKeywordEditText.getText().length() != 0) {
                String keyword = mInputKeywordEditText.getText().toString();
                keyword = KeywordUtil.removeTag(keyword);
                hideSoftInput();
                showInputKeywordTextView(keyword);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showInputKeywordEditText() {
        mInputKeywordTextView.setVisibility(View.INVISIBLE);
        mInputKeywordEditText.setVisibility(View.VISIBLE);
        mInputKeywordEditTextIsVisible = true;
    }

    private void showInputKeywordTextView(String keyword) {
        setToolbar(keyword);

        RandomThinkObserver observer = RandomThinkObserver.get();
        if (keyword.equals(getString(R.string.all_think))) {
            mAdapter.updateData(observer.selectAll());
        } else {
            mAdapter.updateData(observer.selectThatHasKeyword(keyword));
        }

        mInputKeywordTextView.setVisibility(View.VISIBLE);
        mInputKeywordEditText.setVisibility(View.INVISIBLE);
        mRecyclerView.requestFocus();
        mInputKeywordEditTextIsVisible = false;
    }

    private void showSoftInput() {
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void hideSoftInput() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public class RTTHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private RandomThink mRandomThink;

        @BindView(R.id.list_item_keywords)
        TextView mKeywords;
        @BindView(R.id.list_item_content)
        TextView mContent;
        @BindView(R.id.list_item_date)
        TextView mDate;
        @BindView(R.id.list_item_background)
        View mBackground;

        public RTTHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            DesignSpec background = DesignSpec.fromResource(mBackground, R.raw.list_item_background);
            mBackground.setBackground(background);
        }

        public void bindRandomThink(RandomThink randomThink) {
            mRandomThink = randomThink;

            String keywords = "";
            for(RandomKeyword keyword : mRandomThink.getKeywords()) {
                keywords += "#" + keyword.getName() + " ";
            }
            mKeywords.setText(keywords);

            mContent.setText(mRandomThink.getContent());
            mDate.setText(DateFormat.format("MMM d EEEE, yyyy", mRandomThink.getCreatedDate()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = TTRandomMainActivity.newIntent(getApplicationContext(), mRandomThink.getId());
            startActivity(intent);
        }
    }

    public class RTTAdapter extends RealmRecyclerViewAdapter<RandomThink, RTTHolder> {

        Context mContext;

        public RTTAdapter(@NonNull Context context, @Nullable OrderedRealmCollection<RandomThink> data) {
            super(context, data, true);
            mContext = context;
        }

        @Override
        public RTTHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.tt_list_item, parent, false);
            return new RTTHolder(view);
        }

        @Override
        public void onBindViewHolder(RTTHolder holder, int position) {
            RandomThink randomThink = getData().get(position);
            holder.bindRandomThink(randomThink);
        }
    }

}
