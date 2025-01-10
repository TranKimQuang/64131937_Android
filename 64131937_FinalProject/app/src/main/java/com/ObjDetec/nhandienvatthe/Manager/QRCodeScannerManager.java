package com.ObjDetec.nhandienvatthe.Manager;

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
                        Log.d(TAG, "QR Code detected: " + qrCodeValue);
                        listener.onQRCodeScanned(qrCodeValue);
                    } else {
                        Log.d(TAG, "No QR Code detected");
                        listener.onQRCodeScanFailed("No QR Code detected");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "QR Code scanning failed", e);
                    listener.onQRCodeScanFailed(e.getMessage());
                });
    }

    public interface QRCodeScanListener {
        void onQRCodeScanned(String qrCodeValue);
        void onQRCodeScanFailed(String errorMessage);
    }
}