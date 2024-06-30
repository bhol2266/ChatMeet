package com.bhola.livevideochat5;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView versionNameText = findViewById(R.id.versionName);
        // Get the current version name and version code
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            versionNameText.setText("v" + String.valueOf(versionCode) + ".0");


        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ImageView back_arrow = findViewById(R.id.back_arrow);
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        TextView bttomTextview = findViewById(R.id.bttomTextview);
        bttomTextview.setText("Copyright @" + String.valueOf(currentYear));

        TextView email = findViewById(R.id.email);
        email.setOnClickListener(v -> {
            String recipient = "ukdevelopers007@gmail.com";
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto",recipient, null));
            intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
            intent.putExtra(Intent.EXTRA_TEXT, "message");
            startActivity(Intent.createChooser(intent, "Choose an Email client :"));
        });



    }
}