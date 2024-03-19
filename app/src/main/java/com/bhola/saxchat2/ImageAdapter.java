package com.bhola.saxchat2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder> {
    private Context context;
    private List<String> imageList;

    public ImageAdapter(Context context, List<String> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // Get the current image resource ID
        int positionInList = position % imageList.size();
//        int imageResId = imageList.get(positionInList);

        // Calculate the pixel values from dp
        float dpWidth = 80f; // Replace with your desired width in dp
        float dpHeight = 80f; // Replace with your desired height in dp

        float density = context.getResources().getDisplayMetrics().density;
        int widthInPixels = (int) (dpWidth * density);
        int heightInPixels = (int) (dpHeight * density);

        Picasso.get().load(imageList.get(positionInList)).resize(widthInPixels, heightInPixels)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }
}


class ImageViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
    }
}


