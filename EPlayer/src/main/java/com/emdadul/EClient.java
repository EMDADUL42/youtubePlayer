package com.emdadul;


import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.widget.FrameLayout;

import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class EClient extends WebChromeClient {

    final Activity activity;
    final Window window;
    final View view;
    View fullscreen;

    public EClient(Activity activity) {
        this.activity = activity;
        this.window = activity.getWindow();
        this.view = activity.findViewById(android.R.id.content);
    }

    @Override
    public void onHideCustomView() {
        if (fullscreen != null) {
            ((FrameLayout) window.getDecorView()).removeView(fullscreen);
            fullscreen = null;
        }
        showSystemUI();
    }

    @Override
    public void onShowCustomView(View view, CustomViewCallback callback) {
        fullscreen = view;
        ((FrameLayout) window.getDecorView()).addView(fullscreen, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        hideSystemUI();
    }

    private void hideSystemUI() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, view);
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void showSystemUI() {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        WindowInsetsControllerCompat controller = new WindowInsetsControllerCompat(window, view);
        controller.show(WindowInsetsCompat.Type.systemBars());
    }
}
