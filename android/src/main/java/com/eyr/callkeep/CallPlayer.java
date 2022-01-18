package com.eyr.callkeep;

import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class CallPlayer implements AudioManager.OnAudioFocusChangeListener {

  private Vibrator vibrator = null;
  private MediaPlayer ringtonePlayer = null;
  private final long[] pattern = { 0L, 1000L, 800L};
  private boolean isPlaying = false;
  private boolean hasAudioFocus;
  protected static final String TAG = "CallPlayer";

  public void play(Context context) {
    if(isPlaying) {
      stop(context);
    }
    if (vibrator == null) {
      vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    boolean needRing = am.getRingerMode() != AudioManager.RINGER_MODE_SILENT;
    if (!needRing) {
      return;
    }
    if (ringtonePlayer!=null) {
      ringtonePlayer.stop();
      ringtonePlayer.reset();
    }
    ringtonePlayer = new MediaPlayer();
    ringtonePlayer.setOnPreparedListener(mediaPlayer -> {
      try {
        ringtonePlayer.start();
        isPlaying = true;
      } catch (Throwable e) {
        Log.e(TAG, "ringtonePlayer.start() failed", e);
      }
    });
    ringtonePlayer.setLooping(true);
    if (isHeadsetOn(am)) {
      ringtonePlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
    } else {
      ringtonePlayer.setAudioStreamType(AudioManager.STREAM_RING);
      am.requestAudioFocus(this, AudioManager.STREAM_RING, AudioManager.AUDIOFOCUS_GAIN);
    }
    try {
      ringtonePlayer.setDataSource(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
      ringtonePlayer.prepareAsync();
    } catch (IOException e) {
      Log.e(TAG, "ringtonePlayer.prepareAsync() failed", e);
      if (ringtonePlayer != null) {
        ringtonePlayer.release();
        ringtonePlayer = null;
      }
    }
    if(!vibrator.hasVibrator()) {
      return;
    }

    if (am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
      } else {
        vibrator.vibrate(pattern, 0);
      }
    }
  }

  public void stop(Context context) {
    if(!isPlaying) {
      return;
    }
    AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    if (ringtonePlayer != null && ringtonePlayer.isPlaying()) {
      ringtonePlayer.stop();
      if (hasAudioFocus) {
        am.abandonAudioFocus(this);
      }
    }
    if (vibrator!=null) {
      vibrator.cancel();
    }
    isPlaying = false;
  }

  private boolean isHeadsetOn(AudioManager am) {
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

  public void onAudioFocusChange(int focusChange) {
    hasAudioFocus = focusChange == AudioManager.AUDIOFOCUS_GAIN;
  }
}
