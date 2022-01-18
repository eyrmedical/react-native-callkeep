package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_SHOW_ONGOING_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_ACCEPT_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_DECLINE_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_END_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.PAYLOAD;
import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_START_CALL_BANNER;

import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;

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

  @ReactMethod
  public Boolean isMIUI() {
    return XiaomiUtilities.isMIUI();
  }

  @ReactMethod
  public Boolean hasMIUIShowWhenLockPermission() {
    return XiaomiUtilities.isCustomPermissionGranted(
      reactContext.getApplicationContext(),
      XiaomiUtilities.OP_SHOW_WHEN_LOCKED);
  }

  @ReactMethod
  public void openMIUIPermissionScreen() {
    try {
      Intent intent = XiaomiUtilities.getPermissionManagerIntent(reactContext.getApplicationContext());
      reactContext.startActivity(intent);
    } catch (Exception e) {
      try {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + reactContext.getApplicationContext().getPackageName()));
        reactContext.startActivity(intent);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  @Override
  public boolean hasConstants() {
    return true;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<>();
    constants.put("EVENT_ACCEPT_CALL", EVENT_ACCEPT_CALL);
    constants.put("EVENT_DECLINE_CALL", EVENT_DECLINE_CALL);
    constants.put("EVENT_END_CALL", EVENT_END_CALL);

    return constants;
  }
}
