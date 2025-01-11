package com.ObjDetec.nhandienvatthe.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ObjDetec.nhandienvatthe.Adapter.ImageAdapter;
import com.ObjDetec.nhandienvatthe.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LibraryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Khởi tạo nút Back
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish()); // Đóng Activity hiện tại

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // Hiển thị 2 cột

        // Lấy danh sách hình ảnh từ thư mục "library"
        File libraryFolder = new File(getExternalFilesDir(null), "library");
        List<File> imageFiles = new ArrayList<>();
        if (libraryFolder.exists()) {
            File[] files = libraryFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".jpg")) {
                        imageFiles.add(file);
                    }
                }
            }
        }

        // Thiết lập Adapter cho RecyclerView
        imageAdapter = new ImageAdapter(this, imageFiles);
        recyclerView.setAdapter(imageAdapter);
    }
}