package com.example.personafi;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TutorialPagerAdapter extends RecyclerView.Adapter<TutorialPagerAdapter.TutorialViewHolder> {
    private List<TutorialPage> pages;

    public TutorialPagerAdapter(List<TutorialPage> pages) {
        this.pages = pages;
    }

    @NonNull
    @Override
    public TutorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tutorial_page, parent, false);
        return new TutorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TutorialViewHolder holder, int position) {
        TutorialPage page = pages.get(position);
        holder.imageView.setImageResource(page.getImageResId());
        holder.titleView.setText(page.getTitle());
        holder.descriptionView.setText(page.getDescription());
    }

    @Override
    public int getItemCount() {
        return pages.size();
    }

    static class TutorialViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView descriptionView;

        TutorialViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tutorialImage);
            titleView = itemView.findViewById(R.id.tutorialTitle);
            descriptionView = itemView.findViewById(R.id.tutorialDescription);
        }
    }
} 