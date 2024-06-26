package com.bhola.livevideochat5.Models;

public class GiftItemModel {
    String giftName;
    String filename;
    int gem;
    boolean isSelected;
    String giftShadow;

    public GiftItemModel() {
    }

    public GiftItemModel(String giftName, String filename, int gem, boolean isSelected, String giftShadow) {
        this.giftName = giftName;
        this.filename = filename;
        this.gem = gem;
        this.isSelected = isSelected;
        this.giftShadow = giftShadow;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getGem() {
        return gem;
    }

    public void setGem(int gem) {
        this.gem = gem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getGiftShadow() {
        return giftShadow;
    }

    public void setGiftShadow(String giftShadow) {
        this.giftShadow = giftShadow;
    }
}
