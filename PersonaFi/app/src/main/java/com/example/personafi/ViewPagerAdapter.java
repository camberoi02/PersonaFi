package com.example.personafi;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.fragment.app.FragmentManager;
import java.util.HashMap;
import java.util.Map;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3; // Updated to include Profile
    private final FragmentActivity activity;
    private Map<Integer, Fragment> fragments = new HashMap<>();

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new SavingsGoalsFragment();
                break;
            case 1:
                fragment = new AchievementsFragment();
                break;
            case 2:
                fragment = ProfileFragment.getInstance();
                break;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public Fragment getFragmentAtPosition(int position) {
        return fragments.get(position);
    }

    public void refreshFragment(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment instanceof AchievementsFragment) {
            ((AchievementsFragment) fragment).refreshAchievements();
        }
    }
} 