package com.ObjDetec.nhandienvatthe.Manager;

import android.content.Context;
import android.util.Log;
import android.util.Size;

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

                // Thiết lập Preview
                Preview preview = new Preview.Builder()
                        .setTargetResolution(new Size(1024, 768)) // Sử dụng kích thước phù hợp
                        .build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Thiết lập ImageAnalysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(1024, 768)) // Sử dụng cùng kích thước với Preview
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalysis.setAnalyzer(cameraExecutor, analyzer);

                // Liên kết các use case với lifecycle
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind các use case
                cameraProvider.unbindAll(); // Hủy liên kết tất cả các use case trước đó
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis);

            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    public void shutdown() {
        cameraExecutor.shutdown();
    }
}