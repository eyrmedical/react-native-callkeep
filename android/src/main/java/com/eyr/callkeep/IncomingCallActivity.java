package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_DISMISS_BANNER;
import static com.eyr.callkeep.EyrCallBannerDisplayService.ACTION_START_CALL_BANNER;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_ACCEPT_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_END_CALL;
import static com.eyr.callkeep.EyrCallBannerDisplayService.PAYLOAD;
import static com.eyr.callkeep.EyrCallBannerDisplayService.EVENT_DECLINE_CALL;
import static com.eyr.callkeep.Utils.getJsBackgroundPayload;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.reactToCall;
import static com.eyr.callkeep.Utils.showOnLockscreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.react.ReactApplication;

import java.util.HashMap;

public class IncomingCallActivity extends AppCompatActivity {

  private HashMap<String, Object> payload = null;
  private LocalBroadcastManager mLocalBroadcastManager;
  private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(EVENT_DECLINE_CALL)){
        finish();
      }
    }
  };

  private final View.OnClickListener onAccept = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Utils.backToForeground(getApplicationContext(),  getJsPayload(payload));
      reactToCall((ReactApplication) getApplication(), EVENT_ACCEPT_CALL, getJsBackgroundPayload(payload));
      finish();
    }
  };

  private final View.OnClickListener onDecline = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Utils.backToForeground(getApplicationContext());
      reactToCall((ReactApplication) getApplication(), EVENT_DECLINE_CALL,null);
      finish();
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    showOnLockscreen(this, true);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_incoming_call);
    payload = (HashMap<String, Object>) getIntent().getSerializableExtra(PAYLOAD);
    setUpUI();
    mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    IntentFilter mIntentFilter = new IntentFilter();
    mIntentFilter.addAction(EVENT_DECLINE_CALL);
    mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
  }

  protected void onDestroy() {
    super.onDestroy();
    mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
  }

  @Override
  protected void onResume() {
    super.onResume();
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

  @Override
  public void onBackPressed() {

  }
}
