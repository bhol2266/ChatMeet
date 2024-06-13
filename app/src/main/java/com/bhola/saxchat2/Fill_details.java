package com.bhola.saxchat2;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bhola.saxchat2.Models.GalleryModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class Fill_details extends AppCompatActivity {

    String selectedGender = "";
    TextInputEditText firstname, lastName;
    String Birthday = "";
    TextView nextBtn;
    String photoUrl;
    int userId;
    private final int PROFILE_IMAGE_CODE = 222;
    boolean DP_changed = false;
    Uri ChangeDP_URI;
    String loggedAs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_details);
        fullscreenMode();

        loggedAs = getIntent().getStringExtra("loggedAs");
        generateUserID();


        nextBtn = findViewById(R.id.nextBtn);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstname.getText().toString().length() > 0 && selectedGender.length() > 0 && Birthday.length() > 0) {
                    int age = new Utils().calculateAge(Birthday);
                    if (age < 18) {
                        Toast.makeText(Fill_details.this, "Under 18 not allowed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (DP_changed) {
                        uploadImagetoFirebaseStorage(ChangeDP_URI);
                    } else {

                        saveProfileDetails();
                        Toast.makeText(Fill_details.this, "Logged In!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Fill_details.this, MainActivity.class));
                        finish();

                    }
                }
                else{
                    Toast.makeText(Fill_details.this, "Fill details", Toast.LENGTH_SHORT).show();
                }
            }
        });
        firstname = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastName);

        firstname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                btnStatus();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        TextView dateOfBirth = findViewById(R.id.dateOfBirth);
        MaterialCardView selectDate = findViewById(R.id.selectDate);
        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper themedContext = new ContextThemeWrapper(Fill_details.this, R.style.DatePickerDialogTheme);
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        themedContext,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // Format the month and day with zero padding if needed
                                String formattedMonth = String.format("%02d", month + 1);
                                String formattedDay = String.format("%02d", dayOfMonth);

                                // Handle the selected date
                                Birthday = year + "-" + formattedMonth + "-" + formattedDay;
                                dateOfBirth.setText(Birthday);
                                btnStatus();
                            }
                        },
                        2023, 0, 1  // Year, Month (0-indexed), Day
                );
                datePickerDialog.show();
            }
        });

        changeProfileImage();
        genderSelector();

        receiveIntent();


    }



    private void saveProfileDetails() {

        Intent receivedIntent = getIntent();
        String email = receivedIntent.getStringExtra("email");

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("email", email);
        editor.putString("photoUrl", photoUrl);
        editor.putString("loginAs", loggedAs);

        editor.putString("nickName", firstname.getText().toString()+" "+lastName.getText().toString().trim());
        editor.putString("Gender", selectedGender);
        editor.putString("Birthday", Birthday);
        editor.putInt("userId", userId);
        editor.putInt("coins", 0);
        editor.apply();


        UserModel userModel = new UserModel(firstname.getText().toString()+" "+lastName.getText().toString().trim(), email, photoUrl, loggedAs, selectedGender, Birthday, "", "English", "", "", false, 0, userId, new java.util.Date(), "", new ArrayList<GalleryModel>(), "", false);
        MyApplication.userModel = userModel;

        Log.d("saveProfileDetails", "saveProfileDetails: "+userModel.toString());


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

    private void receiveIntent() {
        Intent receivedIntent = getIntent();
        String loggedAs = receivedIntent.getStringExtra("loggedAs");
        String displayName = receivedIntent.getStringExtra("nickName");

        if (loggedAs.equals("Google")) {
            firstname.setText(displayName);
        }

    }

    private void genderSelector() {
        MaterialCardView maleCard = findViewById(R.id.maleCard);
        MaterialCardView femaleCard = findViewById(R.id.femaleCard);
        ImageView maleCheckBox = findViewById(R.id.maleCheckBox);
        ImageView femaleCheckBox = findViewById(R.id.femaleCheckBox);

        Drawable checked = ContextCompat.getDrawable(Fill_details.this, R.drawable.checked);
        Drawable unchecked = ContextCompat.getDrawable(Fill_details.this, R.drawable.unchecked);


        maleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedGender.equals("male")) {
                    return;
                }
// Set the drawable to the ImageView
                maleCheckBox.setImageDrawable(checked);
                femaleCheckBox.setImageDrawable(unchecked);

                selectedGender = "male";

                btnStatus();
            }
        });

        femaleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedGender.equals("female")) {
                    return;
                }
                maleCheckBox.setImageDrawable(unchecked);
                femaleCheckBox.setImageDrawable(checked);

                selectedGender = "female";
                btnStatus();

            }
        });


    }

    private void btnStatus() {
        if (firstname.getText().toString().length() > 0 && selectedGender.length() > 0 && Birthday.length() > 0) {
            nextBtn.setAlpha(1);
        } else {
            nextBtn.setAlpha(0.5F);
        }

    }

    private void changeProfileImage() {
        photoUrl = getIntent().getStringExtra("photoUrl");

        CircleImageView profileImage = findViewById(R.id.profileImage);
        if (photoUrl.length() != 0) {
            if (photoUrl.startsWith("http")) {
                Picasso.get().load(photoUrl).into(profileImage);
            } else {
                profileImage.setImageURI(Uri.parse(photoUrl));
            }
        }

        LinearLayout profileImageLayout = findViewById(R.id.editProfilelayout);
        profileImageLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PROFILE_IMAGE_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_IMAGE_CODE && resultCode == RESULT_OK && data != null) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1, 1) // Specify the aspect ratio you want
                    .start(this);

        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImageUri = result.getUri();

                DP_changed = true;
                ChangeDP_URI = croppedImageUri;

                CircleImageView profileImage = findViewById(R.id.profileImage);
                profileImage.setImageURI(croppedImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                // Handle cropping error
            }
        }
    }

    private void uploadImagetoFirebaseStorage(Uri croppedImageUri) {
        Utils utils = new Utils();
        utils.showLoadingDialog(Fill_details.this, "Uploading...");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        // Get a reference to the location where you want to store the file in Firebase Storage
        StorageReference imageRef = storageReference.child("Users/" + String.valueOf(userId) + "/profile.jpg");


        int orientation = ImageResizer.getImageOrientation(croppedImageUri, Fill_details.this);
        Bitmap bitmap = ImageResizer.imageURItoBitmap(croppedImageUri, Fill_details.this);
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
                        Toast.makeText(Fill_details.this, "Logged In!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Fill_details.this, MainActivity.class));
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

    }

    private void fullscreenMode() {
        // Clear any fullscreen flags affecting both status bar and navigation bar
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Ensure the content fits the window
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Create WindowInsetsControllerCompat to manage system bars visibility
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());

        // Hide only the status bar
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());

        // Set the behavior for showing system bars transiently by swipe
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Ensure the navigation bar remains visible
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        // Set the navigation bar color
//        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.vip_membership_goldcolor));

        // For devices with display cutouts, allow content to layout in cutout areas if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        // Handle older Android versions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Clear any previously set fullscreen flag
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            // Hide status bar for older versions
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_FULLSCREEN | // Hide the status bar
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}