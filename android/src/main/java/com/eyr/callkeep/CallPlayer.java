package com.eyr.callkeep;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class CallPlayer {


  private MediaPlayer mMediaPlayer = null;
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
    Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    AudioAttributes attribution = new AudioAttributes.Builder()
      .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
      .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
      .build();
    mMediaPlayer = MediaPlayer.create(context, uri);
    mMediaPlayer.setLooping(true);
    mMediaPlayer.setAudioAttributes(attribution);
    mMediaPlayer.start();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
          stop();
        }
      }
    }, 30000);
  }

  private void stopMusic() {
    if (mMediaPlayer!=null && mMediaPlayer.isPlaying()) {
      mMediaPlayer.stop();
      mMediaPlayer.seekTo(0);
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
