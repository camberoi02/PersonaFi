package com.example.personafi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class TutorialActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private Button nextButton;
    private Button skipButton;
    private LinearLayout indicatorLayout;
    private List<ImageView> indicators;
    private TutorialPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.tutorialViewPager);
        nextButton = findViewById(R.id.nextButton);
        skipButton = findViewById(R.id.skipButton);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        
        setupTutorialPages();
        setupIndicators();
        setupButtons();
    }

    private void setupTutorialPages() {
        List<TutorialPage> pages = new ArrayList<>();
        
        pages.add(new TutorialPage(
            R.drawable.ic_goals,
            "Set Your Goals",
            "Create personalized savings goals and track your progress towards achieving them"
        ));
        
        pages.add(new TutorialPage(
            R.drawable.ic_missions,
            "Complete Missions",
            "Take on exciting missions to earn rewards and boost your savings journey"
        ));
        
        pages.add(new TutorialPage(
            R.drawable.ic_achievements,
            "Earn Achievements",
            "Unlock special achievements as you reach milestones and develop good financial habits"
        ));
        
        pages.add(new TutorialPage(
            R.drawable.ic_level_up,
            "Level Up",
            "Watch your financial persona grow stronger as you progress through levels"
        ));

        adapter = new TutorialPagerAdapter(pages);
        viewPager.setAdapter(adapter);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                if (position == pages.size() - 1) {
                    nextButton.setText("Get Started");
                } else {
                    nextButton.setText("Next");
                }
            }
        });
    }

    private void setupIndicators() {
        indicators = new ArrayList<>();
        int count = adapter.getItemCount();
        
        for (int i = 0; i < count; i++) {
            ImageView indicator = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            indicator.setLayoutParams(params);
            indicator.setImageResource(i == 0 ? R.drawable.indicator_active : R.drawable.indicator_inactive);
            indicators.add(indicator);
            indicatorLayout.addView(indicator);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < indicators.size(); i++) {
            indicators.get(i).setImageResource(
                i == position ? R.drawable.indicator_active : R.drawable.indicator_inactive
            );
        }
    }

    private void setupButtons() {
        skipButton.setOnClickListener(v -> finishTutorial());
        
        nextButton.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == adapter.getItemCount() - 1) {
                finishTutorial();
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
    }

    private void finishTutorial() {
        SharedPreferences prefs = getSharedPreferences("PersonaFiPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("tutorial_completed", true).apply();
        
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
} 