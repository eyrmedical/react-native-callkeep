package com.eyr.callkeep;

import static com.eyr.callkeep.CallKeepService.ACTION_END_CALL;
import static com.eyr.callkeep.CallKeepService.EVENT_ACCEPT_CALL;
import static com.eyr.callkeep.CallKeepService.EVENT_DECLINE_CALL;
import static com.eyr.callkeep.CallKeepService.EVENT_END_CALL;
import static com.eyr.callkeep.CallKeepService.PAYLOAD;
import static com.eyr.callkeep.CallKeepService.ACTION_DISPLAY_INCOMING_CALL;
import static com.eyr.callkeep.CallKeepService.ACTION_DISPLAY_ONGOING_CALL;
import static com.eyr.callkeep.Utils.getMainActivityIntent;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.*;

import java.util.HashMap;
import java.util.Map;

public class CallKeepModule extends ReactContextBaseJavaModule {

  private ReactApplicationContext reactContext;

  public CallKeepModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @NonNull
  @Override
  public String getName() {
    return "CallKeepModule";
  }

  @ReactMethod
  public void configure(@NonNull ReadableMap options) {

  }

  @ReactMethod
  public void displayIncomingCall(@Nullable ReadableMap incomingCallParams) {
    Intent intent = new Intent(reactContext.getApplicationContext(), CallKeepService.class);
    intent.setAction(ACTION_DISPLAY_INCOMING_CALL);
    if (incomingCallParams != null) {
      intent.putExtra(PAYLOAD, incomingCallParams.toHashMap());
    }
    reactContext.getApplicationContext().startService(intent);
  }

  @ReactMethod
  public void displayOngoingCall(@Nullable ReadableMap ongoingCallParams) {
    Intent intent = new Intent(reactContext.getApplicationContext(), CallKeepService.class);
    intent.setAction(ACTION_DISPLAY_ONGOING_CALL);
    if (ongoingCallParams != null) {
      intent.putExtra(PAYLOAD, ongoingCallParams.toHashMap());
    }
    reactContext.getApplicationContext().startService(intent);
  }

  @ReactMethod
  public void endCall() {
    Intent intent = getMainActivityIntent(reactContext.getApplicationContext());
    intent.setAction(ACTION_END_CALL);
    reactContext.getApplicationContext().startActivity(intent);
  }

  @ReactMethod
  public void dispose() {
    Intent intent = new Intent(reactContext.getApplicationContext(), CallKeepService.class);
    reactContext.getApplicationContext().stopService(intent);
  }

  @ReactMethod
  public Boolean isMIUI() {
    return XiaomiUtilities.isMIUI();
  }

  @ReactMethod
  public Boolean hasMIUIShowWhenLockPermission() {
    return XiaomiUtilities.isCustomPermissionGranted(
      reactContext.getApplicationContext(),
      XiaomiUtilities.OP_SHOW_WHEN_LOCKED);
  }

  @ReactMethod
  public void openMIUIPermissionScreen() {
    try {
      Intent intent = XiaomiUtilities.getPermissionManagerIntent(reactContext.getApplicationContext());
      reactContext.startActivity(intent);
    } catch (Exception e) {
      try {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + reactContext.getApplicationContext().getPackageName()));
        reactContext.startActivity(intent);
      } catch (Exception e1) {
        e1.printStackTrace();
      }
    }
  }

  @Override
  public boolean hasConstants() {
    return true;
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    HashMap<String, Object> constants = new HashMap<>();
    constants.put("EVENT_ACCEPT_CALL", EVENT_ACCEPT_CALL);
    constants.put("EVENT_DECLINE_CALL", EVENT_DECLINE_CALL);
    constants.put("EVENT_END_CALL", EVENT_END_CALL);

    return constants;
  }
}
