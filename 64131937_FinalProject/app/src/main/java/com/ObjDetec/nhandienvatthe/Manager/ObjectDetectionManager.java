package com.ObjDetec.nhandienvatthe.Manager;

import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import com.ObjDetec.nhandienvatthe.Util.ObjectDetectionHelper;
import com.google.mlkit.vision.common.InputImage;
import java.util.List;

public class ObjectDetectionManager {

    private final ObjectDetectionHelper objectDetectionHelper;

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

    public interface ObjectDetectionListener {
        void onSuccess(List<MyDetectedObject> myDetectedObjects);
        void onFailure(Exception e);
    }
}