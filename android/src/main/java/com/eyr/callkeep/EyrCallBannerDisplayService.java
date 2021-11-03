package com.eyr.callkeep;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

import static com.eyr.callkeep.EyrCallBannerControllerModule.*;

public class EyrCallBannerDisplayService extends Service {
  public static final int CALL_NOTIFICATION_ID = 23;

  private static final String ACCEPT_CALL = "ACCEPT_CALL";
  private static final String SHOW_INCOMING_CALL = "SHOW_INCOMING_CALL";
  private Vibrator v;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Nullable
  private Class getMainActivityClass() {
    Class mainActivityClass = null;
    try {
      String mainApplicationPackageName = this.getApplication().getClass().getPackage().getName();
      // TODO: Do not hard code MainActivity since the app itself can use a different class name
      String mainActivityClassName = mainApplicationPackageName + ".MainActivity";
      mainActivityClass = Class.forName(mainActivityClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return mainActivityClass;
  }

  @Nullable
  private Class getIncomingCallActivityClass() {
    Class mainActivityClass = null;
    try {
      String mainApplicationPackageName = this.getApplication().getClass().getPackage().getName();
      // TODO: Do not hard code MainActivity since the app itself can use a different class name
      String mainActivityClassName = mainApplicationPackageName + ".IncomingCallActivity";
      mainActivityClass = Class.forName(mainActivityClassName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    return mainActivityClass;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    if (v!= null) {
      v.cancel();
    }
    return super.onUnbind(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (v!= null) {
      v.cancel();
    }
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    String action = intent.getAction();

    Intent dismissBannerIntent = new Intent(this, getClass());
    dismissBannerIntent.setAction(DISMISS_BANNER);

    Intent acceptCallAndOpenApp = new Intent(this, getClass());
    acceptCallAndOpenApp.setAction(ACCEPT_CALL);

    Intent openIncomingCallScreen = new Intent(this, getIncomingCallActivityClass());
    openIncomingCallScreen.setAction(SHOW_INCOMING_CALL);

    PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
      dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent acceptCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      acceptCallAndOpenApp, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openIncomingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT);
    HashMap<String, Object> payload = (HashMap<String, Object>) intent.getSerializableExtra(ACTION_PAYLOAD_KEY);
    if (action.equals(START_CALL_BANNER)) {

      NotificationCompat.Builder notificationBuilder =
        new EyrNotificationCompatBuilderArgSerializer(payload).createNotificationFromContext(this)
          .addAction(new NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            EyrNotificationCompatBuilderArgSerializer.parseAcceptBtnTitle(payload),
            acceptCallPendingIntent).build())
          .addAction(new NotificationCompat.Action.Builder(
            R.mipmap.ic_launcher,
            EyrNotificationCompatBuilderArgSerializer.parseDeclineBtnTitle(payload),
            pendingDismissBannerIntent).build())
          .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
          .setFullScreenIntent(openIncomingCallScreenPendingIntent, true)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setVibrate(null)
          .setOngoing(true);
      startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());

      v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v.vibrate(VibrationEffect.createWaveform(new long[] {1000, 1000, 1000, 1000, 1000, 1000}, 1));
      } else {
        //deprecated in API 26
        v.vibrate(new long[] {1000, 1000, 1000, 1000, 1000, 1000}, 1);
      }
    }



    if (action.equals(DISMISS_BANNER)) {
      ReactApplication application = (ReactApplication) this.getApplication();

      ReactNativeHost reactNativeHost = application.getReactNativeHost();
      ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
      ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
      if (reactContext != null) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("CALL_IS_DECLINED", null);
      }
    }

    if (action.equals(ACCEPT_CALL)) {
      /**/
      ReactApplication application = (ReactApplication) this.getApplication();
      ReactNativeHost reactNativeHost = application.getReactNativeHost();
      ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
      ReactContext reactContext = reactInstanceManager.getCurrentReactContext();
      if (reactContext != null) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit("ACCEPT_CALL_EVENT", payload);
      }
      Intent acceptIntent = new Intent(this, getMainActivityClass());
      acceptIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Bundle acceptCallAndOpenAppInitialProps = new Bundle();
      acceptCallAndOpenAppInitialProps.putBoolean("isCallAccepted", true);
      acceptCallAndOpenApp.putExtras(acceptCallAndOpenAppInitialProps);
      startActivity(acceptIntent, acceptCallAndOpenAppInitialProps);
      v.cancel();
      stopForeground(true);
    }

    if (action.equals(SHOW_INCOMING_CALL)) {
      Intent openIncomingCallIntent = new Intent(this, getMainActivityClass());
      openIncomingCallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Bundle openIncomingCallScreenInitialProps = new Bundle();
      openIncomingCallScreenInitialProps.putBoolean("isIncomingCall", true);
      openIncomingCallIntent.putExtras(openIncomingCallScreenInitialProps);
      startActivity(openIncomingCallIntent);
      stopForeground(true);
    }

    return START_NOT_STICKY;
  }
}
