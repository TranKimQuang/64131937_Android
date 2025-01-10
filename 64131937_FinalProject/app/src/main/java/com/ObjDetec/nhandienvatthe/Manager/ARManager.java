package com.ObjDetec.nhandienvatthe.Manager;

import android.util.Log;
import android.view.MotionEvent;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class ARManager {

    private static final String TAG = "ARManager";
    private ArFragment arFragment;
    private DistanceCallback distanceCallback;

    // Sửa constructor để chấp nhận cả ArFragment và DistanceCallback
    public ARManager(ArFragment arFragment, DistanceCallback distanceCallback) {
        this.arFragment = arFragment;
        this.distanceCallback = distanceCallback;
    }

    public void setupTapListener() {
        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            if (plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                // Tạo Anchor tại vị trí chạm
                Anchor anchor = hitResult.createAnchor();

                // Lấy vị trí của Anchor và thiết bị
                Pose anchorPose = anchor.getPose();
                Pose devicePose = arFragment.getArSceneView().getArFrame().getCamera().getPose();

                // Tính toán khoảng cách
                float distance = calculateDistance(devicePose, anchorPose);
                Log.d(TAG, "Khoảng cách: " + distance + " mét");

                // Gọi callback để xử lý khoảng cách
                if (distanceCallback != null) {
                    distanceCallback.onDistanceCalculated(distance);
                }

                // Hiển thị đối tượng AR
                displayObjectAtAnchor(anchor);
            }
        });
    }

    private float calculateDistance(Pose pose1, Pose pose2) {
        float dx = pose1.tx() - pose2.tx();
        float dy = pose1.ty() - pose2.ty();
        float dz = pose1.tz() - pose2.tz();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void displayObjectAtAnchor(Anchor anchor) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setParent(anchorNode);
        node.select();
    }

    public interface DistanceCallback {
        void onDistanceCalculated(float distance);
    }
}