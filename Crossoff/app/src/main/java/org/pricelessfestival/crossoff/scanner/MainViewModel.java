package org.pricelessfestival.crossoff.scanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Main ViewModel for handling UI updates and interfacing with data sources
 */
public class MainViewModel extends ViewModel implements TicketAuthenticator.ResultHandler {

    // string to be displayed after the each attempted scan
    private final MutableLiveData<String> scanResultMessage =
            new MutableLiveData<>("");
    public LiveData<String> getScanResultMessage() {
        return scanResultMessage;
    }

    private TicketAuthenticator authenticator = new TicketAuthenticator(this);

    private String baseUrl = "";
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void validateTicketCode(String ticketCode) {
        authenticator.validateTicketCode(baseUrl, ticketCode);
    }

    @Override
    public void postServerResult(String serverResult) {
        // send the server reply message to the UI
        scanResultMessage.postValue(serverResult); //UI thread
    }
}