package com.eyr.callkeep;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;

public class EyrCallBannerControllerModule extends ReactContextBaseJavaModule {
    // TODO: Make the consumer application to configure their own name and id for the call
    // banner channel
    public static final String CALL_INCOMING_CHANNEL_NAME = "Ongoing Call";
    public static final String CALL_INCOMING_CHANNEL_ID = "com.eyr.callkeep.incoming_call";
    public static final String START_CALL_BANNER = "START_CALL_BANNER";
    public static final String DISMISS_BANNER = "DISMISS_BANNER";
    public static final String ACTION_PAYLOAD_KEY = "ACTION_PAYLOAD_KEY";

    private ReactApplicationContext reactContext;

    public EyrCallBannerControllerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "EyrCallBannerControllerModule";
    }

    @ReactMethod
    public void startCallBanner(@Nullable ReadableMap callBannerPayload) {
        Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
        // TODO: convert callBannerPayload to native map
        HashMap<String, String> nativeCallBannerPayload = new HashMap<>();
        intent.setAction(START_CALL_BANNER);
        intent.putExtra(ACTION_PAYLOAD_KEY, nativeCallBannerPayload);
        reactContext.getApplicationContext().startService(intent);
    }

    @ReactMethod
    public void stopCallBanner() {
        Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
        reactContext.getApplicationContext().stopService(intent);
    }

    @Override
    public boolean hasConstants() {
        return true;
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<>();
        constants.put("CALL_INCOMING_CHANNEL_NAME", CALL_INCOMING_CHANNEL_NAME);
        constants.put("CALL_INCOMING_CHANNEL_ID", CALL_INCOMING_CHANNEL_ID);
        return constants;
    }
}
