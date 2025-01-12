package com.ObjDetec.nhandienvatthe.Manager;

import android.graphics.Rect;
import android.util.Log;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class QRCodeScannerManager {

    private static final String TAG = "QRCodeScannerManager";
    private BarcodeScanner barcodeScanner;

    public QRCodeScannerManager() {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    public void scanQRCode(InputImage image, QRCodeScanListener listener) {
        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes.size() > 0) {
                        Barcode barcode = barcodes.get(0);
                        String qrCodeValue = barcode.getRawValue();
                        Rect boundingBox = barcode.getBoundingBox(); // Lấy bounding box của QR Code
                        Log.d(TAG, "Mã QR đã được xác định" + qrCodeValue);
                        listener.onQRCodeScanned(qrCodeValue, boundingBox); // Truyền cả qrCodeValue và boundingBox
                    } else {
                        Log.d(TAG, "Không tìm thấy mã QR , đang tìm kiếm...");

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "QR Code scanning failed", e);
                    listener.onQRCodeScanFailed(e.getMessage());
                });
    }

    public interface QRCodeScanListener {
        void onQRCodeScanned(String qrCodeValue, Rect boundingBox); // Thêm tham số Rect boundingBox
        void onQRCodeScanFailed(String errorMessage);
    }
}