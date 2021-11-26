package com.eyr.callkeep;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class CallPlayer {

  private Vibrator vibrator = null;
  private MediaPlayer ringtonePlayer = null;
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
    playVibrate(context);
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

  private void playMusic(Context context) {
    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
      return;
    }
    Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
    ringtonePlayer = new MediaPlayer();
    ringtonePlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
      @Override
      public void onPrepared(MediaPlayer mp) {
        try {
          ringtonePlayer.start();
        } catch (Throwable e) {
          e.printStackTrace();
        }
        ringtonePlayer.setLooping(true);

      }
    });
    AudioAttributes.Builder audioAttributeBuilder = new AudioAttributes.Builder();
    audioAttributeBuilder.setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE);
    if (isHeadsetOn(audioManager)) {
      audioAttributeBuilder.setLegacyStreamType(AudioManager.STREAM_VOICE_CALL);
    } else {
      audioAttributeBuilder.setLegacyStreamType(AudioManager.STREAM_RING);
    }
    try {
      ringtonePlayer.setAudioAttributes(audioAttributeBuilder.build());
      ringtonePlayer.setDataSource(context, defaultRingtoneUri);
    } catch (Exception e) {
      e.printStackTrace();
    }
    ringtonePlayer.prepareAsync();

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        stop();
      }
    }, 120000);
  }

  private boolean isHeadsetOn(AudioManager am) {
    if (am == null)
      return false;

    AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

    for (AudioDeviceInfo device : devices) {
      if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
        || device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
        return true;
      }
    }
    return false;
  }

  private void stopMusic() {
    if (ringtonePlayer!=null && ringtonePlayer.isPlaying()) {
      ringtonePlayer.stop();
      ringtonePlayer.release();
    }
  }

  private void playVibrate(Context context) {
    if(vibrator==null || !vibrator.hasVibrator()) {
      return;
    }

    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
      handler.post(new Runnable() {
        @Override
        public void run() {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
          } else {
            vibrator.vibrate(pattern, 0);
          }
        }
      });

    }

  }

  private void stopVibrate() {
    if (vibrator!=null) {
      vibrator.cancel();
    }
  }

}
