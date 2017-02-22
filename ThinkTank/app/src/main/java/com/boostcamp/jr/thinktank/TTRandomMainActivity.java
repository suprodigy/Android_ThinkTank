package com.boostcamp.jr.thinktank;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import me.drakeet.materialdialog.MaterialDialog;

public class TTRandomMainActivity extends MyActivity
        implements OnMenuItemClickListener {

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
    @BindViews({R.id.think_keyword1, R.id.think_keyword2, R.id.think_keyword3})
    List<TextView> mKeywordTextViews;

    private ContextMenuDialogFragment mMenuDialogFragment;
    private MaterialDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tt_random_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        setMenuItem();
        setCalendar();

        DesignSpec background = DesignSpec.fromResource(mLayout, R.raw.background);
        mLayout.setBackground(background);
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

        MenuObject generateRandoms = new MenuObject(getString(R.string.generate_randoms));
        generateRandoms.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_generate_random));

        MenuObject save = new MenuObject(getString(R.string.save));
        save.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_save));

        MenuObject select = new MenuObject(getString(R.string.select));
        select.setDrawable(PhotoUtil.getResizedBitmapDrawable(this, R.drawable.ic_load));

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(close);
        menuObjects.add(generateRandoms);
        menuObjects.add(save);
        menuObjects.add(select);

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

        switch (position) {
            case 0:
                break;
            case 1:
                new GenerateRandomsTask().execute();
                break;
            case 2:
                onSaveClicked();
                break;
            case 3:
                onSelectClicked();
                break;
        }
    }

    private void onSaveClicked() {

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

    }

    private void onAllThinkClicked() {

    }

    private void onSelectByDateClicked() {

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
            mMenuDialogFragment.show(getSupportFragmentManager(), "ContextMenuDialogFragment");
            return true;
        } else if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
