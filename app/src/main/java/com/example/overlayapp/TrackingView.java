package com.example.overlayapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class TrackingView extends View {
    private Paint targetPaint;
    private Paint textPaint;
    private Paint boxPaint;
    private float targetX = 300;
    private float targetY = 300;
    private float targetRadius = 100;
    private float dx = 5;
    private float dy = 3;
    private ObjectDetector detector;
    private boolean isDetecting = false;
    private Handler handler;
    private static final int FRAME_RATE = (int) (1000 / 30);
    private Rect boundingBox;
    private String detectedObject = "Object";
    private float confidence = 0.0f;

    public TrackingView(Context context) {
        super(context);
        detector = new ObjectDetector(context);
        setupPaints();
        handler = new Handler(Looper.getMainLooper());
        startTracking();
    }

    private void setupPaints() {
        targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        targetPaint.setColor(Color.RED);
        targetPaint.setStyle(Paint.Style.STROKE);
        targetPaint.setStrokeWidth(5);
        
        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setColor(Color.GREEN);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(3);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
        textPaint.setShadowLayer(3, 1, 1, Color.BLACK);
    }
    
    private void startTracking() {
        updatePosition();
    }
    
    private void updatePosition() {
        handler.postDelayed(() -> {
            updateTargetPosition();
            invalidate();
            updatePosition();
        }, FRAME_RATE);
    }
    
    private void updateTargetPosition() {
        targetX += dx;
        targetY += dy;
        
        if (targetX - targetRadius < 0 || targetX + targetRadius > getWidth()) {
            dx = -dx;
        }
        
        if (targetY - targetRadius < 0 || targetY + targetRadius > getHeight()) {
            dy = -dy;
        }
        
        if (Math.random() < 0.05) {
            isDetecting = true;
            boundingBox = new Rect(
                (int)(targetX - targetRadius * 1.2),
                (int)(targetY - targetRadius * 1.2),
                (int)(targetX + targetRadius * 1.2),
                (int)(targetY + targetRadius * 1.2)
            );
            confidence = (float)(0.7 + Math.random() * 0.3);
        } else {
            isDetecting = false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(220, 0, 0, 0));
        canvas.drawCircle(targetX, targetY, targetRadius, targetPaint);
        canvas.drawLine(
            targetX - targetRadius, targetY, 
            targetX + targetRadius, targetY, 
            targetPaint
        );
        canvas.drawLine(
            targetX, targetY - targetRadius, 
            targetX, targetY + targetRadius, 
            targetPaint
        );
        
        if (isDetecting && boundingBox != null) {
            canvas.drawRect(boundingBox, boxPaint);
            String label = String.format("%s: %.0f%%", detectedObject, confidence * 100);
            canvas.drawText(label, boundingBox.left, boundingBox.top - 10, textPaint);
        }
        
        canvas.drawText("Tracking Demo", 20, 60, textPaint);
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacksAndMessages(null);
        if (detector != null) {
            detector.close();
            detector = null;
        }
    }
}
