package org.pricelessfestival.crossoff.scanner;

import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Service Class for interfacing with the play-services-code-scanner
 */
public class BarcodeScanner {

    @Retention(SOURCE)
    @IntDef({Barcode.CODE_128, Barcode.ALL_FORMATS})
    public @interface BarcodeType {}
    // using ALL_FORMATS is a little slower

    private final GmsBarcodeScanner scanner;
    private final BarcodeCallback callback;

    public BarcodeScanner(Context context, @BarcodeType int barcodeType, BarcodeCallback callback) {
        this.callback = callback;

        /*
            If you know which barcode formats you expect to read, you can improve the speed of the
            barcode detector by configuring it to only detect those formats.
         */
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(barcodeType)
                .build();

        scanner = GmsBarcodeScanning.getClient(context.getApplicationContext(), options);
    }

    public void start() {
        if (scanner == null) return;

        scanner.startScan()
                .addOnSuccessListener(
                        barcode -> {
                            // Task completed successfully
                            String displayValue = barcode.getDisplayValue();
                            callback.onBarcodeDetected(displayValue);
                        })
                .addOnCanceledListener(
                        () -> { // Task canceled, back button
                        })
                .addOnFailureListener(
                        e -> { // Task failed with an exception
                            Log.e("BarcodeScanner", e.getMessage());
                            callback.onBarcodeError(e);
                        });
    }

    public interface BarcodeCallback {
        void onBarcodeDetected(String barcode);
        void onBarcodeError(Exception e);
    }
}


