package com.bhola.livevideochat5.Fill_details_fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bhola.livevideochat5.R;

public class FragmentNickName extends Fragment {

    private EditText etNickname;
    private TextView btnNext;

    public FragmentNickName() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nick_name, container, false);

        etNickname = view.findViewById(R.id.nickNameEditext);
        btnNext = view.findViewById(R.id.btn_next);

        if (!Fill_details_fragment_holder.displayName.isEmpty() && Fill_details_fragment_holder.loggedAs.equals("Google")) {
            etNickname.setText(Fill_details_fragment_holder.displayName);
        }

        NextButtonGlareAnim(view);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = etNickname.getText().toString();

                if (nickname.isEmpty()) {
                    Toast.makeText(btnNext.getContext(), "Enter nick name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pass nickname to the next fragment
                Bundle bundle = new Bundle();
                bundle.putString("nickname", nickname);

                FragmentGender_BirthDay fragmentProfilePicture = new FragmentGender_BirthDay();
                fragmentProfilePicture.setArguments(bundle);

                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(
                        R.anim.slide_in_right, R.anim.slide_out_left,
                        R.anim.slide_in_left, R.anim.slide_out_right
                );
                transaction.replace(R.id.fragment_container, fragmentProfilePicture);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        return view;
    }


    private void NextButtonGlareAnim(View view ) {

        ImageView button_glare=view.findViewById(R.id.button_glare);

        Animation glareAnimation = AnimationUtils.loadAnimation(view.getContext(), R.anim.glare_animation);
        button_glare.startAnimation(glareAnimation);

    }

}
