package com.ObjDetec.nhandienvatthe.Activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.ObjDetec.nhandienvatthe.Manager.CameraManager;
import com.ObjDetec.nhandienvatthe.Manager.ObjectDetectionManager;
import com.ObjDetec.nhandienvatthe.Manager.TextToSpeechManager;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Util.LabelTranslator;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PreviewView previewView;
    private TextView tvResult;
    private ExecutorService cameraExecutor;
    private CameraManager cameraManager;
    private ObjectDetectionManager objectDetectionManager;
    private TextToSpeechManager textToSpeechManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các view
        previewView = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvResult);

        // Khởi tạo ExecutorService cho camera
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Khởi tạo các manager
        cameraManager = new CameraManager(this, previewView, cameraExecutor, this);
        objectDetectionManager = new ObjectDetectionManager();
        textToSpeechManager = new TextToSpeechManager(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeechManager.setLanguage(Locale.getDefault());
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Thiết lập camera và nhận diện vật thể
        setupCameraAndDetection();
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void setupCameraAndDetection() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Khởi tạo Preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Khởi tạo ImageAnalysis
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Thiết lập ImageAnalysis.Analyzer
                imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                    InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    objectDetectionManager.detectObjects(image, imageProxy, new ObjectDetectionManager.ObjectDetectionListener() {
                        @Override
                        public void onSuccess(List<MyDetectedObject> myDetectedObjects) {
                            // Xử lý kết quả nhận diện vật thể
                            StringBuilder resultText = new StringBuilder("Detected Objects:\n");
                            for (MyDetectedObject object : myDetectedObjects) {
                                String label = object.getLabels().isEmpty() ? "Unknown" : object.getLabels().get(0).getText();
                                String translatedLabel = LabelTranslator.translateLabel(label);
                                resultText.append(translatedLabel).append(" (").append(object.getConfidence()).append("%)\n");
                            }
                            runOnUiThread(() -> tvResult.setText(resultText.toString()));
                            textToSpeechManager.speak(resultText.toString());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "Object detection failed: ", e);
                        }
                    });
                });

                // Gắn các use case vào lifecycle
                cameraProvider.bindToLifecycle(
                        this,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                );
            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng và giải phóng tài nguyên
        cameraExecutor.shutdown();
        textToSpeechManager.shutdown();
    }
}