package com.callkeepdemo.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.RemoteMessage;

public class BackgroundCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteMessage remoteMessage = new RemoteMessage(intent.getExtras());
        if (remoteMessage.getNotification() != null) {
            Intent backgroundIntent = new Intent(context, CallBannerEventEmitterService.class);
            context.startService(backgroundIntent);
            HeadlessJsTaskService.acquireWakeLockNow(context);
        }
    }
}
