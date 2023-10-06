package com.example.adiuvo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash2);

        // After initialization, transition to the main activity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // Optional, to prevent the user from going back to the splash screen
    }
}
