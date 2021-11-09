package com.callkeepdemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

public class MainActivity extends ReactActivity {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  @Override
  protected String getMainComponentName() {
    return "CallKeepDemo";
  }

  @Override
  protected ReactActivityDelegate createReactActivityDelegate() {
    return new ReactActivityDelegate(this, this.getMainComponentName()) {
      @Nullable
      @Override
      protected Bundle getLaunchOptions() {
        Intent intent = getIntent();
        return intent.getExtras();
      }
    };
  }
}
