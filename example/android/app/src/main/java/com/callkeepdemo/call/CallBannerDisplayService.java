package com.callkeepdemo.call;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;

import static com.callkeepdemo.call.BackgroundCallBannerModule.*;

public class CallBannerDisplayService extends Service {
    public static final int CALL_NOTIFICATION_ID = 23;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Intent dismissBannerIntent = new Intent(this, CallBannerDisplayService.class);
        dismissBannerIntent.setAction(DISMISS_BANNER);

        PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
                dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (action.equals(START_CALL_BANNER)) {
            HashMap<String, String> payload = (HashMap<String, String>) intent.getSerializableExtra(ACTION_PAYLOAD_KEY);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CALL_INCOMING_CHANNEL_ID)
//                            .setContentTitle(payload.get("caller_name"))
                            .setContentTitle("Test caller")
                            .setSound(null)
                            .setVibrate(null)
                            .setContentText("Please pick up the call")
                            .setAutoCancel(true)
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .addAction(new NotificationCompat.Action.Builder(
                                    0,
                                    "Accept",
                                    pendingDismissBannerIntent).build())
                            .setContentIntent(pendingDismissBannerIntent);

            startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
        }

        if (action.equals(DISMISS_BANNER)) {
            stopForeground(true);
        }

        return START_NOT_STICKY;
    }
}
