package com.example.personafi;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.text.DecimalFormat;

public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private RectF circleRect;
    private float progress = 0;
    private float strokeWidth = 20;
    private int progressColor = Color.BLUE;
    private int backgroundColor = Color.LTGRAY;
    private float innerPadding = 10f;
    private ValueAnimator progressAnimator;

    public CircularProgressView(Context context) {
        super(context);
        init(null);
    }

    public CircularProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CircularProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        circleRect = new RectF();

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CircularProgressView);
            progressColor = a.getColor(R.styleable.CircularProgressView_progressColor, progressColor);
            backgroundColor = a.getColor(R.styleable.CircularProgressView_backgroundColor, backgroundColor);
            strokeWidth = a.getDimension(R.styleable.CircularProgressView_strokeWidth, strokeWidth);
            progress = a.getFloat(R.styleable.CircularProgressView_progress, progress);
            a.recycle();

            backgroundPaint.setStrokeWidth(strokeWidth);
            backgroundPaint.setColor(backgroundColor);
            progressPaint.setStrokeWidth(strokeWidth);
            progressPaint.setColor(progressColor);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        // Calculate the dimensions for the progress arc
        // Apply padding to create space between the stroke and the edges
        // We need extra space for the rounded caps
        float totalPadding = strokeWidth + innerPadding * 2;
        
        float diameter = Math.min(w, h) - totalPadding;
        float centerX = w / 2f;
        float centerY = h / 2f;
        
        circleRect.set(
            centerX - diameter / 2,
            centerY - diameter / 2,
            centerX + diameter / 2,
            centerY + diameter / 2
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Define the angles to create a gap at the bottom center
        float startAngle = 135; // Start from bottom-left
        float arcSweepAngle = 270; // 3/4 of a circle, leaving the bottom center open

        // Draw background 3/4 circle
        canvas.drawArc(circleRect, startAngle, arcSweepAngle, false, backgroundPaint);

        // Draw progress arc - adjusted to fill only within the 3/4 circle
        float progressSweepAngle = arcSweepAngle * progress / 100f;
        canvas.drawArc(circleRect, startAngle, progressSweepAngle, false, progressPaint);
    }

    /**
     * Set the progress value with animation
     *
     * @param progress Progress value (0-100)
     */
    public void setProgress(float progress) {
        // Ensure progress is in valid range
        final float validProgress = Math.max(0, Math.min(100, progress));
        
        // Cancel any ongoing animations
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        
        // Animate progress change
        progressAnimator = ValueAnimator.ofFloat(this.progress, validProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            this.progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        progressAnimator.start();
    }

    /**
     * Set progress without animation
     */
    public void setProgressImmediately(float progress) {
        this.progress = Math.max(0, Math.min(100, progress));
        invalidate();
    }

    /**
     * Get current progress value
     */
    public float getProgress() {
        return progress;
    }

    /**
     * Set the inner padding to create space between the stroke and the edges
     */
    public void setInnerPadding(float padding) {
        this.innerPadding = padding;
        // Force recalculation of circle dimensions
        requestLayout();
    }

    /**
     * Format large numbers in a human-readable format:
     * - Less than 1M: Format as regular number with commas (e.g., 1,234)
     * - Millions: Format as X.YM (e.g., 1.5M)
     * - Billions: Format as X.YB (e.g., 1.2B)
     * - Trillions: Format as X.YT (e.g., 1.8T)
     *
     * @param value The number to format
     * @return The formatted string
     */
    public static String formatLargeNumber(double value) {
        if (value == 0) {
            return "0";
        }
        
        // Handle negative values
        boolean isNegative = value < 0;
        double absValue = Math.abs(value);
        
        // Define thresholds in ascending order
        final double MILLION = 1_000_000;
        final double BILLION = 1_000_000_000;
        final double TRILLION = 1_000_000_000_000D;
        
        String formattedValue;
        
        DecimalFormat df = new DecimalFormat("#,###");
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.0");
        
        if (absValue >= TRILLION) {
            // Format as X.YT (trillions)
            formattedValue = decimalFormat.format(absValue / TRILLION) + "T";
        } else if (absValue >= BILLION) {
            // Format as X.YB (billions)
            formattedValue = decimalFormat.format(absValue / BILLION) + "B";
        } else if (absValue >= MILLION) {
            // Format as X.YM (millions)
            formattedValue = decimalFormat.format(absValue / MILLION) + "M";
        } else {
            // Format as regular number with commas
            formattedValue = df.format(absValue);
        }
        
        // Add negative sign if needed
        if (isNegative) {
            formattedValue = "-" + formattedValue;
        }
        
        return formattedValue;
    }
}