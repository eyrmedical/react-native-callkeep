package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerControllerModule.CALL_INCOMING_CHANNEL_ID;
import static com.eyr.callkeep.EyrCallBannerControllerModule.INITIAL_CALL_STATE_PROP_NAME;
import static com.eyr.callkeep.EyrCallBannerControllerModule.NOTIFICATION_EXTRA_PAYLOAD;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;

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

  public static Boolean isDeviceScreenLocked(Context context) {
    boolean isLocked;

    // First we check the locked state
    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    boolean inKeyguardRestrictedInputMode = keyguardManager.inKeyguardRestrictedInputMode();

    if (inKeyguardRestrictedInputMode) {
      isLocked = true;

    } else {
      // If password is not set in the settings, the inKeyguardRestrictedInputMode() returns false,
      // so we need to check if screen on for this case

      PowerManager powerManager = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
      isLocked = !powerManager.isInteractive();
    }
    return isLocked;
  }

  /**
   * @return true if pattern set, false if not (or if an issue when checking)
   */
  private static Boolean isPatternSet(Context context) {
    ContentResolver cr  = context.getContentResolver();
    try {
      int lockPatternEnable  = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
      return lockPatternEnable == 1;
    } catch (Settings.SettingNotFoundException e) {
      return false;
    }
  }

  /**
   * @return true if pass or pin set
   */
  private static Boolean isPassOrPinSet(Context context) {
    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
    return keyguardManager.isKeyguardSecure();
  }

  /**
   * @return true if pass or pin or pattern locks screen
   */
  @TargetApi(Build.VERSION_CODES.M)
  private static Boolean isDeviceLocked(Context context) {
    TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    int simState = telMgr.getSimState();
    KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    return keyguardManager.isKeyguardSecure() && simState != TelephonyManager.SIM_STATE_ABSENT;
  }

  /**
   * Back main activity to foreground.
   */
  public static void backToForeground(Context applicationContext,@Nullable Bundle bundle) {
        /*
        val mainActivityIntent = Intent(context, getMainActivityClass(context)).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mainActivityIntent)
        */

    String packageName = applicationContext.getPackageName();
    Intent focusIntent = applicationContext.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();

    focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
    focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    focusIntent.putExtra(INITIAL_CALL_STATE_PROP_NAME, bundle);
    applicationContext.startActivity(focusIntent);

  }

  public static Class getMainActivity(Context context) {
    String packageName = context.getPackageName();
    Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
    String className = launchIntent.getComponent().getClassName();
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Nullable
  public static Class getIncomingCallActivityClass() {
    Class mainActivityClass = null;
    try {
      // TODO: Do not hard code MainActivity since the app itself can use a different class name
      String mainActivityClassName = "com.eyr.callkeep.IncomingCallActivity";
      mainActivityClass = Class.forName(mainActivityClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return mainActivityClass;
  }

  public static void reactToCall(ReactApplication application, String event, WritableMap payload) {
    ReactNativeHost reactNativeHost = application.getReactNativeHost();
    ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
    ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
      if (reactContext != null) {
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
    if (payload==null) return null;
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

  public static void createIncomingCallNotificationChannel(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationManager notificationManager =
        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      NotificationChannel channel;
      Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.nosound);
      try {
        channel = notificationManager.getNotificationChannel(CALL_INCOMING_CHANNEL_ID);
      } catch (Exception e) {
        channel = new NotificationChannel(CALL_INCOMING_CHANNEL_ID,
          "Ongoing call",
          NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
      }
      if (channel==null) {
        channel = new NotificationChannel(CALL_INCOMING_CHANNEL_ID,
          "Ongoing call",
          NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
      }
      AudioAttributes audioAttributes = new AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .build();
      channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
      channel.setSound(uri, audioAttributes);
    }
  }
}
