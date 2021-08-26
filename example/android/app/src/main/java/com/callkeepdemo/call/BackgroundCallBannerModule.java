package com.callkeepdemo.call;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.facebook.react.bridge.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class BackgroundCallBannerModule extends ContextBaseJavaModule {
    public static final String CALL_INCOMING_CHANNEL_ID = "Ongoing Call";
    public static final String START_CALL_BANNER = "START_CALL_BANNER";
    public static final String ACTION_PAYLOAD_KEY = "ACTION_PAYLOAD_KEY";

    public BackgroundCallBannerModule(Context context) {
        super(context);
    }

    @NonNull
    @NotNull
    @Override
    public String getName() {
        return "BackgroundCallBannerModule";
    }

    @ReactMethod
    public void startCallBanner(@Nullable ReadableMap callBannerPayload) {
        Context appContext = getContext();
        Intent intent = new Intent(appContext, CallBannerDisplayService.class);
        // TODO: convert callBannerPayload to native map
        HashMap<String, String> nativeCallBannerPayload = new HashMap<>();
        intent.setAction(START_CALL_BANNER);
        intent.putExtra(ACTION_PAYLOAD_KEY, nativeCallBannerPayload);
        appContext.startService(intent);
    }
}
