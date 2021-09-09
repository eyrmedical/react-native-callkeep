package com.eyr.callkeep;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
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
    private static final String OPEN_INCOMING_CALL = "OPEN_INCOMING_CALL";

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        Intent dismissBannerIntent = new Intent(this, this.getClass());
        dismissBannerIntent.setAction(DISMISS_BANNER);

        Intent acceptCallIntent = new Intent(this, this.getClass());
        acceptCallIntent.setAction(ACCEPT_CALL);

        Intent openAppIntent = new Intent(this, this.getClass());
        openAppIntent.setAction(OPEN_INCOMING_CALL);

        PendingIntent pendingDismissBannerIntent = PendingIntent.getService(getApplicationContext(), 0,
                dismissBannerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent acceptCallPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, 
                acceptCallIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent openIncomingCallScreenPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, 
                openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT);

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
                                    acceptCallPendingIntent).build())
                            .addAction(new NotificationCompat.Action.Builder(
                                    R.mipmap.ic_launcher,
                                    "Decline",
                                    pendingDismissBannerIntent).build())
                            .setContentIntent(openIncomingCallScreenPendingIntent);

            startForeground(CALL_NOTIFICATION_ID, notificationBuilder.build());
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
            Intent acceptCallAndOpenApp = new Intent(this, getMainActivityClass());
            acceptCallAndOpenApp.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle acceptCallAndOpenAppInitialProps = new Bundle();
            acceptCallAndOpenAppInitialProps.putBoolean("isCallAccepted", true);
            acceptCallAndOpenApp.putExtras(acceptCallAndOpenAppInitialProps);
            startActivity(acceptCallAndOpenApp);
            // remove call notification
            stopForeground(true);
        }

        if (action.equals(OPEN_INCOMING_CALL)) {
            Intent openIncomingCallScreen = new Intent(this, getMainActivityClass());
            openIncomingCallScreen.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Bundle openIncomingCallScreenInitialProps = new Bundle();
            openIncomingCallScreenInitialProps.putBoolean("isIncomingCall", true);
            openIncomingCallScreen.putExtras(openIncomingCallScreenInitialProps);
            startActivity(openIncomingCallScreen);
            // remove call notification
            stopForeground(true);
        }

        return START_NOT_STICKY;
    }
}
