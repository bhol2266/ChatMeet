package com.bhola.saxchat2.Models;


import android.graphics.drawable.Drawable;

public class FlipItem {
    private String itemName;
    private Drawable itemImage;

    public FlipItem(String itemName, Drawable itemImage) {
        this.itemName = itemName;
        this.itemImage = itemImage;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Drawable getItemImage() {
        return itemImage;
    }

    public void setItemImage(Drawable itemImage) {
        this.itemImage = itemImage;
    }
}
