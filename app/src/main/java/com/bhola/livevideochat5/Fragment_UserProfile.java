package com.bhola.livevideochat5;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

public class Fragment_UserProfile extends Fragment {


    ImageView profileImage;
    TextView name, coins, id;
    LinearLayout logout;
    View view;
    Context context;


    public Fragment_UserProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_user__profile, container, false);

        context = getContext();


        setProfileDetails();


        RelativeLayout memberShip = view.findViewById(R.id.memberShip);
        memberShip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, VipMembership.class));
            }
        });

        oprnPrivacy_Terms(view, context);

        LinearLayout about = view.findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, About.class));
            }
        });

        LinearLayout becomeVIP = view.findViewById(R.id.becomeVIP);
        becomeVIP.setOnClickListener(view -> {
            view.getContext().startActivity(new Intent(view.getContext(), PremiumMembership.class));
        });

        LinearLayout customerSupport = view.findViewById(R.id.customerSupport);
        customerSupport.setOnClickListener(v -> {
            startActivity(new Intent(context, CustomerCare.class));
        });

        profileEdit();

//        notificationBar();


        return view;
    }

    private void setProfileDetails() {
        SharedPreferences sh = context.getSharedPreferences("UserInfo", MODE_PRIVATE);

        profileImage = view.findViewById(R.id.profileUrl);
        name = view.findViewById(R.id.profileName);
        coins = view.findViewById(R.id.coins);
        id = view.findViewById(R.id.id);
        int userId = sh.getInt("userId", 0);

        id.setText(String.valueOf(userId));
        coins.setText(String.valueOf("Coins: " + MyApplication.coins));
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoogleSignInOptions gso;
                GoogleSignInClient gsc;
                if (MyApplication.userLoggedIAs.equals("Google")) {
                    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
                    gsc = GoogleSignIn.getClient(context, gso);
                    gsc.signOut().addOnCompleteListener(new com.google.android.gms.tasks.OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                            FirebaseAuth.getInstance().signOut();
                            clearUserInfo();
                        }
                    });

                } else {
                    clearUserInfo();
                }


                Toast.makeText(context, "Logged Out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(context, LoginScreen.class));
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        String Birthday = sh.getString("Birthday", "");
        int age = 0;
        try {
            age = new Utils().calculateAge(Birthday);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        TextView location = view.findViewById(R.id.location);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                location.setText(MyApplication.currentCity);
            }
        }, 2000);


        if (MyApplication.userLoggedIn) {

            String fullname = sh.getString("nickName", "not set");
            name.setText(fullname + ", " + String.valueOf(age));


            Picasso.get().load(MyApplication.userModel.getProfilepic()).into(profileImage);

        }


    }


    private void notificationBar() {
//        CardView notificationCard = view.findViewById(R.id.notificationCard);
//        if (Fragment_Messenger.count == 0) {
//            notificationCard.setVisibility(View.INVISIBLE);
//        } else {
//            notificationCard.setVisibility(View.VISIBLE);
//
//        }
//        ImageView bellIcon = view.findViewById(R.id.bellIcon);
//        ImageView crossIcon = view.findViewById(R.id.crossIcon);
//        TextView notification_message = view.findViewById(R.id.notification_message);
//
//        Animation scaleAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_up_down);
//        bellIcon.startAnimation(scaleAnimation);
//
//        crossIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                notificationCard.setVisibility(View.INVISIBLE);
//            }
//        });
//
//        notification_message.setText(String.valueOf(Fragment_Messenger.count) + " new messages,click to read!");
//        notification_message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity.viewPager2.setCurrentItem(2); // Switch to Fragment B
//
//            }
//        });
    }

    private void profileEdit() {
        ImageView edit = view.findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, UserProfileEdit.class));
            }
        });
    }

    private void oprnPrivacy_Terms(View view, Context context) {
        LinearLayout terms = view.findViewById(R.id.terms);
        terms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, Terms_Conditions.class));
            }
        });

        LinearLayout privacy = view.findViewById(R.id.privacy);
        privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, PrivacyPolicy.class));
            }
        });
    }

    private void clearUserInfo() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

// Clear the SharedPreferences
        editor.clear();
        editor.apply();

    }

}