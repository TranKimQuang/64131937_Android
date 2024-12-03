package com.ObjDetec.nhandienvatthe;

import static androidx.camera.core.ImageCaptureExtKt.takePicture;
import java.io.File;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PreviewView previewView;
    private TextView tvResult;

    private ObjectDetectionHelper objectDetectionHelper;
    private ExecutorService cameraExecutor;
    private ImageCapture imageCapture;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false; // Biến kiểm soát trạng thái đọc
    private String lastLabel = ""; // Biến lưu trữ nhãn cuối cùng đã được đọc
    private SpeechRecognizer speechRecognizer;
    private Button speakButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvResult);

        cameraExecutor = Executors.newSingleThreadExecutor();
        setupCamera();

        objectDetectionHelper = new ObjectDetectionHelper();

        // Khởi tạo TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "TextToSpeech started: " + utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        synchronized (MainActivity.this) {
                            isSpeaking = false; // Cập nhật trạng thái khi đọc xong
                            Log.d(TAG, "TextToSpeech completed: " + utteranceId);
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        synchronized (MainActivity.this) {
                            isSpeaking = false; // Cập nhật trạng thái khi gặp lỗi
                            Log.e(TAG, "TextToSpeech error: " + utteranceId);
                        }
                    }
                });
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });
    }
    private void setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "Ready for speech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "Speech beginning");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "Speech ended");
            }

            @Override
            public void onError(int error) {
                Log.e(TAG, "Speech error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.contains("chụp")) {
                    takePicture();
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        startSpeechRecognition();
    }
    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.startListening(intent);
    }
    private void takePicture() {
        File photoFile = createImageFile(); // Sử dụng phương thức tạo file

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                Log.d(TAG, "Photo saved to: " + photoFile.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage());
            }
        });
    }




    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                @SuppressWarnings("UnsafeOptInUsageError")
                ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
                if (planes.length > 0) {
                    InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
                    detectObjects(image, imageProxy);
                }
            }
        });

        cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture,
                imageAnalysis
        );
    }

    private void detectObjects(InputImage image, ImageProxy imageProxy) {
        objectDetectionHelper.detectObjects(image, new ObjectDetectionHelper.ObjectDetectionListener() {
            @Override
            public void onSuccess(List<MyDetectedObject> myDetectedObjects) {
                runOnUiThread(() -> {
                    updateDetectedObjects(myDetectedObjects);
                    imageProxy.close(); // Đóng imageProxy sau khi xử lý
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Object detection failed: ", e);
                runOnUiThread(() -> updateNoDetection());
                imageProxy.close(); // Đóng imageProxy ngay cả khi xảy ra lỗi
            }
        });
    }


    private File createImageFile() {
        File storageDir = new File(getExternalFilesDir(null), "Images");
        if (!storageDir.exists()) {
            storageDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }
        String fileName = "photo_" + System.currentTimeMillis() + ".jpg";
        return new File(storageDir, fileName);
    }

    private void updateNoDetection() {
        String noDetectionText = "Không nhận diện được đối tượng nào.";
        tvResult.setText(noDetectionText);
        Log.d(TAG, noDetectionText);
        isSpeaking = false; // Reset trạng thái khi không nhận diện được đối tượng nào
        lastLabel = ""; // Reset nhãn cuối cùng
    }

    private void updateDetectedObjects(List<MyDetectedObject> detectedObjects) {
        StringBuilder result = new StringBuilder();
        if (detectedObjects == null || detectedObjects.isEmpty()) {
            updateNoDetection();
        } else {
            for (MyDetectedObject myDetectedObject : detectedObjects) {
                Rect boundingBox = myDetectedObject.getBoundingBox();
                Integer confidence = myDetectedObject.getConfidence();
                List<com.google.mlkit.vision.objects.DetectedObject.Label> labels = myDetectedObject.getLabels();

                if (labels != null && !labels.isEmpty()) {
                    for (com.google.mlkit.vision.objects.DetectedObject.Label label : labels) {
                        String labelText = label.getText();
                        if (labelText != null) { // Kiểm tra null trước khi thêm vào result
                            switch (labelText) {
                                case "Food":
                                    labelText = "Thực phẩm";
                                    break;
                                case "Home good":
                                    labelText = "Đồ gia dụng";
                                    break;
                                case "Fashion good":
                                    labelText = "Đồ thời trang";
                                    break;
                                default:
                                    labelText = "Vật thể lạ";
                                    break;
                            }
                            result.append(labelText).append("\n");
                            Log.d(TAG, "Detected object: " + labelText); // Ghi Log
                            if (!isSpeaking && (!labelText.equals(lastLabel))) { // Chỉ đọc nếu nhãn khác với nhãn cuối cùng
                                lastLabel = labelText;
                                readTextAloud(labelText);
                            }
                        } else {
                            Log.d(TAG, "Detected object label text is null.");
                        }
                    }
                } else {
                    result.append("Vật thể lạ\n"); // Thông báo khi không có nhãn nào được nhận diện
                    Log.d(TAG, "No labels detected in the object.");
                    if (!isSpeaking && (!"Vật thể lạ".equals(lastLabel))) {
                        lastLabel = "Vật thể lạ";
                        readTextAloud("Vật thể lạ");
                    }
                }
            }
            tvResult.setText(result.toString());
        }
    }

    private synchronized void readTextAloud(String text) {
        if (text != null && textToSpeech != null && !isSpeaking) { // Kiểm tra null và trạng thái đọc
            Log.d(TAG, "Reading text aloud: " + text);
            isSpeaking = true;
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        } else {
            Log.d(TAG, "Text or TextToSpeech is null or already speaking.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        cameraExecutor.shutdown();
    }
}
