package com.example.personafi;

import android.os.Bundle;
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
        
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update achievements list when fragment becomes visible
        if (achievementManager != null) {
            updateAchievementsList(achievementManager.getAchievements());
        }
        // Start celebration animation when fragment becomes visible
        if (celebrationView != null) {
            celebrationView.startCelebration();
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
} 