package com.emdadul;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.emdadul.listener.OnEventListener;



public class EPlayer extends WebView {

    public static final int ENDED = 0;
    public static final int PLAYING = 1;
    public static final int PAUSED = 2;
    public static final int BUFFERING = 3;
    public static final int CUED = 5;


    static {
        System.loadLibrary("emdadul");
    }


    private int playbackState = -1;
    private OnEventListener listener;

    public EPlayer(@NonNull Context context) {
        super(context);
        init();
    }

    public EPlayer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    void init() {
        try {
            setLayerType(LAYER_TYPE_HARDWARE, null);
            getSettings().setJavaScriptEnabled(true);
            getSettings().setMediaPlaybackRequiresUserGesture(false);
            addJavascriptInterface(this, getClass().getSimpleName());
            loadDataWithBaseURL("http://www.youtube.com", initialize(), "text/html", "UTF-8", null);
            setWebChromeClient(new EClient((Activity) getContext()));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Sorry, there was a problem setting up the player. Please try restarting the app. If the issue persists, tap here to contact support.", Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        loadUrl("javascript:player.pauseVideo();");
    }

    public void playVideoAt(int position) {
        loadUrl("javascript:player.playVideoAt(" + position + ")");
    }

    public void cueVideo(String videoId, int position) {
        loadUrl(loadAllVideos(videoId, position));
    }

    public void cueVideo(String videoId) {
        loadUrl("javascript: player.cueVideoById('" + videoId + "', 0);");
    }

    public void loadVideo(String videoId) {
        loadUrl("javascript: player.loadVideoById('" + videoId + "', 0);");
    }

    public void onReceiveVideoIds() {
        loadUrl(getAllVideoIds());
    }

    public boolean isPlaying() {
        return playbackState == PLAYING;
    }

    @JavascriptInterface
    public void onPlayerReady() {
        post(() -> {
            if (listener != null) {
                listener.onPlayerReady();
            }
        });
    }

    @JavascriptInterface
    public void onPlaybackStateChange(int state) {
        post(() -> {
            playbackState = state;
            if (listener != null) {
                listener.onPlaybackStateChange(playbackState);
            }
        });
    }


    @JavascriptInterface
    public void onPlayerError() {
        post(() -> {
            playbackState = -123;
            if (listener != null) {
                listener.onPlaybackStateChange(playbackState);
            }
        });
    }

    @JavascriptInterface
    public void onReceiveVideoIds(String[] videoIds) {
        post(() -> {
            if (listener != null) {
                listener.onReceiveVideoIds(videoIds);
            }
        });
    }





    private native String initialize();

    private native String getAllVideoIds();

    public native String loadAllVideos(String videoId, int position);

    public void addOnEventListener(OnEventListener listener) {
        this.listener = listener;
    }
}