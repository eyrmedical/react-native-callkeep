package com.callkeepdemo.call;

import android.app.Application;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BackgroundCallBannerModule extends ReactContextBaseJavaModule {
    public static final String CALL_INCOMING_CHANNEL_ID = "Ongoing Call";
    public static final String START_CALL_BANNER = "START_CALL_BANNER";
    public static final String DISMISS_BANNER = "DISMISS_BANNER";
    public static final String ACTION_PAYLOAD_KEY = "ACTION_PAYLOAD_KEY";

    private Application application;

    public BackgroundCallBannerModule(ReactApplicationContext reactContext, Application application) {
        super(reactContext);
        this.application = application;
    }

    @NonNull
    @NotNull
    @Override
    public String getName() {
        return "BackgroundCallBannerModule";
    }

    @ReactMethod
    public void startCallBanner(@Nullable ReadableMap callBannerPayload) {
        Intent intent = new Intent(application, CallBannerDisplayService.class);
        // TODO: convert callBannerPayload to native map
        HashMap<String, String> nativeCallBannerPayload = new HashMap<>();
        intent.setAction(START_CALL_BANNER);
        intent.putExtra(ACTION_PAYLOAD_KEY, nativeCallBannerPayload);
        application.startService(intent);
    }

    @ReactMethod
    public void stopCallBanner() {
        Intent intent = new Intent(application, CallBannerDisplayService.class);
        application.stopService(intent);
    }

    @Override
    public boolean hasConstants() {
        return true;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<>();
        constants.put("CALL_INCOMING_CHANNEL_ID", CALL_INCOMING_CHANNEL_ID);
        return constants;
    }
}
