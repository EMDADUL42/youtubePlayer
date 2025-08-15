package com.emdadul;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;


import com.emdadul.listener.ResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class VideoInfoRepository {

    private static final String TAG = "VideoInfoRepository";
    private static final String PREFS_NAME = "video_info_cache";
    private static final String KEY_DATA = "data";
    private static final String KEY_TIMESTAMP = "timestamp";

    // Cache expiry duration in milliseconds (24 hours)
    private static final long CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L;

    static {
        System.loadLibrary("emdadul");
    }

    private static OkHttpClient client;

    private final SharedPreferences sharedPreferences;

    public VideoInfoRepository(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (client == null) {
            client = new OkHttpClient();
        }
    }

    /**
     * Fetches video info JSON for given videoId.
     * Uses cached data if not expired, otherwise fetches from network.
     */
    public void getInfo(@NonNull String videoId, @NonNull ResponseListener responseListener) {
        CacheEntry cacheEntry = getCacheEntry(videoId);
        long currentTime = System.currentTimeMillis();

        if (cacheEntry != null && (currentTime - cacheEntry.timestamp) < CACHE_EXPIRATION_MS) {
            Log.d(TAG, "Cache hit for videoId: " + videoId);
            JSONObject cachedJson = parseJson(cacheEntry.data, responseListener);
            if (cachedJson != null) {
                responseListener.onResponse(cachedJson);
                return;
            } else {
                Log.w(TAG, "Cache JSON parse failed, fetching from network");
            }
        } else {
            Log.d(TAG, "Cache miss or expired for videoId: " + videoId);
        }

        // Fetch from network
        String url = getInfo(videoId);
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
                responseListener.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                ResponseBody responseBody = response.body();
                if (responseBody == null) {
                    IOException e = new IOException("Empty response body");
                    Log.e(TAG, e.getMessage());
                    responseListener.onFailure(e);
                    return;
                }
                try {
                    String responseString = responseBody.string();
                    JSONObject jsonObject = parseJson(responseString, responseListener);
                    if (jsonObject != null) {
                        saveCacheEntry(videoId, responseString, System.currentTimeMillis());
                        responseListener.onResponse(jsonObject);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    responseListener.onFailure(e);
                }
            }
        });
    }

    /**
     * Parses a JSON string into JSONObject.
     */
    private JSONObject parseJson(String jsonString, ResponseListener responseListener) {
        if (jsonString == null) return null;
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            responseListener.onFailure(e);
            return null;
        }
    }

    /**
     * Loads cached JSON data and timestamp for a videoId.
     */
    private CacheEntry getCacheEntry(String videoId) {
        String jsonString = sharedPreferences.getString(videoId, null);
        if (jsonString == null) return null;

        try {
            JSONObject obj = new JSONObject(jsonString);
            String data = obj.optString(KEY_DATA, null);
            long timestamp = obj.optLong(KEY_TIMESTAMP, 0);
            if (data != null && timestamp > 0) {
                return new CacheEntry(data, timestamp);
            }
        } catch (JSONException e) {
            Log.w(TAG, "Corrupted cache data for videoId: " + videoId);
        }
        return null;
    }

    /**
     * Saves JSON data and timestamp atomically in SharedPreferences.
     */
    private void saveCacheEntry(String videoId, String data, long timestamp) {
        JSONObject obj = new JSONObject();
        try {
            obj.put(KEY_DATA, data);
            obj.put(KEY_TIMESTAMP, timestamp);
            sharedPreferences.edit().putString(videoId, obj.toString()).apply();
        } catch (JSONException e) {
            Log.w(TAG, "Failed to save cache for videoId: " + videoId);
        }
    }

    /**
     * Native method that returns URL to fetch video info JSON for given video ID.
     */
    private native String getInfo(String videoId);

    /**
     * Holder for cached data and timestamp.
     */
    private static class CacheEntry {
        final String data;
        final long timestamp;

        CacheEntry(String data, long timestamp) {
            this.data = data;
            this.timestamp = timestamp;
        }
    }
}


/*

package com.emdadul;


import android.content.Context;

import androidx.annotation.NonNull;


import com.emdadul.listener.ResponseListener;
import com.jummania.DataManager;
import com.jummania.DataManagerFactory;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class InfoRepository {


    static {
        System.loadLibrary("emdadul");
    }

    private static OkHttpClient client;

    private final DataManager dataManager;

    public InfoRepository(@NonNull Context context) {
        dataManager = DataManagerFactory.create(context.getCacheDir());
        if (client == null) client = new OkHttpClient();
    }

    public void getInfo(@NonNull String videoId, @NonNull ResponseListener responseListener) {
        JSONObject jsonObject = toJsonObject(dataManager.getRawString(videoId), responseListener);
        if (jsonObject != null) {
            responseListener.onResponse(jsonObject);
            return;
        }

        Request request = new Request.Builder().url(getInfo(videoId)).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                responseListener.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try (ResponseBody responseBody = response.body()) {
                    String responseString = responseBody.string();
                    responseListener.onResponse(toJsonObject(responseString, responseListener));
                    dataManager.saveString(videoId, responseString);
                } catch (Exception e) {
                    responseListener.onFailure(e);
                }
            }
        });
    }

    private JSONObject toJsonObject(String jsonString, ResponseListener responseListener) {
        if (jsonString == null) return null;
        try {
            return new JSONObject(jsonString);
        } catch (Exception e) {
            responseListener.onFailure(e);
        }
        return null;
    }

    private native String getInfo(String id);
}


 */