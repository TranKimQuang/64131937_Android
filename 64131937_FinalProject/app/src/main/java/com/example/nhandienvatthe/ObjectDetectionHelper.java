package com.example.nhandienvatthe;

import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;

import java.util.ArrayList;
import java.util.List;

public class ObjectDetectionHelper {

    private ObjectDetector objectDetector;

    public ObjectDetectionHelper() {
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .build();

        objectDetector = ObjectDetection.getClient(options);
    }

    public void detectObjects(InputImage image, ObjectDetectionListener listener) {
        objectDetector.process(image)
                .addOnSuccessListener(detectedObjects -> {
                    List<MyDetectedObject> myDetectedObjects = new ArrayList<>();
                    for (DetectedObject detectedObject : detectedObjects) {
                        Rect boundingBox = detectedObject.getBoundingBox();
                        Integer confidence = (int) (detectedObject.getLabels().isEmpty() ? 0 : detectedObject.getLabels().get(0).getConfidence() * 100);
                        List<DetectedObject.Label> labels = detectedObject.getLabels();
                        MyDetectedObject myDetectedObject = new MyDetectedObject(boundingBox, confidence, labels);
                        myDetectedObjects.add(myDetectedObject);

                        Log.d("DetectedObject", "Bounding Box: " + boundingBox + ", Confidence: " + confidence);
                    }
                    listener.onSuccess(myDetectedObjects);
                })
                .addOnFailureListener(e -> {
                    Log.e("ObjectDetectionError", "Detection failed", e);
                    listener.onFailure(e);
                });
    }

    public interface ObjectDetectionListener {
        void onSuccess(List<MyDetectedObject> myDetectedObjects);
        void onFailure(Exception e);
    }
}
