package org.pricelessfestival.crossoff.scanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class TicketAuthenticator implements HttpClient.ReplyHandler {

    ResultHandler consumer;
    // acceptable barcodes: subset of Code 128 printable characters (no / + or [space])
    private final static Pattern VALID_TICKET_CODE = Pattern.compile("^[A-Z0-9\\-\\$\\%\\*]+$");

    public TicketAuthenticator(ResultHandler consumer){
        this.consumer = consumer;
    }

    public static boolean validCodeFormat(String code) {
        return code != null && VALID_TICKET_CODE.matcher(code).matches();
    }

    public void validateTicketCode(String baseUrl, String ticketCode) {
        if (!validCodeFormat(ticketCode)) {
            if(consumer != null)
                consumer.postServerResult("Weird barcode scanned. "
                    + "Make sure camera is pointed at ticket!\n(scan = " + ticketCode + ")");
        } else {
            serverValidation(baseUrl, ticketCode);
        }
    }

    public void serverValidation(String baseUrl, String ticketCode) {
        HttpClient httpClient = new HttpClient();
        String url = baseUrl + "/tickets/" + ticketCode;
        httpClient.post(url, "", this);
    }

    @Override
    public void serverReply(int statusCode, String body) {
        boolean accepted = false;
        try {
            String serverResult;
            if (body == null) {
                serverResult = "ERROR: Communication with server failed. Check network and try again. ("+statusCode+")";
            } else if (statusCode == 200) {
                serverResult = parseServerResult(body);
            } else {
                serverResult = "ERROR: Unexpected response from server: HTTP " + statusCode;
            }
            accepted = true;
            if(consumer != null)
                consumer.postServerResult(serverResult);
        } finally {
            if (!accepted && consumer != null) {
                consumer.postServerResult("Internal error!");
            }
        }
    }

    public String parseServerResult(String body) {
        String result;
        JSONObject scanResult = null;
        try {
            scanResult = new JSONObject(body);
        } catch (JSONException e) {
            // handled below
        }
        if (scanResult != null) {
            boolean accepted = false;
            String message = null;
            JSONObject ticket = null;
            try {
                accepted = scanResult.getBoolean("accepted");
                message = scanResult.getString("message");
                if (scanResult.has("ticket")) {
                    ticket = scanResult.getJSONObject("ticket");
                }
            } catch (JSONException e) {
                // handled below
            }
            if (message != null) {
                if (accepted) {
                    result = "Scan Accepted: " + message;
                    if (ticket != null) {
                        try {
                            result += "\n" + ticket.getString("description");
                            if (ticket.has("ticketholder")) {
                                result += "\nfor " + ticket.getString("ticketholder");
                            }
                            if (ticket.has("notes")) {
                                result += "\nNOTE: " + ticket.getString("notes");
                            }
                        } catch (JSONException e) {
                            result += "\n(error retrieving ticket type info!)";
                        }
                    } else {
                        result += "\n(didn't get ticket details from server!)";
                    }
                } else {
                    result = "Uh oh! SCAN NOT ACCEPTED!\n" + message;
                }
            } else {
                result = "ERROR: Incompatible server response!";
            }
        } else {
            result = "ERROR: Failed to parse server response!";
        }
        return result;
    }

    public interface ResultHandler {
        void postServerResult(final String serverResult);
    }
}
