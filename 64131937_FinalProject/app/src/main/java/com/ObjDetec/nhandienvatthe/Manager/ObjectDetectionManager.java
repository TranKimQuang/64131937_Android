package com.ObjDetec.nhandienvatthe.Manager;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.ObjDetec.nhandienvatthe.Util.ObjectDetectionHelper;
import com.google.mlkit.vision.common.InputImage;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import java.util.List;

public class ObjectDetectionManager {

    private static final String TAG = "ObjectDetectionManager";
    private ObjectDetectionHelper objectDetectionHelper;

    public ObjectDetectionManager() {
        objectDetectionHelper = new ObjectDetectionHelper();
    }

    public void detectObjects(InputImage image, ImageProxy imageProxy, ObjectDetectionListener listener) {
        objectDetectionHelper.detectObjects(image, new ObjectDetectionHelper.ObjectDetectionListener() {
            @Override
            public void onSuccess(List<MyDetectedObject> myDetectedObjects) {
                listener.onSuccess(myDetectedObjects);
                imageProxy.close();
            }

            @Override
            public void onFailure(Exception e) {
                listener.onFailure(e);
                imageProxy.close();
            }
        });
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    public ImageAnalysis.Analyzer getImageAnalyzer(ObjectDetectionListener listener) {
        return imageProxy -> {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            detectObjects(image, imageProxy, listener);
        };
    }

    public interface ObjectDetectionListener {
        void onSuccess(List<MyDetectedObject> myDetectedObjects);
        void onFailure(Exception e);
    }
}