package com.callkeepdemo.call;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.callkeepdemo.MainActivity;
import com.callkeepdemo.MainApplication;
import com.callkeepdemo.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;

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

        Intent openAppWithCall = new Intent(this, MainActivity.class);
        openAppWithCall.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Bundle openAppWithCallInitialProps = new Bundle();
        openAppWithCallInitialProps.putBoolean("isCall", true);
        openAppWithCall.putExtras(openAppWithCallInitialProps);

        PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
                dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent openAppWithCallIntent = PendingIntent.getActivity(getApplicationContext(), 0, openAppWithCall, PendingIntent.FLAG_UPDATE_CURRENT);

        if (action.equals(START_CALL_BANNER)) {
            HashMap<String, String> payload = (HashMap<String, String>) intent.getSerializableExtra(ACTION_PAYLOAD_KEY);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CALL_INCOMING_CHANNEL_ID)
//                            .setContentTitle(payload.get("caller_name"))
                            // Add small icon is a must so that the notification content can be customized
                            // https://stackoverflow.com/questions/34225779/how-to-set-the-app-icon-as-the-notification-icon-in-the-notification-drawer
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Test caller")
                            .setContentText("Please pick up the call")
                            .setAutoCancel(true)
                            .setOngoing(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setCategory(NotificationCompat.CATEGORY_CALL)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .addAction(new NotificationCompat.Action.Builder(
                                    R.mipmap.ic_launcher,
                                    "Accept",
                                    openAppWithCallIntent).build())
                            .addAction(new NotificationCompat.Action.Builder(
                                    R.mipmap.ic_launcher,
                                    "Decline",
                                    pendingDismissBannerIntent).build())
                            .setContentIntent(openAppWithCallIntent);

            startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
        }

        if (action.equals(DISMISS_BANNER)) {
            MainApplication application = (MainApplication) this.getApplication();

            ReactNativeHost reactNativeHost = application.getReactNativeHost();
            ReactInstanceManager reactInstanceManager = reactNativeHost.getReactInstanceManager();
            ReactContext reactContext = reactInstanceManager.getCurrentReactContext();

            Log.d("ReactNativeJS", "trying to get react context " + (reactContext == null ? "No :(" : "Yes!"));
            if (reactContext != null) {
                reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit("CALL_IS_DECLINED", null);
            }
//            stopForeground(true);
        }

        return START_NOT_STICKY;
    }
}
