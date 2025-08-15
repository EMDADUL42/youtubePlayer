package com.emdadul.includeplayer;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class PlayListStorage {

    private static final String PREFS_NAME = "player_prefs";
    private static final String KEY_PLAYLIST = "playlist";
    private static final String KEY_LAST_FETCH_TIME = "last_fetch_time";

    private final SharedPreferences sharedPreferences;

    public PlayListStorage(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void savePlaylist(List<String> videoIds) {
        JSONArray jsonArray = new JSONArray();
        for (String id : videoIds) {
            jsonArray.put(id);
        }
        sharedPreferences.edit()
                .putString(KEY_PLAYLIST, jsonArray.toString())
                .apply();
    }

    public List<String> loadPlaylist() {
        List<String> videoIds = new ArrayList<>();
        String jsonString = sharedPreferences.getString(KEY_PLAYLIST, null);
        if (jsonString == null) return videoIds;

        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                videoIds.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return videoIds;
    }

    public void saveLastFetchTime(long timeMillis) {
        sharedPreferences.edit().putLong(KEY_LAST_FETCH_TIME, timeMillis).apply();
    }

    public long getLastFetchTime() {
        return sharedPreferences.getLong(KEY_LAST_FETCH_TIME, 0);
    }
}
