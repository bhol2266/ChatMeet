package com.bhola.livevideochat4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.common.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import de.hdodenhof.circleimageview.CircleImageView;


public class CameraActivity extends AppCompatActivity {

    CardView textureCardview;
    VideoView videoView;
    int currentSeekPosition = 0;
    Runnable runnable2, runnable3;
    Handler handler2, handler3;
    MediaPlayer mediaPlayer;

    int videoView_Height;
    Model_Profile model_profile;
    //Camera stuffs
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123;
    private TextureView textureView;

    CameraManager cameraManager;
    Size previewSize;
    String cameraId;
    Handler backgroundHandler;
    HandlerThread handlerThread;
    public static int currentVideoIndex = 0;
    RelativeLayout progressBarLayout;
    LinearLayout controlsLayout;
    ImageView taptoReply;
    View tapToReplyView;
    CameraCaptureSession cameraCaptureSession;
    CameraDevice cameraDevice;
    CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevicee) {
            cameraDevice = cameraDevicee;

            try {
                createPreviewSession();
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            CameraActivity.this.cameraDevice = null;
        }
    };
    String currentCameraSide = "Front";
    private String TAG = "activity_camera";
    public static ArrayList<Girl> girlsList;
    androidx.appcompat.app.AlertDialog disclaimer_dialog = null;
    private CountDownTimer countDownTimer;
    private long timeRemaining = 0; // Store remaining time when paused for coundown timer

    androidx.appcompat.app.AlertDialog block_user_dialog = null;
    androidx.appcompat.app.AlertDialog report_user_dialog = null;
    AlertDialog report_userSucessfully_dialog = null;
    Handler callhandler;
    Runnable callRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);
//       fullscreenMode();

        if (SplashScreen.Ads_State.equals("active")) {
            showAds();
        }
        actionbar();

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        boolean disclaimerAccepted = sharedPreferences.getBoolean("disclaimerAccepted", false);
        currentVideoIndex = sharedPreferences.getInt("currentVideoIndex", 0);
        if (!disclaimerAccepted) {
            disclaimerDialog();
        }

        textureView = findViewById(R.id.textureView);
        textureCardview = findViewById(R.id.draggableView);
        controlCamera();
        likeBtn();

    }

    private void likeBtn() {
        ImageView heart = findViewById(R.id.heart);
        taptoReply = findViewById(R.id.taptoReply);
        tapToReplyView = findViewById(R.id.tapToReplyView);

        Animation taptoReply_anim = AnimationUtils.loadAnimation(this, R.anim.taptoreply_anim);
        taptoReply.startAnimation(taptoReply_anim);


        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.breathing_anim);
        heart.startAnimation(pulse);

        heart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (girlsList.get(currentVideoIndex).isLiked()) {
                    return;
                }


                ImageViewCompat.setImageTintList(heart, null);
                taptoReply.setVisibility(View.GONE);
                heart.setImageResource(R.drawable.heart_liked);
                heart.clearAnimation();
                LottieAnimationView heart_lottie = findViewById(R.id.heart_lottie);
                heart_lottie.setVisibility(View.VISIBLE);
                heart_lottie.setProgress(0f);
                heart_lottie.playAnimation();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        heart_lottie.cancelAnimation();
                        heart_lottie.setVisibility(View.GONE);


                    }
                }, 1600);


                getCall();
                showCustomToast();

            }
        });
    }

    private void showCustomToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);

        TextView profileName = layout.findViewById(R.id.profileName);
        profileName.setText(model_profile.getName());
        // You can customize the text and other properties of the view elements here

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 130); // Adjust the margin (bottom) here
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        girlsList.get(currentVideoIndex).setLiked(true);
    }

    private void getCall() {
        if (SplashScreen.App_updating.equals("active")) {
            return;
        }
        //storing these variable again because the call will come after 10s. so after 10s the model_profile may gets udpated
        String name = model_profile.getName();
        String profileImage = model_profile.getProfilePhoto();
        String usernamee = model_profile.getUsername();
        callhandler = new Handler();
        callRunnable = new Runnable() {
            @Override
            public void run() {


                FragmentManager fragmentManager = getSupportFragmentManager();

                if (!fragmentManager.isDestroyed()) {
                    try {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        Fragment_Calling fragment = new Fragment_Calling();

                        Bundle args = new Bundle();
                        args.putString("name", name); // Replace "key" with an appropriate key and "Your Data" with the data you want to pass
                        args.putString("profile", profileImage); // Replace "key" with an appropriate key and "Your Data" with the data you want to pass
                        args.putString("username", usernamee); // Replace "key" with an appropriate key and "Your Data" with the data you want to pass

                        fragment.setArguments(args);
                        fragmentTransaction.replace(R.id.player, fragment);
                        fragmentTransaction.commit();
                    } catch (Exception e) {

                    }
                }
            }
        };
        callhandler.postDelayed(callRunnable, 10000);

    }

    private void playRinging() {

        loadDataArraylist();

        handler2 = new Handler();
        runnable2 = new Runnable() {
            @Override
            public void run() {


// Set desired width and height in dp
                int desiredWidthDp = 100;
                int desiredHeightDp = 165;
// Convert dp values to pixels
                float scale = getResources().getDisplayMetrics().density;
                int desiredWidthPx = (int) (desiredWidthDp * scale + 0.5f);
                int desiredHeightPx = (int) (desiredHeightDp * scale + 0.5f);
// Get the layout parameters of the TextureView
                ViewGroup.LayoutParams layoutParams = textureView.getLayoutParams();

// Set the width and height
                layoutParams.width = desiredWidthPx;
                layoutParams.height = desiredHeightPx;

// Apply the updated layout parameters to the TextureView
//                textureView.setLayoutParams(layoutParams);// when textureView size change the textureView listener does the remaining job

                //Since call is received enable speaker and microphone icon and webcam

                ImageView speaker = findViewById(R.id.speaker);
                speaker.setVisibility(View.VISIBLE);
                ImageView microphone = findViewById(R.id.microphone);
                microphone.setVisibility(View.VISIBLE);

                textureCardview.setVisibility(View.VISIBLE);

            }
        };
        handler2.postDelayed(runnable2, 1000);


    }

    private void resetButtons() {
        ImageView heart = findViewById(R.id.heart);
        heart.setImageResource(R.drawable.heart);

        Animation pulse = AnimationUtils.loadAnimation(this, R.anim.breathing_anim);
        heart.startAnimation(pulse);

        int tintColor = getResources().getColor(R.color.white); // Use the appropriate resource ID for your color
        ImageViewCompat.setImageTintList(heart, ColorStateList.valueOf(tintColor));
        ImageViewCompat.setImageTintMode(heart, PorterDuff.Mode.SRC_IN);

        taptoReply.setVisibility(View.VISIBLE);
        ImageView speaker = findViewById(R.id.speaker);
        speaker.setImageResource(R.drawable.speaker_off);

    }


    private void loadDataArraylist() {


        girlsList = new ArrayList<>();
        if (SplashScreen.App_updating.equals("active")) {
            Girl girl = new Girl();
            girl.setUsername("Artlimbo");
            girl.setCensored(true);
            girl.setSeen(false);
            girl.setLiked(false);
            girlsList.add(girl);
            currentVideoIndex = 0;
            playVideoinBackground();

        } else {

            boolean girlList_available = save_retreive_Chatbots_insharedPrefrence("retreive");
            if (girlList_available) {
                filterGirlList(); // this is to remove uncensored video if app is not logged with google
                playVideoinBackground();
                return;
            }

            readGirlsVideo();

        }

    }

    private void playVideoinBackground() {


        videoView = findViewById(R.id.videoView);
        controlsLayout.setVisibility(View.GONE);

        FrameLayout playerLayout = findViewById(R.id.player);

        // Get the height of the VideoView area before playing the video
        playerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int videoViewHeight = playerLayout.getHeight();
                int videoViewWidth = playerLayout.getWidth();

                // Remove the listener to avoid redundant calls
                playerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                videoView_Height = videoViewHeight;

            }
        });
        String videoPath = SplashScreen.databaseURL_video + "InternationalChatVideos/" + girlsList.get(currentVideoIndex).getUsername() + ".mp4";


        getGirlProfile_DB(girlsList.get(currentVideoIndex).getUsername()); //this method is for reading single girl data from db top update name and use data for intent when user wants to goto profile

        Log.d(SplashScreen.TAG, "videoPath: " + videoPath);


        Uri videoUri = Uri.parse(videoPath);
        videoView.setVideoURI(videoUri);
        videoView.start();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                progressBarLayout = findViewById(R.id.progressBarLayout);

                setTimer();

                mediaPlayer = mp;
                mp.setVolume(0f, 0f);
                mp.setLooping(true);

                int viewHeight = videoView.getHeight();


                float scale = (float) videoView_Height / viewHeight;


                videoView.setScaleY(scale);
                videoView.setScaleX(scale);


                videoView.start();

                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (currentVideoIndex == girlsList.size() - 1) {
                    editor.putInt("currentVideoIndex", 0);
                } else {
                    editor.putInt("currentVideoIndex", currentVideoIndex + 1);
                }
                editor.apply(); // Apply the changes to SharedPreferences


                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {


                        progressBarLayout.setVisibility(View.GONE);
                        controlsLayout.setVisibility(View.VISIBLE);
                        tapToReplyView.setVisibility(View.GONE);
                        resetButtons();


                    }
                }, 500);


            }
        });
    }


    private void getGirlProfile_DB(String userName) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = new DatabaseHelper(CameraActivity.this, SplashScreen.DB_NAME, SplashScreen.DB_VERSION, "GirlsProfile").readSingleGirl(userName);
                if (cursor.moveToFirst()) {
                    model_profile = SplashScreen.readCursor(cursor);
                }
                cursor.close();
                ((Activity) CameraActivity.this).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                TextView profileName = findViewById(R.id.profileName);
                                profileName.setText(model_profile.getName());
                                CircleImageView profileImage = findViewById(R.id.profileImage);

                                profileName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        profileImage.performClick();
                                    }
                                });
                                Picasso.get().load(model_profile.getProfilePhoto()).into(profileImage);
                                profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(CameraActivity.this, Profile.class);
                                        intent.putExtra("userName", girlsList.get(currentVideoIndex).getUsername());
                                        intent.putExtra("online", true);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }, 200);

                    }
                });
            }
        }).start();


    }


    private void setTimer() {
        TextView counterText = findViewById(R.id.counterText);
        counterText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timeRemaining = 0;
                if (currentVideoIndex < girlsList.size()) {

                    if (SplashScreen.App_updating.equals("active")) {
                        onBackPressed();
                        return;
                    }

                    currentVideoIndex = currentVideoIndex + 1;
                    videoView.stopPlayback();
                    progressBarLayout.setVisibility(View.VISIBLE);
                    controlsLayout.setVisibility(View.GONE);
                    tapToReplyView.setVisibility(View.VISIBLE);
                    String videoPath = SplashScreen.databaseURL_video + "InternationalChatVideos/" + girlsList.get(currentVideoIndex).getUsername() + ".mp4";


                    Uri videoUri = Uri.parse(videoPath);
                    videoView.setVideoURI(videoUri);
                    // Start playing the new video
                    videoView.start();
                    getGirlProfile_DB(girlsList.get(currentVideoIndex).getUsername());
                    countDownTimer.cancel();
                }
                if (currentVideoIndex == girlsList.size() - 1) {
                    for (Girl girll : girlsList) {
                        girll.setLiked(false);
                    }
                    currentVideoIndex = 0;
                }
            }
        });
        TextView counterTextCircular = findViewById(R.id.counterTextCircular);

        // Set the initial value of the timer in seconds
        int initialSeconds = (timeRemaining > 0) ? (int) timeRemaining : 5;

        countDownTimer = new CountDownTimer(initialSeconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timerTextView with the remaining seconds

                int seconds = (int) (millisUntilFinished / 1000);
                timeRemaining = seconds;
                counterText.setText("Your dream girl will be gone in " + String.valueOf(seconds) + " seconds");
                counterTextCircular.setText(String.valueOf(seconds));

                if (seconds == 0 && currentVideoIndex < girlsList.size()) {

                    if (SplashScreen.App_updating.equals("active")) {
                        onBackPressed();
                        return;
                    }

                    currentVideoIndex = currentVideoIndex + 1;
                    videoView.stopPlayback();
                    progressBarLayout.setVisibility(View.VISIBLE);
                    controlsLayout.setVisibility(View.GONE);
                    tapToReplyView.setVisibility(View.VISIBLE);
                    String videoPath = SplashScreen.databaseURL_video + "InternationalChatVideos/" + girlsList.get(currentVideoIndex).getUsername() + ".mp4";


                    Uri videoUri = Uri.parse(videoPath);
                    videoView.setVideoURI(videoUri);
                    // Start playing the new video
                    videoView.start();
                    getGirlProfile_DB(girlsList.get(currentVideoIndex).getUsername());
                    countDownTimer.cancel();
                }
                if (currentVideoIndex == girlsList.size() - 1) {
                    for (Girl girll : girlsList) {
                        girll.setLiked(false);
                    }
                    currentVideoIndex = 0;
                }
            }

            @Override
            public void onFinish() {
                // Timer has finished, update the UI or perform necessary actions
            }
        };

        countDownTimer.start();

    }


    private void readGirlsVideo() {


//         Read and parse the JSON file

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("VideoItems");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {

                    String username = (String) userSnapshot.child("username").getValue();
                    String censorship = (String) userSnapshot.child("censorship").getValue();
                    boolean censored = true;
                    if (censorship.equals("uncensored")) {
                        censored = false;
                    }

                    boolean seen = false;
                    boolean liked = false;

                    Girl girl = new Girl();
                    girl.setUsername(username);
                    girl.setCensored(censored);
                    girl.setSeen(seen);
                    girl.setLiked(liked);


                    girlsList.add(girl);

                }
                Collections.shuffle(girlsList);
                save_retreive_Chatbots_insharedPrefrence("save");
                filterGirlList(); // this is to remove uncensored video if app is not logged with google

                playVideoinBackground();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(SplashScreen.TAG, " userList.size(): " + databaseError.getMessage());

            }
        });


    }

    private void filterGirlList() {
        Iterator<Girl> iterator = girlsList.iterator();
        if (SplashScreen.userLoggedIn && SplashScreen.userLoggedIAs.equals("Google") && SplashScreen.App_updating.equals("inactive")) {

        } else {

            while (iterator.hasNext()) {
                Girl girl = iterator.next();
                if (!girl.isCensored()) {
                    iterator.remove(); // Removes the current girl from the list
                }
            }
        }
    }

    private boolean save_retreive_Chatbots_insharedPrefrence(String saveORretreive) {
        SharedPreferences sharedPreferences = CameraActivity.this.getSharedPreferences("Firebase_VideoItems", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (saveORretreive.equals("save")) {
            Gson gson = new Gson();
            String json = gson.toJson(girlsList);
            editor.putString("girlsList", json);
            editor.apply();
            return true;
        } else {
            // Retrieve the JSON string from SharedPreferences
            String json = sharedPreferences.getString("girlsList", null);
            // Convert the JSON string back to ArrayList
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Girl>>() {
            }.getType();


            if (json == null) {
                // Handle case when no ArrayList is saved in SharedPreferences
                return false;
            } else {
                girlsList = gson.fromJson(json, type);
                return true;
            }
        }
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getAssets().open("girls_video.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }


    private void setUpCamera(String cameraSide) {
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (cameraSide.equals("Front")) {
                    currentCameraSide = "Front";
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_FRONT) {
                        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                        previewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());
                        this.cameraId = cameraId;
                    }
                } else {
                    currentCameraSide = "Back";
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                        StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(
                                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                        previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
                        previewSize = chooseOptimalSize(streamConfigurationMap.getOutputSizes(SurfaceTexture.class), textureView.getWidth(), textureView.getHeight());
                        this.cameraId = cameraId;
                    }
                }

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void connectCamera() throws CameraAccessException {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private void closeCamera() {
        if (cameraCaptureSession != null) {
            cameraCaptureSession.close();
            cameraCaptureSession = null;
        }

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void createPreviewSession() throws CameraAccessException {
        Log.d(TAG, "createPreviewSession ");

        SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(previewSurface);
        cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                if (cameraDevice == null) {
                    return;
                }
                try {

                    CaptureRequest captureRequest = captureRequestBuilder.build();
                    CameraActivity.this.cameraCaptureSession = cameraCaptureSession;
                    cameraCaptureSession.setRepeatingRequest(captureRequest,
                            null, backgroundHandler);

                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

            }
        }, backgroundHandler);


    }


    private void controlCamera() {
        ImageView speaker = findViewById(R.id.speaker);
        speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    final Bitmap currentImage = ((BitmapDrawable) speaker.getDrawable()).getBitmap();
                    Drawable myDrawable = getResources().getDrawable(R.drawable.speaker);
                    final Bitmap speaker_on = ((BitmapDrawable) myDrawable).getBitmap();

                    if (currentImage.sameAs(speaker_on)) {
                        mediaPlayer.setVolume(0f, 0f);
                        speaker.setImageResource(R.drawable.speaker_off);
                    } else {
                        mediaPlayer.setVolume(1f, 1f);
                        speaker.setImageResource(R.drawable.speaker);

                    }
                } catch (Exception e) {

                }


            }
        });
        ImageView microphone = findViewById(R.id.microphone);
        microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Bitmap currentImage = ((BitmapDrawable) microphone.getDrawable()).getBitmap();
                Drawable myDrawable = getResources().getDrawable(R.drawable.microphone);
                final Bitmap microphone_on = ((BitmapDrawable) myDrawable).getBitmap();

                if (currentImage.sameAs(microphone_on)) {
                    microphone.setImageResource(R.drawable.microphone_off);
                } else {
                    microphone.setImageResource(R.drawable.microphone);
                }

            }
        });
        ImageView camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(100);
                closeCamera();
                if (currentCameraSide.equals("Front")) {
                    setUpCamera("Back");
                } else {
                    setUpCamera("Front");
                }
                try {
                    connectCamera();
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }

            }
        });


    }


    private void openBackgroundHandler() {
        handlerThread = new HandlerThread("camera_app");
        handlerThread.start();
        backgroundHandler = new Handler(handlerThread.getLooper());
    }

    private void closeBackgroundHandler() {
        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }
        backgroundHandler = null;
    }


    private Size chooseOptimalSize(Size[] outputSizes, int width, int height) {


        double preferredRatio = height / (double) width;
        Size currentOptimalSize = outputSizes[0];
        double currentOptimalRatio = currentOptimalSize.getWidth() / (double) currentOptimalSize.getHeight();
        for (Size currentSize : outputSizes) {
            double currentRatio = currentSize.getWidth() / (double) currentSize.getHeight();
            if (Math.abs(preferredRatio - currentRatio) <
                    Math.abs(preferredRatio - currentOptimalRatio)) {
                currentOptimalSize = currentSize;
                currentOptimalRatio = currentRatio;
            }
        }

        return currentOptimalSize;
    }

    private void dragCamera() {
        View draggableView = findViewById(R.id.draggableView);
        draggableView.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private float offsetX;
            private float offsetY;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Record the initial touch position and view position
                        startX = event.getRawX();
                        startY = event.getRawY();
                        offsetX = view.getX();
                        offsetY = view.getY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the new position based on the touch offset
                        float newX = offsetX + event.getRawX() - startX;
                        float newY = offsetY + event.getRawY() - startY;

                        // Update the view's position
                        view.setX(newX);
                        view.setY(newY);
                        return true;

                    default:
                        return false;
                }
            }
        });
    }

    private void fullscreenMode() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat windowInsetsCompat = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        windowInsetsCompat.hide(WindowInsetsCompat.Type.statusBars());
        windowInsetsCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    private void disclaimerDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_disclaimer, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);


        disclaimer_dialog = builder.create();
        disclaimer_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Toast.makeText(CameraActivity.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
                disclaimer_dialog.dismiss();

// Get SharedPreferences instance
                SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);

// Obtain the editor to modify SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();

// Save the boolean value
                boolean myBooleanValue = true;
                editor.putBoolean("disclaimerAccepted", myBooleanValue);
                editor.apply();


            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        disclaimer_dialog.getWindow().setBackgroundDrawable(inset);

        Window window = disclaimer_dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);

    }


    @Override
    public void onBackPressed() {
        finish();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (handler2 != null && handler2.hasCallbacks(runnable2)) {
                handler2.removeCallbacks(runnable2);
            }
            if (callhandler != null && callhandler.hasCallbacks(callRunnable)) {
                callhandler.removeCallbacks(callRunnable);
            }

        }


    }

    private void showAds() {
        if (SplashScreen.Ad_Network_Name.equals("admob")) {
            ADS_ADMOB.Interstitial_Ad(this);
        } else {
            com.facebook.ads.InterstitialAd facebook_IntertitialAds = null;
            ADS_FACEBOOK.interstitialAd(this, facebook_IntertitialAds, getString(R.string.Facebook_InterstitialAdUnit));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                Log.d(TAG, "onSurfaceTextureAvailable");
                playRinging();
                textureCardview.setVisibility(View.GONE);
                openBackgroundHandler();
                setUpCamera("Front");
                try {
                    connectCamera();
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                dragCamera();

            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                //when call received the camera becomes the following functions are called to resize the camera
                closeCamera();
                setUpCamera(currentCameraSide);
                try {
                    connectCamera();
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                dragCamera();
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        if (mediaPlayer != null) {
            videoView.seekTo(currentSeekPosition);
            videoView.start();

        }

        if (timeRemaining > 0) {
            setTimer(); // Resume the timer with remaining time
        }

    }

    @Override
    protected void onPause() {

        super.onPause();
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                currentSeekPosition = videoView.getCurrentPosition();
                videoView.pause();
            }
            if (countDownTimer != null) {
                countDownTimer.cancel(); // Pause the timer
            }
        } catch (Exception e) {

        }

        closeBackgroundHandler();
    }


    private void actionbar() {
        controlsLayout = findViewById(R.id.controlsLayout);

        ImageView warningSign = findViewById(R.id.warningSign);
        ImageView menuDots = findViewById(R.id.menuDots);


        warningSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blockUserDialog();
            }
        });

        menuDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportUserDialog();
            }
        });


    }


    private void blockUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_block_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);
        TextView cancel = promptView.findViewById(R.id.cancel);


        block_user_dialog = builder.create();
        block_user_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CameraActivity.this, "User blocked succesfully", Toast.LENGTH_SHORT).show();
                block_user_dialog.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                block_user_dialog.dismiss();
            }
        });

        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        block_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView report = promptView.findViewById(R.id.reportBtn);
        ImageView cross = promptView.findViewById(R.id.cross);


        report_user_dialog = builder.create();
        report_user_dialog.show();


        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
                reportUserSucessfullDialog();
            }
        });

        cross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                report_user_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_user_dialog.getWindow().setBackgroundDrawable(inset);

    }

    private void reportUserSucessfullDialog() {

        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(CameraActivity.this);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        View promptView = inflater.inflate(R.layout.dialog_report_user_sucessfull, null);
        builder.setView(promptView);
        builder.setCancelable(true);

        TextView confirm = promptView.findViewById(R.id.confirm);


        report_userSucessfully_dialog = builder.create();
        report_userSucessfully_dialog.show();


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CameraActivity.this, "User Reported", Toast.LENGTH_SHORT).show();
                report_userSucessfully_dialog.dismiss();
            }
        });


        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        report_userSucessfully_dialog.getWindow().setBackgroundDrawable(inset);

    }


}

class Girl {
    private String username;
    private boolean censored;
    private boolean seen;
    private boolean liked;

    public Girl() {
    }

    public Girl(String username, boolean censored, boolean seen, boolean liked) {
        this.username = username;
        this.censored = censored;
        this.seen = seen;
        this.liked = liked;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isCensored() {
        return censored;
    }

    public void setCensored(boolean censored) {
        this.censored = censored;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
