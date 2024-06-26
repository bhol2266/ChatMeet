package com.bhola.livevideochat5;

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.bhola.livevideochat5.Models.CountryInfo_Model;
import com.bhola.livevideochat5.Models.GiftItemModel;
import com.bhola.livevideochat5.Models.Model_Profile;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class Utils {

    private static ProgressDialog progressDialog;


    public static void showCustomProgressDialog(Context context, String message) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        progressDialog = new ProgressDialog(context);

        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View customView = inflater.inflate(R.layout.custom_progress_dialog, null);

        // Set the custom view to the ProgressDialog
        progressDialog.show();
        progressDialog.setContentView(customView);

        // Optionally, you can customize further
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set the message
        TextView progressText = customView.findViewById(R.id.progress_text);
        progressText.setText(message);

        // Make it cancelable
        progressDialog.setCancelable(false);
    }

    public static void dismissCustomProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    public void updateProfileonFireStore(String key, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(key, value);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });

    }

    public void updateDateonFireStore(String key, Date date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("Users");
        String userId = String.valueOf(MyApplication.userModel.getUserId()); // Replace with the actual user ID
        DocumentReference userDocRef = usersRef.document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put(key, date);

        userDocRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    // The field(s) were successfully updated
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that might occur during the update
                });
    }

    public static void replaceFCMToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                new Utils().updateProfileonFireStore("fcmToken", token);
            }
        });
    }


    public void downloadProfile_andGetURI(String image_url, Context context) throws IOException {
        //this method is used to download profle pic from google signIN option and get Uri to upload to digital ocean space

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                            .permitAll().build();
                    StrictMode.setThreadPolicy(policy);


                    URL url = new URL(image_url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                    File internalStorage = new File(context.getFilesDir(), "images");

                    if (!internalStorage.exists()) {
                        internalStorage.mkdir();
                    }
                    File file = new File(internalStorage, "profile.jpg");
                    Uri imageURI = Uri.fromFile(file);
                    MyApplication.userModel.setProfilepic(MyApplication.databaseURL_images + "RealVideoChat1/profilePic/" + String.valueOf(MyApplication.userModel.getUserId()) + ".jpg");
                    if (file.exists()) file.delete();

                    FileOutputStream outputStream = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();

                } catch (Exception e) {
                    Log.d("SpaceError", "saveProfileDetails: " + e.getMessage());
                }
            }
        }).start();


    }

    public int calculateAge(String birthDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Parse the birthdate string into a Date object
            Date birthDate = sdf.parse(birthDateString);

            // Get the current date
            Calendar currentDate = Calendar.getInstance();
            Date now = currentDate.getTime();

            // Calculate the age

            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(birthDate);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(currentDate.getTime());

            int age = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);

            // Check if the birthdate has occurred this year or not
            if (cal2.get(Calendar.DAY_OF_YEAR) < cal1.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (java.text.ParseException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public Bitmap applyBlur(Bitmap inputBitmap, int radius, Context context) {
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript renderScript = RenderScript.create(context);

        Allocation tmpIn = Allocation.createFromBitmap(renderScript, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blurScript.setInput(tmpIn);
        blurScript.setRadius(radius);
        blurScript.forEach(tmpOut);

        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    public static boolean isInternetAvailable(Context context) {
        if (context == null) return false;


        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {


            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {

                try {
                    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                    if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                        Log.i("update_statut", "Network is available : true");
                        return true;
                    }
                } catch (Exception e) {
                    Log.i("update_statut", "" + e.getMessage());
                }
            }
        }
        Log.i("update_statut", "Network is available : FALSE ");
        return false;
    }


    public static String decryption(String encryptedText) {

        int key = 5;
        String decryptedText = "";

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return decryptedText;
    }

    public static String encryption(String text) {

        int key = 5;
        char[] chars = text.toCharArray();
        String encryptedText = "";
        String decryptedText = "";

        //Encryption
        for (char c : chars) {
            c += key;
            encryptedText = encryptedText + c;
        }

        //Decryption
        char[] chars2 = encryptedText.toCharArray();
        for (char c : chars2) {
            c -= key;
            decryptedText = decryptedText + c;
        }
        return encryptedText;
    }

    public static Model_Profile readCursor(Cursor cursor) {

        // Extract data from the cursor and populate the Model_Profile object
        String Username = Utils.decryption(cursor.getString(0));
        String Name = Utils.decryption(cursor.getString(1));
        String Country = cursor.getString(2);
        if (Country.contains(",")) Country = Country.split(",")[0].trim();


        String Languages = cursor.getString(3);
        String Age = cursor.getString(4);
        String InterestedIn = cursor.getString(5);
        String BodyType = cursor.getString(6);
        String Specifics = Utils.decryption(cursor.getString(7));
        String Ethnicity = cursor.getString(8);
        String Hair = cursor.getString(9);
        String EyeColor = cursor.getString(10);
        String Subculture = cursor.getString(11);

        String nationality = "";
        for (CountryInfo_Model countryInfo_model : MyApplication.countryList) {
            if (Country.toLowerCase().trim().equals(countryInfo_model.getCountry().toLowerCase().trim())) {
                nationality = countryInfo_model.getNationality();
            }
        }


        String profilePhoto = MyApplication.databaseURL_images + "VideoChatProfiles/" + nationality + "/" + Username + "/profile.jpg";
        String coverPhoto = profilePhoto;
        int censored = cursor.getInt(17);
        int like = cursor.getInt(18);
        int selectedBot = cursor.getInt(19);

        // Convert JSON strings back to arrays/lists using Gson
        Gson gson = new Gson();


        String interestsJson = Utils.decryption(cursor.getString(12));
        List<Map<String, String>> Interests = gson.fromJson(interestsJson, new TypeToken<List<Map<String, String>>>() {
        }.getType());


        String imagesJson = Utils.decryption(cursor.getString(15));
        List<String> images = new ArrayList<>();

        String videosJson = Utils.decryption(cursor.getString(16));
        List<Map<String, String>> videos = new ArrayList<>();
        try {
            JSONArray imagesArray = new JSONArray(imagesJson);
            for (int i = 0; i < imagesArray.length(); i++) {
                images.add(MyApplication.databaseURL_images + "VideoChatProfiles/" + nationality + "/" + Username + "/" + String.valueOf(i) + ".jpg");
            }

            JSONArray videoArray = new JSONArray(videosJson);
            for (int i = 0; i < videoArray.length(); i++) {
                Map map = new HashMap<>();
                map.put("imageUrl", "");
                map.put("videoUrl", "");
                videos.add(map);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create a new Model_Profile object and populate it
        Model_Profile model_profile = new Model_Profile(Username, Name, Country, Languages, Age, InterestedIn, BodyType, Specifics, Ethnicity, Hair, EyeColor, Subculture, profilePhoto, coverPhoto, Interests, images, videos, censored, like, selectedBot);

        return model_profile;
    }


    public static Model_Profile getSingleRandomGirlVideoObject(Context context) {

        ArrayList<Girl> girlsList = new ArrayList<>();
//         Read and parse the JSON file
        try {
            JSONArray girlsArray = new JSONArray(loadJSONFromAsset(context));

            // Iterate through the girls array
            for (int i = 0; i < girlsArray.length(); i++) {
                JSONObject girlObject = girlsArray.getJSONObject(i);


                Girl girl = new Girl();
                String censorship = girlObject.getString("censorship");
                if (censorship.equals("uncensored")) {
                    girl.setCensored(false);
                } else {
                    girl.setCensored(true);
                }
                girl.setUsername(girlObject.getString("username"));

                girl.setSeen(false);
                girl.setLiked(false);

                // Add the Girl object to the ArrayList
                if (MyApplication.userLoggedIAs.equals("Google")) {
                    girlsList.add(girl);
                } else {
                    if (girl.isCensored()) {
                        girlsList.add(girl);
                    }
                }

            }
            Collections.shuffle(girlsList);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Random random = new Random();
        int randomIndex = random.nextInt(girlsList.size());
        Girl randomgirl = girlsList.get(randomIndex);

        return getGirlProfile_DB(randomgirl.getUsername(), context);

    }


    public static String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open("girls_video.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }


    public static Model_Profile getGirlProfile_DB(String userName, Context context) {
        Model_Profile model_profile = null;

        Cursor cursor = new DatabaseHelper(context, MyApplication.DB_NAME, MyApplication.DB_VERSION, "GirlsProfile").readSingleGirl(userName);
        if (cursor.moveToFirst()) {
            model_profile = Utils.readCursor(cursor);
        }
        cursor.close();

        return model_profile;
    }


    public static void scaleUp(FrameLayout frameLayout) {
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleX", 1.0f, 1.1f);
        scaleAnimator.setDuration(300);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimator.start();

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleY", 1.0f, 1.1f);
        scaleYAnimator.setDuration(300);
        scaleYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimator.start();

    }

    public static void scaleDown(FrameLayout frameLayout) {
        ObjectAnimator scaleAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleX", 1.05f, 1.0f);
        scaleAnimator.setDuration(300);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimator.start();

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(frameLayout, "scaleY", 1.05f, 1.0f);
        scaleYAnimator.setDuration(300);
        scaleYAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimator.start();
    }


    public static void applyGradientToTextView(Context context, TextView textView) {
        if (textView == null) {
            return;
        }
        Typeface typeface = ResourcesCompat.getFont(context, R.font.poppins_bold);
        textView.setTypeface(typeface);
        // Measure the width of the text to apply the gradient
        float width = textView.getPaint().measureText(textView.getText().toString());

        // Create a LinearGradient shader
        Shader textShader = new LinearGradient(
                0, 0, width, textView.getTextSize(),
                new int[]{
                        ContextCompat.getColor(context, R.color.themeColor),
                        ContextCompat.getColor(context, R.color.themeColorlight),
                        ContextCompat.getColor(context, R.color.themeColor)
                },
                new float[]{0, 0.5f, 1},
                Shader.TileMode.CLAMP
        );

        // Apply the shader to the TextView's paint
        textView.getPaint().setShader(textShader);

        // Invalidate the TextView to redraw it with the shader applied
        textView.invalidate();
    }


    public static ArrayList<GiftItemModel> readGiftsJson_FromAsset(Context context) {
        ArrayList<GiftItemModel> giftItems = new ArrayList<>();

        try {
            // Read JSON file from assets
            String json = loadJSONFromAsset(context, "gifts/gifts.json");

            // Parse JSON array
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                // Extract data from JSON object
                String giftname = jsonObject.getString("giftname");
                String filename = jsonObject.getString("filename");
                boolean isSelected = jsonObject.getBoolean("isSelected");
                int gem = jsonObject.getInt("gem");
                String giftShadow = jsonObject.getString("giftShadow");


                // Create GiftItemModel object and add to list
                GiftItemModel giftItem = new GiftItemModel(giftname, filename, gem, isSelected, giftShadow);
                giftItems.add(giftItem);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return giftItems;
    }

    private static String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }


}
