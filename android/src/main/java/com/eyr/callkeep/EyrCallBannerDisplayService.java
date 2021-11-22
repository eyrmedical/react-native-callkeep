package com.eyr.callkeep;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactApplication;

import java.util.HashMap;

import static com.eyr.callkeep.Utils.createIncomingCallNotificationChannel;
import static com.eyr.callkeep.Utils.createOngoingCallNotificationChannel;
import static com.eyr.callkeep.Utils.getJsBackgroundPayload;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.getMainActivityIntent;
import static com.eyr.callkeep.Utils.reactToCall;

public class EyrCallBannerDisplayService extends Service {
  public static final int CALL_NOTIFICATION_ID = 23;
  public static final int ONGOING_CALL_NOTIFICATION_ID = 22;

  public static final String ACTION_ACCEPT_CALL = "ACTION_ACCEPT_CALL";
  public static final String ACTION_SHOW_INCOMING_CALL = "ACTION_SHOW_INCOMING_CALL";
  public static final String ACTION_START_CALL_BANNER = "ACTION_START_CALL_BANNER";
  public static final String ACTION_DISMISS_BANNER = "ACTION_DISMISS_BANNER";
  public static final String ACTION_END_CALL = "ACTION_END_CALL";
  public static final String ACTION_SHOW_ONGOING_CALL = "ACTION_SHOW_ONGOING_CALL";


  public static final String CHANNEL_NAME_INCOMING_CALL = "Incoming Call notification";
  public static final String CHANNEL_NAME_ONGOING_CALL = "Ongoing Call notification";
  public static final String CHANNEL_ID_INCOMING_CALL = "com.eyr.callkeep.incoming_call_really_new";
  public static final String CHANNEL_ID_ONGOING_CALL = "com.eyr.callkeep.ongoing_call_new_really_new";

  public static final String PAYLOAD = "PAYLOAD";

  public static final String EVENT_ACCEPT_CALL = "EVENT_ACCEPT_CALL";
  public static final String EVENT_DECLINE_CALL = "EVENT_DECLINE_CALL";
  public static final String EVENT_END_CALL = "EVENT_END_CALL";


  public static final String NOTIFICATION_EXTRA_PAYLOAD = "NOTIFICATION_EXTRA_PAYLOAD";

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
    localBroadcastManager.sendBroadcast(new Intent(EVENT_DECLINE_CALL));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    createIncomingCallNotificationChannel(this);
    createOngoingCallNotificationChannel(this);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (callPlayer == null) {
      callPlayer = new CallPlayer();
    }
    String action = intent.getAction();

    final HashMap<String, Object> payload = (HashMap<String, Object>) intent.getSerializableExtra(PAYLOAD);
    if (action.equals(ACTION_START_CALL_BANNER)) {
      prepareIncomingNotification(intent, payload);
    }

    if (action.equals(ACTION_DISMISS_BANNER)) {
        reactToCall((ReactApplication) getApplication(), EVENT_DECLINE_CALL, null);
    }

    if (action.equals(ACTION_ACCEPT_CALL)) {
      Utils.backToForeground(getApplicationContext(),getJsPayload(payload));
      reactToCall((ReactApplication) getApplication(), EVENT_ACCEPT_CALL, getJsBackgroundPayload(payload));
      if (callPlayer!=null) {
        callPlayer.stop();
      }
    }

    if (action.equals(ACTION_END_CALL)) {
      reactToCall((ReactApplication) getApplication(), EVENT_END_CALL, getJsBackgroundPayload(payload));
      stopForeground(true);
    }


    if (action.equals(ACTION_SHOW_ONGOING_CALL)) {
      prepareOngoingNotification(payload);
    }

    return START_STICKY;
  }

  private void prepareIncomingNotification(Intent intent, HashMap<String, Object> payload) {
    Intent dismissBannerIntent = new Intent(this, getClass());
    dismissBannerIntent.setAction(ACTION_DISMISS_BANNER);

    Intent acceptCallAndOpenApp = new Intent(this, getClass());
    acceptCallAndOpenApp.setAction(ACTION_ACCEPT_CALL);
    acceptCallAndOpenApp.putExtras(intent);

    Intent openIncomingCallScreen = new Intent(this, IncomingCallActivity.class);
    openIncomingCallScreen.setAction(ACTION_SHOW_INCOMING_CALL);
    openIncomingCallScreen.putExtras(intent);


    PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
      dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent acceptCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      acceptCallAndOpenApp, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openIncomingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT);

    openIncomingCallScreen.addFlags(FLAG_ACTIVITY_NEW_TASK);
    NotificationCompat.Builder notificationBuilder =
      new EyrNotificationCompatBuilderArgSerializer(payload)
        .createNotificationFromContext(this, CHANNEL_ID_INCOMING_CALL)
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          EyrNotificationCompatBuilderArgSerializer.parseAcceptBtnTitle(payload),
          acceptCallPendingIntent).build())
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          EyrNotificationCompatBuilderArgSerializer.parseDeclineBtnTitle(payload),
          pendingDismissBannerIntent).build())
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setFullScreenIntent(openIncomingCallScreenPendingIntent, true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setCategory(Notification.CATEGORY_CALL)
        .setOngoing(true);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build(), FOREGROUND_SERVICE_TYPE_PHONE_CALL);
    } else {
      startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
    }
    callPlayer.play(this);
  }

  private void prepareOngoingNotification(HashMap<String, Object> payload) {
    Intent openOngoingCallScreen = getMainActivityIntent(this);
    PendingIntent openOngoingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openOngoingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT);

    Intent endCallOpenApp = new Intent(this, getClass());
    endCallOpenApp.setAction(ACTION_END_CALL);
    PendingIntent endCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      endCallOpenApp, PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationCompat.Builder notificationBuilder =
      new EyrNotificationCompatBuilderArgSerializer(payload)
        .createNotificationFromContext(this,CHANNEL_ID_ONGOING_CALL)
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          EyrNotificationCompatBuilderArgSerializer.parseEndCallBtnTitle(payload),
          endCallPendingIntent).build())
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setContentIntent(openOngoingCallScreenPendingIntent)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(Notification.CATEGORY_CALL)
        .setAutoCancel(false)
        .setOngoing(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setSilent(true);
    startForeground(ONGOING_CALL_NOTIFICATION_ID, notificationBuilder.build());
  }
}
