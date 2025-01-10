package com.ObjDetec.nhandienvatthe.Activity;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;

import com.ObjDetec.nhandienvatthe.R;
import com.ObjDetec.nhandienvatthe.Manager.CameraManager;
import com.ObjDetec.nhandienvatthe.Manager.TextToSpeechManager;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private PreviewView previewView;
    private TextView tvResult;
    private ExecutorService cameraExecutor;
    private TextToSpeechManager textToSpeechManager;
    private CameraManager cameraManager;

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
        textToSpeechManager = new TextToSpeechManager(this, this::onTextToSpeechInit); // Truyền Context vào đây
        cameraManager = new CameraManager(this, previewView, cameraExecutor);

        // Thiết lập camera
        cameraManager.setupCamera();
    }

    /**
     * Callback khi TextToSpeech được khởi tạo.
     */
    private void onTextToSpeechInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeechManager.setLanguage(Locale.getDefault());
            textToSpeechManager.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    Log.d(TAG, "TextToSpeech started: " + utteranceId);
                }

                @Override
                public void onDone(String utteranceId) {
                    Log.d(TAG, "TextToSpeech completed: " + utteranceId);
                }

                @Override
                public void onError(String utteranceId) {
                    Log.e(TAG, "TextToSpeech error: " + utteranceId);
                }
            });
        } else {
            Log.e(TAG, "TextToSpeech initialization failed");
        }
    }

    /**
     * Callback khi khoảng cách được tính toán (nếu có chức năng tính khoảng cách).
     */
    private void onDistanceCalculated(float distance) {
        String distanceText = "Khoảng cách: " + distance + " mét";
        tvResult.setText(distanceText);
        textToSpeechManager.speak(distanceText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng và giải phóng tài nguyên
        textToSpeechManager.shutdown();
        cameraExecutor.shutdown();
    }
}