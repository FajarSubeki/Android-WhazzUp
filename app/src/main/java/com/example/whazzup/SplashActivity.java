package com.example.whazzup;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.whazzup.base.BaseActivity;
import com.example.whazzup.config.AppConfig;
import com.example.whazzup.helper.PrefManager;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        super.setHideActionBar();
        splsh();
    }

    private void splsh()
    {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                PrefManager prefManager = new PrefManager(getApplicationContext());
                Intent intent;
                if (prefManager.getISLogged_IN())
                {
                    intent = new Intent(getApplicationContext(), ChatDialogActivity.class);
                }else{
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                }
                startActivity(intent);
            }
        }, AppConfig.THREE_DELAY);
    }

}
