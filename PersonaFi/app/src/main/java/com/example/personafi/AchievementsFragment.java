package com.example.personafi;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.List;

public class AchievementsFragment extends Fragment {
    private CelebrationView celebrationView;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private AchievementsViewPagerAdapter viewPagerAdapter;
    private AchievementManager achievementManager;
    private boolean isViewCreated = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_achievements, container, false);
        
        // Initialize celebration view
        celebrationView = view.findViewById(R.id.celebrationView);
        
        // Initialize ViewPager and TabLayout
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        
        // Get AchievementManager from MainActivity
        if (getActivity() instanceof MainActivity) {
            achievementManager = ((MainActivity) getActivity()).getAchievementManager();
        }
        
        // Set up ViewPager
        viewPagerAdapter = new AchievementsViewPagerAdapter(requireActivity());
        viewPager.setAdapter(viewPagerAdapter);
        
        // Set up TabLayout with ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText("Missions");
                        break;
                    case 1:
                        tab.setText("Achievements");
                        break;
                }
            }
        ).attach();

        // Initial update of achievements list
        if (achievementManager != null) {
            updateAchievementsList(achievementManager.getAchievements());
        }
        
        isViewCreated = true;
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force refresh achievements when fragment becomes visible
        refreshAchievements();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isAdded()) {
            // Force refresh achievements when fragment becomes visible
            refreshAchievements();
        }
    }

    // Method to update achievements from MainActivity
    public void updateAchievementsList(List<Achievement> achievements) {
        if (viewPagerAdapter != null) {
            AchievementsListFragment listFragment = viewPagerAdapter.getAchievementsListFragment();
            if (listFragment != null) {
                listFragment.updateAchievementsList(achievements);
            }
        }
    }

    public void refreshAchievements() {
        if (!isAdded() || !isViewCreated) return;
        
        // Post the refresh to the main thread with a slight delay to avoid transaction conflicts
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                // Update achievements list using existing method
                if (achievementManager != null) {
                    // Get the latest achievements
                    List<Achievement> currentAchievements = achievementManager.getAchievements();
                    
                    // Update the list
                    updateAchievementsList(currentAchievements);
                    
                    // Force refresh the ViewPager adapter
                    if (viewPagerAdapter != null) {
                        viewPagerAdapter.notifyDataSetChanged();
                        // Force refresh the achievements list fragment
                        AchievementsListFragment listFragment = viewPagerAdapter.getAchievementsListFragment();
                        if (listFragment != null) {
                            listFragment.updateAchievementsList(currentAchievements);
                        }
                    }
                    
                    // Start celebration animation if any achievements were newly unlocked
                    if (celebrationView != null) {
                        boolean hasNewUnlocked = false;
                        for (Achievement achievement : currentAchievements) {
                            if (achievement.isUnlocked()) {
                                hasNewUnlocked = true;
                                break;
                            }
                        }
                        if (hasNewUnlocked) {
                            celebrationView.startCelebration();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 100); // 100ms delay to avoid transaction conflicts
    }
} 