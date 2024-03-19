package com.bhola.saxchat2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class BeforeVideoCall extends AppCompatActivity {
    ImageView group1, group2, group3;
    Handler handler1, handler2, handler3, handler4;
    int current_value, randomNumber;
    TextView onlineCountTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_video_call);

        group1 = findViewById(R.id.group1);
        group2 = findViewById(R.id.group2);
        group3 = findViewById(R.id.group3);

        Animation scaleUpAnimation1 = AnimationUtils.loadAnimation(this, R.anim.image_scaleup);
        Animation scaleUpAnimation2 = AnimationUtils.loadAnimation(this, R.anim.image_scaleup);
        Animation scaleUpAnimation3 = AnimationUtils.loadAnimation(this, R.anim.image_scaleup);

        Animation scaleDownAnimation = AnimationUtils.loadAnimation(this, R.anim.image_scaledown);
        group1.startAnimation(scaleDownAnimation);
        group2.startAnimation(scaleDownAnimation);
        group3.startAnimation(scaleDownAnimation);


        handler1 = new Handler();
        handler2 = new Handler();
        handler3 = new Handler();
        handler4 = new Handler();

        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                group1.startAnimation(scaleUpAnimation1);
            }
        }, 1000);

        handler2.postDelayed(new Runnable() {
            @Override
            public void run() {
                group2.startAnimation(scaleUpAnimation2);
            }
        }, 2500);

        handler3.postDelayed(new Runnable() {
            @Override
            public void run() {
                group3.startAnimation(scaleUpAnimation3);

            }
        }, 4000);


        // Inside the onCreate() method or any other appropriate location in Activity B
        Intent intent = getIntent();
        String receivedData = intent.getStringExtra("count");

        onlineCountTextview = findViewById(R.id.onlineCount);
        randomNumber = Integer.parseInt(receivedData);
        current_value = 0;
        incrementValueSlowly();

        scaleUpAnimation3.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(BeforeVideoCall.this, CameraActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void incrementValueSlowly() {

        handler4.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (current_value < randomNumber) {
                    current_value = current_value + 10;
                    onlineCountTextview.setText(String.valueOf(current_value));
                    incrementValueSlowly();
                }


            }
        }, 2); // Delay of 50 milliseconds between each increment


    }


}