package com.bhola.saxchat2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

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

import com.bhola.saxchat2.Models.FlipItem;

import java.util.ArrayList;
import java.util.List;

public class PremiumMembership extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private RecyclerView bottomRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_membership);
        viewFlipper=findViewById(R.id.viewFlipper);
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
        FlipItem flipItem=items.get(position);

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