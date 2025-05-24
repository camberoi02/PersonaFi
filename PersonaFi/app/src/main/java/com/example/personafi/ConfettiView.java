package com.example.personafi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class ConfettiView extends View {
    private ArrayList<Confetti> confetti = new ArrayList<>();
    private Random random = new Random();
    private boolean isActive = false;
    private Paint paint = new Paint();
    
    // Confetti colors
    private int[] colors = {
            Color.parseColor("#FFC107"), // Yellow
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#FF5722"), // Orange
            Color.parseColor("#9C27B0")  // Purple
    };
    
    private static final int DEFAULT_CONFETTI_COUNT = 100;
    private static final int MAX_PARTICLES = 150;
    
    public ConfettiView(Context context) {
        super(context);
        init();
    }
    
    public ConfettiView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public ConfettiView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint.setStyle(Paint.Style.FILL);
    }
    
    public void start() {
        isActive = true;
        generateConfetti(DEFAULT_CONFETTI_COUNT);
        invalidate();
    }
    
    public void start(int count) {
        isActive = true;
        generateConfetti(Math.min(count, MAX_PARTICLES));
        invalidate();
    }
    
    public void playShortConfetti() {
        isActive = true;
        generateConfetti(50); // Use a smaller number of particles for a short burst
        invalidate();
    }
    
    public void stop() {
        isActive = false;
        confetti.clear();
        invalidate();
    }
    
    private void generateConfetti(int count) {
        confetti.clear();
        for (int i = 0; i < count; i++) {
            Confetti particle = new Confetti(
                    random.nextFloat() * getWidth(),
                    -random.nextFloat() * getHeight() / 4, // Start above the view
                    random.nextFloat() * 10 + 5,
                    random.nextFloat() * 5 + 2,
                    random.nextFloat() * 6 + 2,
                    colors[random.nextInt(colors.length)],
                    random.nextInt(3) // 0=circle, 1=rectangle, 2=square
            );
            confetti.add(particle);
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isActive) return;
        
        boolean stillActive = false;
        
        for (Confetti particle : confetti) {
            // Update particle position
            particle.y += particle.speed;
            particle.x += particle.speedX;
            
            // Check if particle is still visible
            if (particle.y < getHeight()) {
                stillActive = true;
            }
            
            // Draw the particle
            paint.setColor(particle.color);
            
            switch (particle.shape) {
                case 0: // Circle
                    canvas.drawCircle(particle.x, particle.y, particle.size, paint);
                    break;
                case 1: // Rectangle
                    canvas.drawRect(
                            particle.x - particle.size,
                            particle.y - particle.size / 2,
                            particle.x + particle.size,
                            particle.y + particle.size / 2,
                            paint);
                    break;
                case 2: // Square
                    canvas.drawRect(
                            particle.x - particle.size / 2,
                            particle.y - particle.size / 2,
                            particle.x + particle.size / 2,
                            particle.y + particle.size / 2,
                            paint);
                    break;
            }
        }
        
        if (stillActive) {
            invalidate();
        } else {
            isActive = false;
        }
    }
    
    private static class Confetti {
        float x, y;
        float speed;
        float speedX;
        float size;
        int color;
        int shape;
        
        Confetti(float x, float y, float speed, float speedX, float size, int color, int shape) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.speedX = speedX - 2.5f; // Center around 0 for left/right movement
            this.size = size;
            this.color = color;
            this.shape = shape;
        }
    }
} 