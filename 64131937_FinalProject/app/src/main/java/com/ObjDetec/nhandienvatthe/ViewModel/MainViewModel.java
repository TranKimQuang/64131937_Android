package com.ObjDetec.nhandienvatthe.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.ObjDetec.nhandienvatthe.Model.MyDetectedObject;

import java.util.List;

public class MainViewModel extends ViewModel {

    private MutableLiveData<List<MyDetectedObject>> detectedObjects = new MutableLiveData<>();

    public LiveData<List<MyDetectedObject>> getDetectedObjects() {
        return detectedObjects;
    }

    public void setDetectedObjects(List<MyDetectedObject> objects) {
        detectedObjects.setValue(objects);
    }
}