package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_SHOW_ONGOING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.PAYLOAD;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_ID_INCOMING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_NAME_INCOMING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_START_CALL_BANNER;
import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EyrCallBannerControllerModule extends ReactContextBaseJavaModule {

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
  public void startOngoingCallNotification(@Nullable ReadableMap callBannerPayload) {
    Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
    intent.setAction(ACTION_SHOW_ONGOING_CALL);
    intent.putExtra(PAYLOAD, callBannerPayload.toHashMap());
    reactContext.getApplicationContext().startService(intent);
  }

  @ReactMethod
  public void startCallBanner(@Nullable ReadableMap callBannerPayload) {
    Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
    intent.setAction(ACTION_START_CALL_BANNER);
    intent.putExtra(PAYLOAD, callBannerPayload.toHashMap());
    reactContext.getApplicationContext().startService(intent);
  }

  @ReactMethod
  public void stopCallBanner() {
    Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
    reactContext.getApplicationContext().stopService(intent);
  }

  @ReactMethod
  public void hideOnLockScreen(){
    Objects.requireNonNull(reactContext.getCurrentActivity()).runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Utils.showOnLockscreen(reactContext.getCurrentActivity(),false);
      }
    });

  }

  @Override
  public boolean hasConstants() {
    return true;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<>();
    constants.put("CALL_INCOMING_CHANNEL_NAME", CHANNEL_NAME_INCOMING_CALL);
    constants.put("CALL_INCOMING_CHANNEL_ID", CHANNEL_ID_INCOMING_CALL);

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
