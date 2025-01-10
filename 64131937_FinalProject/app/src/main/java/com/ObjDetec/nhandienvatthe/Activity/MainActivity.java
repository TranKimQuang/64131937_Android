package com.ObjDetec.nhandienvatthe.Activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.ViewModelProvider;
import com.ObjDetec.nhandienvatthe.Manager.CameraManager;
import com.ObjDetec.nhandienvatthe.Manager.ObjectDetectionManager;
import com.ObjDetec.nhandienvatthe.Manager.TextToSpeechManager;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Util.LabelTranslator;
import com.ObjDetec.nhandienvatthe.ViewModel.MainViewModel;
import com.ObjDetec.nhandienvatthe.View.BoundingBoxView;
import com.google.mlkit.vision.common.InputImage;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private MainViewModel viewModel;
    private CameraManager cameraManager;
    private ObjectDetectionManager objectDetectionManager;
    private TextToSpeechManager textToSpeechManager;
    private BoundingBoxView boundingBoxView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo BoundingBoxView
        boundingBoxView = findViewById(R.id.boundingBoxView);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Khởi tạo các manager
        cameraManager = new CameraManager(this, findViewById(R.id.viewFinder), Executors.newSingleThreadExecutor(), this);
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

        // Quan sát LiveData để cập nhật UI
        viewModel.getDetectedObjects().observe(this, this::updateUI);
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void setupCameraAndDetection() {
        cameraManager.setupCamera(imageProxy -> {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            objectDetectionManager.detectObjects(image, imageProxy, new ObjectDetectionManager.ObjectDetectionListener() {
                @Override
                public void onSuccess(List<MyDetectedObject> myDetectedObjects) {
                    viewModel.setDetectedObjects(myDetectedObjects);

                    // Truyền kích thước hình ảnh và danh sách vật thể vào BoundingBoxView
                    if (boundingBoxView != null) {
                        boundingBoxView.setImageDimensions(imageWidth, imageHeight);
                        boundingBoxView.setDetectedObjects(myDetectedObjects);
                    } else {
                        Log.e(TAG, "BoundingBoxView is null");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Object detection failed: ", e);
                }
            });
        });
    }

    private void updateUI(List<MyDetectedObject> myDetectedObjects) {
        StringBuilder resultText = new StringBuilder();
        for (MyDetectedObject object : myDetectedObjects) {
            String label = object.getLabels().isEmpty() ? "Unknown" : object.getLabels().get(0).getText();
            String translatedLabel = LabelTranslator.translateLabel(label);
            int confidence = object.getConfidence();
            resultText.append(getString(R.string.detected_object_label, translatedLabel, confidence)).append("\n");

            // Đọc label ngay khi xác định được vật thể
            if (!textToSpeechManager.isSpeaking()) {
                textToSpeechManager.speak(translatedLabel);
            }
        }
        ((TextView) findViewById(R.id.tvResult)).setText(resultText.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng và giải phóng tài nguyên
        cameraManager.shutdown();
        textToSpeechManager.shutdown();
    }
}