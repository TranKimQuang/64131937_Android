package com.ObjDetec.nhandienvatthe.Activity;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Util.ChatbotHelper;
import com.ObjDetec.nhandienvatthe.Util.ObjectDetectionHelper;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetector;

import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // AR components
    private ArFragment arFragment;

    // Google ML Kit components
    private ObjectDetector objectDetector;
    private TextView tvResult;
    // UI components
    private PreviewView previewView;


    // CameraX components
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    // TensorFlow Lite components
    private TensorFlowHelper tensorFlowHelper;

    // TextToSpeech
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;

    // Helpers
    private ObjectDetectionHelper objectDetectionHelper;
    private ChatbotHelper chatbotHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        previewView = findViewById(R.id.viewFinder);
        tvResult = findViewById(R.id.tvResult);

        // Initialize TensorFlowHelper with multiple models
        String[] modelPaths = {"model1.tflite", "model2.tflite"}; // Add your model paths here
        tensorFlowHelper = new TensorFlowHelper(this, modelPaths);

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Initialize ObjectDetectionHelper and ChatbotHelper
        objectDetectionHelper = new ObjectDetectionHelper();
        chatbotHelper = new ChatbotHelper();

        // Initialize TextToSpeech
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
                            isSpeaking = false; // Update state when done speaking
                            Log.d(TAG, "TextToSpeech completed: " + utteranceId);
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        synchronized (MainActivity.this) {
                            isSpeaking = false; // Update state on error
                            Log.e(TAG, "TextToSpeech error: " + utteranceId);
                        }
                    }
                });
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Start camera
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // Bind camera lifecycle to the lifecycle owner
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Camera setup error: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        // Set up the preview use case
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Set up the image analysis use case
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @OptIn(markerClass = ExperimentalGetImage.class)
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                // Convert ImageProxy to InputImage
                InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                // Run inference using multiple models
                runInference(image);

                // Close the ImageProxy
                imageProxy.close();
            }
        });

        // Bind use cases to the camera
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    private void runInference(InputImage image) {
        // Preprocess the image (convert to float array, resize, etc.)
        float[][] input = preprocessImage(image);

        // Run inference using model 1
        Interpreter interpreter1 = tensorFlowHelper.getInterpreter("model1.tflite");
        if (interpreter1 != null) {
            float[][] output1 = new float[1][1000]; // Adjust output size based on your model
            interpreter1.run(input, output1);
            String result1 = "Model 1 Output: " + Arrays.toString(output1[0]);
            Log.d(TAG, result1);
            tvResult.setText(result1);
            readTextAloud(result1);
        } else {
            Log.e(TAG, "Model 1 Interpreter is null. Model loading failed.");
        }

        // Run inference using model 2
        Interpreter interpreter2 = tensorFlowHelper.getInterpreter("model2.tflite");
        if (interpreter2 != null) {
            float[][] output2 = new float[1][1000]; // Adjust output size based on your model
            interpreter2.run(input, output2);
            String result2 = "Model 2 Output: " + Arrays.toString(output2[0]);
            Log.d(TAG, result2);
            tvResult.append("\n" + result2);
            readTextAloud(result2);
        } else {
            Log.e(TAG, "Model 2 Interpreter is null. Model loading failed.");
        }
    }

    private float[][] preprocessImage(InputImage image) {
        // Implement your image preprocessing logic here
        // For example, resize the image to 224x224 and convert it to a float array
        float[][] input = new float[1][224 * 224 * 3]; // Adjust input size based on your model
        // Add your preprocessing code here
        return input;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown camera executor
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }

        // Close TensorFlow Lite interpreters
        if (tensorFlowHelper != null) {
            tensorFlowHelper.close();
        }

        // Shutdown TextToSpeech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    // Helper class to manage TensorFlow Lite models
    private static class TensorFlowHelper {

        private final Map<String, Interpreter> interpreters = new HashMap<>();

        public TensorFlowHelper(Context context, String[] modelPaths) {
            for (String modelPath : modelPaths) {
                try {
                    MappedByteBuffer modelBuffer = loadModelFile(context, modelPath);
                    Interpreter interpreter = new Interpreter(modelBuffer);
                    interpreters.put(modelPath, interpreter);
                    Log.d(TAG, "Model loaded successfully: " + modelPath);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to load model: " + modelPath, e);
                }
            }
        }

        private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
            FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
            FileChannel fileChannel = fileInputStream.getChannel();
            long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
            long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }

        public Interpreter getInterpreter(String modelPath) {
            return interpreters.get(modelPath);
        }

        public void close() {
            for (Interpreter interpreter : interpreters.values()) {
                if (interpreter != null) {
                    interpreter.close();
                }
            }
            Log.d(TAG, "All interpreters closed");
        }

    }
}