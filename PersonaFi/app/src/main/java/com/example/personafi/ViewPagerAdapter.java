package com.example.personafi;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.fragment.app.FragmentManager;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int NUM_PAGES = 3; // Updated to include Profile
    private final FragmentActivity activity;

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.activity = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SavingsGoalsFragment();
            case 1:
                return new AchievementsFragment();
            case 2:
                return ProfileFragment.getInstance();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

    public Fragment getFragmentAtPosition(int position) {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        return fragmentManager.findFragmentByTag("f" + position);
    }
} 