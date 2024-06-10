package org.pricelessfestival.crossoff.scanner.config;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.pricelessfestival.crossoff.R;
import org.pricelessfestival.crossoff.scanner.BarcodeScanner;

import java.util.Locale;

/**
 * Class for displaying and handling updated settings for the application
 */
public class ConfigDialog extends BottomSheetDialogFragment {

    private final String IP_ADDRESS_REGEX = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
    private final String IP_ADDRESS_REGEX_2 = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}))))))";

    private Boolean addressGood = true;
    private Boolean portGood = true;
    private Button saveButton;
    private View.OnClickListener saveListener;
    final private SettingsListener settingsSaveListener;
    final private String serverAddress;
    final private int serverPort;
    @BarcodeScanner.BarcodeType int barcodeType;

    public ConfigDialog(String serverAddress, int serverPort,
                        @BarcodeScanner.BarcodeType int barcodeType, SettingsListener listener){
        settingsSaveListener = listener;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.barcodeType = barcodeType;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_config, container, false);
        TextInputEditText editServerAddress = view.findViewById(R.id.editTextServerAddress);
        TextInputLayout editServerAddressLayout = view.findViewById(R.id.editTextServerAddress_layout);
        TextInputEditText editServerPort = view.findViewById(R.id.editTextServerPort);
        TextInputLayout editServerPortLayout = view.findViewById(R.id.editTextServerPort_layout);
        SwitchMaterial switchEnableAllBarcodeTypes = view.findViewById(R.id.switchEnableAllBarcodeTypes);
        TextView toggleWarningText = view.findViewById(R.id.restartWarning);

        saveButton = view.findViewById(R.id.button_save);
        saveListener = v -> {
            @BarcodeScanner.BarcodeType int barcodeType = Barcode.CODE_128;
            if(switchEnableAllBarcodeTypes.isChecked())
                barcodeType = Barcode.ALL_FORMATS;

            // send new settings to the listener on save
            settingsSaveListener.onSettingsSaved(saveButton.getContext(),
                    editServerAddress.getText().toString(),
                    editServerPort.getText().toString(),
                    barcodeType);
            dismiss();
        };

        // display stored value for the enable switch
        switchEnableAllBarcodeTypes.setChecked(barcodeType == Barcode.ALL_FORMATS);

        Button cancelButton = view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        // set server address
        editServerAddress.setText(serverAddress);
        editServerPort.setText(String.format(Locale.US, "%d", serverPort));

        // reset the text of the server fields
        Button defualtsButton = view.findViewById(R.id.button_defaults);
        defualtsButton.setOnClickListener(v -> {
            editServerAddress.setText(serverAddress);
            editServerPort.setText(String.format(Locale.US, "%d", serverPort));
            switchEnableAllBarcodeTypes.setChecked(barcodeType == Barcode.ALL_FORMATS);
        });

        // filter entered text for proper IP address format
        InputFilter[] filters = new InputFilter[1];
        filters[0] = (source, start, end, dest, dstart, dend) -> {
            // clip out characters that don't match the format
            if (end > start) {
                String destTxt = dest.toString();
                String resultingTxt = destTxt.substring(0, dstart)
                        + source.subSequence(start, end)
                        + destTxt.substring(dend);
                if (!resultingTxt.matches(IP_ADDRESS_REGEX)) {
                    return "";
                } else {
                    String[] splits = resultingTxt.split("\\.");
                    for (String split : splits) {
                        if (Integer.valueOf(split) > 255)
                            return "";
                    }
                }
            }
            return null;
        };
        editServerAddress.setFilters(filters);

        // track errors for the server address entry text
        editServerAddress.addTextChangedListener(new TextWatcher()  {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s)  {
                if (s.toString().matches(IP_ADDRESS_REGEX_2)) {
                    editServerAddressLayout.setError(null);
                    editServerAddressLayout.setErrorEnabled(false);
                    addressGood = true;
                    updateConfigDialog();
                } else {
                    editServerAddressLayout.setError(getString(R.string.dialog_settings_server_address_error));
                    editServerAddressLayout.setErrorEnabled(true);
                    addressGood = false;
                    updateConfigDialog();
                }
            }
        });


        // track errors for the server port entry text
        editServerPort.addTextChangedListener(new TextWatcher()  {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s)  {
                if (!s.toString().isEmpty()) {
                    editServerPortLayout.setError(null);
                    editServerPortLayout.setErrorEnabled(false);
                    portGood = true;
                    updateConfigDialog();
                } else {
                    editServerPortLayout.setError(getString(R.string.dialog_settings_server_port_error));
                    editServerPortLayout.setErrorEnabled(true);
                    portGood = false;
                    updateConfigDialog();
                }
            }
        });

        switchEnableAllBarcodeTypes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // toggle the visibility of the warning for the switch
                if (toggleWarningText.getVisibility() == View.GONE)
                    toggleWarningText.setVisibility(View.VISIBLE);
                else
                    toggleWarningText.setVisibility(View.GONE);
            }
        });

        updateConfigDialog();
        return view;
    }

    private void updateConfigDialog() {
        if(addressGood && portGood)
            enableSaveButton();
        else
            disableSaveButton();
    }
    private void enableSaveButton() {
        saveButton.setEnabled(true);
        saveButton.setOnClickListener(saveListener);
        saveButton.setClickable(true);
    }

    private void disableSaveButton() {
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(null);
        saveButton.setClickable(false);
    }

    public interface SettingsListener {
        void onSettingsSaved(Context context, String address, String port,
                             @BarcodeScanner.BarcodeType int barcodeType);
    }

    public static String TAG = "ConfigDialog";
}
