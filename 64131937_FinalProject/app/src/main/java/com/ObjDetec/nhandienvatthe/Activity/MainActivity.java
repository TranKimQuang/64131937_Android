package com.ObjDetec.nhandienvatthe.Activity;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Util.ChatbotHelper;
import com.ObjDetec.nhandienvatthe.Util.ObjectDetectionHelper;
import com.google.common.util.concurrent.ListenableFuture;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PreviewView previewView;
    private TextView tvResult;
    private ExecutorService cameraExecutor;
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;
    private TensorFlowHelper tensorFlowHelper;
    private ObjectDetectionHelper objectDetectionHelper;
    private ChatbotHelper chatbotHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo các thành phần UI
        previewView = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvResult);

        // Khởi tạo TensorFlowHelper để tải mô hình
        tensorFlowHelper = new TensorFlowHelper(this);

        // Khởi tạo camera và các thành phần khác
        cameraExecutor = Executors.newSingleThreadExecutor();
        setupCamera();

        // Khởi tạo ObjectDetectionHelper và ChatbotHelper
        objectDetectionHelper = new ObjectDetectionHelper();
        chatbotHelper = new ChatbotHelper();

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

        // Thực hiện suy luận với TensorFlow Lite
        performInference();
    }

    private void performInference() {
        // Lấy Interpreter từ TensorFlowHelper
        Interpreter interpreter = tensorFlowHelper.getInterpreter();
        if (interpreter != null) {
            // Tạo dữ liệu đầu vào giả lập (thay đổi kích thước phù hợp với mô hình)
            float[][] input = new float[1][224]; // Ví dụ đầu vào
            float[][] output = new float[1][1000]; // Ví dụ đầu ra

            // Thực hiện suy luận
            interpreter.run(input, output);

            // Hiển thị kết quả
            String result = "Kết quả: " + Arrays.toString(output[0]);
            Log.d(TAG, result);
            tvResult.setText(result);

            // Đọc kết quả bằng TextToSpeech
            readTextAloud(result);
        } else {
            Log.e(TAG, "Interpreter is null. Model loading failed.");
            tvResult.setText("Lỗi khi tải mô hình.");
        }
    }

    private void readTextAloud(String text) {
        if (text != null && textToSpeech != null && !isSpeaking) {
            Log.d(TAG, "Reading text aloud: " + text);
            isSpeaking = true;
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts1");
        } else {
            Log.e(TAG, "Text or TextToSpeech is null or already speaking.");
        }
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

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                // Xử lý hình ảnh từ camera (nếu cần)
                imageProxy.close();
            }
        });

        cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
        );
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

    // Lớp TensorFlowHelper để tải và quản lý mô hình TensorFlow Lite
    private static class TensorFlowHelper {
        private Interpreter interpreter;

        public TensorFlowHelper(Context context) {
            try {
                // Tải mô hình từ assets
                MappedByteBuffer modelBuffer = loadModelFile(context, "model.tflite");
                interpreter = new Interpreter(modelBuffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
            // Mở file từ assets
            FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
            long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }

        public Interpreter getInterpreter() {
            return interpreter;
        }
    }
}