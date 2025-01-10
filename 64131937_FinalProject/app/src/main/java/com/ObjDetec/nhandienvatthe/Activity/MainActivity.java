package com.ObjDetec.nhandienvatthe.Activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    private MyDetectedObject currentObject = null; // Lưu trữ vật thể hiện tại

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
                Log.d(TAG, "TextToSpeech is ready");
                textToSpeechManager.setLanguage(Locale.getDefault());

                // Thiết lập UtteranceProgressListener để theo dõi khi nào hoàn thành việc đọc
                textToSpeechManager.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "Started reading: " + utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "Finished reading: " + utteranceId);
                        // Cho phép đọc label tiếp theo
                        textToSpeechManager.setSpeaking(false);
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "Error reading: " + utteranceId);
                        // Cho phép đọc label tiếp theo
                        textToSpeechManager.setSpeaking(false);
                    }
                });
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });

        // Thiết lập Spinner để chọn ngôn ngữ
        Spinner languageSpinner = findViewById(R.id.languageSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.languages,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);

        // Xử lý sự kiện khi người dùng chọn ngôn ngữ
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] languageCodes = getResources().getStringArray(R.array.language_codes);
                String selectedLanguage = languageCodes[position];
                LabelTranslator.setLanguage(selectedLanguage); // Cập nhật ngôn ngữ trong LabelTranslator
                textToSpeechManager.setLanguage(new Locale(selectedLanguage)); // Cập nhật ngôn ngữ trong TextToSpeech
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không làm gì
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng và giải phóng tài nguyên
        cameraManager.shutdown();
        textToSpeechManager.shutdown();
    }
}