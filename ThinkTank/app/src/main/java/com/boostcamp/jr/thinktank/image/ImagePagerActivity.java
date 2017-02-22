package com.boostcamp.jr.thinktank.image;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.boostcamp.jr.thinktank.MyActivity;
import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.utils.MyLog;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ImagePagerActivity extends MyActivity {

    private static final String EXTRA_IMAGE_POSITION = "position";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image_pager)
    ViewPager mImagePager;

    ImagePagerAdapter mAdapter;

    public static Intent newIntent(Context context, int position) {
        Intent intent = new Intent(context, ImagePagerActivity.class);
        intent.putExtra(EXTRA_IMAGE_POSITION, position);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            MyLog.print("No action bar in ImagePager");
            e.printStackTrace();
        }

        mAdapter = new ImagePagerAdapter(getSupportFragmentManager());
        mImagePager.setAdapter(mAdapter);

        int position = getIntent().getIntExtra(EXTRA_IMAGE_POSITION, 0);
        mImagePager.setCurrentItem(position, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ImageRepository.get().setFiles(null);
        mAdapter.swapFiles();
    }
}
