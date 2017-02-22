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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boostcamp.jr.thinktank.manager.KeywordManager;
import com.boostcamp.jr.thinktank.model.KeywordItem;
import com.boostcamp.jr.thinktank.model.KeywordObserver;
import com.boostcamp.jr.thinktank.utils.KeywordUtil;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.boostcamp.jr.thinktank.utils.PhotoUtil;
import com.boostcamp.jr.thinktank.utils.TestUtil;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import org.lucasr.dspec.DesignSpec;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

public class TTMainActivity extends MyActivity
        implements OnMenuItemClickListener  {

    public static long backKeyTime = 0;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.toolbar_title)
    TextView mTitle;
    @BindView(R.id.layout_show_keyword)
    GridLayout mLayoutShowKeyword;
    @BindView(R.id.layout_progress_bar)
    View mLayoutProgressBar;
    @BindView(R.id.layout_for_all_keyword)
    FrameLayout mLayoutForAllKeyword;
    @BindView(R.id.input_keyword_edittext)
    AutoCompleteTextView mKeywordInputEditText;
    @BindView(R.id.all_keyword_recycler_view)
    RecyclerView mAllKeywordRecyclerView;
    @BindView(R.id.layout_for_calendar)
    LinearLayout mLayoutForCalendar;
    @BindView(R.id.month_textview)
    TextView mMonthTextView;
    @BindView(R.id.calendar_view)
    CompactCalendarView mCalendar;

    private Menu mMenu;

    private ForEffectTask mForEffectTask;
    private List<TextView> mTextViews = new ArrayList<>();
    private boolean mTitleIsShown;

    private ContextMenuDialogFragment mMenuDialogFragment;

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

        setMenuItems();

        setCalendar();

        DesignSpec background1 = DesignSpec.fromResource(mLayoutShowKeyword, R.raw.background);
        mLayoutShowKeyword.setBackground(background1);

        DesignSpec background2 = DesignSpec.fromResource(mLayoutForAllKeyword, R.raw.background);
        mLayoutForAllKeyword.setBackground(background2);

        DesignSpec background3 = DesignSpec.fromResource(mLayoutForCalendar, R.raw.background);
        mLayoutForCalendar.setBackground(background3);

//        new TestTask().execute();
    }

    private void setMainAutoComplete() {
        List<String> items = KeywordObserver.get().getAllKeywordNames();
        items.add(getString(R.string.all_think));
        items.add(getString(R.string.all_keyword));

        mKeywordInputEditText.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                items
        ));

        mKeywordInputEditText.setOnKeyListener(new View.OnKeyListener() {
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
                textView.setGravity(Gravity.CENTER);
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);
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

    private void setCalendar() {
        mMonthTextView.setText((new Date().getMonth() + 1) + "월");

        mCalendar.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                MyLog.print("Day was clicked: " + dateClicked);
                Intent intent = TTListActivity.newIntent(getApplicationContext(), dateClicked);
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
    protected void onResume() {
        super.onResume();
        showTitle();
        initLayoutShowKeyword();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mForEffectTask != null) {
            mForEffectTask.cancel(true);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tt_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {

            if (mTitleIsShown) {

                hideTitle();
                mKeywordInputEditText.requestFocus();
                showSoftInput();

            } else {
                String keywordName = mKeywordInputEditText.getText().toString();
                keywordName = KeywordUtil.removeTag(keywordName);

                if (keywordName.length() == 0) {
                    StyleableToast.makeText(this,
                            getString(R.string.no_keyword), Toast.LENGTH_SHORT, R.style.StyledToast).show();
                } else if (keywordName.equals(getString(R.string.all_keyword))) {
                    onAllKeywordSelected();
                } else if (keywordName.equals(getString(R.string.all_think))) {
                    onAllThinkSelected();
                } else {
                    hideSoftInput();
                    setLayoutShowKeyword(keywordName);
                    mKeywordInputEditText.setText("#" + keywordName);
                }

            }

            return true;

        } else if (id == R.id.action_more) {
            mMenuDialogFragment.show(getSupportFragmentManager(), "ContextMenuDialogFragment");
            return true;
        }

//        else if (id == R.id.action_all_keyword) {
//
//            new ShowAllKeywordTask().execute();
//            hideSoftInput();
//
//        } else if (id == R.id.action_all_think) {
//
//            hideSoftInput();
//            Intent intent = TTListActivity
//                    .newIntent(getApplicationContext(), getString(R.string.all_think));
//            startActivity(intent);
//        }
//         else if (id == R.id.generate_data) {
//            new TestTask().execute();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                MyLog.print("Write Permission is granted");
                return true;
            } else {
                MyLog.print("Write Permission is revoked");
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

    private void showProgressBar() {
        mLayoutShowKeyword.setVisibility(View.INVISIBLE);
        mLayoutForAllKeyword.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.INVISIBLE);
        mLayoutProgressBar.setVisibility(View.VISIBLE);
    }

    private void showResult() {
        mLayoutProgressBar.setVisibility(View.INVISIBLE);
        mLayoutForAllKeyword.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.INVISIBLE);
        mLayoutShowKeyword.setVisibility(View.VISIBLE);
    }

    private void showRecyclerView() {
        mLayoutProgressBar.setVisibility(View.INVISIBLE);
        mLayoutShowKeyword.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.INVISIBLE);
        mLayoutForAllKeyword.setVisibility(View.VISIBLE);
    }

    private void showCalendarView() {
        mLayoutProgressBar.setVisibility(View.INVISIBLE);
        mLayoutShowKeyword.setVisibility(View.INVISIBLE);
        mLayoutForAllKeyword.setVisibility(View.INVISIBLE);
        mLayoutForCalendar.setVisibility(View.VISIBLE);
    }

    public void showTitle() {
        mKeywordInputEditText.setVisibility(View.INVISIBLE);
        mTitle.setVisibility(View.VISIBLE);
        mTitleIsShown = true;
        mKeywordInputEditText.setText("");
    }

    public void hideTitle() {
        mTitleIsShown = false;
        mTitle.setVisibility(View.INVISIBLE);
        mKeywordInputEditText.setVisibility(View.VISIBLE);
        Animation leftToRight = AnimationUtils.loadAnimation(this, R.anim.left_to_right);
        mKeywordInputEditText.startAnimation(leftToRight);
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

    }

    private class ForEffectTask extends AsyncTask<String, Integer, Void> {

        List<Integer> mNumbers;
        List<Pair<String, Integer>> mKeywordList;
        Pair<Integer, Integer> mMinMaxCount;
        Boolean mIsCancelled = false;

        @Override
        protected void onPreExecute() {
            showResult();
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
                    Thread.sleep(120);
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

                final Animation in = new AlphaAnimation(0.0f, 1.0f);
                in.setDuration(1000);

                textView.setText("#" + mKeywordList.get(i).first);
                textView.startAnimation(in);

                float textSize = KeywordUtil.getTextSize(mKeywordList.get(i).second, mMinMaxCount);
                textView.setTextSize(textSize);

                if (i == 0) {
                    textView.setTextColor(getColor(R.color.red));
                } else {
                    if (textSize > 25 && textSize <= 30) {
                        textView.setTextColor(getColor(R.color.blue1));
                    } else if (textSize >= 20 && textSize <= 25) {
                        textView.setTextColor(getColor(R.color.blue2));
                    } else {
                        textView.setTextColor(getColor(R.color.blue3));
                    }
                }

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

                textView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        TextView textView = (TextView) v;
                        if (textView.getText().length() != 0) {
                            hideTitle();
                            mKeywordInputEditText.setText(textView.getText());
                            MenuItem item = mMenu.findItem(R.id.action_search);
                            onOptionsItemSelected(item);
                        }
                        return true;
                    }
                });
            }
        }

        public void setIsCancelled(boolean flag) {
            mIsCancelled = flag;
        }

    }

    private class ShowAllKeywordTask extends AsyncTask<Void, Void, List<String>> {

        private KeywordAdapter mKeywordAdapter;

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            List<String> ret = new ArrayList<>();

            OrderedRealmCollection<KeywordItem> keywordList = KeywordObserver.get().selectAllOrderByName();

            for (KeywordItem keyword : keywordList) {
                ret.add(keyword.getName());
            }

            return ret;
        }

        @Override
        protected void onPostExecute(List<String> keywords) {
            showRecyclerView();
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
            Intent intent = TTListActivity.newIntent(getApplicationContext(), keywordName);
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

    @Override
    public void onBackPressed() {

        Toast toast;

        if (System.currentTimeMillis() > backKeyTime + 2000) {
            backKeyTime = System.currentTimeMillis();
            StyleableToast.makeText(this,
                    getString(R.string.wanna_exit), Toast.LENGTH_SHORT, R.style.StyledToast).show();
            return;
        }

        if (System.currentTimeMillis() <= backKeyTime + 2000) {
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    private List<MenuObject> getMenuObjects() {
        MenuObject close = new MenuObject();
        close.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_cancel));

        MenuObject allThink = new MenuObject(getString(R.string.all_think));
        allThink.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_all_think));

        MenuObject allKeyword = new MenuObject(getString(R.string.all_keyword));
        allKeyword.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_all_keyword));

        MenuObject findByDate = new MenuObject(getString(R.string.find_by_date));
        findByDate.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_calendar));

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(close);
        menuObjects.add(allThink);
        menuObjects.add(allKeyword);
        menuObjects.add(findByDate);

        for (MenuObject menuObject : menuObjects) {
            menuObject.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            menuObject.setBgColor(getResources().getColor(R.color.colorPrimary));
            menuObject.setDividerColor(R.color.white);
        }

        return menuObjects;
    }

    private void setMenuItems() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.action_bar_size));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(true);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        mMenuDialogFragment.dismiss();

        switch (position) {
            case 0:
                break;
            case 1:
                onAllThinkSelected();
                break;
            case 2:
                onAllKeywordSelected();
                break;
            case 3:
                showCalendarView();
                break;
        }
    }

    private void onAllThinkSelected() {
        hideSoftInput();
        Intent intent = TTListActivity
                .newIntent(getApplicationContext(), getString(R.string.all_think));
        startActivity(intent);
    }

    private void onAllKeywordSelected() {
        new ShowAllKeywordTask().execute();
        hideSoftInput();
    }

}
