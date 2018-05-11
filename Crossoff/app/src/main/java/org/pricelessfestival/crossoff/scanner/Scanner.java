package org.pricelessfestival.crossoff.scanner;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class Scanner {

    // if running as Android Virtual Device, 10.0.2.2 is the magic address of host system localhost
    private static final String CROSSOFF_SERVER_HOST_ADDR = "10.0.2.2";

    // acceptable barcodes: subset of Code 39 printable characters (no / + or [space])
    private static Pattern VALID_TICKET_CODE = Pattern.compile("^[A-Z0-9\\-\\$\\%\\*]+$");

    public static boolean validTicketCode(String code) {
        return code != null && VALID_TICKET_CODE.matcher(code).matches();
    }

    public static void scanTicket(String ticketCode, ResultHandler consumer) {
        if (!validTicketCode(ticketCode)) {
            consumer.accept("Weird barcode scanned. "
                    + "Make sure camera is pointed at ticket!\n(scan = " + ticketCode + ")");
        } else {
            serverScan(ticketCode, consumer);
        }
    }

    private static void serverScan(String ticketCode, final ResultHandler consumer) {
        HttpClient httpClient = new HttpClient();
        String url = "http://" + CROSSOFF_SERVER_HOST_ADDR + ":8080/tickets/" + ticketCode;
        httpClient.post(url, "", new HttpClient.Handler() {
            @Override
            public void accept(int statusCode, String body) {
                boolean accepted = false;
                try {
                    String scanResult;
                    if (body == null) {
                        scanResult = "ERROR: Communication with server failed. Check network and try again.";
                    } else if (statusCode == 200) {
                        scanResult = parseScanResult(body);
                    } else {
                        scanResult = "ERROR: Unexpected response from server: HTTP " + statusCode;
                    }
                    accepted = true;
                    consumer.accept(scanResult);
                } finally {
                    if (!accepted) {
                        consumer.accept("Internal error!");
                    }
                }
            }
        });
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

    public interface ResultHandler {

        void accept(final String scanResult);
    }
}
