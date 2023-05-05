package com.eyr.callkeep;

import static com.eyr.callkeep.CallKeepService.NOTIFICATION_EXTRA_PAYLOAD;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

public class Utils {

  public static final String INITIAL_CALL_STATE_PROP_NAME = "initialCallState";

  public static Intent getMainActivityIntent(Context applicationContext) {
    String packageName = applicationContext.getPackageName();
    Intent focusIntent = applicationContext.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();

    focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    return focusIntent;
  }

  public static void emitEventToJS(ReactApplication application, String event, WritableMap payload) {
    ReactNativeHost reactNativeHost = application.getReactNativeHost();
    ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
    if (reactContext != null) {
      Log.d("emitEventToJS", event);
      reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(event, payload);
    }
  }

  public static WritableMap getJsBackgroundPayload(HashMap<String, Object> payload) {
    if (payload==null) return null;
    WritableMap jsMap = new WritableNativeMap();
    for (HashMap.Entry<String, Object> entry : payload.entrySet()) {
      if (entry.getKey().equals(NOTIFICATION_EXTRA_PAYLOAD)) {
        HashMap<String, Object> miniPayload = (HashMap<String, Object>) entry.getValue();
        for (Map.Entry<String, Object> in : miniPayload.entrySet()) {
          jsMap.putString(in.getKey(), String.valueOf(in.getValue()));
        }
      }
    }
    return jsMap;
  }

  public static Bundle getJsPayload(HashMap<String, Object> payload) {
    if (payload == null) return null;
    Bundle jsMap = new Bundle();
    for (HashMap.Entry<String, Object> entry : payload.entrySet()) {
      if (entry.getKey().equals(NOTIFICATION_EXTRA_PAYLOAD)) {
        HashMap<String, Object> miniPayload = (HashMap<String, Object>) entry.getValue();
        for (Map.Entry<String, Object> in : miniPayload.entrySet()) {
          jsMap.putString(in.getKey(), String.valueOf(in.getValue()));
        }
      }
    }
    return jsMap;
  }

  public static void showOnLockscreen(Activity activity, boolean shouldShow) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      if (shouldShow) {

        activity.setTurnScreenOn(true);
        activity.setShowWhenLocked(true);

        activity.getWindow().addFlags(
          WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        return;
      }
      activity.getWindow().clearFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
      activity.setTurnScreenOn(false);
      activity.setShowWhenLocked(false);
    } else {
      if (shouldShow) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
          | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
          | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
          | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        return;
      }
      activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
  }


}
