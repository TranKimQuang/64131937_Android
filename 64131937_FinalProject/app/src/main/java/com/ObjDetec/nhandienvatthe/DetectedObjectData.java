package com.ObjDetec.nhandienvatthe;

public class DetectedObjectData {
    private String objectName;
    private String timestamp;

    public DetectedObjectData() {
        // Default constructor required for calls to DataSnapshot.getValue(DetectedObjectData.class)
    }

    public DetectedObjectData(String objectName, String timestamp) {
        this.objectName = objectName;
        this.timestamp = timestamp;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

