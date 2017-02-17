package com.boostcamp.jr.thinktank.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by jr on 2017-02-17.
 */

public class PhotoUtil {

    public static Bitmap getScaledBitmap(String filePath, int destWidth, int destHeight) {

        // 파일의 이미지 크기를 알아낸다.
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        // 얼마나 크기를 조정할지 파악한다.
        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // Bitmap을 생성&반환
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        boolean isVertical = true;

        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    isVertical = false;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    isVertical = false;
                    break;
            }
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        if (isVertical) {
            return bitmap;
        } else {
            Matrix rotateMatrix = new Matrix();
            rotateMatrix.postRotate(90); //-360~360


            Bitmap sideInversionImg = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), rotateMatrix, false);

            return sideInversionImg;
        }
    }

    public static File getPhotoFile(Context packageContext, String fileName) {
        File externalFileDir = packageContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (externalFileDir == null) {
            return null;
        }

//        MyLog.print(externalFileDir.getPath());

        return new File(externalFileDir, fileName);
    }

    public static boolean isMyImage(Context context, File photoFile) {
        return photoFile.getPath()
                .equals(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + "/" + photoFile.getName());
    }

}
