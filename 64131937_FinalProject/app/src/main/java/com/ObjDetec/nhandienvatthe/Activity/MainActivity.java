package com.ObjDetec.nhandienvatthe.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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

import java.io.File;
import java.util.ArrayList;
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
    private MyDetectedObject currentObject = null;
    private boolean isQRCodeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần
        boundingBoxView = findViewById(R.id.boundingBoxView);
        Switch switchQRCodeMode = findViewById(R.id.switchQRCodeMode);
        Button takeObjButton = findViewById(R.id.takeObj);
        Button libraryButton = findViewById(R.id.library);

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Khởi tạo CameraManager
        cameraManager = new CameraManager(this, findViewById(R.id.viewFinder), Executors.newSingleThreadExecutor(), this);

        // Khởi tạo ObjectDetectionManager
        objectDetectionManager = new ObjectDetectionManager();

        // Khởi tạo TextToSpeechManager
        textToSpeechManager = new TextToSpeechManager(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TextToSpeech is ready");
                textToSpeechManager.setLanguage(Locale.getDefault());
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Thiết lập sự kiện cho Switch
        switchQRCodeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isQRCodeMode = isChecked;
            if (isQRCodeMode) {
                Log.d(TAG, "QR Code Mode: ON");
            } else {
                Log.d(TAG, "QR Code Mode: OFF");
            }
        });

        // Thiết lập sự kiện cho nút Chụp
        takeObjButton.setOnClickListener(v -> {
            File libraryFolder = createLibraryFolder();
            String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
            File photoFile = new File(libraryFolder, fileName);

            cameraManager.takePicture(photoFile, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Ảnh đã được lưu: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.e(TAG, "Lỗi khi chụp ảnh: ", exception);
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        // Thiết lập sự kiện cho nút Thư viện
        libraryButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LibraryActivity.class);
            startActivity(intent);
        });

        // Thiết lập camera và nhận diện vật thể
        setupCameraAndDetection();

        // Quan sát LiveData để cập nhật UI
        viewModel.getDetectedObjects().observe(this, this::updateUI);
    }

    private File createLibraryFolder() {
        File libraryFolder = new File(getExternalFilesDir(null), "library");
        if (!libraryFolder.exists()) {
            libraryFolder.mkdirs();
        }
        return libraryFolder;
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void setupCameraAndDetection() {
        cameraManager.setupCamera(imageProxy -> {
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();

            if (isQRCodeMode) {
                // Chỉ quét QR code khi chế độ QR code được bật
                scanQRCode(image);
            } else {
                // Chỉ nhận diện vật thể khi chế độ QR code tắt
                objectDetectionManager.detectObjects(image, imageProxy, new ObjectDetectionManager.ObjectDetectionListener() {
                    @Override
                    public void onSuccess(List<MyDetectedObject> myDetectedObjects) {
                        viewModel.setDetectedObjects(myDetectedObjects);

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
            }
        });
    }

    private void updateUI(List<MyDetectedObject> myDetectedObjects) {
        StringBuilder resultText = new StringBuilder();
        for (MyDetectedObject object : myDetectedObjects) {
            String label = object.getLabels().isEmpty() ? "Unknown" : object.getLabels().get(0).getText();
            String translatedLabel = LabelTranslator.translateLabel(label);
            int confidence = object.getConfidence();
            resultText.append(translatedLabel).append(" (").append(confidence).append("%)\n");

            // Chỉ đọc label nếu vật thể mới khác với vật thể hiện tại
            if (currentObject == null || !object.equals(currentObject)) {
                Log.d(TAG, "New object detected: " + translatedLabel);
                currentObject = object; // Cập nhật vật thể hiện tại
                textToSpeechManager.speak(translatedLabel, TextToSpeech.QUEUE_FLUSH, null, "tts1");
            }
        }

        // Hiển thị kết quả trên TextView
        TextView tvResult = findViewById(R.id.tvResult);
        tvResult.setText(resultText.toString());
    }

    private void scanQRCode(InputImage image) {
        // Logic quét QR code
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraManager.shutdown();
        textToSpeechManager.shutdown();
    }
}