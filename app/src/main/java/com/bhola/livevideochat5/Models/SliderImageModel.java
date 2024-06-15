package com.bhola.livevideochat5.Models;

import android.graphics.drawable.Drawable;

public class SliderImageModel {
    Drawable imageDrawable;
    String title,subTitle;

    public SliderImageModel() {
    }

    public SliderImageModel(Drawable imageDrawable, String title, String subTitle) {
        this.imageDrawable = imageDrawable;
        this.title = title;
        this.subTitle = subTitle;
    }

    public Drawable getImageDrawable() {
        return imageDrawable;
    }

    public void setImageDrawable(Drawable imageDrawable) {
        this.imageDrawable = imageDrawable;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }
}
