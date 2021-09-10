package com.eyr.callkeep;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.List;

public class EyrNotificationCompatBuilderArgSerializer {

  private HashMap<String, Object> mArgs;

  public EyrNotificationCompatBuilderArgSerializer(HashMap<String, Object> args) {
    mArgs = args;
  }

  private void maybeAddAutoCancel(NotificationCompat.Builder builder) {
    @Nullable Boolean autoCancel = (Boolean) mArgs.get("autoCancel");
    if (autoCancel != null) {
      builder.setAutoCancel(autoCancel);
    }
  }

  private void maybeAddOngoing(NotificationCompat.Builder builder) {
    @Nullable Boolean ongoing = (Boolean) mArgs.get("ongoing");
    if (ongoing != null) {
      builder.setAutoCancel(ongoing);
    }
  }

  private void maybeAddPriority(NotificationCompat.Builder builder) {
    Double priority = (Double) mArgs.get("priority");
    if (priority != null) {
      builder.setPriority(priority.intValue());
    }
  }

  private void maybeAddVisibility(NotificationCompat.Builder builder) {
    Double visibility = (Double) mArgs.get("visibility");
    if (visibility != null) {
      builder.setPriority(visibility.intValue());
    }
  }

  private void maybeAddCategory(NotificationCompat.Builder builder) {
    String category = (String) mArgs.get("category");
    if (category != null) {
      builder.setCategory(category);
    }
  }

  private void maybeAddTitle(NotificationCompat.Builder builder) {
    String title = (String) mArgs.get("title");
    if (title != null) {
      builder.setContentTitle(title);
    }
  }

  private void maybeAddSubtitle(NotificationCompat.Builder builder) {
    String subtitle = (String) mArgs.get("subtitle");
    if (subtitle != null) {
      builder.setContentText(subtitle);
    }
  }

  private void maybeSetSound(Context context, NotificationCompat.Builder builder) {
    String sound = (String) mArgs.get("sound");
    if (sound != null) {
      SoundResolver resolver = new SoundResolver(context);
      builder.setSound(resolver.resolve(sound));
    }
  }

  private void maybeSetVibration(NotificationCompat.Builder builder) {
    List<?> vibrateJsonArray = (List<?>) mArgs.get("vibration");
    if (vibrateJsonArray != null) {
      builder.setVibrate(getVibrationPattern(vibrateJsonArray));
    }
  }

  private long[] getVibrationPattern(List<?> vibrateJsonArray) {
    try {
      if (vibrateJsonArray != null) {
        long[] pattern = new long[vibrateJsonArray.size()];
        for (int i = 0; i < vibrateJsonArray.size(); i++) {
          if (vibrateJsonArray.get(i) instanceof Number) {
            pattern[i] = ((Number) vibrateJsonArray.get(i)).longValue();
          } else {
            throw new Exception("Invalid vibration array");
          }
        }
        return pattern;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

  @Nullable
  public static String parseAcceptBtnTitle(HashMap<String, Object> args) {
    return (String) args.get("acceptTitle");
  }

  @Nullable
  public static String parseDeclineBtnTitle(HashMap<String, Object> args) {
    return (String) args.get("declineTitle");
  }

  public NotificationCompat.Builder createNotificationFromContext(Context context) {
    @NonNull String notificationChannelId = (String) mArgs.get("channelId");
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, notificationChannelId);
    builder
      .setSmallIcon(R.mipmap.ic_launcher);
    maybeAddAutoCancel(builder);
    maybeAddOngoing(builder);
    maybeAddPriority(builder);
    maybeAddCategory(builder);
    maybeAddVisibility(builder);
    maybeAddTitle(builder);
    maybeAddSubtitle(builder);
    maybeSetSound(context, builder);
    maybeSetVibration(builder);
    return builder;
  }
}
