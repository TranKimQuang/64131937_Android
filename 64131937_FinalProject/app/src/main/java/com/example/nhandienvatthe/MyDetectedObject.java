package com.example.nhandienvatthe;

import android.graphics.Rect;
import com.google.mlkit.vision.objects.DetectedObject.Label;
import java.util.List;

public class MyDetectedObject {
    private Rect boundingBox;
    private Integer confidence;
    private List<Label> labels;

    public MyDetectedObject(Rect boundingBox, Integer confidence, List<Label> labels) {
        this.boundingBox = boundingBox;
        this.confidence = confidence;
        this.labels = labels;
    }

    // Getter and Setter methods
    public Rect getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(Rect boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Integer getConfidence() {
        return confidence;
    }

    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }

    public List<Label> getLabels() {
        return labels;
    }

    public void setLabels(List<Label> labels) {
        this.labels = labels;
    }
}
