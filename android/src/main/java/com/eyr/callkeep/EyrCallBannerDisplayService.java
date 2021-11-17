package com.eyr.callkeep;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

import static com.eyr.callkeep.EyrCallBannerControllerModule.*;
import static com.eyr.callkeep.Utils.createIncomingCallNotificationChannel;
import static com.eyr.callkeep.Utils.getIncomingCallActivityClass;
import static com.eyr.callkeep.Utils.getJsBackgroundPayload;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.getMainActivity;
import static com.eyr.callkeep.Utils.isDeviceScreenLocked;
import static com.eyr.callkeep.Utils.reactToCall;

public class EyrCallBannerDisplayService extends Service {
  public static final int CALL_NOTIFICATION_ID = 23;

  private static final String ACCEPT_CALL = "ACCEPT_CALL";
  private static final String SHOW_INCOMING_CALL = "SHOW_INCOMING_CALL";

  private CallPlayer callPlayer = new CallPlayer();;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (callPlayer!=null) {
      callPlayer.stop();
    }
    stopIncomingCallActivity();
  }

  private void stopIncomingCallActivity() {
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    localBroadcastManager.sendBroadcast(new Intent(CALL_IS_DECLINED));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    createIncomingCallNotificationChannel(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (callPlayer == null) {
      callPlayer = new CallPlayer();
    }
    String action = intent.getAction();

    final HashMap<String, Object> payload = (HashMap<String, Object>) intent.getSerializableExtra(ACTION_PAYLOAD_KEY);
    if (action.equals(START_CALL_BANNER)) {
      prepareNotification(intent, payload);
    }

    if (action.equals(DISMISS_BANNER)) {
        reactToCall((ReactApplication) getApplication(), CALL_IS_DECLINED, null);
    }

    if (action.equals(ACCEPT_CALL)) {
      Utils.backToForeground(getApplicationContext(),getJsPayload(payload));
      reactToCall((ReactApplication) getApplication(), ACCEPT_CALL_EVENT, getJsBackgroundPayload(payload));
      stopForeground(true);
    }

    return START_NOT_STICKY;
  }
  @SuppressLint("WrongConstant")
  private void prepareNotification(Intent intent, HashMap<String, Object> payload) {
    Intent dismissBannerIntent = new Intent(this, getClass());
    dismissBannerIntent.setAction(DISMISS_BANNER);

    Intent acceptCallAndOpenApp = new Intent(this, getClass());
    acceptCallAndOpenApp.setAction(ACCEPT_CALL);
    acceptCallAndOpenApp.putExtras(intent);

    Intent openIncomingCallScreen = new Intent(this, IncomingCallActivity.class);
    openIncomingCallScreen.setAction(SHOW_INCOMING_CALL);
    openIncomingCallScreen.putExtras(intent);


    PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
      dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent acceptCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      acceptCallAndOpenApp, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openIncomingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT);


    openIncomingCallScreen.addFlags(FLAG_ACTIVITY_NEW_TASK);
    NotificationCompat.Builder notificationBuilder =
      new EyrNotificationCompatBuilderArgSerializer(payload).createNotificationFromContext(this)
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          EyrNotificationCompatBuilderArgSerializer.parseAcceptBtnTitle(payload),
          acceptCallPendingIntent).build())
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          EyrNotificationCompatBuilderArgSerializer.parseDeclineBtnTitle(payload),
          pendingDismissBannerIntent).build())
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setFullScreenIntent(openIncomingCallScreenPendingIntent, isDeviceScreenLocked(getApplicationContext()))
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(Notification.CATEGORY_CALL)
        .setSound(null)
        .setVibrate(null)
        .setOngoing(true);
    startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
    callPlayer.play(this);
  }
}
