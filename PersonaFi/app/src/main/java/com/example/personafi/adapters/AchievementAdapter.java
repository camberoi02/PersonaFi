package com.example.personafi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personafi.R;
import com.example.personafi.models.Achievement;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private List<Achievement> achievements = new ArrayList<>();

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.icon.setImageResource(achievement.getIconResId());
        holder.text.setText(achievement.getTitle());
        
        // Set alpha based on locked state
        if (!achievement.isUnlocked()) {
            holder.icon.setAlpha(0.5f);
            holder.text.setAlpha(0.5f);
        } else {
            holder.icon.setAlpha(1.0f);
            holder.text.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
        notifyDataSetChanged();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        ImageView icon;
        TextView text;

        AchievementViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            icon = itemView.findViewById(R.id.achievement_icon);
            text = itemView.findViewById(R.id.achievement_text);
        }
    }
} 