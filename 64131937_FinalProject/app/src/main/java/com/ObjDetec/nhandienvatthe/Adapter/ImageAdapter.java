package com.ObjDetec.nhandienvatthe.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ObjDetec.nhandienvatthe.R;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<File> imageFiles;
    private Context context;

    public ImageAdapter(Context context, List<File> imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        File imageFile = imageFiles.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        holder.imageView.setImageBitmap(bitmap);

        // Thêm mô tả trợ năng
        holder.imageView.setContentDescription("Hình ảnh " + (position + 1));
    }

    @Override
    public int getItemCount() {
        return imageFiles.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}