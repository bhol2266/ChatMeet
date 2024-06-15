package com.bhola.livevideochat5;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

public class Feedback extends AppCompatActivity {
    RelativeLayout problemOption;
    BottomSheetDialog bottomSheetDialog;
    GridLayout gridLayout;
    TextInputEditText email, description;
    TextView problem;
    private static final int REQUEST_IMAGE_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        problemOption = findViewById(R.id.problemOption);
        problem = findViewById(R.id.problem);

        gridLayout = findViewById(R.id.gridLayout);
        email = findViewById(R.id.email);
        description = findViewById(R.id.description);

        problemOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetDialog();
            }
        });
        uploadImage();
        submitBtn();
        actionBar();
    }

    private void actionBar() {
        ImageView back_arrow=findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void submitBtn() {
        TextView submitBtn = findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(problem.getText().equals("The Problem is related with")){
                    problemOption.performClick();
                    return;
                }
                if (email.getText().toString().length() ==0) {
                    Toast.makeText(Feedback.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email.getText().toString())) {
                    Toast.makeText(Feedback.this, "Enter email correctly", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (description.getText().toString().length() ==0) {
                    Toast.makeText(Feedback.this, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(Feedback.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }

    private boolean isValidEmail(String email){


        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }


    private void openBottomSheetDialog() {
        bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        createListView(view);
    }

    private void createListView(View view) {

        ListView listView = view.findViewById(R.id.listView);

        String[] items = {"Recharge", "Functions", "Performance", "Advices on functions", "Need Help", "Account Issues", "Can't Deposit", "Successfull Payment But No Product", "Can't Find Desired Payment Method"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.list_textview_bottomsheetdialog_feedback, items);

        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = items[position];

                problem.setText(selectedItem);
                bottomSheetDialog.cancel();

            }
        });

    }


    private void uploadImage() {

        CardView uploadImage = findViewById(R.id.uploadImage);
        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemCount = gridLayout.getChildCount();
                if (itemCount < 7) {
                    launchGalleryIntent();
                }

            }
        });
    }


    private void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();


            Bitmap bitmap = resizeIMage(imageUri);
            setImageinGridLayout(bitmap);

        }
    }

    private Bitmap resizeIMage(Uri imageUri) {
        // Convert Uri to Bitmap
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

// Check the EXIF orientation and rotate the bitmap if necessary
        try {
            ExifInterface exifInterface = new ExifInterface(getContentResolver().openInputStream(imageUri));
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    // No rotation needed
                    break;
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

// Calculate the desired width and height while maintaining aspect ratio
        int reqWidth = 500; // Desired width in pixels
        int reqHeight = 500; // Desired height in pixels

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        float widthRatio = (float) reqWidth / originalWidth;
        float heightRatio = (float) reqHeight / originalHeight;
        float scaleFactor = Math.min(widthRatio, heightRatio);

        int finalWidth = Math.round(originalWidth * scaleFactor);
        int finalHeight = Math.round(originalHeight * scaleFactor);

// Resize the bitmap
        bitmap = Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, false);
        return bitmap;
    }

    private void setImageinGridLayout(Bitmap bimap) {

// Create a new ImageView
        ImageView imageView = new ImageView(this);
// Set the desired image resource or drawable to the ImageView
        imageView.setImageBitmap(bimap);
// Set layout parameters for width and height
        int sizeInPixels = (int) (80 * getResources().getDisplayMetrics().density); // Set the desired size in dp
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.width = sizeInPixels;
        layoutParams.height = sizeInPixels;

        // Set margins to create gaps between grid items
        int gapInPixels = (int) (5 * getResources().getDisplayMetrics().density); // Set the desired gap size in dp
        layoutParams.setMargins(gapInPixels, gapInPixels, gapInPixels, gapInPixels);

// Add the ImageView to the GridLayout
        gridLayout.addView(imageView, layoutParams);


    }


}