package com.bhola.livevideochat5.Models;

import java.util.ArrayList;
import java.util.Date;

public class UserModel {

    String fullname, email, profilepic, loggedAs, selectedGender, birthday, location, language, bio, intrestedIn;
    boolean streamer;
    int coins;
    int userId;
    Date date;
    String memberShipExpiryDate;
    ArrayList<GalleryModel> galleryImages;
    private String fcmToken;
    boolean banned;

    public UserModel() {
    }

    public UserModel(String fullname, String email, String profilepic, String loggedAs, String selectedGender, String birthday, String location, String language, String bio, String intrestedIn, boolean streamer, int coins, int userId, Date date, String memberShipExpiryDate, ArrayList<GalleryModel> galleryImages, String fcmToken, boolean banned) {
        this.fullname = fullname;
        this.email = email;
        this.profilepic = profilepic;
        this.loggedAs = loggedAs;
        this.selectedGender = selectedGender;
        this.birthday = birthday;
        this.location = location;
        this.language = language;
        this.bio = bio;
        this.intrestedIn = intrestedIn;
        this.streamer = streamer;
        this.coins = coins;
        this.userId = userId;
        this.date = date;
        this.memberShipExpiryDate = memberShipExpiryDate;
        this.galleryImages = galleryImages;
        this.fcmToken = fcmToken;
        this.banned = banned;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilepic() {
        return profilepic;
    }

    public void setProfilepic(String profilepic) {
        this.profilepic = profilepic;
    }

    public String getLoggedAs() {
        return loggedAs;
    }

    public void setLoggedAs(String loggedAs) {
        this.loggedAs = loggedAs;
    }

    public String getSelectedGender() {
        return selectedGender;
    }

    public void setSelectedGender(String selectedGender) {
        this.selectedGender = selectedGender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getIntrestedIn() {
        return intrestedIn;
    }

    public void setIntrestedIn(String intrestedIn) {
        this.intrestedIn = intrestedIn;
    }

    public boolean isStreamer() {
        return streamer;
    }

    public void setStreamer(boolean streamer) {
        this.streamer = streamer;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMemberShipExpiryDate() {
        return memberShipExpiryDate;
    }

    public void setMemberShipExpiryDate(String memberShipExpiryDate) {
        this.memberShipExpiryDate = memberShipExpiryDate;
    }

    public ArrayList<GalleryModel> getGalleryImages() {
        return galleryImages;
    }

    public void setGalleryImages(ArrayList<GalleryModel> galleryImages) {
        this.galleryImages = galleryImages;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}
