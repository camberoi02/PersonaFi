package com.example.personafi;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.ViewHolder> {

    private List<Achievement> allAchievements;
    private List<Achievement> filteredAchievements;
    private FragmentActivity activity;
    private FilterType currentFilter = FilterType.ALL;

    public enum FilterType {
        ALL,
        COMPLETED,
        IN_PROGRESS
    }

    public AchievementAdapter(List<Achievement> achievements, FragmentActivity activity) {
        this.allAchievements = new ArrayList<>(achievements);
        this.filteredAchievements = new ArrayList<>(achievements);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_achievement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Achievement achievement = filteredAchievements.get(position);
        holder.textViewAchievementName.setText(achievement.getName().toUpperCase());
        holder.textViewAchievementDescription.setText(achievement.getDescription());
        
        // Set up visual differences based on unlock status
        if (achievement.isUnlocked()) {
            // Unlocked state
            holder.itemView.setAlpha(1.0f);
            holder.imageViewAchievementIcon.setAlpha(1.0f);
            holder.textViewAchievementStatus.setText("COMPLETED");
            holder.textViewAchievementStatus.setTextColor(holder.itemView.getResources().getColor(R.color.primary, null));
            holder.textViewAchievementStatus.setBackgroundColor(holder.itemView.getResources().getColor(R.color.primary_container, null));
            holder.imageViewLockIcon.setVisibility(View.GONE);
            holder.imageViewAchievementIcon.setImageResource(achievement.getIconResourceId());
            holder.imageViewAchievementIcon.setColorFilter(holder.itemView.getResources().getColor(R.color.primary, null));
        } else {
            // Locked state
            holder.itemView.setAlpha(0.9f);
            holder.imageViewAchievementIcon.setAlpha(0.7f);
            holder.textViewAchievementStatus.setText("IN PROGRESS");
            holder.textViewAchievementStatus.setTextColor(holder.itemView.getResources().getColor(R.color.outline, null));
            holder.textViewAchievementStatus.setBackgroundColor(holder.itemView.getResources().getColor(R.color.surface_variant, null));
            holder.imageViewLockIcon.setVisibility(View.VISIBLE);
            holder.imageViewAchievementIcon.setImageResource(achievement.getIconResourceId());
            holder.imageViewAchievementIcon.setColorFilter(holder.itemView.getResources().getColor(R.color.on_surface, null));
        }

        // Set click listener to show enlarged achievement or locked message
        holder.itemView.setOnClickListener(v -> {
            if (achievement.isUnlocked()) {
                AchievementPopupDialog.newInstance(achievement.getIconResourceId())
                    .show(activity.getSupportFragmentManager(), "achievement_popup");
            } else {
                Toast.makeText(activity, 
                    "This achievement is still locked. Keep working to unlock it!", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredAchievements.size();
    }

    public void updateAchievements(List<Achievement> newAchievements) {
        this.allAchievements.clear();
        this.allAchievements.addAll(newAchievements);
        filterAchievements(currentFilter);
    }

    public void setFilter(FilterType filter) {
        currentFilter = filter;
        filterAchievements(filter);
    }

    private void filterAchievements(FilterType filter) {
        filteredAchievements.clear();
        switch (filter) {
            case COMPLETED:
                for (Achievement achievement : allAchievements) {
                    if (achievement.isUnlocked()) {
                        filteredAchievements.add(achievement);
                    }
                }
                break;
            case IN_PROGRESS:
                for (Achievement achievement : allAchievements) {
                    if (!achievement.isUnlocked()) {
                        filteredAchievements.add(achievement);
                    }
                }
                break;
            case ALL:
            default:
                filteredAchievements.addAll(allAchievements);
                break;
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAchievementName;
        TextView textViewAchievementDescription;
        TextView textViewAchievementStatus;
        ImageView imageViewAchievementIcon;
        ImageView imageViewLockIcon;

        ViewHolder(View itemView) {
            super(itemView);
            textViewAchievementName = itemView.findViewById(R.id.textViewAchievementName);
            textViewAchievementDescription = itemView.findViewById(R.id.textViewAchievementDescription);
            textViewAchievementStatus = itemView.findViewById(R.id.textViewAchievementStatus);
            imageViewAchievementIcon = itemView.findViewById(R.id.imageViewAchievementIcon);
            imageViewLockIcon = itemView.findViewById(R.id.imageViewLockIcon);
        }
    }
} 