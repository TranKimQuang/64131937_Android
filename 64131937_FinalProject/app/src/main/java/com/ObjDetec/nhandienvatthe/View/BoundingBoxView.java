package com.ObjDetec.nhandienvatthe.View;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;
import java.util.List;

public class BoundingBoxView extends View {

    private List<MyDetectedObject> detectedObjects;
    private Paint paint;
    private int imageWidth, imageHeight;

    public BoundingBoxView(Context context) {
        super(context);
        init();
    }

    public BoundingBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
    }

    public void setDetectedObjects(List<MyDetectedObject> detectedObjects) {
        this.detectedObjects = detectedObjects;
        invalidate(); // Vẽ lại view
    }

    public void setImageDimensions(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (detectedObjects != null && imageWidth > 0 && imageHeight > 0) {
            int viewWidth = getWidth();
            int viewHeight = getHeight();
            for (MyDetectedObject object : detectedObjects) {
                Rect boundingBox = object.getBoundingBox();
                Rect adjustedBoundingBox = adjustBoundingBox(boundingBox, viewWidth, viewHeight, imageWidth, imageHeight);
                canvas.drawRect(adjustedBoundingBox, paint);
            }
        }
    }

    private Rect adjustBoundingBox(Rect boundingBox, int viewWidth, int viewHeight, int imageWidth, int imageHeight) {
        // Tính tỷ lệ giữa kích thước PreviewView và kích thước hình ảnh gốc
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;

        // Điều chỉnh tọa độ bounding box
        return new Rect(
                (int) (boundingBox.left * scaleX),
                (int) (boundingBox.top * scaleY),
                (int) (boundingBox.right * scaleX),
                (int) (boundingBox.bottom * scaleY)
        );
    }
}