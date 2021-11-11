package com.eyr.callkeep;

import static com.eyr.callkeep.EyrCallBannerControllerModule.ACCEPT_CALL_EVENT;
import static com.eyr.callkeep.EyrCallBannerControllerModule.ACTION_PAYLOAD_KEY;
import static com.eyr.callkeep.EyrCallBannerControllerModule.CALL_IS_DECLINED;
import static com.eyr.callkeep.Utils.getJsPayload;
import static com.eyr.callkeep.Utils.reactToCall;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.react.ReactApplication;

import java.util.HashMap;

public class IncomingCallActivity extends AppCompatActivity {

  private HashMap<String, Object> payload = null;

  private final View.OnClickListener onAccept = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      reactToCall((ReactApplication) getApplication(), ACCEPT_CALL_EVENT, getJsPayload(payload));
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
    showOnLockscreen();
    payload = (HashMap<String, Object>) getIntent().getSerializableExtra(ACTION_PAYLOAD_KEY);
    setUpUI();
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
    tvName.setText(String.valueOf(payload.get("callerName")));
    tvDetail.setText(String.valueOf(payload.get("subtitle")));
    tvAccept.setText(String.valueOf(payload.get("acceptTitle")));
    tvDecline.setText(String.valueOf(payload.get("declineTitle")));
  }





  private void showOnLockscreen() {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    } else {
      getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
      setTurnScreenOn(true);
      setShowWhenLocked(true);

      KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
      keyguardManager.requestDismissKeyguard(this, null);
    }
  }

}
