package com.boostcamp.jr.thinktank.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.utils.PhotoUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jr on 2017-02-18.
 */

public class ImageFragment extends Fragment {

    private static final String ARGS_IMAGE_POSITION = "position";

    @BindView(R.id.pager_image_view)
    ImageView mImageView;

    private File mFile;

    public static ImageFragment create(int position) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_IMAGE_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int position = getArguments().getInt(ARGS_IMAGE_POSITION);
        mFile = ImageRepository.get().getFiles().get(position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.image_pager_item, container, false);
        ButterKnife.bind(this, rootView);

        ViewTreeObserver observer = mImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updateImageView(mImageView.getWidth(), mImageView.getHeight());
            }
        });

        return rootView;
    }

    private void updateImageView(int destWidth, int destHeight) {
        if (mFile == null || !mFile.exists()) {
            mImageView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PhotoUtil.getScaledBitmap(
                    mFile.getPath(), destWidth, destHeight
            );
            mImageView.setImageBitmap(bitmap);
        }
    }

}
