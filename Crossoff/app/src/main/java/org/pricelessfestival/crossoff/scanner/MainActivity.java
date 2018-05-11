package org.pricelessfestival.crossoff.scanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.pricelessfestival.crossoff.R;
import org.pricelessfestival.crossoff.scanner.barcode.BarcodeCaptureActivity;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mResultTextView;
    private Button mBarcodeButton;
    private Scanner scanner = new Scanner();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResultTextView = findViewById(R.id.result_textview);
        mBarcodeButton = findViewById(R.id.scan_barcode_button);
        mBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    mResultTextView.setText(Scanner.scanTicket(barcode.displayValue));
                } else
                    mResultTextView.setText(R.string.no_barcode_captured);
            } else {
                String errorMessage = String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode));
                mResultTextView.setText(errorMessage);
                Log.e(TAG, errorMessage);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
