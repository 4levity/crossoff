package org.pricelessfestival.crossoff.scanner.config;

import com.google.android.gms.vision.barcode.Barcode;
import org.pricelessfestival.crossoff.scanner.BarcodeScanner;

public class AppSettings {
    // if running as Android Virtual Device, 10.0.2.2 is the magic address of host system localhost
    public static final String CROSSOFF_SERVER_HOST_ADDR = "10.0.2.2";
    public static final int CROSSOFF_SERVER_PORT = 8080;
    @BarcodeScanner.BarcodeType public static final int CROSSOFF_BARCODE_TYPE = Barcode.CODE_128;
}
