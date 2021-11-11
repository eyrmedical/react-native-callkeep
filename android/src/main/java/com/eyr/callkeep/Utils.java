package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerControllerModule.NOTIFICATION_EXTRA_PAYLOAD;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

public class Utils {

  public static Boolean isDeviceScreenLocked(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return isDeviceLocked(context);
    } else {
      return isPatternSet(context) || isPassOrPinSet(context);
    }
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
  public static void backToForeground(Context applicationContext, Activity activity) {
        /*
        val mainActivityIntent = Intent(context, getMainActivityClass(context)).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(mainActivityIntent)
        */

    String packageName = applicationContext.getPackageName();
    Intent focusIntent = applicationContext.getPackageManager().getLaunchIntentForPackage(packageName).cloneFilter();

    focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

    if (activity != null) {
      activity.startActivity(focusIntent);
    } else {
      focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      applicationContext.startActivity(focusIntent);
    }

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

  public static WritableMap getJsPayload(HashMap<String, Object> payload) {
    if (payload==null) return null;
    WritableMap jsMap = new WritableNativeMap();
    for (HashMap.Entry<String, Object> entry : payload.entrySet()) {
      if (entry.getKey().equals(NOTIFICATION_EXTRA_PAYLOAD)) {
        jsMap = (WritableMap) entry.getValue();
      }
    }
    return jsMap;
  }
}
