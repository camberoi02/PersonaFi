package com.example.personafi;

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

public class GameAnimatedShapesView extends View {
    
    private static final int DEFAULT_SHAPE_COUNT = 25;
    private static final int UPDATE_INTERVAL_MS = 16; // ~60fps
    private static final float MIN_ALPHA = 0.20f;
    private static final float MAX_ALPHA = 0.35f;
    
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
    
    public GameAnimatedShapesView(Context context) {
        super(context);
        init();
    }
    
    public GameAnimatedShapesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public GameAnimatedShapesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        random = new Random();
        shapes = new ArrayList<>();
        
        // Darker theme colors for game page
        shapeColors = new int[] {
            Color.parseColor("#FF5252"),  // red
            Color.parseColor("#FFD600"),  // yellow
            Color.parseColor("#00BFA5"),  // teal
            Color.parseColor("#7B1FA2"),  // purple
            Color.parseColor("#FF4081")   // pink
        };
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.width = w;
        this.height = h;
        
        if (shapes.isEmpty()) {
            initShapes();
        }
    }
    
    private void initShapes() {
        shapes.clear();
        
        for (int i = 0; i < DEFAULT_SHAPE_COUNT; i++) {
            generateShape();
        }
        
        post(animator);
    }
    
    private void generateShape() {
        int type = random.nextInt(4); // 0: circle, 1: star, 2: heart, 3: triangle
        int size = random.nextInt(width / 8) + width / 15;
        
        float x = random.nextFloat() * width;
        float y = random.nextFloat() * height;
        
        // Some shapes start outside the view
        if (random.nextBoolean() && random.nextFloat() > 0.6f) {
            if (random.nextBoolean()) {
                x = -size;
            } else {
                x = width + size;
            }
        }
        
        float alpha = MIN_ALPHA + random.nextFloat() * (MAX_ALPHA - MIN_ALPHA);
        int color = shapeColors[random.nextInt(shapeColors.length)];
        
        float speedX = (random.nextFloat() * 2 - 1) * (random.nextFloat() * 2 + 1);
        float speedY = (random.nextFloat() * 2 - 1) * (random.nextFloat() * 2 + 1);
        
        float rotation = random.nextFloat() * 360;
        float rotationSpeed = (random.nextFloat() * 2 - 1) * 1.5f;
        
        Shape shape = new Shape(type, x, y, size, color, alpha, speedX, speedY, rotation, rotationSpeed);
        shapes.add(shape);
    }
    
    private void updateShapes() {
        for (int i = 0; i < shapes.size(); i++) {
            Shape shape = shapes.get(i);
            
            shape.x += shape.speedX;
            shape.y += shape.speedY;
            shape.rotation += shape.rotationSpeed;
            
            if (shape.x < -shape.size * 2 || shape.x > width + shape.size * 2 ||
                shape.y < -shape.size * 2 || shape.y > height + shape.size * 2) {
                
                int side = random.nextInt(4);
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
                
                shape.color = shapeColors[random.nextInt(shapeColors.length)];
                shape.alpha = MIN_ALPHA + random.nextFloat() * (MAX_ALPHA - MIN_ALPHA);
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Draw dark background
        canvas.drawColor(Color.parseColor("#1A1A1A")); // Dark background
        
        for (Shape shape : shapes) {
            paint.setColor(shape.color);
            paint.setAlpha((int)(shape.alpha * 255));
            
            canvas.save();
            canvas.rotate(shape.rotation, shape.x, shape.y);
            
            switch (shape.type) {
                case 0: // Circle
                    canvas.drawCircle(shape.x, shape.y, shape.size / 2, paint);
                    break;
                    
                case 1: // Star
                    drawStar(canvas, shape.x, shape.y, shape.size / 2, paint);
                    break;
                    
                case 2: // Heart
                    drawHeart(canvas, shape.x, shape.y, shape.size / 2, paint);
                    break;
                    
                case 3: // Triangle
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
    
    private void drawStar(Canvas canvas, float x, float y, float radius, Paint paint) {
        Path path = new Path();
        float outerRadius = radius;
        float innerRadius = radius * 0.4f;
        int numPoints = 5;
        
        for (int i = 0; i < numPoints * 2; i++) {
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            float angle = (float) (i * Math.PI / numPoints);
            float px = x + (float) Math.cos(angle) * r;
            float py = y + (float) Math.sin(angle) * r;
            
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.close();
        canvas.drawPath(path, paint);
    }
    
    private void drawHeart(Canvas canvas, float x, float y, float size, Paint paint) {
        Path path = new Path();
        float width = size * 2;
        float height = size * 1.8f;
        
        path.moveTo(x, y + height * 0.3f);
        path.cubicTo(
            x, y, 
            x - width * 0.5f, y, 
            x - width * 0.5f, y + height * 0.3f
        );
        path.cubicTo(
            x - width * 0.5f, y + height * 0.6f,
            x, y + height * 0.8f,
            x, y + height
        );
        path.cubicTo(
            x, y + height * 0.8f,
            x + width * 0.5f, y + height * 0.6f,
            x + width * 0.5f, y + height * 0.3f
        );
        path.cubicTo(
            x + width * 0.5f, y,
            x, y,
            x, y + height * 0.3f
        );
        
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
    
    private static class Shape {
        int type;
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