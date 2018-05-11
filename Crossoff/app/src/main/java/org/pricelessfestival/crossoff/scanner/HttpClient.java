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

    public interface Handler {
        void accept(int statusCode, String body);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public void post(final String url, final String json, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                doPost(url, json, handler);
            }
        }).start();
    }

    private void doPost(final String url, final String json, final Handler handler) {
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
                handler.accept(code, content);
            }
        } finally {
            if (!accepted) {
                handler.accept(0, null);
            }
        }
    }
}
