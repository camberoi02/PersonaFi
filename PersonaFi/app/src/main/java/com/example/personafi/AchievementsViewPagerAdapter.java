package com.example.personafi;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.HashMap;
import java.util.Map;

public class AchievementsViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 2;
    private Map<Integer, Fragment> fragments = new HashMap<>();

    public AchievementsViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new MissionsListFragment();
                break;
            case 1:
                fragment = new AchievementsListFragment();
                break;
            default:
                fragment = new MissionsListFragment();
        }
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public AchievementsListFragment getAchievementsListFragment() {
        Fragment fragment = fragments.get(1);
        return fragment instanceof AchievementsListFragment ? (AchievementsListFragment) fragment : null;
    }

    public MissionsListFragment getMissionsListFragment() {
        Fragment fragment = fragments.get(0);
        return fragment instanceof MissionsListFragment ? (MissionsListFragment) fragment : null;
    }

    public void refreshFragment(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment instanceof AchievementsListFragment) {
            ((AchievementsListFragment) fragment).refreshAchievements();
        }
    }
} 