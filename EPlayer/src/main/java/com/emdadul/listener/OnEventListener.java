package com.emdadul.listener;

public interface OnEventListener {
    void onPlayerReady();

    void onPlaybackStateChange(int playerState);

    void onReceiveVideoIds(String[] videoIds);
}
