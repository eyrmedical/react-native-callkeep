package com.eyr.callkeep;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

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
  public static final String ACCEPT_CALL_EVENT = "ACCEPT_CALL_EVENT";
  public static final String CALL_IS_DECLINED = "CALL_IS_DECLINED";
  public static final String NOTIFICATION_EXTRA_PAYLOAD = "NOTIFICATION_EXTRA_PAYLOAD";

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
  public void configure(@NonNull ReadableMap options) {

  }

  @ReactMethod
  public void startCallBanner(@Nullable ReadableMap callBannerPayload) {
    Log.d("ReactNativeJS", "starting call banner");
    Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
    // TODO: convert callBannerPayload to native map
    intent.setAction(START_CALL_BANNER);
    intent.putExtra(ACTION_PAYLOAD_KEY, callBannerPayload.toHashMap());
    reactContext.getApplicationContext().startService(intent);
    Log.d("ReactNativeJS", "call banner service started");
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

    constants.put("PRIORITY_MAX", NotificationCompat.PRIORITY_MAX);
    constants.put("PRIORITY_HIGH", NotificationCompat.PRIORITY_HIGH);
    constants.put("PRIORITY_DEFAULT", NotificationCompat.PRIORITY_DEFAULT);
    constants.put("PRIORITY_MIN", NotificationCompat.PRIORITY_MIN);
    constants.put("PRIORITY_LOW", NotificationCompat.PRIORITY_LOW);

    constants.put("CATEGORY_CALL", NotificationCompat.CATEGORY_CALL);

    constants.put("VISIBILITY_PUBLIC", NotificationCompat.VISIBILITY_PUBLIC);
    constants.put("VISIBILITY_PRIVATE", NotificationCompat.VISIBILITY_PRIVATE);
    constants.put("VISIBILITY_SECRET", NotificationCompat.VISIBILITY_SECRET);

    return constants;
  }
}
