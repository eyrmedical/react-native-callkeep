package com.eyr.callkeep;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.ReactApplication;

import java.util.HashMap;

import static com.eyr.callkeep.Utils.getJsBackgroundPayload;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.getMainActivityIntent;
import static com.eyr.callkeep.Utils.emitEventToJS;
import static com.eyr.callkeep.Utils.INITIAL_CALL_STATE_PROP_NAME;

public class CallKeepService extends Service {
  private static final String TAG = "CallKeepService";
  public static final int INCOMING_CALL_NOTIFICATION_ID = 23;
  public static final int ONGOING_CALL_NOTIFICATION_ID = 22;

  public static final String ACTION_DISPLAY_INCOMING_CALL = "ACTION_DISPLAY_INCOMING_CALL";
  public static final String ACTION_ACCEPT_CALL = "ACTION_ACCEPT_CALL";
  public static final String ACTION_DECLINE_CALL = "ACTION_DECLINE_CALL";
  public static final String ACTION_DISPLAY_ONGOING_CALL = "ACTION_DISPLAY_ONGOING_CALL_NOTIFICATION";
  public static final String ACTION_END_CALL = "ACTION_END_CALL";

  public static final String ACTION_END_CALL_FROM_NOTIFICATION = "ACTION_END_CALL_FROM_NOTIFICATION";
  public static final String EVENT_ACCEPT_CALL = "EVENT_ACCEPT_CALL";
  public static final String EVENT_DECLINE_CALL = "EVENT_DECLINE_CALL";
  public static final String EVENT_END_CALL = "EVENT_END_CALL";


  public static final String CHANNEL_NAME_INCOMING_CALL = "Incoming Call notification";
  public static final String CHANNEL_NAME_ONGOING_CALL = "Ongoing Call notification";
  public static final String CHANNEL_ID_INCOMING_CALL = "com.eyr.callkeep.incoming_call_event";
  public static final String CHANNEL_ID_ONGOING_CALL = "com.eyr.callkeep.ongoing_call_event";

  public static final String PAYLOAD = "PAYLOAD";

  public static final String NOTIFICATION_EXTRA_PAYLOAD = "NOTIFICATION_EXTRA_PAYLOAD";

  private CallKeepPlayer callKeepPlayer = new CallKeepPlayer();

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    prepareIncomingCallNotificationChannel();
    prepareOngoingCallNotificationChannel();
  }

  @Override
  public void onDestroy() {
    if (callKeepPlayer != null) {
      callKeepPlayer.stop(this);
    }
    stopIncomingCallActivity();
    super.onDestroy();
  }

  private void stopIncomingCallActivity() {
    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
    localBroadcastManager.sendBroadcast(new Intent(EVENT_DECLINE_CALL));
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (callKeepPlayer == null) {
      callKeepPlayer = new CallKeepPlayer();
    }
    if (intent != null) {
      String action = intent.getAction();
      ReactApplication application = (ReactApplication) getApplication();
      Log.d(TAG, action);
      final HashMap<String, Object> payload = (HashMap<String, Object>) intent.getSerializableExtra(PAYLOAD);
      if (action.equals(ACTION_DISPLAY_INCOMING_CALL)) {
        Notification notification = buildIncomingCallNotification(intent, payload);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          startForeground(INCOMING_CALL_NOTIFICATION_ID, notification, FOREGROUND_SERVICE_TYPE_PHONE_CALL);
        } else {
          startForeground(INCOMING_CALL_NOTIFICATION_ID, notification);
        }
        callKeepPlayer.play(this);
      }

      if (action.equals(ACTION_ACCEPT_CALL)) {
        if (callKeepPlayer != null) {
          callKeepPlayer.stop(this);
        }
        emitEventToJS(application, EVENT_ACCEPT_CALL, getJsBackgroundPayload(payload));
        stopForeground(true);
      }

      if (action.equals(ACTION_DECLINE_CALL)) {
        if (callKeepPlayer != null) {
          callKeepPlayer.stop(this);
        }
        emitEventToJS(application, EVENT_DECLINE_CALL, null);
        stopForeground(true);
      }

      if (action.equals(ACTION_END_CALL_FROM_NOTIFICATION)) {
        emitEventToJS(application, EVENT_END_CALL, null);
        stopForeground(true);
      }

      if (action.equals(ACTION_END_CALL)) {
        stopForeground(true);
      }

      if (action.equals(ACTION_DISPLAY_ONGOING_CALL)) {
        Notification notification = buildOngoingCallNotification(payload);
        startForeground(ONGOING_CALL_NOTIFICATION_ID, notification);
      }
    }
    super.onStartCommand(intent, flags, startId);
    return START_STICKY;
  }

  @TargetApi(26)
  private void prepareIncomingCallNotificationChannel() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);

    if (notificationManager != null) {
      NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID_INCOMING_CALL);

      if (channel == null) {
        channel = new NotificationChannel(CHANNEL_ID_INCOMING_CALL,
          CHANNEL_NAME_INCOMING_CALL,
          NotificationManager.IMPORTANCE_HIGH);
        channel.setSound(null, null);
        channel.enableVibration(false);
        notificationManager.createNotificationChannel(channel);
      }
    }
  }

  @TargetApi(26)
  public void prepareOngoingCallNotificationChannel() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);

    if (notificationManager != null) {
      NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID_ONGOING_CALL);

      if (channel == null) {
        channel = new NotificationChannel(CHANNEL_ID_ONGOING_CALL,
          CHANNEL_NAME_ONGOING_CALL,
          NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null, null);
        channel.enableVibration(false);
        notificationManager.createNotificationChannel(channel);
      }
    }
  }

  private Notification buildIncomingCallNotification(Intent intent, HashMap<String, Object> payload) {
    Intent declineCall = new Intent(this, getClass());
    declineCall.setAction(ACTION_DECLINE_CALL);

    Intent acceptCallAndOpenApp = getMainActivityIntent(getApplicationContext());
    acceptCallAndOpenApp.setAction(ACTION_ACCEPT_CALL);
    acceptCallAndOpenApp.putExtras(intent);
    acceptCallAndOpenApp.putExtra(INITIAL_CALL_STATE_PROP_NAME, getJsPayload(payload));

    Intent openIncomingCallScreen = new Intent(this, IncomingCallActivity.class);
    openIncomingCallScreen.setAction(ACTION_DISPLAY_INCOMING_CALL);
    openIncomingCallScreen.putExtras(intent);
    openIncomingCallScreen.addFlags(FLAG_ACTIVITY_NEW_TASK);

    PendingIntent pendingDeclineCallIntent = PendingIntent.getService(getApplicationContext(), 0,
      declineCall, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    PendingIntent acceptCallPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      acceptCallAndOpenApp, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openIncomingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    return new NotificationCompatBuilderArgSerializer(payload)
        .createNotificationFromContext(this, CHANNEL_ID_INCOMING_CALL)
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          NotificationCompatBuilderArgSerializer.parseDeclineBtnTitle(payload),
          pendingDeclineCallIntent).build())
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          NotificationCompatBuilderArgSerializer.parseAcceptBtnTitle(payload),
          acceptCallPendingIntent).build())
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setFullScreenIntent(openIncomingCallScreenPendingIntent, true)
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(Notification.CATEGORY_CALL)
        .setOngoing(true)
        .build();
  }

  private Notification buildOngoingCallNotification(HashMap<String, Object> payload) {
    Intent openOngoingCallScreen = getMainActivityIntent(getApplicationContext());
    PendingIntent openOngoingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openOngoingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    Intent endCall = new Intent(this, getClass());
    endCall.setAction(ACTION_END_CALL_FROM_NOTIFICATION);
    PendingIntent endCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      endCall, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

    return new NotificationCompatBuilderArgSerializer(payload)
        .createNotificationFromContext(this,CHANNEL_ID_ONGOING_CALL)
        .addAction(new NotificationCompat.Action.Builder(
          R.drawable.ic_notification,
          NotificationCompatBuilderArgSerializer.parseEndCallBtnTitle(payload),
          endCallPendingIntent).build())
        .setDefaults(Notification.DEFAULT_LIGHTS)
        .setContentIntent(openOngoingCallScreenPendingIntent)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setCategory(Notification.CATEGORY_CALL)
        .setAutoCancel(false)
        .setOngoing(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .setSilent(true)
        .build();
  }

  public static void reportMainActivityReady(Context context, Intent originalIntent) {
    Intent intent = new Intent(originalIntent);
    intent.setClass(context, CallKeepService.class);
    context.startService(intent);
  }
}
