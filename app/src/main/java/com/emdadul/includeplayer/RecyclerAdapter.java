package com.emdadul.includeplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.myViewHolder> {

    Context context;
    List<String> videoId;
    OnItemClickListener listener;
    private int currentPosition = -1; // Track currently playing video

    public RecyclerAdapter(List<String> videoId, Context context, OnItemClickListener listener) {
        this.videoId = videoId;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item, parent, false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, @SuppressLint("RecyclerView") int position) {
        String getVideoId = videoId.get(position);
        String thumb = "https://img.youtube.com/vi/" + getVideoId + "/hqdefault.jpg";
        Glide.with(context).load(thumb).into(holder.imageView);

        // Highlight currently playing video
        if (position == currentPosition) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.colorAccent));
            // Add a play icon overlay if desired
            // holder.playIcon.setVisibility(View.VISIBLE);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.cardview_light_background));
            // holder.playIcon.setVisibility(View.GONE);
        }

        // Use the listener interface instead of direct access
        holder.cardView.setOnClickListener(v -> listener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return videoId.size();
    }

    // Method to update the currently playing video position
    public void updateCurrentPosition(int position) {
        int previousPosition = currentPosition;
        currentPosition = position;

        // Update previous and new positions
        if (previousPosition != -1) {
            notifyItemChanged(previousPosition);
        }
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public static class myViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imageView;
        CardView cardView;
        // Add play icon if desired: AppCompatImageView playIcon;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            cardView = itemView.findViewById(R.id.cardView);
            // playIcon = itemView.findViewById(R.id.play_icon); // If you add a play icon
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
