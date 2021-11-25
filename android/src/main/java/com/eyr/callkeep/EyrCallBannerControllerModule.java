package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_SHOW_ONGOING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_ID_ONGOING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_NAME_ONGOING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_ACCEPT_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_DECLINE_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_END_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.PAYLOAD;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_ID_INCOMING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.CHANNEL_NAME_INCOMING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_START_CALL_BANNER;

import android.content.Intent;

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
  public void startCallNotificationService(@Nullable ReadableMap callBannerPayload) {
    Intent intent = new Intent(reactContext.getApplicationContext(), EyrCallBannerDisplayService.class);
    intent.setAction(ACTION_START_CALL_BANNER);
    intent.putExtra(PAYLOAD, callBannerPayload.toHashMap());
    reactContext.getApplicationContext().startService(intent);
  }

  @ReactMethod
  public void stopNotificationService() {
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
    constants.put("CHANNEL_NAME_INCOMING_CALL", CHANNEL_NAME_INCOMING_CALL);
    constants.put("CHANNEL_ID_INCOMING_CALL", CHANNEL_ID_INCOMING_CALL);
    constants.put("CHANNEL_ID_ONGOING_CALL", CHANNEL_ID_ONGOING_CALL);
    constants.put("CHANNEL_NAME_ONGOING_CALL", CHANNEL_NAME_ONGOING_CALL);


    constants.put("EVENT_ACCEPT_CALL", EVENT_ACCEPT_CALL);
    constants.put("EVENT_DECLINE_CALL", EVENT_DECLINE_CALL);
    constants.put("EVENT_END_CALL", EVENT_END_CALL);


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
