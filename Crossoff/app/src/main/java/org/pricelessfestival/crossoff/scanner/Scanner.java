package org.pricelessfestival.crossoff.scanner;

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
            switch (statusCode[0]) {
                case 200: // accepted scan
                    result = "Ticket Was Accepted!\n" + ticketCode;
                    break;
                case 409: // ticket already scanned
                    result = "ERROR: Ticket was already scanned!\n" + ticketCode;
                    break;
                case 404: // unknown ticket
                    result = "ERROR: Invalid ticket code (unknown ticket)\n" + ticketCode;
                    break;
                default:
                    result = "Unexpected response from server: HTTP " + statusCode[0];
            }
        } else {
            result = "Communication with server failed. Check network and try again.";
        }
        return result;
    }
}
