package com.example.cs501_runbuddy;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fitbit.authentication.AuthenticationHandler;
import com.fitbit.authentication.AuthenticationManager;
import com.fitbit.authentication.AuthenticationResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity implements AuthenticationHandler {

    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private Button signInAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        email = (EditText) findViewById(R.id.emailAddress);
        password = (EditText) findViewById(R.id.password);
        signInAccount = (Button) findViewById(R.id.signInAccount);

        mAuth = FirebaseAuth.getInstance();

        signInAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            String uid = user.getUid();
        }
    }
    //Used to sign in existing users
    private void signInUser() {
        if (email.getText().toString().equals("") || !isEmailValid(email.getText().toString())) {
            // If sign in fails, display a message to the user.
            Toast.makeText(SignInActivity.this, "Email is invalid, please try again.",
                    Toast.LENGTH_SHORT).show();
        } else {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                //If access token for fitbit valid
                                //Go straight to dashboard
                                if(AuthenticationManager.isLoggedIn()){
                                    Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
                                    startActivity(intent);
                                }
                                //Request user to sign into fitbit
                                //and store access token
                                else{
                                    AuthenticationManager.login(SignInActivity.this);
                                }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SignInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    //Will check if fitbit sign up is finished and go to main activity
    @Override
    public void onAuthFinished(AuthenticationResult result) {
        if(result.isSuccessful()){
            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            startActivity(intent);
        }else{

        }
    }

    //Authentication handler functions
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AuthenticationManager.onActivityResult(requestCode, resultCode, data, (AuthenticationHandler)this);
    }
}