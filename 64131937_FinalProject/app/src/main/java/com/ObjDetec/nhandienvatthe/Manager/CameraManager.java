package com.ObjDetec.nhandienvatthe.Manager;

import android.content.Context;
import android.util.Log;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutorService;

public class CameraManager {

    private static final String TAG = "CameraManager";
    private Context context;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;

    public CameraManager(Context context, PreviewView previewView, ExecutorService cameraExecutor) {
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = cameraExecutor;
    }

    public void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraProvider.bindToLifecycle(
                (LifecycleOwner) context,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalysis
        );
    }

    public ImageCapture getImageCapture() {
        return imageCapture;
    }
}