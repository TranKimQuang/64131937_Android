package com.ObjDetec.nhandienvatthe.Activity;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
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

import java.io.File;
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
    private QRCodeScannerManager qrCodeScannerManager;
    private BoundingBoxView boundingBoxView;
    private MyDetectedObject currentObject = null;
    private boolean isQRCodeMode = false;
    private long lastQRProcessedTime = 0; // Thời gian lần cuối xử lý hình ảnh
    private static final long QR_PROCESS_INTERVAL = 5000;
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

        // Khởi tạo QRCodeScannerManager
        qrCodeScannerManager = new QRCodeScannerManager();

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

        // Thiết lập sự kiện cho Switch
        switchQRCodeMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isQRCodeMode = isChecked;
            if (isQRCodeMode) {
                Log.d(TAG, "QR Code Mode: ON");
            } else {
                Log.d(TAG, "QR Code Mode: OFF");
                // Xóa bounding box khi tắt chế độ QR Code
                boundingBoxView.setDetectedObjects(new ArrayList<>());
                boundingBoxView.invalidate(); // Vẽ lại view để xóa bounding box

                // Khởi động lại quá trình quét vật thể
                setupCameraAndDetection();
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
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastQRProcessedTime < QR_PROCESS_INTERVAL) {
                    imageProxy.close(); // Bỏ qua nếu chưa đủ thời gian
                    return;
                }
                lastQRProcessedTime = currentTime; // Cập nhật thời gian xử lý QR code cuối cùng

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

                // Kiểm tra xem TextToSpeech có đang phát âm thanh không
                if (!textToSpeechManager.isSpeaking()) {
                    textToSpeechManager.speak(translatedLabel, TextToSpeech.QUEUE_FLUSH, null, "tts1");
                }
            }
        }

        // Hiển thị kết quả trên TextView
        TextView tvResult = findViewById(R.id.tvResult);
        tvResult.setText(resultText.toString());
    }

    private void scanQRCode(InputImage image) {
        qrCodeScannerManager.scanQRCode(image, new QRCodeScannerManager.QRCodeScanListener() {
            @Override
            public void onQRCodeScanned(String qrCodeValue, Rect boundingBox) {
                Log.d(TAG, "QR Code Scanned: " + qrCodeValue);
                runOnUiThread(() -> {
                    TextView tvResult = findViewById(R.id.tvResult);
                    tvResult.setText("QR Code: " + qrCodeValue);
                    textToSpeechManager.speak(LabelTranslator.translateSystemMessage("QR Code detected"), TextToSpeech.QUEUE_FLUSH, null, "tts1");

                    // Vẽ bounding box xung quanh QR Code
                    if (boundingBoxView != null) {
                        boundingBoxView.setImageDimensions(image.getWidth(), image.getHeight());
                        List<MyDetectedObject> qrCodeObject = new ArrayList<>();
                        qrCodeObject.add(new MyDetectedObject(boundingBox, 100, new ArrayList<>())); // Tạo một MyDetectedObject giả để vẽ bounding box
                        boundingBoxView.setDetectedObjects(qrCodeObject);
                    }

                    // Kiểm tra xem QR code có phải là một link hợp lệ không
                    if (isValidUrl(qrCodeValue)) {
                        // Hiển thị hộp thoại hỏi người dùng có muốn mở link không
                        showOpenLinkDialog(qrCodeValue);
                    }
                });
            }

            @Override
            public void onQRCodeScanFailed(String errorMessage) {
                Log.e(TAG, "QR Code Scan Failed: " + errorMessage);
                runOnUiThread(() -> {
                    TextView tvResult = findViewById(R.id.tvResult);
                    tvResult.setText(LabelTranslator.translateSystemMessage("No QR Code detected, continuing to scan..."));
                });
            }
        });
    }

    // Phương thức hiển thị hộp thoại hỏi người dùng có muốn mở link không
    private void showOpenLinkDialog(String url) {
        new AlertDialog.Builder(this)
                .setTitle("Mở liên kết")
                .setMessage("Bạn có muốn mở liên kết này không?")
                .setPositiveButton("Có", (dialog, which) -> {
                    // Mở link trong trình duyệt
                    openUrlInBrowser(url);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    // Không làm gì, đóng hộp thoại
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
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
        cameraManager.shutdown();
        textToSpeechManager.shutdown();
    }
}