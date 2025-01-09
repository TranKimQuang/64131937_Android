package com.ObjDetec.nhandienvatthe.Util;

import android.content.Context;
import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class TensorFlowHelper {

    private Interpreter interpreter;

    public TensorFlowHelper(Context context) {
        try {
            // Tải mô hình từ assets
            MappedByteBuffer modelBuffer = loadModelFile(context, "1.tflite");
            interpreter = new Interpreter(modelBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        // Mở file từ assets
        InputStream inputStream = context.getAssets().open(modelPath);
        File tempFile = File.createTempFile("1", ".tflite", context.getCacheDir());
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        // Sao chép dữ liệu từ InputStream vào FileOutputStream
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        outputStream.write(buffer);

        // Đóng luồng
        inputStream.close();
        outputStream.close();

        // Đọc file vào MappedByteBuffer
        FileChannel fileChannel = new FileInputStream(tempFile).getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }
}