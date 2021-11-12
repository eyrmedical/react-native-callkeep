package com.eyr.callkeep;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class CallPlayer {


  private Ringtone defaultRingtone = null;
  private Vibrator vibrator = null;
  private final long[] pattern = { 0L, 1000L, 800L};
  private boolean isPlaying = false;

  private final Handler handler = new Handler();

  public void play(Context context) {
    if(isPlaying) {
      stop();
    }
    if (vibrator == null) {
      vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    playVibrate();
    playMusic(context);
    isPlaying = true;
  }

  public void stop() {
    if(!isPlaying) {
      return;
    }

    stopMusic();
    stopVibrate();
    isPlaying = false;
  }

  public Boolean isPlaying() {
    return isPlaying;
  }

  private void playMusic(Context context) {
    Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
    defaultRingtone = RingtoneManager.getRingtone(context, defaultRingtoneUri);
    defaultRingtone.play();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      defaultRingtone.setLooping(true);
    }
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (defaultRingtone!=null && defaultRingtone.isPlaying()) {
          stop();
        }
      }
    }, 30000);
  }

  private void stopMusic() {
    if (defaultRingtone!=null && defaultRingtone.isPlaying()) {
      defaultRingtone.stop();
    }
  }

  private void playVibrate() {
    if(vibrator==null || !vibrator.hasVibrator()) {
      return;
    }

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
      vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
    } else {
      vibrator.vibrate(pattern, 0);
    }
  }

  private void stopVibrate() {
    if (vibrator!=null) {
      vibrator.cancel();
    }
  }

}
