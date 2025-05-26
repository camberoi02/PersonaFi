package com.example.personafi;

public class LevelManager {
    private static final int MAX_LEVEL = 10;
    private static final int BASE_XP = 1000; // Base XP requirement for level 1

    public static int getLevelForXp(int xp) {
        for (int level = 1; level <= MAX_LEVEL; level++) {
            if (xp < getXpRequiredForLevel(level)) {
                return level - 1;
            }
        }
        return MAX_LEVEL;
    }

    public static int getXpRequiredForLevel(int level) {
        if (level < 1 || level > MAX_LEVEL) {
            return 0;
        }
        return level * BASE_XP;
    }

    public static int getXpProgressForCurrentLevel(int currentXp) {
        int currentLevel = getLevelForXp(currentXp);
        int xpForCurrentLevel = getXpRequiredForLevel(currentLevel);
        int xpForNextLevel = getXpRequiredForLevel(currentLevel + 1);
        
        if (currentLevel >= MAX_LEVEL) {
            return 100; // Return 100% for max level
        }
        
        int xpInCurrentLevel = currentXp - xpForCurrentLevel;
        int xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;
        
        return (xpInCurrentLevel * 100) / xpNeededForNextLevel;
    }

    public static int getXpRemainingForNextLevel(int currentXp) {
        int currentLevel = getLevelForXp(currentXp);
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        return getXpRequiredForLevel(currentLevel + 1) - currentXp;
    }

    public static boolean isMaxLevel(int xp) {
        return getLevelForXp(xp) >= MAX_LEVEL;
    }
} 