package com.eyr.callkeep;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;

import static com.eyr.callkeep.EyrCallBannerControllerModule.*;
import static com.eyr.callkeep.Utils.getIncomingCallActivityClass;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.getMainActivity;
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
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (callPlayer == null) {
      callPlayer = new CallPlayer();
    }
    String action = intent.getAction();

    Intent dismissBannerIntent = new Intent(this, getClass());
    dismissBannerIntent.setAction(DISMISS_BANNER);

    Intent acceptCallAndOpenApp = new Intent(this, getClass());
    acceptCallAndOpenApp.setAction(ACCEPT_CALL);
    acceptCallAndOpenApp.putExtras(intent);

    Intent openIncomingCallScreen = new Intent(this, getIncomingCallActivityClass());
    openIncomingCallScreen.setAction(SHOW_INCOMING_CALL);
    openIncomingCallScreen.putExtras(intent);

    PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
      dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent acceptCallPendingIntent = PendingIntent.getService(getApplicationContext(), 0,
      acceptCallAndOpenApp, PendingIntent.FLAG_UPDATE_CURRENT);

    PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
      openIncomingCallScreen, PendingIntent.FLAG_UPDATE_CURRENT);
    final HashMap<String, Object> payload = (HashMap<String, Object>) intent.getSerializableExtra(ACTION_PAYLOAD_KEY);
    if (action.equals(START_CALL_BANNER)) {
      if (!Utils.isDeviceScreenLocked(getApplicationContext())) {
        openIncomingCallScreen.addFlags(FLAG_ACTIVITY_NEW_TASK);
        startActivity(openIncomingCallScreen);
      } else {
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
            .setFullScreenIntent(openIncomingCallScreenPendingIntent, true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(null)
            .setOngoing(true);
        startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
      }
      callPlayer.play(getApplicationContext());
    }

    if (action.equals(DISMISS_BANNER)) {
        reactToCall((ReactApplication) getApplication(), CALL_IS_DECLINED, null);
    }

    if (action.equals(ACCEPT_CALL)) {
      Utils.backToForeground(getApplicationContext());
      new CountDownTimer(1250, 1000) {

        public void onTick(long millisUntilFinished) {
          //here you can have your logic to set text to edittext
        }

        public void onFinish() {
          reactToCall((ReactApplication) getApplication(), ACCEPT_CALL_EVENT, getJsPayload(payload));
          stopForeground(true);
        }

      }.start();
      callPlayer.stop();

    }

    return START_NOT_STICKY;
  }
}
