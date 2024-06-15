package com.bhola.livevideochat5;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class PagerAdapter extends FragmentStateAdapter {
    public PagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Fragment_Trending();
            case 1:
                return new Fragment_HomePage();
            case 2:
                return new Fragment_Messenger();
            default:
                return new Fragment_UserProfile();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
