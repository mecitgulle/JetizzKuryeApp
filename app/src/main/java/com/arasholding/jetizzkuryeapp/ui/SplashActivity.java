package com.arasholding.jetizzkuryeapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.arasholding.jetizzkuryeapp.R;
import com.arasholding.jetizzkuryeapp.apimodels.LoginResponse;
import com.arasholding.jetizzkuryeapp.pref.Prefences;

public class SplashActivity extends AppCompatActivity {
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo = findViewById(R.id.img_logo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.transition);
        imgLogo.setAnimation(animation);

        LoginResponse loginResponse = Prefences.readFromPreferences(getApplicationContext());
        token = loginResponse.getAccess_token();

        new Handler().postDelayed(new Runnable() {
                                      @Override
                                      public void run() {
                                          if (!token.isEmpty()) {
                                              Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                              startActivity(intent);
                                              finish();
                                          } else {
                                              Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                                              startActivity(intent);
                                              finish();
                                          }
                                      }
                                  },
                2000);
    }
}
