package com.tilatina.guardmonitor;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by jaime on 19/04/16.
 */
public class NoveltyThumbnail {
    private Bitmap thumbnail;

    public NoveltyThumbnail() {

    }

    public NoveltyThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public NoveltyThumbnail setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

}
