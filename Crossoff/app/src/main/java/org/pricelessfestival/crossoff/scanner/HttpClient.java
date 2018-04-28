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
        void accept(int code, String content);
    }

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public boolean post(final String url, final String json, final Handler handler) {
        final boolean[] result = new boolean[1];
        result[0] = false;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] = doPost(url, json, handler);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "Interrupted during network operation: " + e.toString());
        }
        return result[0];
    }

    private boolean doPost(final String url, final String json, final Handler handler) {
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
            handler.accept(code, content);
            return true;
        }
        return false;
    }
}
