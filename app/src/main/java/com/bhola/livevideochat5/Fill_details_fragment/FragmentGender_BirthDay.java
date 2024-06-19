package com.bhola.livevideochat5.Fill_details_fragment;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.support.annotation.Nullable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bhola.livevideochat5.R;
import com.bhola.livevideochat5.Utils;
import com.google.android.material.card.MaterialCardView;

public class FragmentGender_BirthDay extends Fragment {

    TextView btnNext;
    String nickname;
    String selectedGender = "";
    LinearLayout maleLayout, femaleLayout;
    String Birthday = "";

    public FragmentGender_BirthDay() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the nickname from the bundle
        if (getArguments() != null) {
            nickname = getArguments().getString("nickname");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        selectedGender="";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_gender__birth_day, container, false);

        btnNext = view.findViewById(R.id.btn_next);
        maleLayout = view.findViewById(R.id.maleLayout);
        femaleLayout = view.findViewById(R.id.femaleLayout);

        maleLayout.setOnClickListener(v -> {
            if (selectedGender.equals("male")) return;
            int themeColor = ContextCompat.getColor(v.getContext(), R.color.themeColorlight); // Replace R.color.your_color with your color resource
            int color = Color.parseColor("#71507F");


            maleLayout.setBackgroundColor(themeColor);
            femaleLayout.setBackgroundColor(color);

            selectedGender = "male";
        });

        femaleLayout.setOnClickListener(v -> {
            if (selectedGender.equals("female")) return;
            int themeColor = ContextCompat.getColor(v.getContext(), R.color.themeColorlight); // Replace R.color.your_color with your color resource
            int color = Color.parseColor("#71507F");

            maleLayout.setBackgroundColor(color);
            femaleLayout.setBackgroundColor(themeColor);

            selectedGender = "female";

        });

        MaterialCardView selectDate = view.findViewById(R.id.selectDate);
        TextView dateOfBirth = view.findViewById(R.id.dateOfBirth);

        selectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContextThemeWrapper themedContext = new ContextThemeWrapper(view.getContext(), R.style.DatePickerDialogTheme);
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
                            }
                        },
                        2023, 0, 1  // Year, Month (0-indexed), Day
                );
                datePickerDialog.show();
            }
        });


        btnNext.setOnClickListener(v -> {
            if (selectedGender.isEmpty()) {
                Toast.makeText(btnNext.getContext(), "Select gender", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Birthday.isEmpty()) {
                Toast.makeText(btnNext.getContext(), "Enter birthday", Toast.LENGTH_SHORT).show();
                return;
            }
            int age = new Utils().calculateAge(Birthday);
            if (age < 18) {
                Toast.makeText(v.getContext(), "Under 18 not allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pass data to next fragment
            Bundle bundle = new Bundle();
            bundle.putString("nickname", nickname);
            bundle.putString("selectedGender", selectedGender);
            bundle.putString("Birthday", Birthday);


            Fragment_addprofile_image fragmentProfilePicture = new Fragment_addprofile_image();
            fragmentProfilePicture.setArguments(bundle);

            // Perform fragment transaction with animation
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(
                    R.anim.slide_in_right, R.anim.slide_out_left,
                    R.anim.slide_in_left, R.anim.slide_out_right
            );
            transaction.replace(R.id.fragment_container, fragmentProfilePicture);
            transaction.addToBackStack(null);
            transaction.commit();

        });

        NextButtonGlareAnim(view);
        return view;

    }

    private void NextButtonGlareAnim(View view) {

        ImageView button_glare = view.findViewById(R.id.button_glare);

        Animation glareAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.glare_animation);
        button_glare.startAnimation(glareAnimation);

    }

}