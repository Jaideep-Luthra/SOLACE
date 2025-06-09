package com.projects.solace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.projects.solace.common.Constants;
import com.projects.solace.driver.DriverActivity;
import com.projects.solace.login.LoginActivity;
import com.projects.solace.student.StudentActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private final Context context = SplashActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Retrieving data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        String name = sharedPreferences.getString("name", null);
        String userType = sharedPreferences.getString("userType", null);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        Constants.email = email;
        Constants.name = name;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (isLoggedIn) {
                    assert userType != null;
                    if (Constants.getUserTypeFromString(userType) == Constants.UserType.STUDENT) {
                        Intent intent = new Intent(context, StudentActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(context, DriverActivity.class);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 1000);
    }
}