package com.example.personafi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personafi.R;
import com.example.personafi.models.Mission;
import java.util.ArrayList;
import java.util.List;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {
    private List<Mission> missions = new ArrayList<>();

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        Mission mission = missions.get(position);
        holder.title.setText(mission.getTitle());
        holder.points.setText(mission.getPoints() + " points");
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
        notifyDataSetChanged();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView points;

        MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mission_title);
            points = itemView.findViewById(R.id.mission_points);
        }
    }
} 