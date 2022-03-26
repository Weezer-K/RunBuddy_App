package com.example.cs501_runbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class IntroActivity extends AppCompatActivity {

    private Button goToSignIn;
    private Button goToSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        goToSignIn = (Button) findViewById(R.id.goToSignIn);
        goToSignUp = (Button) findViewById(R.id.goToSignUp);

        goToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToSignInActivity = new Intent(getApplicationContext(), SignInActivity.class);
                startActivity(goToSignInActivity);
            }
        });

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