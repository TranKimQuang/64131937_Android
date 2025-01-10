package com.ObjDetec.nhandienvatthe.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageProxy;
import androidx.lifecycle.ViewModelProvider;

import com.ObjDetec.nhandienvatthe.Manager.CameraManager;
import com.ObjDetec.nhandienvatthe.Manager.ObjectDetectionManager;
import com.ObjDetec.nhandienvatthe.Manager.QRCodeScannerManager;
import com.ObjDetec.nhandienvatthe.Manager.TextToSpeechManager;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Util.LabelTranslator;
import com.ObjDetec.nhandienvatthe.ViewModel.MainViewModel;
import com.ObjDetec.nhandienvatthe.View.BoundingBoxView;
import com.google.mlkit.vision.common.InputImage;

import java.net.MalformedURLException;
import java.net.URL;
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
    private MyDetectedObject currentObject = null; // Lưu trữ vật thể hiện tại
    private QRCodeScannerManager qrCodeScannerManager;
    private Switch switchQRCodeMode;
    private boolean isQRCodeMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeScannerManager = new QRCodeScannerManager();

        // Khởi tạo BoundingBoxView
        boundingBoxView = findViewById(R.id.boundingBoxView);

        // Khởi tạo Switch
        switchQRCodeMode = findViewById(R.id.switchQRCodeMode);
        switchQRCodeMode.setChecked(false); // Mặc định là OFF

        // Thiết lập sự kiện khi Switch thay đổi trạng thái
        switchQRCodeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isQRCodeMode = isChecked;
            if (isQRCodeMode) {
                Log.d(TAG, "QR Code Mode: ON");
                // Xóa kết quả nhận diện vật thể nếu có
                viewModel.setDetectedObjects(new ArrayList<>());
                boundingBoxView.setDetectedObjects(new ArrayList<>());
                boundingBoxView.invalidate(); // Xóa bounding box trên màn hình
            } else {
                Log.d(TAG, "QR Code Mode: OFF");
            }

            // Thiết lập lại camera và chế độ nhận diện
            setupCameraAndDetection();
        });

        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Khởi tạo các manager
        cameraManager = new CameraManager(this, findViewById(R.id.viewFinder), Executors.newSingleThreadExecutor(), this);        objectDetectionManager = new ObjectDetectionManager();
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
        qrCodeScannerManager.scanQRCode(image, new QRCodeScannerManager.QRCodeScanListener() {
            @Override
            public void onQRCodeScanned(String qrCodeValue) {
                Log.d(TAG, "QR Code Scanned: " + qrCodeValue);
                runOnUiThread(() -> {
                    TextView tvResult = findViewById(R.id.tvResult);
                    tvResult.setText("QR Code: " + qrCodeValue);
                    textToSpeechManager.speak("QR Code detected: " + qrCodeValue, TextToSpeech.QUEUE_FLUSH, null, "tts1");

                    // Kiểm tra xem QR code có phải là một link hợp lệ không
                    if (isValidUrl(qrCodeValue)) {
                        // Mở link trong trình duyệt
                        openUrlInBrowser(qrCodeValue);
                    }
                });
            }

            @Override
            public void onQRCodeScanFailed(String errorMessage) {
                // Không hiển thị gì nếu không quét được QR code
                Log.d(TAG, "No QR Code detected");
            }
        });
    }

    // Phương thức kiểm tra xem chuỗi có phải là một URL hợp lệ không
    private boolean isValidUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    // Phương thức mở URL trong trình duyệt
    private void openUrlInBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng và giải phóng tài nguyên
        cameraManager.shutdown();
        textToSpeechManager.shutdown();
    }
}