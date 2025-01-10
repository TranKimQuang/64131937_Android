package com.ObjDetec.nhandienvatthe.Manager;

import android.content.Context;
import android.util.Log;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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
    private LifecycleOwner lifecycleOwner;

    public CameraManager(Context context, PreviewView previewView, ExecutorService cameraExecutor, LifecycleOwner lifecycleOwner) {
        this.context = context;
        this.previewView = previewView;
        this.cameraExecutor = cameraExecutor;
        this.lifecycleOwner = lifecycleOwner;
    }

    public void setupCamera(ImageAnalysis.Analyzer analyzer) {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }
}