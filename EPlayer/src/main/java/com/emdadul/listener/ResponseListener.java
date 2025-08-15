package com.emdadul.listener;

import androidx.annotation.NonNull;

import org.json.JSONObject;

public interface ResponseListener {
    void onResponse(@NonNull JSONObject jsonObject);

    default void onFailure(@NonNull Exception e) {
    }

}
