package org.pricelessfestival.crossoff.scanner.config;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

/**
 * Shared preferences for CrossOff
 * Store on device config for server address and port
 */
public class SharedPrefs {

    private static final String CROSSOFF_PREFERENCES = "CROSSOFF_PREFERENCES";
    private static final String KEY_SERVER_ADDRESS = "KEY_SERVER_ADDRESS";
    private static final String KEY_SERVER_PORT = "KEY_SERVER_PORT";


    private static SharedPrefs sInstance;
    private final SharedPreferences mPrefs;

    /**
     * Constructor with dependency injection
     */
    private SharedPrefs(SharedPreferences sharedPreferences) {
        mPrefs = sharedPreferences;
    }

    /**
     * Return singleton instance of SharedPrefs.
     * First call to this method will initialize a static instance
     */
    public static SharedPrefs instance(Context context) {
        if (sInstance == null) {
            sInstance = new SharedPrefs(context.getApplicationContext().getSharedPreferences(CROSSOFF_PREFERENCES,
                    Context.MODE_PRIVATE));
        }
        return sInstance;
    }

    //Server Address Storage
    public String getServerAddress() {
        return mPrefs.getString(KEY_SERVER_ADDRESS, AppSettings.CROSSOFF_SERVER_HOST_ADDR);
    }
    public void setServerAddress(String address) {
        mPrefs.edit().putString(KEY_SERVER_ADDRESS, address).apply();
    }

    //Server Port Storage
    public int getServerPort() {
        return mPrefs.getInt(KEY_SERVER_PORT, AppSettings.CROSSOFF_SERVER_PORT);
    }
    public void setServerPort(int port) {
        mPrefs.edit().putInt(KEY_SERVER_PORT, port).apply();
    }

    public String getBaseUrl() {
        return String.format(Locale.US, "http://%s:%d", getServerAddress(), getServerPort());
    }
}
