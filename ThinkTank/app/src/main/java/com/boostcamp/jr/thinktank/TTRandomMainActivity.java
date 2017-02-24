package com.boostcamp.jr.thinktank;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.model.RandomKeyword;
import com.boostcamp.jr.thinktank.model.RandomKeywordObserver;
import com.boostcamp.jr.thinktank.model.RandomThink;
import com.boostcamp.jr.thinktank.model.RandomThinkObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.boostcamp.jr.thinktank.utils.PhotoUtil;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import org.lucasr.dspec.DesignSpec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmList;
import me.drakeet.materialdialog.MaterialDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

public class TTRandomMainActivity extends MyActivity
        implements OnMenuItemClickListener {

    private static String EXTRA_ID = "id";

    public static Intent newIntent(Context packageContext, String id) {
        Intent intent = new Intent(packageContext, TTRandomMainActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @BindView(R.id.layout_for_input_think)
    View mLayoutForInputThink;
    @BindView(R.id.layout_for_all_keyword)
    View mLayoutForAllKeyword;
    @BindView(R.id.layout_for_calendar)
    View mLayoutForCalendar;
    @BindView(R.id.layout_for_grid)
    View mLayout;
    @BindView(R.id.month_textview)
    TextView mMonthTextView;
    @BindView(R.id.calendar_view)
    CompactCalendarView mCalendar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.think_content)
    EditText mThinkContentEditText;
    @BindViews({R.id.think_keyword1, R.id.think_keyword2, R.id.think_keyword3})
    List<TextView> mKeywordTextViews;
    @BindView(R.id.all_keyword_recycler_view)
    RecyclerView mAllKeywordRecyclerView;

    private ContextMenuDialogFragment mMenuDialogFragment;
    private MaterialDialog mDialog;
    private boolean mDeleted;
    private boolean mIsAdded;

    private RandomThink mRandomThink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_random_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setCalendar();

        DesignSpec background1 = DesignSpec.fromResource(mLayout, R.raw.background);
        mLayout.setBackground(background1);

        DesignSpec background2 = DesignSpec.fromResource(mLayoutForAllKeyword, R.raw.background);
        mLayoutForAllKeyword.setBackground(background2);

        DesignSpec background3 = DesignSpec.fromResource(mLayoutForCalendar, R.raw.background);
        mLayoutForCalendar.setBackground(background3);

        mLayout.requestFocus();

        setEventListener();
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setMenuItem();
    }

    private void init() {
        mDeleted = false;
        String id = getIntent().getStringExtra(EXTRA_ID);

        if (id == null) {
            setIfItemNotAdded();
        } else {
            setIfItemAdded(id);
        }
    }

    private void setIfItemNotAdded() {
        mIsAdded = false;
        mRandomThink = new RandomThink();
    }

    private void setIfItemAdded(String id) {
        mIsAdded = true;
        RandomThinkObserver observer = RandomThinkObserver.get();
        RandomThink randomThink = observer.selectItemThatHasId(id);
        mRandomThink = observer.getCopiedObject(randomThink);
        setView();
    }

    private void setView() {
        RealmList<RandomKeyword> keywords = mRandomThink.getKeywords();
        for (int i=0; i<keywords.size(); i++) {
            mKeywordTextViews.get(i).setText("#" + keywords.get(i).getName());
        }
        mThinkContentEditText.setText(mRandomThink.getContent());
    }

    private void setEventListener() {
        mThinkContentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mRandomThink.setContent(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setMenuItem() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.action_bar_size));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(true);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private List<MenuObject> getMenuObjects() {
        MenuObject close = new MenuObject();
        close.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_cancel));

        List<MenuObject> menuObjects = new ArrayList<>();

        if (!mIsAdded) {
            MenuObject generateRandoms = new MenuObject(getString(R.string.generate_randoms));
            generateRandoms.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_generate_random));

            MenuObject save = new MenuObject(getString(R.string.save));
            save.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_save));

            MenuObject select = new MenuObject(getString(R.string.select));
            select.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_load));

            menuObjects.add(close);
            menuObjects.add(generateRandoms);
            menuObjects.add(save);
            menuObjects.add(select);
        } else {
            MenuObject save = new MenuObject(getString(R.string.save));
            save.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_save));

            MenuObject select = new MenuObject(getString(R.string.select));
            select.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_load));

            MenuObject delete = new MenuObject(getString(R.string.delete_menu));
            delete.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_think_delete));

            menuObjects.add(close);
            menuObjects.add(save);
            menuObjects.add(select);
            menuObjects.add(delete);
        }

        for (MenuObject menuObject : menuObjects) {
            menuObject.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            menuObject.setBgColor(getResources().getColor(R.color.colorPrimary));
            menuObject.setDividerColor(R.color.white);
        }

        return menuObjects;
    }


    @Override
    public void onMenuItemClick(View clickedView, int position) {
        mMenuDialogFragment.dismiss();

        if (!mIsAdded) {
            switch (position) {
                case 0:
                    break;
                case 1:
                    showLayoutForInputThink();
                    new GenerateRandomsTask().execute();
                    break;
                case 2:
                    onSaveClicked();
                    break;
                case 3:
                    onSelectClicked();
                    break;
            }
        } else {
            switch (position) {
                case 0:
                    break;
                case 1:
                    onSaveClicked();
                    break;
                case 2:
                    onSelectClicked();
                    break;
                case 3:
                    onDeleteClicked();
                    break;
            }
        }
    }

    private void onSaveClicked() {

        if (mDeleted) {
            return;
        }

        String content = mRandomThink.getContent();
        if (content == null || content.length() == 0 ||
                mKeywordTextViews.get(1).getText().length() == 0) {
            return;
        }

        RandomThinkObserver rtObserver = RandomThinkObserver.get();
        mIsAdded = (rtObserver.selectItemThatHasId(mRandomThink.getId()) != null);

        if (!mIsAdded) {
            RealmList<RandomKeyword> keywords = new RealmList<>();
            for (TextView textView : mKeywordTextViews) {
                String keywordName = textView.getText().toString();
                keywordName = KeywordUtil.removeTag(keywordName);
                RandomKeywordObserver observer = RandomKeywordObserver.get();
                observer.createOrUpdate(keywordName);
                keywords.add(observer.getKeywordByName(keywordName));
            }

            mRandomThink.setKeywords(keywords);

            rtObserver.insert(mRandomThink);
            StyleableToast.makeText(getApplicationContext(), getString(R.string.on_add_memo),
                    Toast.LENGTH_SHORT, R.style.StyledToast).show();

            for (TextView textView : mKeywordTextViews) {
                textView.setText("");
            }
            mKeywordTextViews.get(0).setText(getString(R.string.no_keyword_hint));
            mThinkContentEditText.setText("");
        } else {
            rtObserver.update(mRandomThink);
            StyleableToast.makeText(getApplicationContext(), getString(R.string.on_update_memo),
                    Toast.LENGTH_SHORT, R.style.StyledToast).show();
        }

    }

    private void onSelectClicked() {
        View v = getLayoutInflater().inflate(R.layout.dialog_select_random_think, null);

        Button allKeyword = (Button) v.findViewById(R.id.all_keyword);
        allKeyword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                onAllKeywordClicked();
            }
        });

        Button allThink = (Button) v.findViewById(R.id.all_think);
        allThink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                onAllThinkClicked();
            }
        });

        Button selectByDate = (Button) v.findViewById(R.id.select_by_date);
        selectByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
                onSelectByDateClicked();
            }
        });

        mDialog = new MaterialDialog(this)
                .setView(v)
                .setCanceledOnTouchOutside(true);

        mDialog.show();
    }

    private void onAllKeywordClicked() {
        new ShowAllKeywordTask().execute();
    }

    private void onAllThinkClicked() {
        Intent intent = TTRandomListActivity
                .newIntent(getApplicationContext(), getString(R.string.all_think));
        startActivity(intent);
    }

    private void onSelectByDateClicked() {
        showLayoutForCalendar();
    }

    private void onDeleteClicked() {
        RandomThinkObserver.get().delete(mRandomThink);
        mDeleted = true;
        finish();
    }

    private void setCalendar() {
        mMonthTextView.setText((new Date().getMonth() + 1) + "월");

        mCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                MyLog.print("Day was clicked: " + dateClicked);
                Intent intent = TTRandomListActivity.newIntent(getApplicationContext(), dateClicked);
                startActivity(intent);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                MyLog.print("Month was scrolled to: " + firstDayOfNewMonth);
                mMonthTextView.setText((firstDayOfNewMonth.getMonth() + 1) + "월");
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        onSaveClicked();
        hideSoftInput();
    }

    private class GenerateRandomsTask extends AsyncTask<Void, Integer, Void> {

        List<String> mKeywords = new ArrayList<>();
        Set<String> mKeywordsSet = new HashSet<>();
        String mKeyword;

        @Override
        protected void onPreExecute() {
            StyleableToast.makeText(getApplicationContext(), getString(R.string.generate_randoms_start),
                    Toast.LENGTH_SHORT, R.style.StyledToast).show();

            for (TextView textView: mKeywordTextViews) {
                textView.setText("");
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            AssetManager assetManager = getApplicationContext().getAssets();

            try {
                InputStream is = assetManager.open("hangul.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line;

                while ((line = reader.readLine()) != null) {
                    mKeywords.add(line);
                }

                for (int i=0; i<3; i++) {
                    int idx = (int)(Math.random() * mKeywords.size());

                    mKeyword = mKeywords.get(idx);
                    if (mKeywordsSet.contains(mKeyword)) {
                        i--;
                    } else {
                        mKeywordsSet.add(mKeyword);
                        publishProgress(i);
                        Thread.sleep(500);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int idx = values[0];

            TextView textView = mKeywordTextViews.get(idx);

            final Animation in = new AlphaAnimation(0.0f, 0.1f);
            in.setDuration(500);

            textView.setText("#" + mKeyword);
            textView.startAnimation(in);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showSoftInput();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tt_random_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_more) {
            hideSoftInput();
            mMenuDialogFragment.show(getSupportFragmentManager(), "ContextMenuDialogFragment");
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void showLayoutForInputThink() {
        mLayoutForAllKeyword.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.INVISIBLE);
        mLayoutForInputThink.setVisibility(View.VISIBLE);
    }

    private void showLayoutForAllKeyword() {
        mLayoutForInputThink.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.INVISIBLE);
        mLayoutForAllKeyword.setVisibility(View.VISIBLE);
    }

    private void showLayoutForCalendar() {
        mLayoutForAllKeyword.setVisibility(View.INVISIBLE);
        mLayoutForInputThink.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.VISIBLE);
    }

    private class ShowAllKeywordTask extends AsyncTask<Void, Void, List<String>> {
        private KeywordAdapter mKeywordAdapter;

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> ret = new ArrayList<>();

            OrderedRealmCollection<RandomKeyword> keywordList = RandomKeywordObserver.get().selectAllOrderByName();

            for (RandomKeyword keyword : keywordList) {
                ret.add(keyword.getName());
            }

            return ret;
        }

        @Override
        protected void onPostExecute(List<String> keywords) {
            showLayoutForAllKeyword();
            mAllKeywordRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
            mKeywordAdapter = new KeywordAdapter(getApplicationContext(), keywords);
            mAllKeywordRecyclerView.setAdapter(mKeywordAdapter);
            mAllKeywordRecyclerView.setHasFixedSize(true);
        }
    }

    public class KeywordHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        @BindView(android.R.id.text1)
        TextView mTextView;

        public KeywordHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindKeyword(String keywordName) {
            mTextView.setText("#" + keywordName);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTextSize(26);
            mTextView.setTextColor(getColor(R.color.blue));
            mTextView.setOnClickListener(this);

            CalligraphyUtils.applyFontToTextView(getApplicationContext()
                    , mTextView, "fonts/NanumPen.ttf");
        }

        @Override
        public void onClick(View v) {
            String keywordName = KeywordUtil.removeTag(mTextView.getText().toString());
            Intent intent = TTRandomListActivity.newIntent(getApplicationContext(), keywordName);
            startActivity(intent);
        }

    }

    private class KeywordAdapter extends RecyclerView.Adapter<KeywordHolder> {

        Context mContext;

        List<String> mKeywords;

        public KeywordAdapter(Context context, List<String> keywords) {
            mContext = context;
            mKeywords = keywords;
        }

        @Override
        public KeywordHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext)
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new KeywordHolder(v);
        }

        @Override
        public void onBindViewHolder(KeywordHolder holder, int position) {
            String keywordName = mKeywords.get(position);
            holder.bindKeyword(keywordName);
        }

        @Override
        public int getItemCount() {
            return (mKeywords == null) ? 0 : mKeywords.size();
        }

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

}
