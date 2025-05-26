package com.example.personafi;

import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import android.content.Context;

public class AchievementManager {
    private List<Achievement> achievements;
    private Context context;

    public static final String ACHIEVEMENT_FIRST_GOAL_CREATED = "First Personal Goal Created!";
    public static final String ACHIEVEMENT_GOAL_GETTER = "Goal Getter!";
    public static final String ACHIEVEMENT_JUNIOR_SAVER = "Junior Saver!";
    public static final String ACHIEVEMENT_SAVER_PRO = "Saver Pro!";

    public static final String ACHIEVEMENT_SAVED_1000 = "Thrifty Thousand!";
    public static final String ACHIEVEMENT_SAVED_5000 = "Fantastic Five K!";
    public static final String ACHIEVEMENT_HALF_WAY_THERE = "Halfway Hero!";
    public static final String ACHIEVEMENT_GOAL_MASTER = "Goal Master!";
    public static final String ACHIEVEMENT_7_DAY_STREAK = "Weekly Saver Streak!";
    public static final String ACHIEVEMENT_1_MONTH_STREAK = "Monthly Saver Champion!";
    public static final String ACHIEVEMENT_10_DAILY_MISSIONS = "Daily Mission Pro!";
    public static final String ACHIEVEMENT_5_WEEKLY_MISSIONS = "Weekly Mission Conqueror!";

    public AchievementManager(Context context) {
        this.context = context.getApplicationContext();
        achievements = new ArrayList<>();
        // Define achievements with their descriptions and icons
        achievements.add(new Achievement(ACHIEVEMENT_FIRST_GOAL_CREATED, "Created your first personal savings goal.", R.drawable.achievement3));
        achievements.add(new Achievement(ACHIEVEMENT_SAVED_1000, "Saved your first 1000 pesos.", R.drawable.achievement));
        achievements.add(new Achievement(ACHIEVEMENT_SAVED_5000, "Saved your first 5000 pesos.", R.drawable.achievement1));
        achievements.add(new Achievement(ACHIEVEMENT_HALF_WAY_THERE, "1st to reach 50% savings goal.", R.drawable.achievement2));
        achievements.add(new Achievement(ACHIEVEMENT_7_DAY_STREAK, "7 Days saving streak.", R.drawable.achievement4));
        achievements.add(new Achievement(ACHIEVEMENT_1_MONTH_STREAK, "1 Month saving streak.", R.drawable.achievement5));
        achievements.add(new Achievement(ACHIEVEMENT_10_DAILY_MISSIONS, "Completed 10 Daily Missions.", R.drawable.achievement6));
        achievements.add(new Achievement(ACHIEVEMENT_5_WEEKLY_MISSIONS, "Completed 5 Weekly Missions.", R.drawable.achievement7));
        achievements.add(new Achievement(ACHIEVEMENT_GOAL_MASTER, "1st to complete/finish your personal savings goal.", R.drawable.achievement8));
        
        // Note: The original ACHIEVEMENT_SAVER_PRO (Save 1000 pesos in total) is now covered by ACHIEVEMENT_SAVED_1000.
        // If it was intended to be a separate achievement, its definition or criteria might need adjustment.
        // For now, it's effectively replaced/updated by the new list.
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public boolean unlockAchievement(String achievementName) {
        for (Achievement achievement : achievements) {
            if (achievement.getName().equals(achievementName)) {
                if (!achievement.isUnlocked()) {
                    achievement.setUnlocked(true);
                    return true; // Achievement was newly unlocked
                }
                return false; // Achievement was already unlocked
            }
        }
        return false; // Achievement not found
    }

    public Achievement getAchievementByName(String name) {
        for (Achievement achievement : achievements) {
            if (achievement.getName().equals(name)) {
                return achievement;
            }
        }
        return null;
    }

    // Renamed for clarity and to match new constant
    public void checkFirstGoalCreatedAchievement(List<SavingsGoal> goals) {
        if (!goals.isEmpty()) {
            unlockAchievement(ACHIEVEMENT_FIRST_GOAL_CREATED);
        }
    }

    // Checks if ANY goal has been achieved for the first time.
    public void checkFirstGoalCompletedAchievement(List<SavingsGoal> goals) {
        for (SavingsGoal goal : goals) {
            if (goal.isAchieved()) {
                // This unlocks ACHIEVEMENT_GOAL_GETTER. 
                // If ACHIEVEMENT_GOAL_MASTER is different, its logic needs to be specific.
                unlockAchievement(ACHIEVEMENT_GOAL_GETTER); 
                // If ACHIEVEMENT_GOAL_MASTER is for the *very first* goal completed among all goals ever, 
                // additional logic might be needed to track this specifically.
                // For now, assuming GOAL_GETTER is for the first goal achieved overall.
                // And GOAL_MASTER is for the first time *any* specific goal is completed by the user.
                // The descriptions are similar, so this needs clarification if the intent is different.
                break; // Unlock only once for the first achieved goal
            }
        }
    }
    
    // Check for reaching 50% of any goal for the first time
    public void checkHalfwayHeroAchievement(List<SavingsGoal> goals) {
        for (SavingsGoal goal : goals) {
            if (goal.getCurrentAmount() >= (goal.getTargetAmount() / 2) && goal.getTargetAmount() > 0) {
                // Check if this specific halfway point was already awarded to avoid multiple toasts for the same halfway achievement
                Achievement halfwayHero = getAchievementByName(ACHIEVEMENT_HALF_WAY_THERE);
                if(halfwayHero != null && !halfwayHero.isUnlocked()) {
                    // This condition might need to be more specific if each goal can trigger its own "halfway" achievement
                    // Or if it's a one-time achievement for the first goal to reach 50%.
                    // For now, assume it's a one-time achievement.
                     unlockAchievement(ACHIEVEMENT_HALF_WAY_THERE);
                     break; // Unlock once for the first goal to hit 50%
                }
            }
        }
    }
    
    // Check for completing any specific savings goal for the first time (distinct from overall first goal completion)
    public void checkGoalMasterAchievement(List<SavingsGoal> goals) {
        // This logic depends on how "1st to complete/finish *your* personal savings goal" is interpreted.
        // If it's about the very first goal the user *ever* completes, it's similar to checkFirstGoalCompletedAchievement.
        // If it's about completing a specific, perhaps significant, goal, more context is needed.
        // For now, let's assume it's for the first time ANY goal is fully completed.
        for (SavingsGoal goal : goals) {
            if (goal.isAchieved()) {
                Achievement goalMaster = getAchievementByName(ACHIEVEMENT_GOAL_MASTER);
                if (goalMaster != null && !goalMaster.isUnlocked()) {
                     unlockAchievement(ACHIEVEMENT_GOAL_MASTER);
                     break; // Unlock once
                }
            }
        }
    }

    public void checkTotalSavedAchievements(List<SavingsGoal> goals) {
        double totalSaved = 0;
        for (SavingsGoal goal : goals) {
            totalSaved += goal.getCurrentAmount();
        }
        if (totalSaved >= 100) {
            if (unlockAchievement(ACHIEVEMENT_JUNIOR_SAVER)) {
                showAchievementToast(ACHIEVEMENT_JUNIOR_SAVER);
            }
        }
        if (totalSaved >= 1000) {
            if (unlockAchievement(ACHIEVEMENT_SAVED_1000)) {
                showAchievementToast(ACHIEVEMENT_SAVED_1000);
            }
        }
        if (totalSaved >= 5000) {
            if (unlockAchievement(ACHIEVEMENT_SAVED_5000)) {
                showAchievementToast(ACHIEVEMENT_SAVED_5000);
            }
        }
    }

    private void showAchievementToast(String achievementName) {
        if (achievementName != null && context != null) {
            CustomToast.showSuccess(context, "Achievement Unlocked: " + achievementName);
        }
    }

    // Combined check method - needs to be updated for new achievements
    public void checkAllAchievements(List<SavingsGoal> goals /*, Potential other params like daily/weekly mission counts, streak data */) {
        checkFirstGoalCreatedAchievement(goals);
        checkFirstGoalCompletedAchievement(goals); // For ACHIEVEMENT_GOAL_GETTER
        checkTotalSavedAchievements(goals);
        checkHalfwayHeroAchievement(goals);
        checkGoalMasterAchievement(goals); // For ACHIEVEMENT_GOAL_MASTER

        // TODO: Implement logic for streak and mission achievements
        // unlockAchievement(ACHIEVEMENT_7_DAY_STREAK);
        // unlockAchievement(ACHIEVEMENT_1_MONTH_STREAK);
        // unlockAchievement(ACHIEVEMENT_10_DAILY_MISSIONS);
        // unlockAchievement(ACHIEVEMENT_5_WEEKLY_MISSIONS);
    }
} 