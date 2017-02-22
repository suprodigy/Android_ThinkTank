package com.boostcamp.jr.thinktank.image;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.boostcamp.jr.thinktank.R;
import com.boostcamp.jr.thinktank.utils.MyLog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

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
    private Context mContext;
    private Handler mHandler;

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
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.image_pager_item, container, false);
        ButterKnife.bind(this, rootView);
        updateImageView();

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    private void updateImageView() {
        if (mFile == null || !mFile.exists()) {
            Glide.clear(mImageView);
            mImageView.setImageBitmap(null);
        } else {
            Glide.with(mContext)
                    .load(mFile)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<File, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                            MyLog.print("Image Exception : " + e.toString());
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, File model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(mImageView);
        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                if (mFile == null || !mFile.exists()) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mImageView.setImageDrawable(null);
//                        }
//                    });
//                } else {
//                    final Bitmap bitmap = PhotoUtil.getScaledBitmap(
//                            mFile.getPath(), destWidth, destHeight
//                    );
//                    MyLog.print("updateImageView.......................................................");
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            mImageView.setImageBitmap(bitmap);
//                        }
//                    });
//                }
//            }
//        }).start();

    }

}
