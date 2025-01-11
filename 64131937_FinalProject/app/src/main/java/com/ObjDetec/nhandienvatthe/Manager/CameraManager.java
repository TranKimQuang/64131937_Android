package com.ObjDetec.nhandienvatthe.Manager;

import android.content.Context;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutorService;

public class CameraManager {
    private static final String TAG = "CameraManager";
    private Context context;
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private LifecycleOwner lifecycleOwner;
    private ImageCapture imageCapture;

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

                // Thiết lập Preview
                Preview preview = new Preview.Builder()
                        .setTargetResolution(new Size(1024, 768))
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Thiết lập ImageAnalysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1024, 768))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                // Thiết lập ImageCapture
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Liên kết các use case với lifecycle
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind các use case
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis, imageCapture);

            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void takePicture(File file, ImageCapture.OnImageSavedCallback callback) {
        if (imageCapture != null) {
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, cameraExecutor, callback);
        }
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }
}