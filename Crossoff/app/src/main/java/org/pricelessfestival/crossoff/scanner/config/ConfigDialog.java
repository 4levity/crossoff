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
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.pricelessfestival.crossoff.R;

import java.util.Locale;

public class ConfigDialog extends BottomSheetDialogFragment {

    private final String IP_ADDRESS_REGEX = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?";
    private final String IP_ADDRESS_REGEX_2 = "^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}))))))";


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

        Button saveButton = view.findViewById(R.id.button_save);
        View.OnClickListener saveListener = v -> {
            saveConfig(saveButton.getContext(), editServerAddress.getText().toString(), editServerPort.getText().toString());
            dismiss();
        };

        Button cancelButton = view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        // retrieve server address
        String serverAddress = SharedPrefs.instance(view.getContext()).getServerAddress();
        int serverPort = SharedPrefs.instance(view.getContext()).getServerPort();
        editServerAddress.setText(serverAddress);
        editServerPort.setText(String.format(Locale.US, "%d", serverPort));

        // reset the text of the server fields
        Button defualtsButton = view.findViewById(R.id.button_defaults);
        defualtsButton.setOnClickListener(v -> {
            editServerAddress.setText(serverAddress);
            editServerPort.setText(String.format(Locale.US, "%d", serverPort));});

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

                    saveButton.setEnabled(true);
                    saveButton.setOnClickListener(saveListener);
                    saveButton.setClickable(true);
                } else {
                    editServerAddressLayout.setError(getString(R.string.dialog_settings_server_address_error));
                    editServerAddressLayout.setErrorEnabled(true);

                    saveButton.setEnabled(false);
                    saveButton.setOnClickListener(null);
                    saveButton.setClickable(false);
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

                    saveButton.setEnabled(true);
                    saveButton.setOnClickListener(saveListener);
                    saveButton.setClickable(true);
                } else {
                    editServerPortLayout.setError(getString(R.string.dialog_settings_server_port_error));
                    editServerPortLayout.setErrorEnabled(true);

                    saveButton.setEnabled(false);
                    saveButton.setOnClickListener(null);
                    saveButton.setClickable(false);
                }
            }
        });

        return view;
    }

    private void saveConfig(Context context, String address, String port) {
        // cheating by not saving these in the background thread
        SharedPrefs.instance(context).setServerAddress(address);
        SharedPrefs.instance(context).setServerPort(Integer.parseInt(port));
        Toast.makeText(context, R.string.dialog_settings_updated, Toast.LENGTH_SHORT).show();
    }

    public static String TAG = "ConfigDialog";
}
