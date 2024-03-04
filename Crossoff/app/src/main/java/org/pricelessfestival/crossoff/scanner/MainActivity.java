package org.pricelessfestival.crossoff.scanner;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.pricelessfestival.crossoff.R;
import org.pricelessfestival.crossoff.scanner.barcode.BarcodeCaptureActivity;
import org.pricelessfestival.crossoff.scanner.config.ConfigDialog;
import org.pricelessfestival.crossoff.scanner.config.SharedPrefs;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mResultTextView;
    TextView clearButton;
    private Button mBarcodeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mResultTextView = findViewById(R.id.result_textview);
        mBarcodeButton = findViewById(R.id.scan_barcode_button);
        mBarcodeButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
        });

        findViewById(R.id.search_button).setOnClickListener(v -> {
            Uri searchUiUri = Uri.parse(SharedPrefs.instance(this).getBaseUrl());
            startActivity(new Intent(Intent.ACTION_VIEW, searchUiUri));
        });

        clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            mResultTextView.setText("");
            clearButton.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    mBarcodeButton.setEnabled(false);
                    mResultTextView.setText(getString(R.string.connecting));

                    String baseUrl = SharedPrefs.instance(this).getBaseUrl();
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Scanner.scanTicket(baseUrl, barcode.displayValue, new Scanner.ResultHandler() {
                        @Override
                        public void accept(final String result) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResultTextView.setText(result);
                                    mBarcodeButton.setEnabled(true);
                                    clearButton.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                } else {
                    mResultTextView.setText(R.string.no_barcode_captured);
                    clearButton.setVisibility(View.VISIBLE);
                }
            } else {
                String errorMessage = String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode));
                mResultTextView.setText(errorMessage);
                clearButton.setVisibility(View.VISIBLE);
                Log.e(TAG, errorMessage);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.config:
                // start config window
                new ConfigDialog().show(getSupportFragmentManager(), ConfigDialog.TAG);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
