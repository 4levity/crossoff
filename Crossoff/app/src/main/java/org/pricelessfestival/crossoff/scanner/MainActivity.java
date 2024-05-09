package org.pricelessfestival.crossoff.scanner;

import android.content.Context;
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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.pricelessfestival.crossoff.R;
import org.pricelessfestival.crossoff.scanner.barcode.BarcodeCaptureActivity;
import org.pricelessfestival.crossoff.scanner.config.ConfigDialog;
import org.pricelessfestival.crossoff.scanner.config.SharedPrefs;

import androidx.lifecycle.ViewModelProvider;

public class MainActivity extends AppCompatActivity implements BarcodeScanner.BarcodeCallback,
        ConfigDialog.SettingsListener {

    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView resultTextView;
    private TextView clearButton;
    private Button barcodeButton;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.setBaseUrl(SharedPrefs.instance(this).getBaseUrl());

        // fast and efficient barcode scanner from google play services
        @BarcodeScanner.BarcodeType int storedBarcodeType = SharedPrefs.instance(this).getBarcodeType();
        BarcodeScanner barcodeScanner = new BarcodeScanner(this, storedBarcodeType, this);

        resultTextView = findViewById(R.id.result_textview);

        // barcode scan button
        barcodeButton = findViewById(R.id.scan_barcode_button);
        barcodeButton.setOnClickListener(v -> {
            barcodeScanner.start();
        });

        // manual search button
        findViewById(R.id.search_button).setOnClickListener(v -> {
            Uri searchUiUri = Uri.parse(SharedPrefs.instance(this).getBaseUrl());
            startActivity(new Intent(Intent.ACTION_VIEW, searchUiUri));
        });

        // results clear button
        clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            resultTextView.setText("");
            clearButton.setVisibility(View.INVISIBLE);
        });

        // observe the results of the server check
        viewModel.getScanResultMessage().observe(this, scanResultMessage -> {
            // update UI
            barcodeButton.setEnabled(true);
            setResultText(scanResultMessage);
        });
    }

    private void setResultText(String resultText) {
        resultTextView.setText(resultText);
        clearButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBarcodeDetected(String barcode) {
        if (barcode != null) {
            processTicketCode(barcode);
        } else {
            setResultText(getString(R.string.no_barcode_captured));
        }
    }

    @Override
    public void onBarcodeError(Exception e) {
        String errorMessage = String.format(getString(R.string.barcode_error_format),
                e.getMessage());
        setResultText(errorMessage);

        // Kinda hacky, but run the old method of scanning barcodes
        Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
            startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
    }

    /*
        Old return method for the barcode scanner.
        We're keeping this around for the very old devices which can't get their PlayStore updated.
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    String barcodeValue = barcode.displayValue;
                    processTicketCode(barcodeValue);
                } else {
                    setResultText(getString(R.string.no_barcode_captured));
                }
            } else {
                String errorMessage = String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode));
                setResultText(errorMessage);
                Log.e(TAG, errorMessage);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processTicketCode(String barcodeValue) {
        barcodeButton.setEnabled(false);
        setResultText(getString(R.string.connecting));
        viewModel.validateTicketCode(barcodeValue);
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

                String serverAddress = SharedPrefs.instance(this).getServerAddress();
                int serverPort = SharedPrefs.instance(this).getServerPort();
                int storedBarcodeType = SharedPrefs.instance(this).getBarcodeType();

                // start config window
                new ConfigDialog(serverAddress,serverPort, storedBarcodeType, this)
                        .show(getSupportFragmentManager(), ConfigDialog.TAG);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSettingsSaved(Context context, String address, String port,
                            @BarcodeScanner.BarcodeType int barcodeType) {
        // cheating by not saving these in the background thread
        SharedPrefs.instance(context).setServerAddress(address);
        SharedPrefs.instance(context).setServerPort(Integer.parseInt(port));
        SharedPrefs.instance(context).setBarcodeType(barcodeType);
        Toast.makeText(context, R.string.dialog_settings_updated, Toast.LENGTH_SHORT).show();

        // update viewmodel
        viewModel.setBaseUrl(SharedPrefs.instance(this).getBaseUrl());
    }
}
