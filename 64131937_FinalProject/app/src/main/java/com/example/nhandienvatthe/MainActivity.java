package com.example.nhandienvatthe;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.camera.view.PreviewView;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import android.util.Size;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private TextToSpeech textToSpeech;
    private ExecutorService cameraExecutor;
    private Button btnStartDetection;  // Nút bắt đầu nhận diện
    private PreviewView viewFinder;  // Preview camera feed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo Text-to-Speech
        textToSpeech = new TextToSpeech(this, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int langResult = textToSpeech.setLanguage(Locale.getDefault());
                    if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("MainActivity", "Ngôn ngữ không được hỗ trợ hoặc thiếu dữ liệu");
                    } else {
                        Log.d("MainActivity", "Khởi tạo Text-to-Speech thành công");
                    }
                } else {
                    Log.e("MainActivity", "Khởi tạo thất bại");
                }
            }
        });

        // Khởi tạo Camera Executor cho CameraX
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Lấy tham chiếu đến nút bắt đầu nhận diện
        btnStartDetection = findViewById(R.id.btnStartDetection);
        viewFinder = findViewById(R.id.viewFinder);

        // Thiết lập sự kiện click cho nút
        btnStartDetection.setOnClickListener(view -> {
            // Kiểm tra quyền sử dụng Camera trước khi bắt đầu nhận diện
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
            } else {
                startObjectDetection();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startObjectDetection();
            } else {
                Toast.makeText(this, "Cần cấp quyền sử dụng Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void startObjectDetection() {
        // Cấu hình cho Object Detection
        ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)  // Chế độ stream
                .enableMultipleObjects()  // Phát hiện nhiều đối tượng
                .build();

        ObjectDetector objectDetector = ObjectDetection.getClient(options);

        // Khởi tạo CameraX để lấy ảnh
        ProcessCameraProvider.getInstance(this).addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(MainActivity.this).get();

                    // Chọn camera (lưng)
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                    // Thiết lập ImageAnalysis use case
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1280, 720)) // Độ phân giải cho phân tích ảnh
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER) // Cấu hình backpressure
                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                        try {
                            // Chuyển đổi ImageProxy thành InputImage
                            InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());

                            // Phát hiện đối tượng
                            objectDetector.process(inputImage)
                                    .addOnSuccessListener(detectedObjects -> {
                                        for (DetectedObject detectedObject : detectedObjects) {
                                            // Lấy nhãn của đối tượng
                                            List<DetectedObject.Label> labels = detectedObject.getLabels();
                                            StringBuilder objectDescription = new StringBuilder();
                                            for (DetectedObject.Label label : labels) {
                                                objectDescription.append(label.getText()).append(" ");
                                            }

                                            // Phát âm mô tả của đối tượng
                                            speakOut(objectDescription.toString());
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("MainActivity", "Phát hiện đối tượng thất bại", e));
                        } finally {
                            image.close();
                        }
                    });

                    // Liên kết camera và ImageAnalysis với lifecycle
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageAnalysis);

                } catch (ExecutionException | InterruptedException e) {
                    Log.e("CameraX", "Khởi tạo camera thất bại", e);
                }
            }
        }, ContextCompat.getMainExecutor(this));  // Sử dụng Executor của UI
    }

    private void speakOut(String text) {
        if (textToSpeech != null && text != null && !text.isEmpty()) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
