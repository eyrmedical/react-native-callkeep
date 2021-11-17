package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerControllerModule.ACCEPT_CALL_EVENT;
import static com.eyr.callkeep.EyrCallBannerControllerModule.ACTION_PAYLOAD_KEY;
import static com.eyr.callkeep.EyrCallBannerControllerModule.CALL_IS_DECLINED;
import static com.eyr.callkeep.Utils.getJsBackgroundPayload;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.reactToCall;
import static com.eyr.callkeep.Utils.showOnLockscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.react.ReactApplication;

import java.util.HashMap;

public class IncomingCallActivity extends AppCompatActivity {

  private HashMap<String, Object> payload = null;
  private LocalBroadcastManager mLocalBroadcastManager;
  private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(CALL_IS_DECLINED)){
        finish();
      }
    }
  };

  private final View.OnClickListener onAccept = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Utils.backToForeground(getApplicationContext(),  getJsPayload(payload));
      reactToCall((ReactApplication) getApplication(), ACCEPT_CALL_EVENT, getJsBackgroundPayload(payload));
      finish();
    }
  };

  private final View.OnClickListener onDecline = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      reactToCall((ReactApplication) getApplication(), CALL_IS_DECLINED,null);
      finish();
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_incoming_call);
    payload = (HashMap<String, Object>) getIntent().getSerializableExtra(ACTION_PAYLOAD_KEY);
    setUpUI();
    mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    IntentFilter mIntentFilter = new IntentFilter();
    mIntentFilter.addAction(CALL_IS_DECLINED);
    mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
  }

  protected void onDestroy() {
    super.onDestroy();
    mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
  }

  @Override
  protected void onResume() {
    super.onResume();
    showOnLockscreen(this);
  }

  private void setUpUI() {
    TextView tvName = findViewById(R.id.tv_name);
    TextView tvDetail = findViewById(R.id.tv_number);
    TextView tvAccept = findViewById(R.id.tvAccept);
    TextView tvDecline = findViewById(R.id.tvDecline);
    ImageView btnAccept = findViewById(R.id.ivAcceptCall);
    ImageView btnDecline = findViewById(R.id.ivDeclineCall);
    btnAccept.setOnClickListener(onAccept);
    btnDecline.setOnClickListener(onDecline);
    if (payload==null) return;
    tvName.setText(String.valueOf(payload.get("title")));
    tvDetail.setText(String.valueOf(payload.get("subtitle")));
    tvAccept.setText(String.valueOf(payload.get("acceptTitle")));
    tvDecline.setText(String.valueOf(payload.get("declineTitle")));
  }



}
