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

public class AnimatedShapesView extends View {
    
    private static final int DEFAULT_SHAPE_COUNT = 25;
    private static final int UPDATE_INTERVAL_MS = 16; // ~60fps
    private static final float MIN_ALPHA = 0.15f;
    private static final float MAX_ALPHA = 0.30f;
    
    private List<Shape> shapes;
    private Paint paint;
    private Random random;
    private int[] shapeColors;
    private int width, height;
    private boolean isRunning = true;
    
    private final Runnable animator = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                updateShapes();
                invalidate();
                postDelayed(this, UPDATE_INTERVAL_MS);
            }
        }
    };
    
    public AnimatedShapesView(Context context) {
        super(context);
        init();
    }
    
    public AnimatedShapesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AnimatedShapesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        random = new Random();
        shapes = new ArrayList<>();
        
        // Colors from the app's theme
        shapeColors = new int[] {
            Color.parseColor("#6750A4"), // primary
            Color.parseColor("#9A82DB"), // lighter purple
            Color.parseColor("#7965b3"), // medium purple
            Color.parseColor("#49D0B0"), // teal accent
            Color.parseColor("#79B6F9")  // light blue
        };
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        
        // Initialize shapes when size is known
        if (shapes.isEmpty()) {
            initShapes();
        }
    }
    
    private void initShapes() {
        shapes.clear();
        
        for (int i = 0; i < DEFAULT_SHAPE_COUNT; i++) {
            generateShape();
        }
        
        // Start animation
        post(animator);
    }
    
    private void generateShape() {
        int type = random.nextInt(3); // 0: circle, 1: rectangle, 2: triangle
        // Increase shape size range to make shapes more visible
        int size = random.nextInt(width / 8) + width / 15; // Size between ~7% and ~20% of width
        
        // Distribute shapes more evenly across the screen, with emphasis on the top area
        float x, y;
        
        // Determine position - make sure we get more shapes at the top of the screen
        if (random.nextFloat() < 0.4f) { // 40% chance to position at the top
            // Generate shape at the top of the screen
            x = random.nextFloat() * width;
            y = -size + random.nextFloat() * (height / 3); // Between slightly above the top and 1/3 down
        } else {
            // Generate shape anywhere else on the screen
            x = random.nextFloat() * width;
            y = random.nextFloat() * height;
        }
        
        // Some shapes start outside the view
        if (random.nextBoolean() && random.nextFloat() > 0.6f) { // Reduce frequency of side-entering shapes
            if (random.nextBoolean()) {
                x = -size; // Left side
            } else {
                x = width + size; // Right side
            }
        }
        
        float alpha = MIN_ALPHA + random.nextFloat() * (MAX_ALPHA - MIN_ALPHA);
        int color = shapeColors[random.nextInt(shapeColors.length)];
        
        // Speed between 1 and 3 pixels per frame
        float speedX = (random.nextFloat() * 2 - 1) * (random.nextFloat() * 2 + 1);
        
        // For savings theme, bias toward upward movement (80% chance of upward)
        float speedY;
        if (random.nextFloat() < 0.8f) { // Increased chance of upward movement
            // Upward movement (negative Y)
            speedY = -random.nextFloat() * 2.5f - 0.5f;
        } else {
            // Some downward movement for variety
            speedY = random.nextFloat() * 1.5f + 0.5f;
        }
        
        // Rotation for all shapes (makes movement look more interesting)
        float rotation = random.nextFloat() * 360;
        float rotationSpeed = (random.nextFloat() * 2 - 1) * 1.5f;
        
        Shape shape = new Shape(type, x, y, size, color, alpha, speedX, speedY, rotation, rotationSpeed);
        shapes.add(shape);
    }
    
    private void updateShapes() {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            
            // Update position
            shape.x += shape.speedX;
            shape.y += shape.speedY;
            
            // Update rotation
            shape.rotation += shape.rotationSpeed;
            
            // If shape is completely off screen, regenerate it
            if (shape.x < -shape.size * 2 || shape.x > width + shape.size * 2 ||
                shape.y < -shape.size * 2 || shape.y > height + shape.size * 2) {
                
                // For shapes that move off the top, have them re-enter from the bottom more often
                float chance = random.nextFloat();
                if (shape.y < -shape.size * 2 && chance < 0.7f) {
                    // When a shape leaves the top, 70% chance it will re-enter from the bottom
                    shape.x = random.nextFloat() * width;
                    shape.y = height + shape.size;
                    // Keep upward movement speed but potentially adjust it
                    if (shape.speedY > 0) shape.speedY *= -1; // Ensure it moves upward
                    shape.speedY = -Math.abs(shape.speedY) * (0.8f + random.nextFloat() * 0.4f); // Slight speed variation
                } else {
                    // Randomly choose a side to re-enter from
                    int side = random.nextInt(4); // 0: top, 1: right, 2: bottom, 3: left
                    
                    switch (side) {
                        case 0: // top
                            shape.x = random.nextFloat() * width;
                            shape.y = -shape.size;
                            break;
                        case 1: // right
                            shape.x = width + shape.size;
                            shape.y = random.nextFloat() * height;
                            break;
                        case 2: // bottom
                            shape.x = random.nextFloat() * width;
                            shape.y = height + shape.size;
                            break;
                        case 3: // left
                            shape.x = -shape.size;
                            shape.y = random.nextFloat() * height;
                            break;
                    }
                    
                    // Ensure speed is heading back into the screen
                    if (side == 0 && shape.speedY < 0) shape.speedY *= -1;
                    if (side == 1 && shape.speedX > 0) shape.speedX *= -1;
                    if (side == 2 && shape.speedY > 0) shape.speedY *= -1;
                    if (side == 3 && shape.speedX < 0) shape.speedX *= -1;
                }
                
                // Refresh other properties
                shape.color = shapeColors[random.nextInt(shapeColors.length)];
                shape.alpha = MIN_ALPHA + random.nextFloat() * (MAX_ALPHA - MIN_ALPHA);
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Shape shape : shapes) {
            paint.setColor(shape.color);
            paint.setAlpha((int)(shape.alpha * 255));
            
            canvas.save();
            canvas.rotate(shape.rotation, shape.x, shape.y);
            
            switch (shape.type) {
                case 0: // Circle
                    canvas.drawCircle(shape.x, shape.y, shape.size / 2, paint);
                    break;
                    
                case 1: // Rectangle
                    float left = shape.x - shape.size / 2;
                    float top = shape.y - shape.size / 2;
                    float right = shape.x + shape.size / 2;
                    float bottom = shape.y + shape.size / 2;
                    canvas.drawRect(left, top, right, bottom, paint);
                    break;
                    
                case 2: // Triangle
                    Path path = new Path();
                    path.moveTo(shape.x, shape.y - shape.size / 2);
                    path.lineTo(shape.x - shape.size / 2, shape.y + shape.size / 2);
                    path.lineTo(shape.x + shape.size / 2, shape.y + shape.size / 2);
                    path.close();
                    canvas.drawPath(path, paint);
                    break;
            }
            
            canvas.restore();
        }
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
    
    /**
     * Configure this view specifically for the Savings tab with
     * appropriate colors and shape patterns
     */
    public void configureSavingsTheme() {
        // Set more vibrant theme colors with increased saturation for better visibility
        shapeColors = new int[] {
            Color.parseColor("#7B5DB5"),  // brighter purple
            Color.parseColor("#AF8EE6"),  // lighter purple, more saturated
            Color.parseColor("#50E6C2"),  // brighter teal
            Color.parseColor("#70534A"),  // slightly lighter brown for coins
            Color.parseColor("#FFDF40")   // slightly more saturated gold
        };
        
        // If we already have shapes, update their colors and increase their size and opacity
        for (Shape shape : shapes) {
            shape.color = shapeColors[random.nextInt(shapeColors.length)];
            
            // Increase size by 30% to make more visible
            shape.size *= 1.3;
            
            // Increase alpha by ensuring it's at least the minimum
            shape.alpha = Math.max(shape.alpha, MIN_ALPHA);
            
            // Also make them move upward more (like money/savings growing)
            if (shape.speedY > 0) {
                shape.speedY *= -0.8f; // Reverse direction if going down, but slower
            } else {
                shape.speedY *= 1.2f; // Speed up if already going up
            }
        }
        
        // Make more shapes move upward in future generations
        // We'll handle this in the generateShape method which is already modified
    }
    
    private static class Shape {
        int type; // 0: circle, 1: rectangle, 2: triangle
        float x, y;
        int size;
        int color;
        float alpha;
        float speedX, speedY;
        float rotation;
        float rotationSpeed;
        
        Shape(int type, float x, float y, int size, int color, float alpha, 
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