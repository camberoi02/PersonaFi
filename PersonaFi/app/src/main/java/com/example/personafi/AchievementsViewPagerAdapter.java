package com.example.personafi;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AchievementsViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;
    private AchievementsListFragment achievementsListFragment;
    private MissionsListFragment missionsListFragment;

    public AchievementsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        achievementsListFragment = new AchievementsListFragment();
        missionsListFragment = new MissionsListFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return missionsListFragment;
            case 1:
                return achievementsListFragment;
            default:
                return missionsListFragment;
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public AchievementsListFragment getAchievementsListFragment() {
        return achievementsListFragment;
    }

    public MissionsListFragment getMissionsListFragment() {
        return missionsListFragment;
    }
} 