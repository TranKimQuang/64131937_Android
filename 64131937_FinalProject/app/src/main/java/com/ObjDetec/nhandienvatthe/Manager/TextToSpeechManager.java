package com.ObjDetec.nhandienvatthe.Manager;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import java.util.Locale;

public class TextToSpeechManager {

    private static final String TAG = "TextToSpeechManager";
    private TextToSpeech textToSpeech;
    private boolean isSpeaking = false;

    public TextToSpeechManager(Context context, TextToSpeech.OnInitListener listener) {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                Log.d(TAG, "TextToSpeech initialized successfully");
                listener.onInit(status);

                // Thêm UtteranceProgressListener để theo dõi trạng thái phát âm thanh
                textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d(TAG, "Speech started: " + utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d(TAG, "Speech finished: " + utteranceId);
                        isSpeaking = false; // Đặt lại trạng thái khi phát âm thanh kết thúc
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "Speech error: " + utteranceId);
                        isSpeaking = false; // Đặt lại trạng thái nếu có lỗi
                    }
                });
            } else {
                Log.e(TAG, "TextToSpeech initialization failed");
            }
        });
    }

    public void setLanguage(Locale locale) {
        textToSpeech.setLanguage(locale);
    }

    public void speak(String text, int queueMode, Bundle params, String utteranceId) {
        if (text != null && !isSpeaking) {
            Log.d(TAG, "Reading text aloud: " + text);
            isSpeaking = true;
            textToSpeech.speak(text, queueMode, params, utteranceId);
        } else {
            Log.d(TAG, "Text or TextToSpeech is null or already speaking.");
        }
    }

    public void stop() {
        if (textToSpeech != null && isSpeaking) {
            textToSpeech.stop();
            isSpeaking = false;
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }

    public void setOnUtteranceProgressListener(UtteranceProgressListener listener) {
        textToSpeech.setOnUtteranceProgressListener(listener);
    }
}