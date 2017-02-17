package com.boostcamp.jr.thinktank.image;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.ImageView;

import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.utils.MyLog;

import java.io.File;
import java.util.List;

import butterknife.BindView;

/**
 * Created by jr on 2017-02-18.
 */

public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private static ImagePagerAdapter sImagePagerAdapter;

    private Context mContext;
    private List<File> mFiles;

    @BindView(R.id.pager_image_view)
    ImageView mImageView;

    public ImagePagerAdapter(FragmentManager manager) {
        super(manager);
        mFiles = ImageRepository.get().getFiles();
    }

    @Override
    public Fragment getItem(int position) {
        MyLog.print("position: " + position);
        return ImageFragment.create(position);
    }

    @Override
    public int getCount() {
        return (mFiles == null) ? 0 : mFiles.size();
    }
}
