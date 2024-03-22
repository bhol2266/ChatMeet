package com.bhola.saxchat2;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bhola.saxchat2.Models.FlipItem;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class PremiumMembership extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private RecyclerView bottomRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_membership);
        viewFlipper = findViewById(R.id.viewFlipper);
        bottomRecyclerView = findViewById(R.id.bottomRecyclerView);

        List<FlipItem> flipItems = new ArrayList<>();

        // Assume you have Drawable objects for your images
        Drawable mm_chat = ContextCompat.getDrawable(this, R.drawable.mm_chat);
        Drawable mm_hello = ContextCompat.getDrawable(this, R.drawable.mm_hello);
        Drawable mm_crown = ContextCompat.getDrawable(this, R.drawable.mm_crown);
        Drawable mm_hair = ContextCompat.getDrawable(this, R.drawable.mm_hair);
        Drawable mm_diamond = ContextCompat.getDrawable(this, R.drawable.mm_diamond);


        flipItems.add(new FlipItem("Unlimited messages", mm_chat));
        flipItems.add(new FlipItem("VIP Greetings", mm_hello));
        flipItems.add(new FlipItem("VIP Badge", mm_crown));
        flipItems.add(new FlipItem("Fresh Girl", mm_hair));
        flipItems.add(new FlipItem("Monthly Coins", mm_diamond));


        FlipItemAdapter adapter = new FlipItemAdapter(flipItems);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        bottomRecyclerView.setAdapter(adapter);


//        viewFlipper.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
//
//            @Override
//            public void onPageSelected(int position) {
//                adapter.setSelectedItem(position);
//                LinearLayoutManager layoutManager = (LinearLayoutManager) bottomRecyclerView.getLayoutManager();
//                layoutManager.scrollToPositionWithOffset(position, 0);
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {}
//        });
//        fullscreenMode();

        tabBtns();
    }

    private void tabBtns() {

        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new PagerAdapter(PremiumMembership.this));

        TabLayout tabLayout = findViewById(R.id.tabLayout);


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {

                    case 0:
                        tab.setIcon(R.drawable.mm_chat);
                        View view5 = getLayoutInflater().inflate(R.layout.item_flip, null);
                        TextView textView = view5.findViewById(R.id.text);
                        textView.setText("Unlimited messages");
                        tab.setCustomView(view5);

                        //By default tab 0 will be selected to change the tint of that tab
//                        View tabView = tab.getCustomView();
//                        ImageView tabIcon = tabView.findViewById(R.id.icon);
//                        tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(PremiumMembership.this, R.color.themeColor));

                        break;
                    case 1:
                        tab.setIcon(R.drawable.videocall2);

                        View view1 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view1.findViewById(R.id.icon).setBackgroundResource(R.drawable.videocall2);
                        tab.setCustomView(view1);

                        break;
                    case 2:
                        tab.setIcon(R.drawable.chat);


                        View view2 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view2.findViewById(R.id.icon).setBackgroundResource(R.drawable.chat);
                        tab.setCustomView(view2);

                        break;


                    case 3:
                        tab.setIcon(R.drawable.info_2);


                        View view3 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view3.findViewById(R.id.icon).setBackgroundResource(R.drawable.info_2);
                        tab.setCustomView(view3);
                        break;


                    default:
                        tab.setIcon(R.drawable.user2);
                        View view4 = getLayoutInflater().inflate(R.layout.customtab, null);
                        view4.findViewById(R.id.icon).setBackgroundResource(R.drawable.user2);
                        tab.setCustomView(view4);
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Get the custom view of the selected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the selected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(PremiumMembership.this, R.color.themeColor));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Get the custom view of the unselected tab
                View tabView = tab.getCustomView();
                if (tabView != null) {
                    // Find the ImageView in the custom view
                    ImageView tabIcon = tabView.findViewById(R.id.icon);

                    // Set the background tint color for the unselected tab
                    tabIcon.setBackgroundTintList(ContextCompat.getColorStateList(PremiumMembership.this, com.google.android.ads.mediationtestsuite.R.color.gmts_light_gray));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Tab reselected, no action needed
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

}

class FlipItemAdapter extends RecyclerView.Adapter<FlipItemAdapter.FlipItemViewHolder> {

    private List<FlipItem> items;
    private int selectedItem = -1;

    public FlipItemAdapter(List<FlipItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public FlipItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flip, parent, false);
        return new FlipItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlipItemViewHolder holder, int position) {
        FlipItem flipItem = items.get(position);

        holder.text.setText(flipItem.getItemName());
        holder.image.setImageDrawable(flipItem.getItemImage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setSelectedItem(int position) {
        selectedItem = position;
        notifyDataSetChanged();
    }

    static class FlipItemViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        LinearLayout parentLayout;
        CardView imageCardView;
        ImageView image;


        public FlipItemViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
            parentLayout = itemView.findViewById(R.id.parentLayout);
            imageCardView = itemView.findViewById(R.id.imageCardView);
            image = itemView.findViewById(R.id.image);
        }
    }
}