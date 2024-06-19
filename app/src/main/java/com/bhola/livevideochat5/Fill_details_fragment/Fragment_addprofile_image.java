package com.bhola.livevideochat5.Fill_details_fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bhola.livevideochat5.ImageResizer;
import com.bhola.livevideochat5.MainActivity;
import com.bhola.livevideochat5.Models.GalleryModel;
import com.bhola.livevideochat5.Models.UserModel;
import com.bhola.livevideochat5.MyApplication;
import com.bhola.livevideochat5.R;
import com.bhola.livevideochat5.Utils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Random;

public class Fragment_addprofile_image extends Fragment {

    private static final int PROFILE_IMAGE_CODE = 222;
    private ImageView profileImage;
    Uri changeDP_URI;
    boolean DP_changed = false;
    TextView btnNext;
    private String nickname;
    String selectedGender;
    String Birthday;
    String photoUrl;
    Context context;
    int userId;

    public Fragment_addprofile_image() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the nickname from the bundle
        if (getArguments() != null) {
            nickname = getArguments().getString("nickname");
            selectedGender = getArguments().getString("selectedGender");
            Birthday = getArguments().getString("Birthday");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_addprofile_image, container, false);
        context = view.getContext();

        NextButtonGlareAnim(view);
        generateUserID();

        btnNext = view.findViewById(R.id.btn_next);

        profileImage = view.findViewById(R.id.profile_image);
        CardView profileImageLayout = view.findViewById(R.id.editProfilelayout);

        if (!Fill_details_fragment_holder.photoUrl.isEmpty() && Fill_details_fragment_holder.loggedAs.equals("Google")) {
            Picasso.get().load(Fill_details_fragment_holder.photoUrl).into(profileImage);
            photoUrl=Fill_details_fragment_holder.photoUrl;
        } else {
            if (selectedGender.equals("male"))
                photoUrl = MyApplication.databaseURL_images + "random/male.jpg";
            else
                photoUrl = MyApplication.databaseURL_images + "random/female.jpg";


            Picasso.get().load(photoUrl).into(profileImage);
        }


        profileImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PROFILE_IMAGE_CODE);
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (DP_changed) {
                    uploadImagetoFirebaseStorage(changeDP_URI);
                } else {

                    saveProfileDetails();
                    Toast.makeText(v.getContext(), "Logged In!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(v.getContext(), MainActivity.class));

                }
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_IMAGE_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1).start(getContext(), this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == getActivity().RESULT_OK) {
                Uri croppedImageUri = result.getUri();
                DP_changed = true;
                changeDP_URI = croppedImageUri;
                profileImage.setImageURI(croppedImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // Handle cropping error
            }
        }
    }


    private void uploadImagetoFirebaseStorage(Uri croppedImageUri) {
        Utils utils = new Utils();
        utils.showLoadingDialog(context, "Uploading...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Get a reference to the location where you want to store the file in Firebase Storage
        StorageReference imageRef = storageReference.child("Users/" + String.valueOf(userId) + "/profile.jpg");


        int orientation = ImageResizer.getImageOrientation(croppedImageUri, context);
        Bitmap bitmap = ImageResizer.imageURItoBitmap(croppedImageUri, context);
        Bitmap rotatedBitmap = ImageResizer.rotateBitmap(bitmap, orientation);

        Bitmap redusedBitmap = ImageResizer.reduceBitmapSize(rotatedBitmap, 400000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        redusedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();


// Upload the file to Firebase Storage
        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // File uploaded successfully
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        photoUrl = downloadUrl;

                        utils.dismissLoadingDialog();

                        saveProfileDetails();
                        Toast.makeText(context, "Logged In!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(context, MainActivity.class));
                    });
                })
                .addOnFailureListener(e -> {
                    Log.d("sdafsdfdsfdsaf", "uploadImagetoFirebaseStorage: " + e.getMessage());
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }


    private void saveProfileDetails() {


        SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("email", Fill_details_fragment_holder.email);
        editor.putString("loginAs", Fill_details_fragment_holder.loggedAs);
        editor.putString("photoUrl", photoUrl);
        editor.putString("nickName", nickname);
        editor.putString("Gender", selectedGender);
        editor.putString("Birthday", Birthday);
        editor.putInt("userId", userId);
        editor.putInt("coins", 0);
        editor.apply();


        UserModel userModel = new UserModel(nickname, Fill_details_fragment_holder.email, photoUrl, Fill_details_fragment_holder.loggedAs, selectedGender, Birthday, "", "English", "", "", false, 0, userId, new java.util.Date(), "", new ArrayList<GalleryModel>(), "", false);
        MyApplication.userModel = userModel;


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(String.valueOf(userId))
                .set(userModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle the error if the data upload fails
                        // You can add error handling code here
                    }
                });

        //add fcm token
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                new Utils().updateProfileonFireStore("fcmToken", token);
            }
        });


    }

    private void generateUserID() {
        Random random = new Random();
        int min = 1000000; // The minimum 7-digit number (1,000,000)
        int max = 9999999; // The maximum 7-digit number (9,999,999)

        int randomInt = random.nextInt((max - min) + 1) + min;

        String collectionPath = "Users";
        String documentId = String.valueOf(randomInt);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> documentSnapshotTask = db.collection(collectionPath).document(documentId).get();

        documentSnapshotTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    generateUserID();
                } else {
                    userId = randomInt;
                }
            } else {
                userId = randomInt;
            }
        });

    }

    private void NextButtonGlareAnim(View view) {

        ImageView button_glare = view.findViewById(R.id.button_glare);

        Animation glareAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.glare_animation);
        button_glare.startAnimation(glareAnimation);

    }


}