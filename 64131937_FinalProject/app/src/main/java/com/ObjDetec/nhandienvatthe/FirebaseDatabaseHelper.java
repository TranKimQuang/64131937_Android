package com.ObjDetec.nhandienvatthe;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseHelper {

    private DatabaseReference databaseReference;

    public FirebaseDatabaseHelper() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("nhandienvatthe");
    }

    public void saveImageUrl(String imageUrl) {
        String objectId = databaseReference.push().getKey();
        if (objectId != null) {
            databaseReference.child(objectId).setValue(imageUrl);
        }
    }
}
