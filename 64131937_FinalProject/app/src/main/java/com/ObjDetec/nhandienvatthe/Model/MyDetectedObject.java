package com.ObjDetec.nhandienvatthe.Model;

import android.graphics.Rect;
import com.google.mlkit.vision.objects.DetectedObject.Label;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyDetectedObject that = (MyDetectedObject) o;

        // So sánh label (chỉ cần so sánh label đầu tiên)
        String thisLabel = this.labels.isEmpty() ? "" : this.labels.get(0).getText();
        String thatLabel = that.labels.isEmpty() ? "" : that.labels.get(0).getText();
        return thisLabel.equals(thatLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, confidence, labels);
    }
}