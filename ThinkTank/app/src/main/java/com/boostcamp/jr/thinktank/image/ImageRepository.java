package com.boostcamp.jr.thinktank.image;

import java.io.File;
import java.util.List;

/**
 * Created by jr on 2017-02-18.
 */

public class ImageRepository {

    private static ImageRepository sImageRepository;

    private List<File> mFiles;

    public static ImageRepository get() {
        if (sImageRepository == null) {
            sImageRepository = new ImageRepository();
        }

        return sImageRepository;
    }

    public void setFiles(List<File> files) {
        mFiles = files;
    }

    public List<File> getFiles() {
        return mFiles;
    }

}
