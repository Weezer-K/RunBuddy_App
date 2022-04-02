package com.example.cs501_runbuddy;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    private Button goToSignIn;
    private Button goToSignUp;
    private ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        goToSignIn = (Button) findViewById(R.id.goToSignIn);
        goToSignUp = (Button) findViewById(R.id.goToSignUp);
        img = (ImageView) findViewById(R.id.imageView);
        img.setImageResource(R.drawable.zombierunner);


        //Used to initiate the sign in page for registered users
        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToSignInActivity = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(goToSignInActivity);
            }
        });

        //Used to initiate the sign in page for unregistered users
        goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToSignUpActivity = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(goToSignUpActivity);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}