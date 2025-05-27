package com.example.personafi;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MissionManager {
    private static MissionManager instance;
    private Mission currentDailyMission;
    private Mission currentWeeklyMission;
    private List<Mission> dailyMissions;
    private List<Mission> weeklyMissions;
    private Random random;
    private Context context;
    private static final String PREFS_NAME = "MissionPrefs";
    private static final String KEY_DAILY_MISSION_TITLE = "daily_mission_title";
    private static final String KEY_DAILY_MISSION_PROGRESS = "daily_mission_progress";
    private static final String KEY_DAILY_MISSION_LAST_UPDATED = "daily_mission_last_updated";
    private static final String KEY_WEEKLY_MISSION_TITLE = "weekly_mission_title";
    private static final String KEY_WEEKLY_MISSION_PROGRESS = "weekly_mission_progress";
    private static final String KEY_WEEKLY_MISSION_LAST_UPDATED = "weekly_mission_last_updated";
    private static final String KEY_FIRST_RUN = "first_run";

    // Interface for mission progress updates
    public interface OnMissionProgressUpdateListener {
        void onMissionProgressUpdated();
    }
    private List<OnMissionProgressUpdateListener> progressListeners = new ArrayList<>();

    public void addProgressListener(OnMissionProgressUpdateListener listener) {
        if (!progressListeners.contains(listener)) {
            progressListeners.add(listener);
        }
    }

    public void removeProgressListener(OnMissionProgressUpdateListener listener) {
        progressListeners.remove(listener);
    }

    private void notifyProgressListeners() {
        for (OnMissionProgressUpdateListener listener : progressListeners) {
            listener.onMissionProgressUpdated();
        }
    }

    private MissionManager(Context context) {
        this.context = context.getApplicationContext();
        random = new Random();
        initializeMissions();
        loadCurrentMissions();
    }

    public static MissionManager initialize(Context context) {
        if (instance == null) {
            instance = new MissionManager(context);
        }
        return instance;
    }

    public static MissionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MissionManager must be initialized with a Context first");
        }
        return instance;
    }

    private void initializeMissions() {
        // Initialize daily missions
        dailyMissions = new ArrayList<>();
        dailyMissions.add(new Mission("Quick Save", "Save at least 20 pesos", 50, 20, true));
        dailyMissions.add(new Mission("Goal Contributor", "Add any amount in each Saving Goal", 75, 1, true));
        dailyMissions.add(new Mission("Growth Spurt", "Increase your savings by 20 pesos compared to the previous amount", 100, 10, true));
        dailyMissions.add(new Mission("Big Saver", "Save at least 30 pesos before the day ends", 150, 30, true));
        dailyMissions.add(new Mission("Multi-Goal", "Add any amount to at least 2 different savings goals", 100, 2, true));
        dailyMissions.add(new Mission("Progress Maker", "Add a higher amount than previous amount to any savings goal", 75, 1, true));
        dailyMissions.add(new Mission("Perfect Save", "Save exactly 25 pesos today", 200, 25, true));
        dailyMissions.add(new Mission("Early Bird", "Add any amount to your savings before noon", 50, 1, true));

        // Initialize weekly missions
        weeklyMissions = new ArrayList<>();
        weeklyMissions.add(new Mission("Friday Bonus", "Add an extra 20 pesos to your savings every Friday", 300, 20, false));
        weeklyMissions.add(new Mission("Weekly Goal", "Put 100 pesos towards your savings goal by the end of the week", 500, 100, false));
        weeklyMissions.add(new Mission("Weekly Growth", "Increase your weekly savings by 10 pesos compared to last week", 400, 10, false));
        weeklyMissions.add(new Mission("Wednesday Special", "Every Wednesday, transfer 30 pesos to one of your saving goals", 350, 30, false));
    }

    private void loadCurrentMissions() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Check if it's first run
        boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);
        if (isFirstRun) {
            prefs.edit().putBoolean(KEY_FIRST_RUN, false).apply();
        }
        
        // Load daily mission
        String dailyMissionTitle = prefs.getString(KEY_DAILY_MISSION_TITLE, null);
        int dailyMissionProgress = prefs.getInt(KEY_DAILY_MISSION_PROGRESS, 0);
        long dailyMissionLastUpdated = prefs.getLong(KEY_DAILY_MISSION_LAST_UPDATED, 0);
        
        // Load weekly mission
        String weeklyMissionTitle = prefs.getString(KEY_WEEKLY_MISSION_TITLE, null);
        int weeklyMissionProgress = prefs.getInt(KEY_WEEKLY_MISSION_PROGRESS, 0);
        long weeklyMissionLastUpdated = prefs.getLong(KEY_WEEKLY_MISSION_LAST_UPDATED, 0);

        // Check if we need to select new missions
        long currentTime = System.currentTimeMillis();
        boolean needNewDaily = dailyMissionTitle == null || 
                             (currentTime - dailyMissionLastUpdated >= 24 * 60 * 60 * 1000);
        boolean needNewWeekly = weeklyMissionTitle == null || 
                               (currentTime - weeklyMissionLastUpdated >= 7 * 24 * 60 * 60 * 1000);

        if (needNewDaily) {
            selectNewDailyMission(isFirstRun);
        } else {
            // Find and restore the saved daily mission
            for (Mission mission : dailyMissions) {
                if (mission.getTitle().equals(dailyMissionTitle)) {
                    currentDailyMission = mission;
                    currentDailyMission.setCurrentProgress(dailyMissionProgress);
                    currentDailyMission.setLastUpdated(dailyMissionLastUpdated);
                    break;
                }
            }
            if (currentDailyMission == null) {
                selectNewDailyMission(isFirstRun);
            }
        }

        if (needNewWeekly) {
            selectNewWeeklyMission();
        } else {
            // Find and restore the saved weekly mission
            for (Mission mission : weeklyMissions) {
                if (mission.getTitle().equals(weeklyMissionTitle)) {
                    currentWeeklyMission = mission;
                    currentWeeklyMission.setCurrentProgress(weeklyMissionProgress);
                    currentWeeklyMission.setLastUpdated(weeklyMissionLastUpdated);
                    break;
                }
            }
            if (currentWeeklyMission == null) {
                selectNewWeeklyMission();
            }
        }
    }

    private void saveMissionState() {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        
        if (currentDailyMission != null) {
            editor.putString(KEY_DAILY_MISSION_TITLE, currentDailyMission.getTitle());
            editor.putInt(KEY_DAILY_MISSION_PROGRESS, currentDailyMission.getCurrentProgress());
            editor.putLong(KEY_DAILY_MISSION_LAST_UPDATED, currentDailyMission.getLastUpdated());
        }
        
        if (currentWeeklyMission != null) {
            editor.putString(KEY_WEEKLY_MISSION_TITLE, currentWeeklyMission.getTitle());
            editor.putInt(KEY_WEEKLY_MISSION_PROGRESS, currentWeeklyMission.getCurrentProgress());
            editor.putLong(KEY_WEEKLY_MISSION_LAST_UPDATED, currentWeeklyMission.getLastUpdated());
        }
        
        editor.apply();
    }

    public void selectNewDailyMission() {
        selectNewDailyMission(false);
    }

    private void selectNewDailyMission(boolean isFirstRun) {
        if (!dailyMissions.isEmpty()) {
            List<Mission> availableMissions = new ArrayList<>(dailyMissions);
            
            // Remove specific missions on first run
            if (isFirstRun) {
                availableMissions.removeIf(mission -> 
                    mission.getTitle().equals("Progress Maker") || 
                    mission.getTitle().equals("Growth Spurt"));
            }
            
            if (!availableMissions.isEmpty()) {
                currentDailyMission = availableMissions.get(random.nextInt(availableMissions.size()));
                currentDailyMission.setCurrentProgress(0);
                currentDailyMission.setLastUpdated(System.currentTimeMillis());
                saveMissionState();
            }
        }
    }

    public void selectNewWeeklyMission() {
        if (!weeklyMissions.isEmpty()) {
            currentWeeklyMission = weeklyMissions.get(random.nextInt(weeklyMissions.size()));
            currentWeeklyMission.setCurrentProgress(0);
            currentWeeklyMission.setLastUpdated(System.currentTimeMillis());
            saveMissionState();
        }
    }

    public Mission getCurrentDailyMission() {
        return currentDailyMission;
    }

    public Mission getCurrentWeeklyMission() {
        return currentWeeklyMission;
    }

    public void updateMissionProgress(boolean isDaily, int progress) {
        Mission mission = isDaily ? currentDailyMission : currentWeeklyMission;
        if (mission != null && !mission.isCompleted()) {
            mission.setCurrentProgress(mission.getCurrentProgress() + progress);
            saveMissionState();
            // Notify listeners of progress update
            notifyProgressListeners();
            
            if (mission.isCompleted()) {
                // Award XP to user
                ProfileFragment profileFragment = ProfileFragment.getInstance();
                if (profileFragment != null) {
                    profileFragment.addXp(mission.getXpReward());
                    // Show completion toast
                    if (context != null) {
                        String message = String.format("Mission Complete! +%d XP", mission.getXpReward());
                        CustomToast.showSuccess(context, message);
                    }
                }
            }
        }
    }

    public void checkAndUpdateMissions() {
        long currentTime = System.currentTimeMillis();
        
        // Check if daily mission needs to be updated (24 hours)
        if (currentDailyMission != null && 
            currentTime - currentDailyMission.getLastUpdated() >= 24 * 60 * 60 * 1000) {
            selectNewDailyMission();
        }

        // Check if weekly mission needs to be updated (7 days)
        if (currentWeeklyMission != null && 
            currentTime - currentWeeklyMission.getLastUpdated() >= 7 * 24 * 60 * 60 * 1000) {
            selectNewWeeklyMission();
        }
    }
} 