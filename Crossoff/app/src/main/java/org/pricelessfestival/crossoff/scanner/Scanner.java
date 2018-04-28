package org.pricelessfestival.crossoff.scanner;

import org.json.JSONException;
import org.json.JSONObject;

public class Scanner {

    public static CharSequence scanTicket(String ticketCode) {
        String result;
        if (!looksLikeTicketBarcode(ticketCode)) {
            result = "Weird barcode scanned. Make sure camera is pointed at ticket!\n(scan = "
                    + ticketCode + ")";
        } else {
            result = serverResult(ticketCode);
        }
        return result;
    }

    private static boolean looksLikeTicketBarcode(String ticketCode) {
        // consider improving validation
        return ticketCode != null && ticketCode.length() > 1;
    }

    private static String serverResult(String ticketCode) {
        HttpClient httpClient = new HttpClient();
        String url = "http://10.0.2.2:8080/tickets/" + ticketCode;
        final int[] statusCode = new int[1];
        final String[] body = new String[1];
        boolean commSuccess = httpClient.post(url, "", new HttpClient.Handler() {
            @Override
            public void accept(int code, String content) {
                statusCode[0] = code;
                body[0] = content;
            }
        });
        String result;
        if (commSuccess) {
            if (statusCode[0] == 200) {
                result = parseScanResult(body[0]);
            } else {
                result = "ERROR: Unexpected response from server: HTTP " + statusCode[0];
            }
        } else {
            result = "ERROR: Communication with server failed. Check network and try again.";
        }
        return result;
    }

    private static String parseScanResult(String body) {
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
                    result = "Ticket Was Accepted!\n" + message;
                    if (ticket != null) {
                        try {
                            result += "\n" + ticket.getString("description");
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
}
