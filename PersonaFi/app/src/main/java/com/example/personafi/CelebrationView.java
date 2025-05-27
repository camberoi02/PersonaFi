package com.example.personafi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CelebrationView extends View {
    private static final int CONFETTI_COUNT = 100;
    private static final int FIREWORK_COUNT = 5;
    private static final int[] COLORS = {
        Color.parseColor("#FFD700"), // Gold
        Color.parseColor("#FF69B4"), // Pink
        Color.parseColor("#00BFFF"), // Blue
        Color.parseColor("#32CD32"), // Green
        Color.parseColor("#FF4500"), // Orange
        Color.parseColor("#9370DB")  // Purple
    };

    private List<Confetti> confettiList;
    private List<Firework> fireworkList;
    private Paint paint;
    private Random random;
    private boolean isAnimating = false;
    private long startTime;

    public CelebrationView(Context context) {
        super(context);
        init();
    }

    public CelebrationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        random = new Random();
        confettiList = new ArrayList<>();
        fireworkList = new ArrayList<>();
        setBackgroundColor(Color.TRANSPARENT); // Make the view background transparent
    }

    public void startCelebration() {
        isAnimating = true;
        startTime = System.currentTimeMillis();
        generateConfetti();
        generateFireworks();
        invalidate();
    }

    private void generateConfetti() {
        confettiList.clear();
        for (int i = 0; i < CONFETTI_COUNT; i++) {
            float x = random.nextFloat() * getWidth();
            float y = -random.nextFloat() * getHeight(); // Start above the screen
            float speedX = (random.nextFloat() - 0.5f) * 8;
            float speedY = random.nextFloat() * 15 + 5;
            float rotation = random.nextFloat() * 360;
            float rotationSpeed = (random.nextFloat() - 0.5f) * 10;
            int color = COLORS[random.nextInt(COLORS.length)];
            float size = random.nextFloat() * 10 + 5;
            
            confettiList.add(new Confetti(x, y, speedX, speedY, rotation, rotationSpeed, color, size));
        }
    }

    private void generateFireworks() {
        fireworkList.clear();
        for (int i = 0; i < FIREWORK_COUNT; i++) {
            float x = random.nextFloat() * getWidth();
            float y = random.nextFloat() * (getHeight() * 0.6f); // Keep fireworks in upper 60% of screen
            int color = COLORS[random.nextInt(COLORS.length)];
            float size = random.nextFloat() * 20 + 30;
            float speed = random.nextFloat() * 2 + 1;
            
            fireworkList.add(new Firework(x, y, color, size, speed));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isAnimating) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - startTime) / 1000f;

        // Draw and update confetti
        for (Confetti confetti : confettiList) {
            paint.setColor(confetti.color);
            paint.setAlpha(200); // Make confetti slightly transparent
            canvas.save();
            canvas.rotate(confetti.rotation, confetti.x, confetti.y);
            canvas.drawRect(
                confetti.x - confetti.size/2,
                confetti.y - confetti.size/2,
                confetti.x + confetti.size/2,
                confetti.y + confetti.size/2,
                paint
            );
            canvas.restore();

            // Update confetti position
            confetti.x += confetti.speedX;
            confetti.y += confetti.speedY;
            confetti.rotation += confetti.rotationSpeed;
            confetti.speedY += 0.2f; // Gravity effect
        }

        // Draw and update fireworks
        for (Firework firework : fireworkList) {
            float progress = (deltaTime - firework.startTime) / firework.duration;
            if (progress < 0 || progress > 1) continue;

            float scale = Math.min(1, progress * 2);
            float alpha = Math.min(1, (1 - progress) * 2);
            paint.setColor(firework.color);
            paint.setAlpha((int)(alpha * 200)); // Make fireworks slightly transparent

            // Draw firework particles
            for (int i = 0; i < 12; i++) {
                float angle = (float) (i * Math.PI * 2 / 12);
                float distance = firework.size * scale;
                float x = firework.x + (float)Math.cos(angle) * distance;
                float y = firework.y + (float)Math.sin(angle) * distance;
                
                canvas.drawCircle(x, y, 4, paint);
            }
        }

        // Check if animation should continue
        boolean hasActiveFireworks = false;
        for (Firework firework : fireworkList) {
            if ((deltaTime - firework.startTime) < firework.duration) {
                hasActiveFireworks = true;
                break;
            }
        }

        if (hasActiveFireworks || !confettiList.isEmpty()) {
            invalidate();
        } else {
            isAnimating = false;
        }
    }

    private static class Confetti {
        float x, y;
        float speedX, speedY;
        float rotation;
        float rotationSpeed;
        int color;
        float size;

        Confetti(float x, float y, float speedX, float speedY, float rotation, 
                float rotationSpeed, int color, float size) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
            this.color = color;
            this.size = size;
        }
    }

    private static class Firework {
        float x, y;
        int color;
        float size;
        float speed;
        float startTime;
        float duration;

        Firework(float x, float y, int color, float size, float speed) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = size;
            this.speed = speed;
            this.startTime = 0;
            this.duration = 1.5f;
        }
    }
} 