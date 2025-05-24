package com.example.personifi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBackgroundView extends View {
    
    private static final int DEFAULT_PARTICLE_COUNT = 30;
    private static final int UPDATE_INTERVAL_MS = 16; // ~60fps
    private static final float MIN_ALPHA = 0.2f;
    private static final float MAX_ALPHA = 0.4f;
    
    private List<Particle> particles;
    private Paint paint;
    private Random random;
    private int[] particleColors;
    private int width, height;
    private boolean isRunning = true;
    
    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                updateParticles();
                invalidate();
                postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }
    };
    
    public GameBackgroundView(Context context) {
        super(context);
        init();
    }
    
    public GameBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GameBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        random = new Random();
        particles = new ArrayList<>();
        
        // Darker, more vibrant colors for game theme
        particleColors = new int[] {
            Color.parseColor("#FF6B6B"), // Vibrant red
            Color.parseColor("#4ECDC4"), // Teal
            Color.parseColor("#FFD93D"), // Gold
            Color.parseColor("#6C5CE7"), // Purple
            Color.parseColor("#00B894")  // Mint
        };
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        
        if (particles.isEmpty()) {
            initParticles();
        }
    }
    
    private void initParticles() {
        particles.clear();
        
        for (int i = 0; i < DEFAULT_PARTICLE_COUNT; i++) {
            generateParticle();
        }
        
        post(animator);
    }
    
    private void generateParticle() {
        int type = random.nextInt(4); // 0: star, 1: diamond, 2: circle, 3: hexagon
        int size = random.nextInt(width / 12) + width / 20; // Size between ~8% and ~15% of width
        
        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        
        // Some particles start outside the view
        if (random.nextFloat() > 0.7f) {
            if (random.nextBoolean()) {
                x = -size;
            } else {
                x = width + size;
            }
        }
        
        float alpha = MIN_ALPHA + random.nextFloat() * (MAX_ALPHA - MIN_ALPHA);
        int color = particleColors[random.nextInt(particleColors.length)];
        
        // Slower, more floating movement
        float speedX = (random.nextFloat() * 2 - 1) * 0.5f;
        float speedY = (random.nextFloat() * 2 - 1) * 0.5f;
        
        // Gentle rotation
        float rotation = random.nextFloat() * 360;
        float rotationSpeed = (random.nextFloat() * 2 - 1) * 0.5f;
        
        Particle particle = new Particle(type, x, y, size, color, alpha, speedX, speedY, rotation, rotationSpeed);
        particles.add(particle);
    }
    
    private void updateParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            
            // Update position with gentle floating movement
            particle.x += particle.speedX;
            particle.y += particle.speedY;
            
            // Add slight vertical oscillation
            particle.y += Math.sin(System.currentTimeMillis() * 0.001 + i) * 0.2f;
            
            // Update rotation
            particle.rotation += particle.rotationSpeed;
            
            // If particle is off screen, regenerate it
            if (particle.x < -particle.size * 2 || particle.x > width + particle.size * 2 ||
                particle.y < -particle.size * 2 || particle.y > height + particle.size * 2) {
                
                generateParticle();
                particles.remove(i);
                i--;
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw dark background
        canvas.drawColor(Color.parseColor("#1A1A2E")); // Dark blue-black background
        
        for (Particle particle : particles) {
            paint.setColor(particle.color);
            paint.setAlpha((int)(particle.alpha * 255));
            
            canvas.save();
            canvas.rotate(particle.rotation, particle.x, particle.y);
            
            switch (particle.type) {
                case 0: // Star
                    drawStar(canvas, particle.x, particle.y, particle.size / 2, paint);
                    break;
                    
                case 1: // Diamond
                    drawDiamond(canvas, particle.x, particle.y, particle.size / 2, paint);
                    break;
                    
                case 2: // Circle
                    canvas.drawCircle(particle.x, particle.y, particle.size / 2, paint);
                    break;
                    
                case 3: // Hexagon
                    drawHexagon(canvas, particle.x, particle.y, particle.size / 2, paint);
                    break;
            }
            
            canvas.restore();
        }
    }
    
    private void drawStar(Canvas canvas, float x, float y, float radius, Paint paint) {
        Path path = new Path();
        float outerRadius = radius;
        float innerRadius = radius * 0.4f;
        
        for (int i = 0; i < 5; i++) {
            float angle = (float) (i * 2 * Math.PI / 5 - Math.PI / 2);
            float outerX = x + outerRadius * (float) Math.cos(angle);
            float outerY = y + outerRadius * (float) Math.sin(angle);
            float innerX = x + innerRadius * (float) Math.cos(angle + Math.PI / 5);
            float innerY = y + innerRadius * (float) Math.sin(angle + Math.PI / 5);
            
            if (i == 0) {
                path.moveTo(outerX, outerY);
            } else {
                path.lineTo(outerX, outerY);
            }
            path.lineTo(innerX, innerY);
        }
        path.close();
        canvas.drawPath(path, paint);
    }
    
    private void drawDiamond(Canvas canvas, float x, float y, float size, Paint paint) {
        Path path = new Path();
        path.moveTo(x, y - size);
        path.lineTo(x + size, y);
        path.lineTo(x, y + size);
        path.lineTo(x - size, y);
        path.close();
        canvas.drawPath(path, paint);
    }
    
    private void drawHexagon(Canvas canvas, float x, float y, float radius, Paint paint) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            float angle = (float) (i * 2 * Math.PI / 6);
            float pointX = x + radius * (float) Math.cos(angle);
            float pointY = y + radius * (float) Math.sin(angle);
            if (i == 0) {
                path.moveTo(pointX, pointY);
            } else {
                path.lineTo(pointX, pointY);
            }
        }
        path.close();
        canvas.drawPath(path, paint);
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isRunning) {
            isRunning = true;
            post(animator);
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isRunning = false;
        removeCallbacks(animator);
    }
    
    private static class Particle {
        int type; // 0: star, 1: diamond, 2: circle, 3: hexagon
        float x, y;
        int size;
        int color;
        float alpha;
        float speedX, speedY;
        float rotation;
        float rotationSpeed;
        
        Particle(int type, float x, float y, int size, int color, float alpha, 
                float speedX, float speedY, float rotation, float rotationSpeed) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.alpha = alpha;
            this.speedX = speedX;
            this.speedY = speedY;
            this.rotation = rotation;
            this.rotationSpeed = rotationSpeed;
        }
    }
} 