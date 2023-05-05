package com.eyr.callkeep;

import static com.eyr.callkeep.CallKeepService.ACTION_ACCEPT_CALL;
import static com.eyr.callkeep.CallKeepService.ACTION_DECLINE_CALL;
import static com.eyr.callkeep.CallKeepService.PAYLOAD;
import static com.eyr.callkeep.CallKeepService.EVENT_DECLINE_CALL;
import static com.eyr.callkeep.Utils.INITIAL_CALL_STATE_PROP_NAME;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.getMainActivityIntent;
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

import java.util.HashMap;

public class IncomingCallActivity extends AppCompatActivity {

  private HashMap<String, Object> payload = null;
  private LocalBroadcastManager mLocalBroadcastManager;
  private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(EVENT_DECLINE_CALL)) {
        finish();
      }
    }
  };

  private final View.OnClickListener onAccept = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent acceptCallAndOpenAppIntent = getMainActivityIntent(getApplicationContext());
      acceptCallAndOpenAppIntent.setAction(ACTION_ACCEPT_CALL);
      payload = (HashMap<String, Object>) getIntent().getSerializableExtra(PAYLOAD);
      acceptCallAndOpenAppIntent.putExtras(getIntent());
      acceptCallAndOpenAppIntent.putExtra(INITIAL_CALL_STATE_PROP_NAME, getJsPayload(payload));
      getApplicationContext().startActivity(acceptCallAndOpenAppIntent);
      finish();
    }
  };

  private final View.OnClickListener onDecline = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      Intent declineCallIntent = new Intent(getApplicationContext(), CallKeepService.class);
      declineCallIntent.setAction(ACTION_DECLINE_CALL);
      getApplicationContext().startService(declineCallIntent);
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
    mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
    super.onDestroy();
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
    if (payload == null) return;
    tvName.setText(String.valueOf(payload.get("title")));
    tvDetail.setText(String.valueOf(payload.get("subtitle")));
    tvAccept.setText(String.valueOf(payload.get("acceptTitle")));
    tvDecline.setText(String.valueOf(payload.get("declineTitle")));
  }

  @Override
  public void onBackPressed() {

  }
}
