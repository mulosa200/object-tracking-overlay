package com.example.overlayapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ObjectDetector {
    private static final String TAG = "ObjectDetector";
    private static final String MODEL_FILE = "model.tflite";
    
    private static final int INPUT_SIZE = 300;
    private static final int NUM_DETECTIONS = 10;
    private static final float DETECTION_THRESHOLD = 0.5f;
    
    private Interpreter tflite;
    private boolean isModelLoaded = false;
    private final Map<Integer, String> labelMap;
    
    public ObjectDetector(Context context) {
        labelMap = new HashMap<>();
        labelMap.put(0, "Background");
        labelMap.put(1, "Person");
        labelMap.put(2, "Car");
        
        try {
            MappedByteBuffer modelBuffer = FileUtil.loadMappedFile(context, MODEL_FILE);
            tflite = new Interpreter(modelBuffer);
            isModelLoaded = true;
            Log.d(TAG, "TFLite model loaded successfully");
        } catch (IOException e) {
            Log.e(TAG, "Error loading TFLite model: " + e.getMessage());
        }
    }
    
    public float[][] detectObjects(Bitmap bitmap) {
        if (!isModelLoaded || tflite == null) {
            return createDummyDetections();
        }
        
        try {
            ByteBuffer inputBuffer = convertBitmapToByteBuffer(bitmap);
            
            float[][][] outputLocations = new float[1][NUM_DETECTIONS][4];
            float[][] outputClasses = new float[1][NUM_DETECTIONS];
            float[][] outputScores = new float[1][NUM_DETECTIONS];
            float[] numDetections = new float[1];
            
            Map<Integer, Object> outputMap = new HashMap<>();
            outputMap.put(0, outputLocations);
            outputMap.put(1, outputClasses);
            outputMap.put(2, outputScores);
            outputMap.put(3, numDetections);
            
            tflite.runForMultipleInputsOutputs(new Object[]{inputBuffer}, outputMap);
            
            int numDetectionsOutput = Math.min(NUM_DETECTIONS, (int) numDetections[0]);
            float[][] results = new float[numDetectionsOutput][6];
            
            for (int i = 0; i < numDetectionsOutput; i++) {
                float score = outputScores[0][i];
                if (score > DETECTION_THRESHOLD) {
                    float top = outputLocations[0][i][0] * bitmap.getHeight();
                    float left = outputLocations[0][i][1] * bitmap.getWidth();
                    float bottom = outputLocations[0][i][2] * bitmap.getHeight();
                    float right = outputLocations[0][i][3] * bitmap.getWidth();
                    int classId = (int) outputClasses[0][i];
                    
                    results[i][0] = top;
                    results[i][1] = left;
                    results[i][2] = bottom;
                    results[i][3] = right;
                    results[i][4] = classId;
                    results[i][5] = score;
                }
            }
            
            return results;
        } catch (Exception e) {
            Log.e(TAG, "Error running object detection: " + e.getMessage());
            return createDummyDetections();
        }
    }
    
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1 * INPUT_SIZE * INPUT_SIZE * 3 * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        resizedBitmap.getPixels(intValues, 0, resizedBitmap.getWidth(), 0, 0, 
                               resizedBitmap.getWidth(), resizedBitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                int val = intValues[pixel++];
                byteBuffer.putFloat(((val >> 16) & 0xFF) / 255.0f);
                byteBuffer.putFloat(((val >> 8) & 0xFF) / 255.0f);
                byteBuffer.putFloat((val & 0xFF) / 255.0f);
            }
        }
        return byteBuffer;
    }
    
    private float[][] createDummyDetections() {
        float[][] dummy = new float[1][6];
        dummy[0][0] = 100;
        dummy[0][1] = 100;
        dummy[0][2] = 300;
        dummy[0][3] = 300;
        dummy[0][4] = 1;
        dummy[0][5] = 0.8f;
        return dummy;
    }
    
    public String getLabelForClass(int classId) {
        return labelMap.getOrDefault(classId, "Unknown");
    }
    
    public void close() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }
}
