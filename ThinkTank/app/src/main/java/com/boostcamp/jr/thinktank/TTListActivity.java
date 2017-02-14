package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.model.ThinkItem;
import com.boostcamp.jr.thinktank.model.ThinkObserver;

import org.lucasr.dspec.DesignSpec;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class TTListActivity extends MyActivity {

    private TTAdapter mAdapter;

    @BindView(R.id.think_list_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_list);
        ButterKnife.bind(this);

        setRecyclerView();

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setSwipeEvent();

        /* for text */
        OrderedRealmCollection<KeywordItem> list = KeywordObserver.get().selectAll();
        Log.d("TTListActivity", "onCreate() : " + list.size());
        for(KeywordItem item : list) {
            Log.d("TTListActivity", item.getName() + ", " + item.getCount());
        }
    }

    private void setRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new TTAdapter(this, ThinkObserver.get().selectAll());
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
                ThinkItem swipedItem = mAdapter.getData().get(position);
                ThinkObserver.get().delete(swipedItem);
            }
        }).attachToRecyclerView(mRecyclerView);
    }

    public class TTHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private ThinkItem mThinkItem;

        @BindView(R.id.list_item_keywords)
        TextView mKeywords;

        @BindView(R.id.list_item_content)
        TextView mContent;

        @BindView(R.id.list_item_date)
        TextView mDate;

        @BindView(R.id.list_item_background)
        View mBackground;

        public TTHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);

            DesignSpec background = DesignSpec.fromResource(mBackground, R.raw.list_item_background);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBackground.getOverlay().add(background);
            } else {
                mBackground.setBackground(background);
            }
        }

        public void bindThinkItem(ThinkItem item) {
            mThinkItem = item;

            String keywords = "";
            for(KeywordItem keyword : mThinkItem.getKeywords()) {
                keywords += "#" + keyword.getName() + " ";
            }
            mKeywords.setText(keywords);

            mContent.setText(mThinkItem.getContent());
            mDate.setText(DateFormat.format("MMM d EEEE, yyyy", mThinkItem.getDateUpdated()));
        }

        @Override
        public void onClick(View v) {
            Intent intent = TTDetailActivity.newIntent(getApplicationContext(), getAdapterPosition());
            startActivity(intent);
        }
    }

    public class TTAdapter extends RealmRecyclerViewAdapter<ThinkItem, TTHolder> {

        private Context mContext;

        public TTAdapter(Context context, OrderedRealmCollection<ThinkItem> data) {
            super(context, data, true);
            mContext = context;
        }

        @Override
        public TTHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View view = inflater.inflate(R.layout.tt_list_item, parent, false);
            return new TTHolder(view);
        }

        @Override
        public void onBindViewHolder(TTHolder holder, int position) {
            ThinkItem item = getData().get(position);
            holder.bindThinkItem(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tt_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // 검색 기능
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
