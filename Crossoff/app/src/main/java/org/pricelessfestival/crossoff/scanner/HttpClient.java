package org.pricelessfestival.crossoff.scanner;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpClient {

    private static final String TAG = "HttpClient";

    public interface ReplyHandler {
        void serverReply(int statusCode, String body);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public void post(final String url, final String json, final ReplyHandler replyHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doPost(url, json, replyHandler);
            }
        }).start();
    }

    private void doPost(final String url, final String json, final ReplyHandler replyHandler) {
        boolean accepted = false;
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            String content = "";
            Integer code;
            Log.i(TAG, "POST " + url + " (payload length = " + json.length() + ")");
            try {
                Response response = client.newCall(request).execute();
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    content = responseBody.string();
                }
                code = response.code();
            } catch (IOException e) {
                Log.i(TAG, "Error connecting to server: " + e.toString());
                code = null;
            }
            if (code != null) {
                accepted = true;
                replyHandler.serverReply(code, content);
            }
        } finally {
            if (!accepted) {
                replyHandler.serverReply(0, null);
            }
        }
    }
}
